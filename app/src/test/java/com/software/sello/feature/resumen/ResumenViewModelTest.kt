package com.software.sello.feature.resumen

import com.software.sello.domain.model.Currency
import com.software.sello.domain.model.StorageFailure
import com.software.sello.domain.port.IncomeMonthState
import com.software.sello.feature.expense.HandIncomeReads
import com.software.sello.feature.expense.incomeId
import com.software.sello.feature.expense.storedIncome
import com.software.sello.feature.resumen.ResumenAction
import com.software.sello.feature.resumen.ResumenState
import com.software.sello.feature.resumen.ResumenViewModel
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
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ResumenViewModelTest {
    @Before
    fun main() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun reset() = Dispatchers.resetMain()

    @Test
    fun usesTheSharedMonthAndNeverShowsAReadFailureAsZeroIncome() {
        val reads = HandIncomeReads()
        val session = MonthSession(HandFinancialClock("2026-10-13"), HandMonotonicClock())
        val money = MoneyFormatter(object : MoneyLabels {
            override fun amount(currency: Currency, digits: String, isOne: Boolean) =
                "$digits pesos"
            override fun negative(amount: String) = "menos $amount"
            override fun positive(amount: String) = "más $amount"
        })
        val model = ResumenViewModel(reads, session, money)
        val october = YearMonth.of(2026, 10)
        assertEquals(ResumenState.Loading, model.state.value)
        reads.stored[incomeId(1)] = storedIncome(1, "2026-10-01", 150_000)
        reads.publish(october)
        assertEquals("150.000 pesos", (model.state.value as ResumenState.Ready).income.spoken)
        reads.of(october).value = IncomeMonthState.Failed(StorageFailure.Unavailable("x"), null)
        assertEquals(ResumenState.Failed, model.state.value)
        model.onAction(ResumenAction.Retry)
        reads.publish(october)
        assertEquals("150.000 pesos", (model.state.value as ResumenState.Ready).income.spoken)
        val september = YearMonth.of(2026, 9)
        session.select(september)
        assertEquals(ResumenState.Loading, model.state.value)
        reads.publish(september)
        assertEquals("0 pesos", (model.state.value as ResumenState.Ready).income.spoken)
    }
}
