package com.software.sello

import com.software.sello.domain.model.BudgetLimit
import com.software.sello.domain.model.CategoryId
import com.software.sello.domain.model.CategoryName
import com.software.sello.domain.model.IconKey
import com.software.sello.domain.model.Money
import com.software.sello.domain.model.Note
import com.software.sello.domain.model.OperationId
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.TransactionAmount
import com.software.sello.domain.port.CategoryCommandOutcome
import com.software.sello.domain.port.CategoryCommands
import com.software.sello.domain.port.CreateCategory
import com.software.sello.domain.port.CreateExpense
import com.software.sello.domain.port.ExpenseCommandOutcome
import com.software.sello.domain.port.ExpenseCommands
import com.software.sello.domain.port.FinancialClock
import com.software.sello.domain.port.SetMonthLimit
import com.software.sello.navigation.MonthSession
import java.time.LocalDate
import java.time.YearMonth
import java.util.UUID
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.koin.core.context.GlobalContext

/**
 * The running app's own commands, for a test that needs more history than is worth
 * tapping in. Everything goes through the same workflows the screens use, so the app
 * hears of each change as it would of its own; nothing is written around them.
 */
object TestCommands {
    private val koin get() = GlobalContext.get()

    val today: LocalDate get() = koin.get<FinancialClock>().today.value.date

    private fun <T> Outcome<T, *>.value(): T = (this as Outcome.Success).value

    private fun operation(): OperationId = OperationId.of(UUID.randomUUID().toString()).value()

    private fun finite(pesos: Long): BudgetLimit = BudgetLimit.finite(Money.cop(pesos)).value()

    /**
     * The month selection lives as long as the process, which every test in a run
     * shares. A test that depends on the month, or changed it, puts it back.
     */
    fun showCurrentMonth() {
        koin.get<MonthSession>().select(YearMonth.from(today))
    }

    /** The identifier of the category called [name]. */
    fun categoryId(name: String): String =
        TestData.rows("SELECT id FROM category WHERE name = '$name'").single()

    /** [limit] in pesos, or null for no limit. */
    fun createCategory(name: String, limit: Long?) = runBlocking {
        val outcome = koin.get<CategoryCommands>().submit(
            CreateCategory(
                operation(),
                1,
                CategoryName.of(name).value(),
                IconKey.of("home").value(),
                limit?.let(::finite) ?: BudgetLimit.Unlimited
            )
        )
        assertTrue("$outcome", outcome is CategoryCommandOutcome.Committed)
    }

    fun setMonthLimit(category: String, month: YearMonth, pesos: Long) = runBlocking {
        val id = categoryId(category)
        val outcome = koin.get<CategoryCommands>().submit(
            SetMonthLimit(
                operation(),
                1,
                CategoryId.of(id).value(),
                TestData.long("SELECT version FROM category WHERE id = '$id'"),
                month,
                finite(pesos)
            )
        )
        assertTrue("$outcome", outcome is CategoryCommandOutcome.Committed)
    }

    fun createExpense(category: String, date: LocalDate, pesos: Long, note: String? = null) =
        runBlocking {
            val outcome = koin.get<ExpenseCommands>().create(
                CreateExpense(
                    operation(),
                    1,
                    CategoryId.of(categoryId(category)).value(),
                    TransactionAmount.of(Money.cop(pesos)).value(),
                    date,
                    Note.of(note).value()
                )
            )
            assertTrue("$outcome", outcome is ExpenseCommandOutcome.Committed)
        }
}
