package com.software.sello.presentation.money

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.software.sello.domain.model.Currency
import com.software.sello.domain.model.Money
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResourceMoneyLabelsTest {
    private val resources = InstrumentationRegistry.getInstrumentation().targetContext.resources
    private val formatter = MoneyFormatter(ResourceMoneyLabels(resources))

    @Test
    fun spokenAmountsUseTheAppsOwnWords() {
        assertEquals("937.200 pesos", formatter.format(Money.cop(937_200)).spoken)
        assertEquals("1 peso", formatter.format(Money.cop(1)).spoken)
        assertEquals("0 pesos", formatter.format(Money.cop(0)).spoken)
        assertEquals("menos 10.000 pesos", formatter.format(Money.cop(-10_000)).spoken)
        assertEquals("más 850.000 pesos", formatter.format(Money.cop(850_000), true).spoken)
        assertEquals(
            "9.223.372.036.854.775.807 pesos",
            formatter.format(Money.cop(Long.MAX_VALUE)).spoken
        )
        assertEquals("2.340,00 USD", formatter.format(Money(234_000, Currency.USD)).spoken)
    }
}
