package com.software.sello.designsystem.component

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Draws typed digits in groups of three, `500000` as `500.000`, so an amount's size
 * can be read while it is typed. Only the drawing changes: the field's value stays
 * exactly what was typed, and the caller validates that.
 *
 * Text is grouped only when it is plain ASCII digits with no leading zero. Anything
 * else (`1e3`, `1.23`, `0500`, a pasted `-5`) is drawn as typed, so text that is not
 * an amount is never made to look like one.
 */
data class DigitGrouping(private val separator: Char = '.') : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val typed = text.text
        if (!groupable(typed)) return TransformedText(text, OffsetMapping.Identity)
        val digits = typed.length
        return TransformedText(
            AnnotatedString(shown(typed)),
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int) = shownOffset(digits, offset)

                override fun transformedToOriginal(offset: Int) = typedOffset(digits, offset)
            }
        )
    }

    /** [typed] as it is drawn. */
    fun shown(typed: String): String {
        if (!groupable(typed)) return typed
        val first = firstGroup(typed.length)
        return buildString(typed.length + typed.length / GROUP) {
            typed.forEachIndexed { index, digit ->
                if (index >= first && (index - first) % GROUP == 0) append(separator)
                append(digit)
            }
        }
    }

    private fun groupable(typed: String) = typed.length > GROUP &&
        typed.all { it in '0'..'9' } &&
        typed[0] != '0'

    companion object {
        private const val GROUP = 3

        /** How many digits come before the first separator. */
        private fun firstGroup(digits: Int) = (digits - 1) % GROUP + 1

        /**
         * Where a cursor at [typedOffset] in [digits] typed digits is in the drawn
         * text. A cursor just after a digit stays just after it, before any separator.
         */
        fun shownOffset(digits: Int, typedOffset: Int): Int {
            val offset = typedOffset.coerceIn(0, digits)
            val first = firstGroup(digits)
            val separatorsBefore = if (offset <= first) 0 else (offset - first - 1) / GROUP + 1
            return offset + separatorsBefore
        }

        /**
         * Where a cursor at [shownOffset] in the drawn text is in [digits] typed
         * digits. Either side of a separator is the position after the digit before it.
         */
        fun typedOffset(digits: Int, shownOffset: Int): Int {
            var typed = digits
            while (typed > 0 && shownOffset(digits, typed) > shownOffset) typed--
            return typed
        }
    }
}
