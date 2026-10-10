package com.software.sello.data.mapper

import com.software.sello.domain.model.Currency
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure

/** Thrown and caught inside [decodeRow] only; it never leaves this file's callers. */
internal class RejectedRow(val failure: StorageFailure.Integrity) : RuntimeException() {
    override fun fillInStackTrace(): Throwable = this
}

/**
 * Checks one stored row field by field. The first field that breaks a rule ends the
 * decode with a failure naming the table, row and column. Nothing is defaulted,
 * trimmed, clamped or skipped.
 */
internal class RowDecoder(private val record: String, private val key: String?) {
    fun reject(field: String): Nothing =
        throw RejectedRow(StorageFailure.Integrity(record, key, field))

    fun <T> valid(field: String, outcome: Outcome<T, *>): T = when (outcome) {
        is Outcome.Success -> outcome.value
        is Outcome.Failure -> reject(field)
    }

    fun check(field: String, holds: Boolean) {
        if (!holds) reject(field)
    }

    /** Exactly a known currency code; `cop` or an unfamiliar code is a damaged row. */
    fun currency(field: String, code: String): Currency =
        Currency.entries.firstOrNull { it.code == code } ?: reject(field)
}

internal inline fun <T> decodeRow(
    record: String,
    key: String?,
    decode: RowDecoder.() -> T
): Outcome<T, StorageFailure.Integrity> = try {
    Outcome.Success(RowDecoder(record, key).decode())
} catch (rejected: RejectedRow) {
    Outcome.Failure(rejected.failure)
}

/** All rows or none: one damaged row fails the whole read instead of shortening it. */
internal inline fun <E, T> List<E>.decodeAll(
    decode: (E) -> Outcome<T, StorageFailure.Integrity>
): Outcome<List<T>, StorageFailure.Integrity> {
    val decoded = ArrayList<T>(size)
    for (row in this) {
        when (val outcome = decode(row)) {
            is Outcome.Success -> decoded += outcome.value
            is Outcome.Failure -> return outcome
        }
    }
    return Outcome.Success(decoded)
}
