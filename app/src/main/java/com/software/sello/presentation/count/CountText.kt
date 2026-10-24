package com.software.sello.presentation.count

/**
 * A count as es-CO writes it, whatever the phone's language: "999", "1.000", "10.000".
 * Only what is shown; a plural is still chosen from the number itself.
 */
fun countText(value: Long): String {
    // Text, not Math.abs: the lowest Long has no positive counterpart.
    val digits = value.toString().removePrefix("-")
    val grouped = digits.reversed().chunked(3).joinToString(".").reversed()
    return if (value < 0) "-$grouped" else grouped
}

fun countText(value: Int): String = countText(value.toLong())
