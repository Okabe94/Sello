package com.software.sello.presentation.money

import com.software.sello.designsystem.component.MoneySign
import com.software.sello.designsystem.component.MoneyTextValue
import com.software.sello.domain.model.Currency
import com.software.sello.domain.model.Money
import org.junit.Assert.assertEquals
import org.junit.Test

class MoneyFormatterTest {
    private object Labels : MoneyLabels {
        override fun amount(currency: Currency, digits: String, isOne: Boolean) =
            "$digits ${currency.code}${if (isOne) "/one" else "/other"}"

        override fun negative(amount: String) = "menos $amount"

        override fun positive(amount: String) = "más $amount"
    }

    private val formatter = MoneyFormatter(Labels)

    private fun cop(pesos: Long, signed: Boolean = false) =
        formatter.format(Money.cop(pesos), signed)

    @Test
    fun copIsGroupedWithPointsAndNoDecimals() {
        // Typed by hand; grouping is not derived with the code under test.
        val expected = mapOf(
            0L to "0",
            1L to "1",
            12L to "12",
            999L to "999",
            1_000L to "1.000",
            1_234L to "1.234",
            12_345L to "12.345",
            937_200L to "937.200",
            1_000_000L to "1.000.000",
            999_999_999_999L to "999.999.999.999",
            1_200_000_000_000L to "1.200.000.000.000",
            Long.MAX_VALUE to "9.223.372.036.854.775.807"
        )
        for ((pesos, digits) in expected) {
            val value = cop(pesos)
            assertEquals(digits, value.digits)
            assertEquals("COP shows no currency mark", "", value.symbol)
            assertEquals(MoneySign.None, value.sign)
        }
    }

    @Test
    fun negativesCarryAMinusSignAndKeepEveryDigit() {
        assertEquals(
            MoneyTextValue(MoneySign.Minus, "", "10.000", "menos 10.000 COP/other"),
            cop(-10_000)
        )
        assertEquals(MoneySign.Minus, cop(-1).sign)
        assertEquals("1", cop(-1).digits)
        // The one amount whose absolute value does not fit a Long.
        val lowest = cop(Long.MIN_VALUE)
        assertEquals(MoneySign.Minus, lowest.sign)
        assertEquals("9.223.372.036.854.775.808", lowest.digits)
    }

    @Test
    fun aPlusAppearsOnlyWhenAskedAndNeverOnZero() {
        assertEquals(MoneySign.None, cop(850_000).sign)
        assertEquals(
            MoneyTextValue(MoneySign.Plus, "", "850.000", "más 850.000 COP/other"),
            cop(850_000, signed = true)
        )
        assertEquals(MoneyTextValue(MoneySign.None, "", "0", "0 COP/other"), cop(0, signed = true))
        assertEquals(MoneySign.Minus, cop(-5, signed = true).sign)
    }

    @Test
    fun spokenValueNamesTheCurrencyAndKnowsSingular() {
        assertEquals("937.200 COP/other", cop(937_200).spoken)
        assertEquals("1 COP/one", cop(1).spoken)
        assertEquals("menos 1 COP/one", cop(-1).spoken)
        assertEquals("0 COP/other", cop(0).spoken)
        assertEquals("1.000 COP/other", cop(1_000).spoken)
    }

    @Test
    fun otherCurrenciesShowTheirDecimalsWithACommaAndTheirCode() {
        fun usd(cents: Long) = formatter.format(Money(cents, Currency.USD))
        assertEquals(
            MoneyTextValue(MoneySign.None, "USD", "2.340,00", "2.340,00 USD/other"),
            usd(234_000)
        )
        assertEquals("0,05", usd(5).digits)
        assertEquals("0,00", usd(0).digits)
        assertEquals("1,00", usd(100).digits)
        assertEquals("1,00 USD/one", usd(100).spoken)
        assertEquals("1,50", usd(-150).digits)
        assertEquals(MoneySign.Minus, usd(-150).sign)
        assertEquals("92.233.720.368.547.758,07", usd(Long.MAX_VALUE).digits)
        assertEquals("EUR", formatter.format(Money(1, Currency.EUR)).symbol)
    }
}
