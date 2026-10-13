package com.software.sello.feature.income

import com.software.sello.domain.model.Currency
import com.software.sello.domain.model.StorageFailure
import com.software.sello.domain.port.IncomeMonthState
import com.software.sello.feature.expense.HandIncomeReads
import com.software.sello.feature.expense.incomeId
import com.software.sello.feature.expense.storedIncome
import com.software.sello.navigation.HandFinancialClock
import com.software.sello.navigation.HandMonotonicClock
import com.software.sello.navigation.MonthSession
import com.software.sello.presentation.money.MoneyFormatter
import com.software.sello.presentation.money.MoneyLabels
import java.time.YearMonth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class IncomeHistoryViewModelTest {
    private val reads = HandIncomeReads()
    private val clock = HandFinancialClock("2026-10-22")
    private val session = MonthSession(clock, HandMonotonicClock())
    private val october = YearMonth.of(2026, 10)
    private val money = MoneyFormatter(object : MoneyLabels {
        override fun amount(currency: Currency, digits: String, isOne: Boolean) = "$digits pesos"
        override fun negative(amount: String) = "menos $amount"
        override fun positive(amount: String) = "más $amount"
    })

    @Before
    fun main() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun reset() = Dispatchers.resetMain()

    @Test
    fun pagesAreBoundedAndTheTotalIsNotCalculatedFromTheFirstPage() {
        repeat(51) { index ->
            reads.stored[incomeId(index + 1)] = storedIncome(index + 1, "2026-10-01", 1_000)
        }
        val model = IncomeHistoryViewModel(reads, session, money, clock)
        reads.publish(october)
        val first = model.state.value as IncomeHistoryState.Ready
        assertEquals(50, first.lines.size)
        assertEquals("51.000 pesos", first.total.spoken)
        assertTrue(first.more)
        model.onAction(IncomeHistoryAction.More)
        val all = model.state.value as IncomeHistoryState.Ready
        assertEquals(51, all.lines.size)
        assertEquals(51, all.lines.map { it.id }.distinct().size)
        assertEquals(listOf(50, 50), reads.pages.map { it.second })
    }

    @Test
    fun failuresAreNotAnEmptyMonthAndChangingMonthDoesNotKeepOldRows() {
        reads.stored[incomeId(1)] = storedIncome(1, "2026-10-01", 150_000)
        val model = IncomeHistoryViewModel(reads, session, money, clock)
        reads.publish(october)
        assertEquals(1, (model.state.value as IncomeHistoryState.Ready).lines.size)
        reads.of(october).value = IncomeMonthState.Failed(StorageFailure.Unavailable("x"), null)
        assertEquals(IncomeHistoryState.Failed, model.state.value)
        val september = YearMonth.of(2026, 9)
        session.select(september)
        assertEquals(IncomeHistoryState.Loading, model.state.value)
        reads.publish(september)
        assertEquals(emptyList<IncomeLine>(), (model.state.value as IncomeHistoryState.Ready).lines)
    }
}
