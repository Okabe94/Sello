package com.software.sello.domain.policy

import com.software.sello.domain.errorOrFail
import com.software.sello.domain.model.BudgetLimit
import com.software.sello.domain.model.BudgetStanding
import com.software.sello.domain.model.Category
import com.software.sello.domain.model.CategoryId
import com.software.sello.domain.model.CategoryName
import com.software.sello.domain.model.DailyAllowance
import com.software.sello.domain.model.Expense
import com.software.sello.domain.model.ExpenseId
import com.software.sello.domain.model.Forecast
import com.software.sello.domain.model.IconKey
import com.software.sello.domain.model.Money
import com.software.sello.domain.model.MonthlySnapshot
import com.software.sello.domain.model.OverallBudget
import com.software.sello.domain.model.PastVerdict
import com.software.sello.domain.model.SnapshotFailure
import com.software.sello.domain.model.TransactionAmount
import com.software.sello.domain.model.Verdict
import com.software.sello.domain.port.FinancialDay
import com.software.sello.domain.valueOrFail
import java.math.BigInteger
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The approved worked examples (B, V, F, A and H series). Every expected figure is the
 * one written in `docs/testing/mvp-financial-examples.md`, not one computed here.
 */
class MonthlyBudgetPolicyTest {
    private val bogota = ZoneId.of("America/Bogota")
    private val recorded = Instant.parse("2026-10-09T15:04:05Z")
    private var nextExpense = 500

    private fun uuid(number: Int) = "00000000-0000-4000-8000-%012d".format(number)

    private fun category(number: Int, name: String, archived: Boolean = false) = Category(
        id = CategoryId.of(uuid(number)).valueOrFail(),
        name = CategoryName.of(name).valueOrFail(),
        icon = IconKey.of("shopping_cart").valueOrFail(),
        archived = archived,
        version = 1,
        createdAt = recorded,
        updatedAt = recorded
    )

    private fun limited(number: Int, pesos: Long, name: String = "Categoría $number") =
        CategoryInMonth(
            category(number, name),
            MonthBudgetState.Limited(
                BudgetLimit.finite(Money.cop(pesos)).valueOrFail(),
                explicit = false
            )
        )

    private fun unlimited(number: Int) = CategoryInMonth(
        category(number, "Libre $number"),
        MonthBudgetState.Limited(BudgetLimit.Unlimited, explicit = false)
    )

    private fun expense(categoryNumber: Int, date: String, pesos: Long) = Expense(
        id = ExpenseId.of(uuid(nextExpense)).valueOrFail(),
        categoryId = CategoryId.of(uuid(categoryNumber)).valueOrFail(),
        date = LocalDate.parse(date),
        sequence = (nextExpense++).toLong(),
        amount = TransactionAmount.of(Money.cop(pesos)).valueOrFail(),
        note = null,
        version = 1,
        createdAt = recorded,
        updatedAt = recorded
    )

    private fun snapshot(
        today: String,
        categories: List<CategoryInMonth>,
        expenses: List<Expense>,
        month: YearMonth = YearMonth.from(LocalDate.parse(today))
    ) = MonthlyBudgetPolicy.snapshot(
        month,
        FinancialDay(LocalDate.parse(today), bogota),
        generation = 3,
        revision = 41,
        categories = categories,
        expenses = expenses
    )

    private fun MonthlySnapshot.limited() = overall as OverallBudget.Limited

    private fun MonthlySnapshot.current() = limited().budget.standing as BudgetStanding.Current

    /** One category with limit 100.000 and [spent] recorded on day 1, read on [today]. */
    private fun oneCategory(today: String, spent: Long, limit: Long = 100_000): MonthlySnapshot {
        val first = LocalDate.parse(today).withDayOfMonth(1).toString()
        val expenses = if (spent == 0L) emptyList() else listOf(expense(1, first, spent))
        return snapshot(today, listOf(limited(1, limit)), expenses).valueOrFail()
    }

    private fun exactly(pesos: Long) = Forecast(pesos.toBigInteger(), 1, Money.cop(pesos))

    @Test
    fun spendingInAnUnlimitedCategoryStillCountsAgainstTheMonth() {
        // B01.
        val read = snapshot(
            "2026-10-15",
            listOf(limited(1, 100_000), unlimited(2)),
            listOf(expense(1, "2026-10-02", 30_000), expense(2, "2026-10-03", 20_000))
        ).valueOrFail()

        assertEquals(Money.cop(50_000), read.spent)
        assertEquals(Money.cop(100_000), read.limited().budget.limit)
        assertEquals(Money.cop(50_000), read.limited().budget.spent)
        assertEquals(Money.cop(50_000), read.limited().budget.remaining)
        assertEquals(
            listOf(Money.cop(30_000), Money.cop(20_000)),
            read.categories.map { it.spent }
        )
        assertNull(read.categories[1].budget)
    }

    @Test
    fun withNoFiniteLimitThereIsNoBudgetAllowanceOrVerdict() {
        // B02, and F08 for a first run with nothing at all.
        val unlimitedOnly = snapshot(
            "2026-10-15",
            listOf(unlimited(1)),
            listOf(expense(1, "2026-10-02", 50_000))
        ).valueOrFail()
        val firstRun = snapshot("2026-10-15", emptyList(), emptyList()).valueOrFail()

        assertEquals(OverallBudget.NoLimit, unlimitedOnly.overall)
        assertEquals(Money.cop(50_000), unlimitedOnly.spent)
        assertEquals(OverallBudget.NoLimit, firstRun.overall)
        assertEquals(Money.cop(0), firstRun.spent)
        assertEquals(emptyList<Any>(), firstRun.categories)
    }

    @Test
    fun onTheLastDayTheAllowanceIsWhatRemainsForToday() {
        // B03 and F03.
        val read = oneCategory("2026-10-31", spent = 90_000)

        assertEquals(Money.cop(10_000), read.limited().budget.remaining)
        assertEquals(DailyAllowance(Money.cop(10_000), availableDays = 1), read.limited().allowance)
        assertEquals(Money.cop(90_000), read.current().forecast.rounded)
        assertEquals(Verdict.OnTrack, read.current().verdict)
    }

    @Test
    fun overspendingLeavesANegativeRemainderAZeroAllowanceAndPasado() {
        // B04 and F07.
        val read = oneCategory("2026-10-31", spent = 110_000)

        assertEquals(Money.cop(-10_000), read.limited().budget.remaining)
        assertEquals(DailyAllowance(Money.cop(0), availableDays = 1), read.limited().allowance)
        assertEquals(Money.cop(110_000), read.current().forecast.rounded)
        assertEquals(Verdict.Over, read.current().verdict)
    }

    @Test
    fun aZeroBudgetIsOnTrackUntilAnythingIsSpent() {
        // B05: no division by the zero limit.
        val nothingSpent = oneCategory("2026-10-15", spent = 0, limit = 0)
        val onePeso = oneCategory("2026-10-15", spent = 1, limit = 0)

        assertEquals(Verdict.OnTrack, nothingSpent.current().verdict)
        assertEquals(Money.cop(0), nothingSpent.limited().budget.remaining)
        assertEquals(Verdict.Over, onePeso.current().verdict)
        assertEquals(Money.cop(-1), onePeso.limited().budget.remaining)
    }

    @Test
    fun aMonthWithLimitsAndNoExpensesIsZeroSpentAndOnTrack() {
        // B06 and F06.
        val read = oneCategory("2026-10-01", spent = 0)

        assertEquals(Money.cop(0), read.spent)
        assertEquals(Money.cop(0), read.current().forecast.rounded)
        assertEquals(Verdict.OnTrack, read.current().verdict)
        assertEquals(DailyAllowance(Money.cop(3_200), availableDays = 31), read.limited().allowance)
    }

    @Test
    fun oneCategoryOverItsLimitDoesNotZeroTheOverallAllowance() {
        // B08, owner-confirmed: 22 October, three limits of 100.000, Food spent 110.000.
        val read = snapshot(
            "2026-10-22",
            listOf(limited(1, 100_000, "Comida"), limited(2, 100_000), limited(3, 100_000)),
            listOf(expense(1, "2026-10-20", 110_000))
        ).valueOrFail()

        val food = read.categories.first().budget!!
        assertEquals(Money.cop(-10_000), food.remaining)
        assertEquals(Verdict.Over, (food.standing as BudgetStanding.Current).verdict)
        assertEquals(Money.cop(300_000), read.limited().budget.limit)
        assertEquals(Money.cop(110_000), read.limited().budget.spent)
        assertEquals(Money.cop(190_000), read.limited().budget.remaining)
        assertEquals(
            DailyAllowance(Money.cop(19_000), availableDays = 10),
            read.limited().allowance
        )
        assertEquals(Money.cop(100_000), read.categories[1].budget!!.remaining)
    }

    @Test
    fun theVerdictBoundariesAreExactlyNinetyFiveAndOneHundredPercent() {
        // V01 to V04: limit 100.000, classifier inputs.
        val limit = Money.cop(100_000)
        val spent = Money.cop(80_000)

        assertEquals(Verdict.OnTrack, MonthlyBudgetPolicy.verdict(limit, spent, exactly(95_000)))
        assertEquals(Verdict.Tight, MonthlyBudgetPolicy.verdict(limit, spent, exactly(95_001)))
        assertEquals(Verdict.Tight, MonthlyBudgetPolicy.verdict(limit, spent, exactly(100_000)))
        assertEquals(Verdict.Over, MonthlyBudgetPolicy.verdict(limit, spent, exactly(100_001)))
        assertEquals(
            Verdict.Over,
            MonthlyBudgetPolicy.verdict(limit, Money.cop(100_001), exactly(90_000))
        )
    }

    @Test
    fun theVerdictUsesTheExactForecastNotTheRoundedOneShown() {
        // V05: 15 February 2027, spent 50.893. Forecast 1.425.004/15, shown as 95.000.
        val read = oneCategory("2027-02-15", spent = 50_893)

        assertEquals(BigInteger.valueOf(1_425_004), read.current().forecast.numerator)
        assertEquals(15, read.current().forecast.denominator)
        assertEquals(Money.cop(95_000), read.current().forecast.rounded)
        assertEquals(Verdict.Tight, read.current().verdict)
    }

    @Test
    fun theForecastAndAllowanceFollowTheApprovedDayCounts() {
        // F01, F02, F04, F05: (today, spent) to (forecast shown, days, allowance, verdict).
        val cases = listOf(
            Triple("2026-10-01", 10_000L, listOf(310_000L, 31L, 2_900L)) to Verdict.Over,
            Triple("2026-10-15", 40_000L, listOf(82_667L, 17L, 3_500L)) to Verdict.OnTrack,
            Triple("2027-02-14", 30_000L, listOf(60_000L, 15L, 4_650L)) to Verdict.OnTrack,
            Triple("2028-02-15", 45_000L, listOf(87_000L, 15L, 3_650L)) to Verdict.OnTrack
        )

        for ((input, verdict) in cases) {
            val (today, spent, expected) = input
            val read = oneCategory(today, spent)

            assertEquals(today, Money.cop(expected[0]), read.current().forecast.rounded)
            assertEquals(
                today,
                DailyAllowance(Money.cop(expected[2]), expected[1].toInt()),
                read.limited().allowance
            )
            assertEquals(today, verdict, read.current().verdict)
        }
    }

    @Test
    fun theAllowanceRoundsDownToFiftyPesosAndNeverBelowZero() {
        // A01 to A08: (remaining, available days) to shown allowance.
        val cases = listOf(
            (59_993L to 17) to 3_500L,
            (60_588L to 17) to 3_550L,
            (849L to 17) to 0L,
            (850L to 17) to 50L,
            (1_699L to 17) to 50L,
            (1_700L to 17) to 100L,
            (49L to 1) to 0L,
            (-10_000L to 1) to 0L
        )

        for ((input, shown) in cases) {
            val (remaining, days) = input

            assertEquals(
                "$input",
                DailyAllowance(Money.cop(shown), days),
                MonthlyBudgetPolicy.allowance(Money.cop(remaining), days)
            )
        }
    }

    @Test
    fun aPastMonthShowsActualsOnlyWithNoForecastOrAllowance() {
        // H01 and H04: September 100.000 with 20.000 spent, read in October.
        val read = snapshot(
            "2026-10-09",
            listOf(limited(1, 100_000)),
            listOf(expense(1, "2026-09-12", 20_000)),
            month = YearMonth.of(2026, 9)
        ).valueOrFail()

        assertEquals(Money.cop(80_000), read.limited().budget.remaining)
        assertEquals(BudgetStanding.Past(PastVerdict.WithinLimit), read.limited().budget.standing)
        assertNull(read.limited().allowance)
        assertEquals(
            BudgetStanding.Past(PastVerdict.WithinLimit),
            read.categories.single().budget!!.standing
        )
    }

    @Test
    fun aPastMonthIsOverOnlyWhenMoreThanTheLimitWasSpent() {
        assertEquals(
            PastVerdict.WithinLimit,
            MonthlyBudgetPolicy.pastVerdict(Money.cop(100_000), Money.cop(100_000))
        )
        assertEquals(
            PastVerdict.Over,
            MonthlyBudgetPolicy.pastVerdict(Money.cop(100_000), Money.cop(100_001))
        )
        assertEquals(
            PastVerdict.WithinLimit,
            MonthlyBudgetPolicy.pastVerdict(Money.cop(0), Money.cop(0))
        )
    }

    @Test
    fun expensesDatedAfterTodayAreLeftOutAndCounted() {
        val read = snapshot(
            "2026-10-15",
            listOf(limited(1, 100_000)),
            listOf(
                expense(1, "2026-10-15", 30_000),
                expense(1, "2026-10-16", 999_000),
                expense(1, "2026-10-31", 1)
            )
        ).valueOrFail()

        assertEquals(Money.cop(30_000), read.spent)
        assertEquals(Money.cop(70_000), read.limited().budget.remaining)
        assertEquals(2, read.futureDatedExpenses)
        assertEquals(Money.cop(30_000), read.categories.single().spent)
    }

    @Test
    fun spendingWithoutAnyLimitStateStillCountsAndArchivedCategoriesShowOnlyWithHistory() {
        val read = snapshot(
            "2026-10-15",
            listOf(
                limited(1, 100_000),
                CategoryInMonth(category(2, "Sin configurar"), MonthBudgetState.Unconfigured),
                CategoryInMonth(category(3, "Archivada con gasto", true), MonthBudgetState.Paused),
                CategoryInMonth(category(4, "Archivada vacía", true), MonthBudgetState.Paused),
                CategoryInMonth(
                    category(5, "Archivada con límite", true),
                    MonthBudgetState.Limited(
                        BudgetLimit.finite(Money.cop(50_000)).valueOrFail(),
                        explicit = true
                    )
                )
            ),
            listOf(expense(2, "2026-10-02", 7_000), expense(3, "2026-10-03", 5_000))
        ).valueOrFail()

        assertEquals(
            listOf("Categoría 1", "Sin configurar", "Archivada con gasto", "Archivada con límite"),
            read.categories.map { it.category.name.value }
        )
        assertEquals(Money.cop(12_000), read.spent)
        // The archived category's saved limit still belongs to the month.
        assertEquals(Money.cop(150_000), read.limited().budget.limit)
        assertEquals(Money.cop(138_000), read.limited().budget.remaining)
    }

    @Test
    fun aTotalThatDoesNotFitIsAFailureNotAClippedAmount() {
        val read = snapshot(
            "2026-10-15",
            listOf(limited(1, Long.MAX_VALUE), limited(2, 1)),
            emptyList()
        )

        assertEquals(SnapshotFailure.Unrepresentable, read.errorOrFail())
    }

    @Test
    fun theLargestLimitOnItsOwnIsStillExact() {
        val read = snapshot("2026-10-15", listOf(limited(1, Long.MAX_VALUE)), emptyList())

        assertEquals(Money.cop(Long.MAX_VALUE), read.valueOrFail().limited().budget.remaining)
    }

    @Test
    fun aMonthAfterTheCurrentOneCannotBeRead() {
        val read = snapshot(
            "2026-10-15",
            listOf(limited(1, 100_000)),
            emptyList(),
            month = YearMonth.of(2026, 11)
        )

        assertEquals(SnapshotFailure.FutureMonth(YearMonth.of(2026, 10)), read.errorOrFail())
    }

    @Test
    fun theSnapshotSaysWhenAndAtWhichRevisionItWasRead() {
        val read = oneCategory("2026-10-15", spent = 40_000)

        assertEquals(YearMonth.of(2026, 10), read.month)
        assertEquals(LocalDate.of(2026, 10, 15), read.asOf)
        assertEquals(bogota, read.zone)
        assertEquals(3L to 41L, read.generation to read.revision)
    }
}
