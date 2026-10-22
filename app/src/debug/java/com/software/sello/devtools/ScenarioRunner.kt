package com.software.sello.devtools

import com.software.sello.devtools.ScenarioStep.AdvanceTo
import com.software.sello.devtools.ScenarioStep.Category
import com.software.sello.devtools.ScenarioStep.Expense
import com.software.sello.devtools.ScenarioStep.Income
import com.software.sello.domain.model.BudgetLimit
import com.software.sello.domain.model.CategoryId
import com.software.sello.domain.model.CategoryName
import com.software.sello.domain.model.IconKey
import com.software.sello.domain.model.IncomeSource
import com.software.sello.domain.model.Money
import com.software.sello.domain.model.Note
import com.software.sello.domain.model.OperationId
import com.software.sello.domain.model.OperationReceipt
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.TransactionAmount
import com.software.sello.domain.port.CategoryCommandOutcome
import com.software.sello.domain.port.CategoryCommands
import com.software.sello.domain.port.CreateCategory
import com.software.sello.domain.port.CreateExpense
import com.software.sello.domain.port.CreateIncome
import com.software.sello.domain.port.ExpenseCommandOutcome
import com.software.sello.domain.port.ExpenseCommands
import com.software.sello.domain.port.IncomeCommandOutcome
import com.software.sello.domain.port.IncomeCommands
import java.util.Locale
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** What one step did, as the tools list it. [operation] and [revision] are absent for a date step. */
data class StepRecord(
    val index: Int,
    val action: String,
    val operation: String?,
    val revision: Long?,
    val result: String
)

data class ScenarioState(
    val scenario: Scenario?,
    /** The first step not yet confirmed; it may have committed already. */
    val next: Int,
    val log: List<StepRecord> = emptyList(),
    val playing: Boolean = false,
    val running: Boolean = false,
    /** Why the run stopped short, until the next step is tried. */
    val problem: String? = null
) {
    val total: Int get() = scenario?.steps?.size ?: 0
    val finished: Boolean get() = scenario != null && next >= total
}

/**
 * Replays the loaded [scenario] through the app's own category, expense and income
 * commands, which the sandbox fences and audits like any other write. Every step uses
 * the operation [Scenario.operation] gives it, so running a step again after the
 * process ended, even one that had committed, returns its original receipt instead
 * of writing a second record.
 *
 * Progress is kept in [store]: [ScenarioState.next] moves on only once a step's result
 * is known. A rejection or an unknown outcome stops the run and says why; the next
 * try of that step uses the same operation. Playing takes one step every [stepMillis]
 * real milliseconds and ends with the scenario, at a stop, or on [pause].
 */
class ScenarioRunner(
    private val scenario: Scenario?,
    private val generation: Long,
    private val time: SandboxTime,
    private val categories: CategoryCommands,
    private val expenses: ExpenseCommands,
    private val incomes: IncomeCommands,
    private val store: SandboxSessionStore,
    private val scope: CoroutineScope,
    private val stepMillis: Long = SandboxTime.STEP_MILLIS
) {
    private val mutable = MutableStateFlow(ScenarioState(scenario, store.read().nextStep))
    val state: StateFlow<ScenarioState> = mutable.asStateFlow()

    private val stepping = Mutex()
    private var player: Job? = null
    private val categoryIds = mutableMapOf<String, CategoryId>()

    /** Lists the steps confirmed in earlier processes from their stored receipts. */
    fun recover(): Job = scope.launch {
        val loaded = scenario ?: return@launch
        stepping.withLock {
            val records = (0 until mutable.value.next).map { index ->
                val step = loaded.steps[index]
                val receipt = receiptOf(loaded, index, step)
                StepRecord(
                    index,
                    describe(step),
                    loaded.operation(index).takeIf { step !is AdvanceTo },
                    receipt?.revision,
                    if (step is AdvanceTo) DATE_SET else "Guardado"
                )
            }
            mutable.update { it.copy(log = records) }
        }
    }

    /** Runs the next step once. Returns whether the scenario moved on. */
    suspend fun step(): Boolean = stepping.withLock {
        val loaded = scenario ?: return@withLock false
        val index = mutable.value.next
        if (index >= loaded.steps.size) return@withLock false
        mutable.update { it.copy(running = true, problem = null) }
        val step = loaded.steps[index]
        val done = try {
            run(loaded, index, step)
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: Exception) {
            Result(null, "Falló: ${failure::class.simpleName}", moved = false)
        } finally {
            mutable.update { it.copy(running = false) }
        }
        val record = StepRecord(
            index,
            describe(step),
            loaded.operation(index).takeIf { step !is AdvanceTo },
            done.revision,
            done.text
        )
        if (done.moved) {
            store.write(store.read().copy(nextStep = index + 1))
            mutable.update { it.copy(next = index + 1, log = it.log + record) }
        } else {
            mutable.update { it.copy(log = it.log + record, problem = done.text) }
        }
        done.moved
    }

    fun play() {
        if (scenario == null || mutable.value.finished) return
        player?.cancel()
        mutable.update { it.copy(playing = true) }
        player = scope.launch {
            try {
                while (!mutable.value.finished) {
                    delay(stepMillis)
                    if (!step()) break
                }
            } finally {
                mutable.update { it.copy(playing = false) }
            }
        }
    }

    fun pause() {
        player?.cancel()
        player = null
        mutable.update { it.copy(playing = false) }
    }

    private class Result(val revision: Long?, val text: String, val moved: Boolean)

    private suspend fun run(loaded: Scenario, index: Int, step: ScenarioStep): Result {
        val operation = OperationId.of(loaded.operation(index)).value()
        val today = time.clock.today.value.date
        return when (step) {
            is AdvanceTo -> when {
                step.date < today -> Result(null, "La fecha del escenario va hacia atrás", false)

                step.date == today -> Result(null, DATE_SET, true)

                else -> when (time.jumpTo(step.date)) {
                    DateChange.Moved -> Result(null, DATE_SET, true)

                    DateChange.RefusedBeforeSavedHistory ->
                        Result(null, "La fecha no puede volver atrás", false)
                }
            }

            is Category -> categories.submit(
                CreateCategory(
                    operation,
                    generation,
                    CategoryName.of(step.name).value(),
                    IconKey.of(step.icon).value(),
                    step.limit?.let { BudgetLimit.finite(Money.cop(it)).value() }
                        ?: BudgetLimit.Unlimited
                )
            ).let { outcome ->
                when (outcome) {
                    is CategoryCommandOutcome.Committed -> {
                        categoryIds[step.key] = outcome.change.categoryId
                        saved(outcome.change.receipt, outcome.replayed)
                    }

                    is CategoryCommandOutcome.Rejected -> refused(outcome.reason)

                    is CategoryCommandOutcome.OutcomeUnknown -> unknown()
                }
            }

            is Expense -> {
                val category = categoryId(loaded, step.category)
                    ?: return Result(null, "No existe la categoría «${step.category}»", false)
                expenses.create(
                    CreateExpense(
                        operation,
                        generation,
                        category,
                        TransactionAmount.of(Money.cop(step.pesos)).value(),
                        today,
                        Note.of(step.note).value()
                    )
                ).let { outcome ->
                    when (outcome) {
                        is ExpenseCommandOutcome.Committed ->
                            saved(outcome.expense.receipt, outcome.replayed)

                        is ExpenseCommandOutcome.Rejected -> refused(outcome.reason)

                        is ExpenseCommandOutcome.OutcomeUnknown -> unknown()
                    }
                }
            }

            is Income -> incomes.create(
                CreateIncome(
                    operation,
                    generation,
                    TransactionAmount.of(Money.cop(step.pesos)).value(),
                    IncomeSource.of(step.source, null).value(),
                    today,
                    null
                )
            ).let { outcome ->
                when (outcome) {
                    is IncomeCommandOutcome.Committed -> saved(
                        outcome.income.receipt,
                        outcome.replayed
                    )

                    is IncomeCommandOutcome.Rejected -> refused(outcome.reason)

                    is IncomeCommandOutcome.OutcomeUnknown -> unknown()
                }
            }
        }
    }

    private fun saved(receipt: OperationReceipt, replayed: Boolean) = Result(
        receipt.revision,
        if (replayed) "Ya estaba guardado" else "Guardado",
        moved = true
    )

    private fun refused(reason: Any) = Result(null, "Rechazado: $reason", moved = false)

    private fun unknown() = Result(
        null,
        "No se sabe si se guardó; el siguiente paso lo intenta con la misma operación",
        moved = false
    )

    /** The identity the category step named [key] received, read back from its receipt. */
    private suspend fun categoryId(loaded: Scenario, key: String): CategoryId? {
        categoryIds[key]?.let { return it }
        val index = loaded.steps.indexOfFirst { it is Category && it.key == key }
        if (index < 0) return null
        val operation = OperationId.of(loaded.operation(index)).value()
        val found = categories.find(operation) as? Outcome.Success ?: return null
        return found.value?.categoryId?.also { categoryIds[key] = it }
    }

    private suspend fun receiptOf(
        loaded: Scenario,
        index: Int,
        step: ScenarioStep
    ): OperationReceipt? {
        val operation = OperationId.of(loaded.operation(index)).value()
        return when (step) {
            is AdvanceTo -> null
            is Category -> (categories.find(operation) as? Outcome.Success)?.value?.receipt
            is Expense -> (expenses.find(operation) as? Outcome.Success)?.value?.receipt
            is Income -> (incomes.find(operation) as? Outcome.Success)?.value?.receipt
        }
    }

    private fun <T> Outcome<T, *>.value(): T = when (this) {
        is Outcome.Success -> value
        is Outcome.Failure -> error("invalid scenario input: $error")
    }

    companion object {
        private const val DATE_SET = "Fecha fijada"

        fun describe(step: ScenarioStep): String = when (step) {
            is AdvanceTo -> "Ir al ${spokenDate(step.date)}"

            is Category ->
                "Categoría ${step.name}" +
                    (step.limit?.let { ", límite ${pesos(it)}" } ?: ", sin límite")

            is Expense -> "Gasto ${pesos(step.pesos)} en ${step.category}"

            is Income -> "Ingreso ${pesos(step.pesos)} (${step.source})"
        }

        private fun pesos(amount: Long): String =
            String.format(Locale.ROOT, "%,d", amount).replace(',', '.')
    }
}
