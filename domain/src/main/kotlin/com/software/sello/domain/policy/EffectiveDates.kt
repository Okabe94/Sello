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

    /**
     * A real date or month that the stored text cannot hold: its year is not four plain
     * digits (before year 0000 or after 9999), so it would be written but never read back.
     */
    data object OutOfRange : EffectiveDateError
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

    /** The text a date is stored and exchanged as; [parseDate] reads it back. */
    fun storedText(date: LocalDate): String = date.toString()

    /** The text a month is stored and exchanged as; [parseMonth] reads it back. */
    fun storedText(month: YearMonth): String = month.toString()

    /**
     * Whether the stored text of [date] reads back as [date]. It is asked of the very
     * functions that write and read, so the two cannot come to disagree.
     */
    fun isStorable(date: LocalDate): Boolean = parseDate(storedText(date)) == Outcome.Success(date)

    fun isStorable(month: YearMonth): Boolean =
        parseMonth(storedText(month)) == Outcome.Success(month)

    /** A manually entered record cannot be dated after [today], and must be storable. */
    fun forManualEntry(date: LocalDate, today: LocalDate): Outcome<LocalDate, EffectiveDateError> =
        when {
            date.isAfter(today) -> Outcome.Failure(EffectiveDateError.InFuture(today))
            !isStorable(date) -> Outcome.Failure(EffectiveDateError.OutOfRange)
            else -> Outcome.Success(date)
        }

    /** A storable month can be selected up to the month of [today], never beyond it. */
    fun forSelection(month: YearMonth, today: LocalDate): Outcome<YearMonth, EffectiveDateError> =
        when {
            month.isAfter(YearMonth.from(today)) ->
                Outcome.Failure(EffectiveDateError.InFuture(today))

            !isStorable(month) -> Outcome.Failure(EffectiveDateError.OutOfRange)

            else -> Outcome.Success(month)
        }
}
