package com.software.sello.domain.policy

import com.software.sello.domain.model.Outcome
import java.time.DateTimeException
import java.time.LocalDate
import java.time.YearMonth

sealed interface EffectiveDateError {
    /** The text is not a real calendar date or month. [original] is what was received. */
    data class Malformed(val original: String) : EffectiveDateError

    /** A real date or month, but after the financial current date. */
    data class InFuture(val today: LocalDate) : EffectiveDateError
}

/**
 * Effective periods are calendar values in the financial zone. "Today" is always
 * passed in from the financial clock; nothing here reads the wall clock.
 */
object EffectiveDates {
    private val isoDate = Regex("([0-9]{4})-([0-9]{2})-([0-9]{2})")
    private val isoMonth = Regex("([0-9]{4})-([0-9]{2})")

    /** Strict `yyyy-MM-dd` as stored and exchanged. `2026-02-30` is malformed. */
    fun parseDate(text: String): Outcome<LocalDate, EffectiveDateError> {
        val parts = isoDate.matchEntire(text)?.groupValues?.drop(1)?.map(String::toInt)
            ?: return Outcome.Failure(EffectiveDateError.Malformed(text))
        return try {
            Outcome.Success(LocalDate.of(parts[0], parts[1], parts[2]))
        } catch (_: DateTimeException) {
            Outcome.Failure(EffectiveDateError.Malformed(text))
        }
    }

    /** Strict `yyyy-MM`. */
    fun parseMonth(text: String): Outcome<YearMonth, EffectiveDateError> {
        val parts = isoMonth.matchEntire(text)?.groupValues?.drop(1)?.map(String::toInt)
            ?: return Outcome.Failure(EffectiveDateError.Malformed(text))
        return try {
            Outcome.Success(YearMonth.of(parts[0], parts[1]))
        } catch (_: DateTimeException) {
            Outcome.Failure(EffectiveDateError.Malformed(text))
        }
    }

    /** A manually entered record cannot be dated after [today]. */
    fun forManualEntry(date: LocalDate, today: LocalDate): Outcome<LocalDate, EffectiveDateError> =
        if (date.isAfter(today)) {
            Outcome.Failure(EffectiveDateError.InFuture(today))
        } else {
            Outcome.Success(date)
        }

    /** A month can be selected up to the month of [today], never beyond it. */
    fun forSelection(month: YearMonth, today: LocalDate): Outcome<YearMonth, EffectiveDateError> =
        if (month.isAfter(YearMonth.from(today))) {
            Outcome.Failure(EffectiveDateError.InFuture(today))
        } else {
            Outcome.Success(month)
        }
}
