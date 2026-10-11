package com.software.sello.designsystem.component

import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** How typed digits are drawn in groups, and where the cursor is on each side of that. */
class DigitGroupingTest {
    private val grouping = DigitGrouping()

    @Test
    fun plainDigitsAreDrawnInGroupsOfThreeFromTheRight() {
        val expected = mapOf(
            "" to "",
            "0" to "0",
            "7" to "7",
            "999" to "999",
            "1000" to "1.000",
            "48700" to "48.700",
            "400000" to "400.000",
            "500000" to "500.000",
            "1234567" to "1.234.567",
            "999999999999" to "999.999.999.999",
            // More than any amount can be: still only digits, drawn so its size is plain.
            "99999999999999999999" to "99.999.999.999.999.999.999"
        )
        for ((typed, shown) in expected) assertEquals(typed, shown, grouping.shown(typed))
    }

    @Test
    fun anythingThatIsNotPlainDigitsIsDrawnExactlyAsTyped() {
        val typed = listOf(
            "1e3", "1.23", "12.34", "400.000", "1.234.567", "12,5", "1,000", "-500000",
            "+500000", "500000 ", " 500000", "5 000", "500000a", "$500000",
            // Leading zeros are not an amount; grouping would dress them up as one.
            "0500000", "00", "0000",
            // Digits of other scripts are not accepted as an amount either.
            "５０００００", "٥٠٠٠٠٠"
        )
        for (text in typed) assertEquals(text, text, grouping.shown(text))
    }

    @Test
    fun theTransformationDrawsWhatShownSaysAndKeepsTheTypedTextLength() {
        val drawn = grouping.filter(AnnotatedString("1234567"))

        assertEquals("1.234.567", drawn.text.text)
        assertEquals(9, drawn.offsetMapping.originalToTransformed(7))
        assertEquals(7, drawn.offsetMapping.transformedToOriginal(9))
        // Text drawn as typed keeps every position where it was.
        val asTyped = grouping.filter(AnnotatedString("1.23"))
        assertEquals("1.23", asTyped.text.text)
        assertEquals(
            listOf(0, 1, 2, 3, 4),
            (0..4).map(asTyped.offsetMapping::originalToTransformed)
        )
    }

    @Test
    fun aCursorBetweenTwoDigitsStaysBetweenThoseTwoDigits() {
        // 1234567 is drawn 1.234.567: after the 1 and after the 4 a dot follows.
        assertEquals(
            listOf(0, 1, 3, 4, 5, 7, 8, 9),
            (0..7).map { DigitGrouping.shownOffset(7, it) }
        )
        // 400000 is drawn 400.000.
        assertEquals(
            listOf(0, 1, 2, 3, 5, 6, 7),
            (0..6).map { DigitGrouping.shownOffset(6, it) }
        )
    }

    @Test
    fun aCursorOnEitherSideOfADotIsAfterTheDigitBeforeIt() {
        // Positions in 1.234.567: 1 and 2 are the two sides of the first dot.
        assertEquals(
            listOf(0, 1, 1, 2, 3, 4, 4, 5, 6, 7),
            (0..9).map { DigitGrouping.typedOffset(7, it) }
        )
        assertEquals(
            listOf(0, 1, 2, 3, 3, 4, 5, 6),
            (0..7).map { DigitGrouping.typedOffset(6, it) }
        )
    }

    @Test
    fun everyCursorPositionSurvivesTheRoundTripForEveryLength() {
        for (digits in 0..24) {
            val shown = (0..digits).map { DigitGrouping.shownOffset(digits, it) }
            assertEquals(
                "$digits",
                (0..digits).toList(),
                shown.map {
                    DigitGrouping.typedOffset(digits, it)
                }
            )
            assertTrue("$digits", shown.zipWithNext().all { (a, b) -> b > a })
            val length = digits + if (digits == 0) 0 else (digits - 1) / 3
            assertEquals("$digits", length, shown.last())
            // Asked beyond either end, the answer stays inside the text.
            assertEquals(digits, DigitGrouping.typedOffset(digits, length + 5))
            assertEquals(length, DigitGrouping.shownOffset(digits, digits + 5))
        }
    }
}
