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
    return MoneyTextValue(sign, "$", digits, words)
}

val usdFixture = MoneyTextValue(MoneySign.None, "USD", "2.340,00", "2.340,00 USD")
