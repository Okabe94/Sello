package com.software.sello.domain.policy

import com.software.sello.domain.model.BudgetLimit
import com.software.sello.domain.model.Category
import com.software.sello.domain.model.CategoryId
import com.software.sello.domain.model.CategoryName
import com.software.sello.domain.model.Expense
import com.software.sello.domain.model.ExpenseId
import com.software.sello.domain.model.IconKey
import com.software.sello.domain.model.Money
import com.software.sello.domain.model.TransactionAmount
import com.software.sello.domain.port.FinancialDay
import com.software.sello.domain.valueOrFail
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

/** The approved ordering fixture (D06): Alimentación, Café, Transporte and unlimited Arte. */
class CategoryOrderPolicyTest {
    private var next = 100

    private fun uuid(number: Int) = "00000000-0000-4000-8000-%012d".format(number)

    private fun category(number: Int, name: String, limit: Long?) = CategoryInMonth(
        Category(
            CategoryId.of(uuid(number)).valueOrFail(),
            CategoryName.of(name).valueOrFail(),
            IconKey.of("home").valueOrFail(),
            archived = false,
            version = 1,
            createdAt = Instant.EPOCH,
            updatedAt = Instant.EPOCH
        ),
        MonthBudgetState.Limited(
            limit?.let { BudgetLimit.finite(Money.cop(it)).valueOrFail() } ?: BudgetLimit.Unlimited,
            explicit = false
        )
    )

    // Numbered so that identifier order differs from every expected order.
    private val food = category(4, "Alimentación", 100_000)
    private val coffee = category(3, "Café", 50_000)
    private val transport = category(2, "Transporte", 200_000)
    private val art = category(1, "Arte", null)
    private val all = listOf(art, transport, coffee, food)

    /** [count] expenses in [category] adding up to [total], dated in [month]. */
    private fun spending(category: CategoryInMonth, month: YearMonth, count: Int, total: Long) =
        List(count) {
            Expense(
                ExpenseId.of(uuid(next)).valueOrFail(),
                category.category.id,
                month.atDay(1 + it),
                (next++).toLong(),
                TransactionAmount.of(Money.cop(total / count)).valueOrFail(),
                note = null,
                version = 1,
                createdAt = Instant.EPOCH,
                updatedAt = Instant.EPOCH
            )
        }

    private fun order(
        month: YearMonth,
        expenses: List<Expense>,
        categories: List<CategoryInMonth> = all
    ) = CategoryOrderPolicy.mostUsed(
        MonthlyBudgetPolicy.snapshot(
            month,
            FinancialDay(LocalDate.of(2026, 10, 31), ZoneId.of("America/Bogota")),
            generation = 1,
            revision = 1,
            categories = categories,
            expenses = expenses
        ).valueOrFail().categories
    ).map { it.category.name.value }

    private val october = YearMonth.of(2026, 10)

    @Test
    fun mostUsedPutsTheCategoryWithMostExpensesFirstAndUnlimitedLast() {
        // O01: October counts 2, 3, 1 and 8 for the unlimited one.
        val expenses = spending(food, october, 2, 30_000) + spending(coffee, october, 3, 15_000) +
            spending(transport, october, 1, 40_000) + spending(art, october, 8, 80_000)

        assertEquals(listOf("Café", "Alimentación", "Transporte", "Arte"), order(october, expenses))
    }

    @Test
    fun theOrderIsOfTheSelectedMonthAndACategoryWithNoExpensesStaysBeforeUnlimitedOnes() {
        // O02: September counts 1, 0, 4 and 6.
        val september = YearMonth.of(2026, 9)
        val expenses =
            spending(food, september, 1, 10_000) + spending(transport, september, 4, 20_000) +
                spending(art, september, 6, 60_000)

        assertEquals(
            listOf("Transporte", "Alimentación", "Café", "Arte"),
            order(september, expenses)
        )
    }

    @Test
    fun equalCountsAreOrderedByNameWhateverOrderTheyWereReadIn() {
        // O07: Café drops to 2, the same as Alimentación.
        val expenses = spending(food, october, 2, 30_000) + spending(coffee, october, 2, 15_000) +
            spending(transport, october, 1, 40_000) + spending(art, october, 8, 80_000)

        val expected = listOf("Alimentación", "Café", "Transporte", "Arte")
        assertEquals(expected, order(october, expenses))
        assertEquals(expected, order(october, expenses, all.reversed()))
    }

    @Test
    fun namesAreComparedIgnoringCaseAndAccentsThenByIdentifier() {
        val categories = listOf(
            category(1, "zapatos", 1_000),
            category(2, "Ándale", 1_000),
            category(3, "arepas", 1_000),
            category(4, "Bici", 1_000)
        )

        assertEquals(
            listOf("Ándale", "arepas", "Bici", "zapatos"),
            order(october, emptyList(), categories)
        )
    }

    @Test
    fun severalUnlimitedCategoriesAreOrderedAmongThemselvesByUseToo() {
        val music = category(5, "Música", null)
        val expenses = spending(art, october, 1, 1_000) + spending(music, october, 3, 3_000)

        assertEquals(
            listOf("Alimentación", "Café", "Transporte", "Música", "Arte"),
            order(october, expenses, all + music)
        )
    }
}
