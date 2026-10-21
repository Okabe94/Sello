package com.software.sello.feature.resumen

import com.software.sello.designsystem.component.ChartPeriod
import com.software.sello.domain.model.Currency
import com.software.sello.domain.model.DailySpending
import com.software.sello.domain.model.Money
import com.software.sello.domain.model.MonthlySnapshot
import com.software.sello.domain.model.OverallBudget
import com.software.sello.domain.model.PastVerdict
import com.software.sello.domain.model.ResumenSnapshot
import com.software.sello.domain.model.SnapshotFailure
import com.software.sello.domain.model.SpendingMonth
import com.software.sello.domain.port.ResumenSnapshotState
import com.software.sello.feature.expense.categoryIn
import com.software.sello.feature.expense.monthOf
import com.software.sello.feature.expense.storedExpense
import com.software.sello.presentation.money.MoneyFormatter
import com.software.sello.presentation.money.MoneyLabels
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

internal fun resumenMoney() = MoneyFormatter(object : MoneyLabels {
    override fun amount(currency: Currency, digits: String, isOne: Boolean) = "$digits pesos"
    override fun negative(amount: String) = "menos $amount"
    override fun positive(amount: String) = "más $amount"
})

internal fun resumenSnapshot(spent: Long = 30_000, income: Long = 150_000): ResumenSnapshot {
    val month = YearMonth.of(2026, 10)
    return ResumenSnapshot(
        MonthlySnapshot(
            month, LocalDate.of(2026, 10, 3), ZoneId.of("America/Bogota"), 1, 7,
            emptyList(), Money.cop(spent), OverallBudget.NoLimit, 0
        ),
        Money.cop(income), Money.cop(income - spent), Money.cop(100_000),
        Money.cop(income - 100_000), PastVerdict.WithinLimit,
        listOf(
            DailySpending(month.atDay(1), Money.cop(10_000), Money.cop(10_000)),
            DailySpending(month.atDay(2), Money.cop(15_000), Money.cop(25_000)),
            DailySpending(month.atDay(3), Money.cop(5_000), Money.cop(spent))
        ),
        listOf(SpendingMonth(month, Money.cop(spent), Money.cop(100_000), null, 3)), 0
    )
}

class ResumenRenderModelTest {
    private val render = ResumenRenderModel(resumenMoney())

    @Test
    fun cashFlowAndPlanningCapacityAreDifferentExactAmounts() {
        val state = render.render(ResumenSnapshotState.Ready(resumenSnapshot()))
        assertTrue(state is ResumenState.Ready)
        state as ResumenState.Ready
        assertEquals("120.000 pesos", state.netFlow.spoken)
        assertEquals("50.000 pesos", state.unallocated.spoken)
        assertFalse(state.stale)
    }

    @Test
    fun chartsKeepExactValuesAndDatesNotRoundedCoordinates() {
        val state = render.render(ResumenSnapshotState.Ready(resumenSnapshot()))
        assertTrue(state is ResumenState.Ready)
        state as ResumenState.Ready
        assertEquals(listOf(10_000L, 25_000L, 30_000L), state.daily.map { it.chart.value })
        assertEquals("2026-10-02", state.daily[1].chart.id)
        assertEquals("25.000 pesos", state.daily[1].amount.spoken)
        assertEquals("15.000 pesos", state.daily[1].detail?.spoken)
        assertTrue(state.daily[1].chart.spoken.contains("25.000 pesos"))
        assertTrue(state.insufficientHistory)
    }

    @Test
    fun largeAndNegativeFiguresStayExact() {
        val snapshot = resumenSnapshot().copy(
            netFlow = Money.cop(-9_007_199_254_740_993),
            unallocated = Money.cop(-50_000),
            history = listOf(
                SpendingMonth(
                    YearMonth.of(2026, 10),
                    Money.cop(9_007_199_254_740_993),
                    null,
                    null,
                    1
                )
            )
        )
        val state = render.render(ResumenSnapshotState.Ready(snapshot))
        assertTrue(state is ResumenState.Ready)
        state as ResumenState.Ready
        assertEquals("menos 9.007.199.254.740.993 pesos", state.netFlow.spoken)
        assertEquals("menos 50.000 pesos", state.unallocated.spoken)
        assertEquals(9_007_199_254_740_993L, state.history.single().chart.value)
        assertEquals("9.007.199.254.740.993 pesos", state.history.single().amount.spoken)
    }

    @Test
    fun readFailureIsNotAnEmptyMonthAndLastGoodIsExplicitlyStale() {
        assertEquals(
            ResumenState.Failed,
            render.render(
                ResumenSnapshotState.Failed(SnapshotFailure.Unrepresentable, null)
            )
        )
        val state = render.render(
            ResumenSnapshotState.Failed(SnapshotFailure.Unrepresentable, resumenSnapshot())
        )
        assertTrue(state is ResumenState.Ready)
        assertTrue((state as ResumenState.Ready).stale)
        assertEquals("2026-10-03", state.asOf)
    }

    // --- v2: the month in progress, the limit line and the ranking ---

    private val october = YearMonth.of(2026, 10)
    private val september = YearMonth.of(2026, 9)

    @Test
    fun theMonthInProgressIsAlwaysMarkedCurrentEvenOverItsLimitAndCarriesTheLimitLine() {
        // September ended over its 100.000; October is over its 100.000 so far.
        val snapshot = resumenSnapshot().copy(
            history = listOf(
                SpendingMonth(september, Money.cop(120_000), Money.cop(100_000), null, 4),
                SpendingMonth(october, Money.cop(130_000), Money.cop(100_000), null, 3)
            )
        )

        val state = render.render(ResumenSnapshotState.Ready(snapshot)) as ResumenState.Ready

        assertEquals(listOf(false, true), state.history.map { it.current })
        assertEquals(
            listOf(ChartPeriod.Over, ChartPeriod.Current),
            state.history.map { it.chart.period }
        )
        assertEquals(
            100_000L to "100.000 pesos",
            state.monthLimit?.let {
                it.value to
                    it.amount.spoken
            }
        )
    }

    @Test
    fun aClosedMonthIsNotDrawnAsInProgressAndAMonthWithoutLimitHasNoLine() {
        val snapshot = resumenSnapshot().copy(
            budget = resumenSnapshot().budget.copy(month = september),
            history = listOf(SpendingMonth(september, Money.cop(80_000), null, null, 2))
        )

        val state = render.render(ResumenSnapshotState.Ready(snapshot)) as ResumenState.Ready

        assertEquals(listOf(false), state.history.map { it.current })
        assertEquals(null, state.monthLimit)
    }

    @Test
    fun categoriesAreRankedByWhatWasSpentWithTheirShareOfTheMonthAndAddUpToIt() {
        val budget = monthOf(
            "2026-10-03",
            listOf(
                categoryIn(1, "Comida", 300_000, icon = "restaurant"),
                categoryIn(2, "Casa", null, icon = "home"),
                categoryIn(3, "Ocio", 50_000, icon = "movie")
            ),
            listOf(
                storedExpense(1, 1, "2026-10-01", 15_000),
                storedExpense(2, 2, "2026-10-02", 45_000),
                storedExpense(3, 1, "2026-10-03", 15_000)
            )
        )
        val snapshot = resumenSnapshot().copy(budget = budget)

        val state = render.render(ResumenSnapshotState.Ready(snapshot)) as ResumenState.Ready

        // Spent 75.000: Casa 45.000 (3/5), Comida 30.000 (2/5); Ocio spent nothing.
        assertEquals(
            listOf("Casa" to "45.000 pesos", "Comida" to "30.000 pesos"),
            state.categories.map { it.name to it.amount.spoken }
        )
        assertEquals(listOf("home", "restaurant"), state.categories.map { it.iconKey })
        assertEquals(0.6f, state.categories[0].share, 0.0001f)
        assertEquals(0.4f, state.categories[1].share, 0.0001f)
        assertEquals("75.000 pesos", state.spent.spoken)
    }
}
