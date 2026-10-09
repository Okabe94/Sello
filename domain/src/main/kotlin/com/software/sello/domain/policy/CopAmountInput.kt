package com.software.sello.domain.policy

import com.software.sello.domain.model.Money
import com.software.sello.domain.model.Outcome

/** Why typed or pasted text is not an amount. [original] is exactly what was received. */
sealed interface AmountInputError {
    val original: String

    data class Empty(override val original: String) : AmountInputError

    /** Signs, decimals, exponents, currency symbols, letters, non-ASCII digits. */
    data class NotDigits(override val original: String) : AmountInputError

    /** Dots that are not es-CO thousands separators, such as `12.34` or `1.2345`. */
    data class MalformedGrouping(override val original: String) : AmountInputError

    data class LeadingZero(override val original: String) : AmountInputError

    data class OutOfRange(override val original: String) : AmountInputError
}

/**
 * Reads a COP amount as the user wrote it: plain ASCII digits (`1234`) or correctly
 * grouped es-CO dots (`1.234`). Surrounding whitespace is the only thing ignored;
 * nothing is stripped or repaired. Zero parses, because a zero budget is a real value;
 * whether zero is allowed is the caller's rule.
 */
object CopAmountInput {
    private val grouped = Regex("[0-9]{1,3}(\\.[0-9]{3})+")

    fun parse(input: String): Outcome<Money, AmountInputError> {
        val text = input.trim()
        val error = when {
            text.isEmpty() -> AmountInputError.Empty(input)
            text.any { it != '.' && it !in '0'..'9' } -> AmountInputError.NotDigits(input)
            '.' in text && !grouped.matches(text) -> AmountInputError.MalformedGrouping(input)
            text.length > 1 && text.startsWith('0') -> AmountInputError.LeadingZero(input)
            else -> null
        }
        if (error != null) return Outcome.Failure(error)
        val pesos = text.replace(".", "").toLongOrNull()
            ?: return Outcome.Failure(AmountInputError.OutOfRange(input))
        return Outcome.Success(Money.cop(pesos))
    }
}
