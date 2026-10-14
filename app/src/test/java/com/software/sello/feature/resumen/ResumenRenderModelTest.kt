package com.software.sello.feature.resumen

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
        assertEquals(PastVerdict.WithinLimit, state.actualVerdict)
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
}
