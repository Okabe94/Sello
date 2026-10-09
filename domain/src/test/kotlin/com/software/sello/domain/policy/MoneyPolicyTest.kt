package com.software.sello.domain.policy

import com.software.sello.domain.errorOrFail
import com.software.sello.domain.model.Currency
import com.software.sello.domain.model.ExactTotal
import com.software.sello.domain.model.Money
import com.software.sello.domain.model.MoneyError
import com.software.sello.domain.model.TransactionAmount
import com.software.sello.domain.model.TransactionAmountError
import com.software.sello.domain.valueOrFail
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyPolicyTest {
    private fun parse(text: String) = CopAmountInput.parse(text)

    // ---- Input grammar: M01–M03 and the D05 paste rules ----

    @Test
    fun plainAndCorrectlyGroupedDigitsParseExactly() {
        val valid = mapOf(
            "1.234" to 1_234L, // M01
            "1234" to 1_234L,
            "0" to 0L,
            "7" to 7L,
            "999" to 999L,
            "1.000" to 1_000L,
            "12.345" to 12_345L,
            "123.456" to 123_456L,
            "1.234.567" to 1_234_567L,
            "999999999999" to 999_999_999_999L,
            "999.999.999.999" to 999_999_999_999L,
            "1000000000000" to 1_000_000_000_000L,
            "9223372036854775807" to Long.MAX_VALUE,
            "9.223.372.036.854.775.807" to Long.MAX_VALUE,
            "  1.234  " to 1_234L,
            "\t50\n" to 50L
        )
        for ((text, pesos) in valid) {
            assertEquals(text, Money.cop(pesos), parse(text).valueOrFail())
        }
    }

    @Test
    fun emptyDraftsAreEmptyNotZero() {
        for (text in listOf("", " ", "   ", "\t", "\n", " ")) {
            assertEquals(AmountInputError.Empty(text), parse(text).errorOrFail())
        }
    }

    @Test
    fun anythingButDigitsAndGroupingDotsIsRejectedWithTheOriginalText() {
        val notDigits = listOf(
            "1e3", "1E3", "-100", "+100", "−100", "1,5", "1,234", "$1234", "$ 1.234",
            "1234 COP", "COP 1234", "1 234", "1 234", "1_234", "1'234", "12a4", "abc",
            "0x10", "١٢٣", "１２３", "1.234,00", "(100)", "1.234-", "1..234e2"
        )
        for (text in notDigits) {
            assertEquals(text, AmountInputError.NotDigits(text), parse(text).errorOrFail())
        }
    }

    @Test
    fun dotsThatAreNotThousandsSeparatorsAreRejected() {
        val malformed = listOf(
            "12.34", "1.23", "1.2345", "1234.567", "1.234.56", "12.3456.789", ".123", "123.",
            "1..234", ".", "1.234.", ".1.234", "1.2.3", "1234.5", "1.5"
        )
        for (text in malformed) {
            assertEquals(text, AmountInputError.MalformedGrouping(text), parse(text).errorOrFail())
        }
    }

    @Test
    fun leadingZerosAreRejectedRatherThanDropped() {
        for (text in listOf("00", "007", "0050", "0.123", "01.234", "000.000")) {
            assertEquals(text, AmountInputError.LeadingZero(text), parse(text).errorOrFail())
        }
    }

    @Test
    fun amountsBeyondLongAreOutOfRangeNeverWrapped() {
        val tooBig = listOf(
            "9223372036854775808",
            "9.223.372.036.854.775.808", // M02
            "99999999999999999999",
            "1" + "0".repeat(40),
            "1" + ".000".repeat(13)
        )
        for (text in tooBig) {
            assertEquals(text, AmountInputError.OutOfRange(text), parse(text).errorOrFail())
        }
    }

    @Test
    fun rejectionKeepsTheOriginalTextIncludingWhitespace() {
        assertEquals(" 1e3 ", parse(" 1e3 ").errorOrFail().original)
        assertEquals(" 12.34", parse(" 12.34").errorOrFail().original)
    }

    // ---- Transaction range: M03–M06, ADR 0003 ----

    @Test
    fun transactionAmountsAreOneTo999Billion() {
        fun of(pesos: Long) = TransactionAmount.of(Money.cop(pesos))
        assertEquals(TransactionAmountError.NotPositive, of(0).errorOrFail()) // M03
        assertEquals(TransactionAmountError.NotPositive, of(-1).errorOrFail())
        assertEquals(TransactionAmountError.NotPositive, of(Long.MIN_VALUE).errorOrFail())
        assertEquals(Money.cop(1), of(1).valueOrFail().money)
        assertEquals(Money.cop(999_999_999_999), of(999_999_999_999).valueOrFail().money) // M04
        // M05
        assertEquals(TransactionAmountError.AboveMaximum, of(1_000_000_000_000).errorOrFail())
        assertEquals(TransactionAmountError.AboveMaximum, of(Long.MAX_VALUE).errorOrFail())
        assertEquals(12, TransactionAmount.MAX_DIGITS)
        assertEquals("999999999999".length, TransactionAmount.MAX_DIGITS)
    }

    @Test
    fun onlyCopCanBeATransactionAmountInTheMvp() {
        for (currency in listOf(Currency.USD, Currency.EUR, Currency.GBP)) {
            assertEquals(
                TransactionAmountError.UnsupportedCurrency(currency),
                TransactionAmount.of(Money(1_000, currency)).errorOrFail()
            )
        }
        assertEquals(listOf(Currency.COP), Currency.entries.filter { it.isMvpEntryCurrency })
    }

    @Test
    fun aParsedZeroIsValidMoneyButNotAValidTransaction() {
        val zero = parse("0").valueOrFail()
        assertEquals(Money.cop(0), zero)
        assertEquals(TransactionAmountError.NotPositive, TransactionAmount.of(zero).errorOrFail())
    }

    @Test
    fun currencyScalesFollowTheArchitecture() {
        assertEquals(
            mapOf("COP" to 0, "USD" to 2, "EUR" to 2, "GBP" to 2),
            Currency.entries.associate { it.code to it.scale }
        )
    }

    // ---- Checked arithmetic ----

    @Test
    fun additionAndSubtractionAreExactWithinRange() {
        assertEquals(Money.cop(50_000), (Money.cop(30_000) + Money.cop(20_000)).valueOrFail())
        assertEquals(Money.cop(-10_000), (Money.cop(100_000) - Money.cop(110_000)).valueOrFail())
        assertEquals(
            Money.cop(Long.MAX_VALUE),
            (Money.cop(Long.MAX_VALUE - 1) + Money.cop(1)).valueOrFail()
        )
        assertEquals(
            Money.cop(Long.MIN_VALUE),
            (Money.cop(Long.MIN_VALUE + 1) - Money.cop(1)).valueOrFail()
        )
    }

    @Test
    fun overflowIsAnExplicitFailureNeverAWrapOrClip() {
        val overflows = listOf(
            Money.cop(Long.MAX_VALUE) + Money.cop(1),
            Money.cop(Long.MIN_VALUE) + Money.cop(-1),
            Money.cop(Long.MIN_VALUE) - Money.cop(1),
            Money.cop(Long.MAX_VALUE) - Money.cop(-1),
            Money.cop(0) - Money.cop(Long.MIN_VALUE)
        )
        overflows.forEach { assertEquals(MoneyError.Overflow, it.errorOrFail()) }
    }

    @Test
    fun differentCurrenciesNeverCombine() {
        val mismatch = MoneyError.CurrencyMismatch(Currency.COP, Currency.USD)
        assertEquals(mismatch, (Money.cop(1) + Money(1, Currency.USD)).errorOrFail())
        assertEquals(mismatch, (Money.cop(1) - Money(1, Currency.USD)).errorOrFail())
        assertEquals(
            mismatch,
            (ExactTotal.zero(Currency.COP) + Money(1, Currency.USD)).toMoney().errorOrFail()
        )
    }

    // ---- Widened totals ----

    @Test
    fun totalsMayExceedTheTransactionMaximum() { // M06
        val expense = TransactionAmount.of(Money.cop(600_000_000_000)).valueOrFail().money
        val total = ExactTotal.of(Currency.COP, listOf(expense, expense)).toMoney().valueOrFail()
        assertEquals(Money.cop(1_200_000_000_000), total)
    }

    @Test
    fun anIntermediateBeyondLongDoesNotFailWhenTheFinalFigureFits() {
        val max = Money.cop(Long.MAX_VALUE)
        val total = ExactTotal.zero(Currency.COP) + max + Money.cop(1) - max
        assertEquals(Money.cop(1), total.toMoney().valueOrFail())

        val min = Money.cop(Long.MIN_VALUE)
        val low = ExactTotal.zero(Currency.COP) + min + min - min - min - Money.cop(5)
        assertEquals(Money.cop(-5), low.toMoney().valueOrFail())
    }

    @Test
    fun aFinalFigureBeyondLongIsAnExplicitFailure() {
        val max = Money.cop(Long.MAX_VALUE)
        assertEquals(
            MoneyError.Overflow,
            ExactTotal.of(Currency.COP, listOf(max, Money.cop(1))).toMoney().errorOrFail()
        )
        assertEquals(
            MoneyError.Overflow,
            (ExactTotal.zero(Currency.COP) - max - Money.cop(2)).toMoney().errorOrFail()
        )
        assertEquals(
            Money.cop(Long.MIN_VALUE),
            (ExactTotal.zero(Currency.COP) - max - Money.cop(1)).toMoney().valueOrFail()
        )
    }

    @Test
    fun everyOrderOfTheSameTermsGivesTheSameResult() {
        // Terms whose running total crosses the Long range in some orders but not others.
        val terms = listOf(
            Long.MAX_VALUE, Long.MAX_VALUE, Long.MIN_VALUE, Long.MIN_VALUE + 1, 600_000_000_000,
            600_000_000_000, -1_200_000_000_000, 937_200, -1, 5_000_000_000_000_000_000
        ).map(Money::cop)
        // Worked by hand: the first four cancel to -1, the 600-billion trio to 0, then
        // 937.200 - 1 = 937.199, plus 5.000.000.000.000.000.000.
        val expected = Money.cop(5_000_000_000_000_937_198)
        val random = Random(20261009)
        repeat(500) {
            val shuffled = terms.shuffled(random)
            val result = ExactTotal.of(Currency.COP, shuffled).toMoney()
            assertEquals(shuffled.toString(), expected, result.valueOrFail())
        }

        val overflowing = terms + Money.cop(Long.MAX_VALUE)
        repeat(200) {
            val result = ExactTotal.of(Currency.COP, overflowing.shuffled(random)).toMoney()
            assertEquals(MoneyError.Overflow, result.errorOrFail())
        }
    }

    @Test
    fun anEmptyTotalIsZeroInItsCurrency() {
        assertEquals(Money.cop(0), ExactTotal.of(Currency.COP, emptyList()).toMoney().valueOrFail())
        assertTrue(ExactTotal.zero(Currency.USD).currency == Currency.USD)
    }
}
