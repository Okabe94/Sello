package com.software.sello.domain.policy

import com.software.sello.domain.model.BudgetLimit
import com.software.sello.domain.model.Category
import com.software.sello.domain.model.CategoryId
import com.software.sello.domain.model.CategoryName
import com.software.sello.domain.model.Expense
import com.software.sello.domain.model.ExpenseId
import com.software.sello.domain.model.IconKey
import com.software.sello.domain.model.LimitAdjustment
import com.software.sello.domain.model.Money
import com.software.sello.domain.model.NormalMonth
import com.software.sello.domain.model.ResumenSnapshot
import com.software.sello.domain.model.SpendingComparison
import com.software.sello.domain.model.SpendingDirection
import com.software.sello.domain.model.TransactionAmount
import com.software.sello.domain.port.FinancialDay
import com.software.sello.domain.valueOrFail
import java.math.BigInteger
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * The comparison, the normal month, the weekdays and the suggestion Resumen shows. Every
 * expected figure is worked out by hand in the comment beside it.
 */
class ResumenInsightsTest {
    private val bogota = ZoneId.of("America/Bogota")
    private val recorded = Instant.parse("2026-10-09T15:04:05Z")
    private val october = YearMonth.of(2026, 10)
    private val september = YearMonth.of(2026, 9)
    private val august = YearMonth.of(2026, 8)
    private var nextExpense = 500

    private fun uuid(number: Int) = "00000000-0000-4000-8000-%012d".format(number)

    private fun id(number: Int) = CategoryId.of(uuid(number)).valueOrFail()

    private fun category(number: Int, name: String, archived: Boolean = false) = Category(
        id(number),
        CategoryName.of(name).valueOrFail(),
        IconKey.of("local_cafe").valueOrFail(),
        archived,
        1,
        recorded,
        recorded
    )

    private fun limited(number: Int, name: String, pesos: Long, archived: Boolean = false) =
        CategoryInMonth(
            category(number, name, archived),
            MonthBudgetState.Limited(
                BudgetLimit.finite(Money.cop(pesos)).valueOrFail(),
                explicit = false
            )
        )

    private fun expense(date: String, pesos: Long, category: Int = 1) = Expense(
        ExpenseId.of(uuid(nextExpense)).valueOrFail(),
        id(category),
        LocalDate.parse(date),
        (nextExpense++).toLong(),
        TransactionAmount.of(Money.cop(pesos)).valueOrFail(),
        null,
        1,
        recorded,
        recorded
    )

    private val oneCategory = listOf(limited(1, "Comida", 1_000_000))

    private fun read(
        expenses: List<Expense>,
        today: String = "2026-10-09",
        month: YearMonth = october,
        categories: (YearMonth) -> List<CategoryInMonth> = { oneCategory }
    ): ResumenSnapshot = ResumenPolicy.snapshot(
        month,
        FinancialDay(LocalDate.parse(today), bogota),
        1,
        1,
        ResumenPolicy.monthsEndingIn(month).map { ResumenMonthInput(it, categories(it), expenses) },
        emptyList()
    ).valueOrFail()

    // --- How this month compares ---

    @Test
    fun theMonthInProgressIsComparedWithTheSameDaysOfTheMonthBefore() {
        val snapshot = read(
            listOf(
                expense("2026-09-03", 40_000),
                expense("2026-09-09", 10_000),
                // After the 9th: not part of the same days.
                expense("2026-09-10", 99_000),
                expense("2026-10-02", 30_000),
                expense("2026-10-09", 15_000)
            )
        )

        // October 1–9: 30.000 + 15.000 = 45.000. September 1–9: 40.000 + 10.000 = 50.000.
        // 5.000 less; 5.000 / 50.000 = 10,0 %.
        assertEquals(
            SpendingComparison(
                september,
                Money.cop(45_000),
                Money.cop(50_000),
                9,
                9,
                Money.cop(5_000),
                SpendingDirection.Less,
                BigInteger.valueOf(100)
            ),
            snapshot.comparison
        )
    }

    @Test
    fun aShorterMonthBeforeIsComparedToItsLastDay() {
        val snapshot = read(
            listOf(expense("2026-02-28", 20_000), expense("2026-03-31", 30_000)),
            today = "2026-03-31",
            month = YearMonth.of(2026, 3)
        )

        // March 1–31: 30.000. February 1–28: 20.000. 10.000 more; 10.000 / 20.000 = 50,0 %.
        val comparison = snapshot.comparison!!
        assertEquals(31 to 28, comparison.throughDay to comparison.previousThroughDay)
        assertEquals(Money.cop(10_000), comparison.difference)
        assertEquals(SpendingDirection.More, comparison.direction)
        assertEquals(BigInteger.valueOf(500), comparison.percentTenths)
    }

    @Test
    fun aFinishedMonthIsComparedWholeWithTheWholeMonthBefore() {
        val snapshot = read(
            listOf(
                expense("2026-08-15", 30_000),
                expense("2026-09-20", 40_000),
                expense("2026-10-01", 1_000)
            ),
            month = september
        )

        // September 40.000 against August 30.000: 10.000 more; 10.000 / 30.000 = 33,33 %,
        // 33,3 % half up.
        assertEquals(
            SpendingComparison(
                august,
                Money.cop(40_000),
                Money.cop(30_000),
                null,
                null,
                Money.cop(10_000),
                SpendingDirection.More,
                BigInteger.valueOf(333)
            ),
            snapshot.comparison
        )
    }

    @Test
    fun withNothingRecordedTheMonthBeforeThereIsNoComparison() {
        val snapshot = read(listOf(expense("2026-10-02", 30_000)))

        assertNull(snapshot.comparison)
    }

    @Test
    fun aMonthBeforeWithSpendingOnlyAfterTheseDaysComparesWithZeroAndNoPercent() {
        val snapshot = read(listOf(expense("2026-09-25", 80_000), expense("2026-10-02", 30_000)))

        // September has data, but nothing from the 1st to the 9th: 30.000 more than 0.
        val comparison = snapshot.comparison!!
        assertEquals(Money.cop(0), comparison.previousSpent)
        assertEquals(Money.cop(30_000), comparison.difference)
        assertEquals(SpendingDirection.More, comparison.direction)
        assertNull(comparison.percentTenths)
    }

    // --- A normal month ---

    @Test
    fun aNormalMonthIsTheAverageOfCompleteMonthsWithSomethingRecorded() {
        val snapshot = read(
            listOf(
                expense("2026-07-05", 100_001),
                // August has nothing recorded: no data, not a zero.
                expense("2026-09-01", 250_000),
                // October is in progress and does not count.
                expense("2026-10-01", 10_000)
            )
        )

        // (100.001 + 250.000) / 2 = 175.000,5, half up 175.001.
        assertEquals(NormalMonth(Money.cop(175_001), 2), snapshot.normalMonth)
    }

    @Test
    fun aFinishedMonthCountsInItsOwnNormalMonth() {
        val snapshot = read(
            listOf(expense("2026-08-05", 100_000), expense("2026-09-01", 200_000)),
            month = september
        )

        // (100.000 + 200.000) / 2 = 150.000.
        assertEquals(NormalMonth(Money.cop(150_000), 2), snapshot.normalMonth)
    }

    @Test
    fun withoutACompleteMonthThereIsNoNormalMonth() {
        assertNull(read(listOf(expense("2026-10-01", 10_000))).normalMonth)
    }

    // --- Weekdays ---

    @Test
    fun eachWeekdayIsAveragedOverHowOftenItCameRound() {
        // From the first expense, Thursday 1 October, to Friday the 9th: nine days.
        val snapshot = read(
            listOf(
                expense("2026-10-01", 10_000),
                expense("2026-10-03", 30_000),
                expense("2026-10-08", 5_001)
            )
        )

        val weekdays = snapshot.weekdays!!
        assertEquals(
            LocalDate.of(2026, 10, 1) to LocalDate.of(2026, 10, 9),
            weekdays.from to weekdays.through
        )
        val averages = weekdays.days.associate { it.day to it.average.minorUnits }
        // Thursdays 1 and 8: (10.000 + 5.001) / 2 = 7.500,5, half up 7.501.
        assertEquals(7_501L, averages[DayOfWeek.THURSDAY])
        // One Saturday: 30.000. Two Fridays with nothing: 0.
        assertEquals(30_000L, averages[DayOfWeek.SATURDAY])
        assertEquals(0L, averages[DayOfWeek.FRIDAY])
        assertEquals(DayOfWeek.entries.toList(), weekdays.days.map { it.day })
        assertEquals(DayOfWeek.SATURDAY, weekdays.busiest)
        // Recorded only since 1 October: not six months yet.
        assertEquals(false, weekdays.lastSixMonths)
    }

    @Test
    fun lessThanAWeekShowsNoWeekdays() {
        // From 1 to 6 October: six days.
        val snapshot = read(listOf(expense("2026-10-01", 10_000)), today = "2026-10-06")

        assertNull(snapshot.weekdays)
    }

    @Test
    fun whenTwoWeekdaysShareTheHighestAverageNoneStandsOut() {
        val snapshot = read(listOf(expense("2026-10-03", 30_000), expense("2026-10-04", 30_000)))

        assertNull(snapshot.weekdays!!.busiest)
    }

    @Test
    fun theWeekdaysLookBackAtMostSixMonthsCountingThisOne() {
        // 30 April is before 1 May, the start of the fifth month before October, so it
        // is left out and the window is the last six months.
        val snapshot = read(listOf(expense("2026-04-30", 900_000), expense("2026-10-01", 10_000)))

        val weekdays = snapshot.weekdays!!
        assertEquals(LocalDate.of(2026, 5, 1), weekdays.from)
        assertEquals(true, weekdays.lastSixMonths)
        assertEquals(DayOfWeek.THURSDAY, weekdays.busiest)
    }

    // --- The suggestion ---

    private val cafe = 1
    private val hogar = 2

    private fun months(
        cafeLimit: Long = 50_000,
        hogarLimit: Long = 200_000,
        cafeArchived: Boolean = false
    ): (YearMonth) -> List<CategoryInMonth> = {
        listOf(
            limited(cafe, "Café", cafeLimit, cafeArchived),
            limited(hogar, "Hogar", hogarLimit)
        )
    }

    @Test
    fun aCategoryOverItsLimitTwoMonthsInARowIsSuggestedAndTheBiggerBudgetWins() {
        val snapshot = read(
            listOf(
                expense("2026-08-10", 60_000, cafe),
                expense("2026-09-10", 80_000, cafe),
                expense("2026-08-10", 250_000, hogar),
                expense("2026-09-10", 210_000, hogar)
            ),
            categories = months()
        )

        // Both are over in August and September; Hogar's 200.000 is the bigger budget.
        // (250.000 + 210.000) / 2 = 230.000.
        assertEquals(
            LimitAdjustment(
                id(hogar),
                CategoryName.of("Hogar").valueOrFail(),
                listOf(august, september),
                Money.cop(230_000),
                Money.cop(200_000)
            ),
            snapshot.suggestion
        )
    }

    @Test
    fun overOnlyOneOfTheTwoMonthsIsNotEnough() {
        val snapshot = read(
            listOf(expense("2026-08-10", 40_000, cafe), expense("2026-09-10", 80_000, cafe)),
            categories = months()
        )

        assertNull(snapshot.suggestion)
    }

    @Test
    fun anArchivedCategoryIsNotSuggested() {
        val snapshot = read(
            listOf(expense("2026-08-10", 60_000, cafe), expense("2026-09-10", 80_000, cafe)),
            categories = months(cafeArchived = true)
        )

        assertNull(snapshot.suggestion)
    }

    @Test
    fun aFinishedMonthHasNoSuggestion() {
        val snapshot = read(
            listOf(
                expense("2026-07-10", 60_000, cafe),
                expense("2026-08-10", 80_000, cafe),
                expense("2026-09-01", 1_000, cafe)
            ),
            month = september,
            categories = months()
        )

        assertNull(snapshot.suggestion)
    }
}
