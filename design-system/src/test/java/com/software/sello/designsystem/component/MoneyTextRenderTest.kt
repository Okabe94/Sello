package com.software.sello.designsystem.component

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import kotlin.math.abs
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MoneyTextRenderTest {
    private fun value(sign: MoneySign, digits: String, symbol: String = "$") =
        MoneyTextValue(sign, symbol, digits, "spoken")

    private val style = TextStyle(fontSize = 60.sp, letterSpacing = (-0.04).em)

    private fun drawn(value: MoneyTextValue, style: MoneyStyle) =
        moneyAnnotatedString(value, style, this.style).text.replace("\u200B", "")

    @Test
    fun theFullFigureIsDrawnWithItsSymbolAndATrueMinus() {
        assertEquals("$937.200", drawn(value(MoneySign.None, "937.200"), MoneyStyle.Hero))
        assertEquals("+$850.000", drawn(value(MoneySign.Plus, "850.000"), MoneyStyle.Total))
        assertEquals("\u2212$10.000", drawn(value(MoneySign.Minus, "10.000"), MoneyStyle.Card))
        assertEquals(
            "\u2212$9.223.372.036.854.775.808",
            drawn(value(MoneySign.Minus, "9.223.372.036.854.775.808"), MoneyStyle.Title)
        )
        assertEquals(
            "USD2.340,00",
            drawn(value(MoneySign.None, "2.340,00", "USD"), MoneyStyle.Title)
        )
    }

    @Test
    fun linesInsideASlipDropTheSymbolButKeepTheSign() {
        assertEquals("18.400", drawn(value(MoneySign.None, "18.400"), MoneyStyle.Line))
        assertEquals("\u221221.500", drawn(value(MoneySign.Minus, "21.500"), MoneyStyle.Line))
        assertEquals("+850.000", drawn(value(MoneySign.Plus, "850.000"), MoneyStyle.Line))
    }

    @Test
    fun plainTextMatchesWhatIsDrawn() {
        for (moneyStyle in MoneyStyle.entries) {
            val v = value(MoneySign.Minus, "1.234.567")
            assertEquals(drawn(v, moneyStyle), moneyPlainText(v, moneyStyle))
        }
    }

    @Test
    fun aLongFigureCanBreakOnlyAfterAThousandsPoint() {
        val text = moneyAnnotatedString(
            value(MoneySign.None, "9.223.372.036.854.775.807"),
            MoneyStyle.Hero,
            style
        ).text
        val pieces = text.split("\u200B")
        assertEquals(listOf("$9.", "223.", "372.", "036.", "854.", "775.", "807"), pieces)
    }

    @Test
    fun thousandsPointsAreTightenedAndTheSymbolIsSmallAndRaised() {
        val text = moneyAnnotatedString(value(MoneySign.None, "1.234"), MoneyStyle.Hero, style)
        val symbol = text.spanStyles.last { it.item.fontSize == 0.56.em }
        assertEquals("$", text.text.substring(symbol.start, symbol.end))
        assertTrue(symbol.item.baselineShift!!.multiplier > 0f)
        // The style's own -0.04em tracking plus 0.13em of tightening.
        val tight = text.spanStyles.filter { abs(it.item.letterSpacing.value + 0.17f) < 1e-4f }
        assertEquals(listOf("1", "."), tight.map { text.text.substring(it.start, it.end) })
    }

    @Test
    fun aCurrencyCodeKeepsItsLettersTogetherAndTheDecimalCommaIsTightened() {
        val text = moneyAnnotatedString(
            value(MoneySign.None, "2.340,00", "USD"),
            MoneyStyle.Title,
            style
        )
        val wide = text.spanStyles.single { it.item.letterSpacing == 0.14.em }
        assertEquals("D", text.text.substring(wide.start, wide.end))
        val tight = text.spanStyles.filter { abs(it.item.letterSpacing.value + 0.17f) < 1e-4f }
        assertEquals(
            listOf("2", ".", "0", ","),
            tight.map {
                text.text.substring(it.start, it.end)
            }
        )
    }

    // ---- Counting ----

    @Test
    fun aCountStartsAndEndsOnTheExactAmounts() {
        val pairs = listOf(
            0L to 937_200L,
            937_200L to 0L,
            -5L to 5L,
            0L to Long.MAX_VALUE,
            Long.MAX_VALUE - 1 to Long.MAX_VALUE,
            Long.MIN_VALUE to Long.MAX_VALUE,
            999_999_999_999L to 1_000_000_000_000L
        )
        for ((from, to) in pairs) {
            assertEquals(from, countFrame(from, to, 0f))
            assertEquals(to, countFrame(from, to, 1f))
            assertEquals(to, countFrame(from, to, 1.2f)) // a spring may overshoot
            assertEquals(from, countFrame(from, to, -0.1f))
        }
    }

    @Test
    fun everyFrameStaysBetweenTheTwoAmounts() {
        val pairs = listOf(0L to 937_200L, 937_200L to 100L, Long.MIN_VALUE to Long.MAX_VALUE)
        for ((from, to) in pairs) {
            var previous = from
            for (step in 1..99) {
                val frame = countFrame(from, to, step / 100f)
                assertTrue("$frame", frame in minOf(from, to)..maxOf(from, to))
                assertTrue(if (to >= from) frame >= previous else frame <= previous)
                previous = frame
            }
        }
        assertEquals(468_600L, countFrame(0, 937_200, 0.5f))
    }

    // ---- Pinked edge ----

    @Test
    fun aWholeNumberOfTeethAlwaysFitsTheWidth() {
        assertEquals(36, toothCount(360f, 10f))
        assertEquals(36, toothCount(364f, 10f)) // stretched, not a partial tooth
        assertEquals(37, toothCount(366f, 10f))
        assertEquals(1, toothCount(4f, 10f))
        assertEquals(1, toothCount(0f, 10f))
    }
}
