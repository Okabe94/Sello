package com.software.sello.catalog.components

import com.software.sello.designsystem.component.MoneySign
import com.software.sello.designsystem.component.MoneyTextValue

/**
 * Synthetic amounts for examples. The application owns real formatting; the catalog
 * cannot depend on it, so this fixture groups digits just well enough to feed the
 * components. It is not a formatter to copy.
 */
fun copFixture(pesos: Long, signed: Boolean = false): MoneyTextValue {
    val digits = pesos.toString().removePrefix("-")
        .reversed().chunked(3).joinToString(".").reversed()
    val sign = when {
        pesos < 0 -> MoneySign.Minus
        signed && pesos > 0 -> MoneySign.Plus
        else -> MoneySign.None
    }
    val words = when (sign) {
        MoneySign.Minus -> "menos $digits pesos"
        MoneySign.Plus -> "más $digits pesos"
        MoneySign.None -> "$digits pesos"
    }
    return MoneyTextValue(sign, "", digits, words)
}

val usdFixture = MoneyTextValue(MoneySign.None, "USD", "2.340,00", "2.340,00 USD")

/**
 * A catalog-only amount draft so the keypad example can be used. The real draft and
 * its rules live in the application's domain; this stand-in only mimics their shape.
 */
data class DraftFixture(
    val digits: String = "",
    val rejected: String? = null,
    val limitPulse: Int = 0
) {
    val amount: MoneyTextValue? get() = digits.toLongOrNull()?.let { copFixture(it) }

    fun type(keys: String): DraftFixture {
        val current = if (rejected != null) "" else digits
        return when {
            current.isEmpty() && keys.all { it == '0' } -> DraftFixture(limitPulse = limitPulse)
            current.length + keys.length > MAX_DIGITS -> copy(limitPulse = limitPulse + 1)
            else -> DraftFixture(current + keys, limitPulse = limitPulse)
        }
    }

    fun backspace(): DraftFixture =
        if (rejected != null) DraftFixture() else copy(digits = digits.dropLast(1))

    fun paste(text: String): DraftFixture {
        val trimmed = text.trim()
        val plain = Regex("[1-9][0-9]{0,11}").matches(trimmed)
        val grouped = Regex("[1-9][0-9]{0,2}(\\.[0-9]{3}){1,3}").matches(trimmed)
        return if (plain || grouped) {
            DraftFixture(trimmed.replace(".", ""))
        } else {
            DraftFixture(rejected = text)
        }
    }

    companion object {
        const val MAX_DIGITS = 12
    }
}
