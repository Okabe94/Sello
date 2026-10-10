package com.software.sello.feature.expense

import com.software.sello.domain.model.BudgetLimit
import com.software.sello.domain.model.Category
import com.software.sello.domain.model.CategoryId
import com.software.sello.domain.model.CategoryName
import com.software.sello.domain.model.Expense
import com.software.sello.domain.model.ExpenseId
import com.software.sello.domain.model.IconKey
import com.software.sello.domain.model.Money
import com.software.sello.domain.model.MonthlySnapshot
import com.software.sello.domain.model.Note
import com.software.sello.domain.model.OperationId
import com.software.sello.domain.model.OperationKind
import com.software.sello.domain.model.OperationReceipt
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.SnapshotFailure
import com.software.sello.domain.model.StorageFailure
import com.software.sello.domain.model.TransactionAmount
import com.software.sello.domain.policy.CategoryInMonth
import com.software.sello.domain.policy.MonthBudgetState
import com.software.sello.domain.policy.MonthlyBudgetPolicy
import com.software.sello.domain.port.CommittedExpense
import com.software.sello.domain.port.CreateExpense
import com.software.sello.domain.port.ExpenseCommandOutcome
import com.software.sello.domain.port.ExpenseCommands
import com.software.sello.domain.port.ExpenseReads
import com.software.sello.domain.port.FinancialDay
import com.software.sello.domain.port.MonthlySnapshots
import com.software.sello.domain.port.SnapshotState
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onStart

fun uuid(number: Int) = "00000000-0000-4000-8000-%012d".format(number)

fun categoryId(number: Int) = (CategoryId.of(uuid(number)) as Outcome.Success).value

fun expenseId(number: Int) = (ExpenseId.of(uuid(number)) as Outcome.Success).value

fun pesos(amount: Long) = (TransactionAmount.of(Money.cop(amount)) as Outcome.Success).value

/** A category as a month sees it: [limit] in pesos, or null for no limit. */
fun categoryIn(
    number: Int,
    name: String,
    limit: Long?,
    archived: Boolean = false,
    icon: String = "home"
) = CategoryInMonth(
    Category(
        categoryId(number),
        (CategoryName.of(name) as Outcome.Success).value,
        (IconKey.of(icon) as Outcome.Success).value,
        archived = archived,
        version = 1,
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH
    ),
    MonthBudgetState.Limited(
        limit?.let { (BudgetLimit.finite(Money.cop(it)) as Outcome.Success).value }
            ?: BudgetLimit.Unlimited,
        explicit = false
    )
)

fun storedExpense(number: Int, category: Int, date: String, amount: Long, note: String? = null) =
    Expense(
        expenseId(number),
        categoryId(category),
        LocalDate.parse(date),
        number.toLong(),
        pesos(amount),
        (Note.of(note) as Outcome.Success).value,
        version = 1,
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH
    )

/** A real snapshot, worked out by the real policy from the rows given. */
fun monthOf(
    today: String,
    categories: List<CategoryInMonth>,
    expenses: List<Expense> = emptyList(),
    month: YearMonth = YearMonth.from(LocalDate.parse(today)),
    generation: Long = 1
): MonthlySnapshot = (
    MonthlyBudgetPolicy.snapshot(
        month,
        FinancialDay(LocalDate.parse(today), ZoneId.of("America/Bogota")),
        generation,
        revision = 1,
        categories = categories,
        expenses = expenses
    ) as Outcome.Success
    ).value

/** A snapshot source a test drives; it records which months were observed. */
class HandSnapshots : MonthlySnapshots {
    val observed = mutableListOf<YearMonth>()
    private val states = mutableMapOf<YearMonth, MutableStateFlow<SnapshotState>>()

    fun of(month: YearMonth) = states.getOrPut(month) { MutableStateFlow(SnapshotState.Loading) }

    fun ready(snapshot: MonthlySnapshot) {
        of(snapshot.month).value = SnapshotState.Ready(snapshot)
    }

    override suspend fun read(month: YearMonth): Outcome<MonthlySnapshot, SnapshotFailure> =
        error("the form observes; it does not read once")

    override fun observe(month: YearMonth): Flow<SnapshotState> =
        of(month).onStart { observed += month }
}

/** Commands a test scripts: what `create` and `find` answer, and what they were sent. */
class HandExpenseCommands : ExpenseCommands {
    val created = mutableListOf<CreateExpense>()
    val looked = mutableListOf<OperationId>()
    var answer: (CreateExpense) -> ExpenseCommandOutcome =
        { ExpenseCommandOutcome.Committed(committed(it.operationId), replayed = false) }
    var found: (
        OperationId
    ) -> Outcome<CommittedExpense?, StorageFailure> = { Outcome.Success(null) }

    /** When set, `create` waits here: the save is in flight. */
    var hold: CompletableDeferred<Unit>? = null

    fun committed(operation: OperationId, expense: Int = 900) = CommittedExpense(
        OperationReceipt(operation, OperationKind.CreateExpense, 1, 1, Instant.EPOCH),
        expenseId(expense)
    )

    override suspend fun create(command: CreateExpense): ExpenseCommandOutcome {
        created += command
        hold?.await()
        return answer(command)
    }

    override suspend fun find(
        operationId: OperationId
    ): Outcome<CommittedExpense?, StorageFailure> {
        looked += operationId
        return found(operationId)
    }
}

/** What "storage" holds, set by the test. */
class HandExpenseReads : ExpenseReads {
    val stored = mutableMapOf<ExpenseId, Expense>()
    var lastUsed: CategoryId? = null
    var failure: StorageFailure? = null
    var reads = 0

    override suspend fun byId(id: ExpenseId): Outcome<Expense?, StorageFailure> {
        reads++
        failure?.let { return Outcome.Failure(it) }
        return Outcome.Success(stored[id])
    }

    override suspend fun lastUsedCategory(): Outcome<CategoryId?, StorageFailure> =
        failure?.let { Outcome.Failure(it) } ?: Outcome.Success(lastUsed)
}
