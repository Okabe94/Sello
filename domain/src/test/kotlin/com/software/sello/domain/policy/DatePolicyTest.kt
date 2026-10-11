package com.software.sello.domain.policy

import com.software.sello.domain.errorOrFail
import com.software.sello.domain.valueOrFail
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Test

class DatePolicyTest {
    private val today = LocalDate.of(2026, 10, 9)

    @Test
    fun realCalendarDatesParse() {
        val valid = mapOf(
            "2026-10-09" to LocalDate.of(2026, 10, 9),
            "2024-02-29" to LocalDate.of(2024, 2, 29), // leap year
            "2000-02-29" to LocalDate.of(2000, 2, 29), // divisible by 400
            "2026-12-31" to LocalDate.of(2026, 12, 31),
            "2026-01-01" to LocalDate.of(2026, 1, 1),
            "0001-01-01" to LocalDate.of(1, 1, 1)
        )
        for ((text, date) in valid) {
            assertEquals(text, date, EffectiveDates.parseDate(text).valueOrFail())
        }
    }

    @Test
    fun impossibleOrLooselyWrittenDatesAreMalformedWithTheOriginalText() {
        val malformed = listOf(
            "2026-02-29", // not a leap year
            "1900-02-29", // divisible by 100, not 400
            "2026-02-30", "2026-04-31", "2026-13-01", "2026-00-10", "2026-10-00", "2026-10-32",
            "2026-1-9", "26-10-09", "2026/10/09", "09-10-2026", "20261009", "2026-10-09T00:00",
            "2026-10-09Z", " 2026-10-09", "2026-10-09 ", "+2026-10-09", "-2026-10-09",
            "12026-10-09", "2026-10", "", "hoy", "٢٠٢٦-١٠-٠٩"
        )
        for (text in malformed) {
            assertEquals(
                text,
                EffectiveDateError.Malformed(text),
                EffectiveDates.parseDate(text).errorOrFail()
            )
        }
    }

    @Test
    fun monthsParseStrictly() {
        assertEquals(YearMonth.of(2026, 10), EffectiveDates.parseMonth("2026-10").valueOrFail())
        assertEquals(YearMonth.of(2024, 2), EffectiveDates.parseMonth("2024-02").valueOrFail())
        for (text in listOf(
            "2026-13",
            "2026-00",
            "2026-1",
            "2026-10-09",
            "26-10",
            "",
            "+2026-10"
        )) {
            assertEquals(
                text,
                EffectiveDateError.Malformed(text),
                EffectiveDates.parseMonth(text).errorOrFail()
            )
        }
    }

    @Test
    fun manualEntriesMayBeDatedTodayOrEarlierButNeverLater() {
        fun check(date: LocalDate) = EffectiveDates.forManualEntry(date, today)
        assertEquals(today, check(today).valueOrFail())
        assertEquals(today.minusDays(1), check(today.minusDays(1)).valueOrFail())
        assertEquals(LocalDate.of(2024, 2, 29), check(LocalDate.of(2024, 2, 29)).valueOrFail())
        assertEquals(EffectiveDateError.InFuture(today), check(today.plusDays(1)).errorOrFail())
        assertEquals(EffectiveDateError.InFuture(today), check(today.plusYears(1)).errorOrFail())
    }

    @Test
    fun theFutureIsJudgedAgainstTheInjectedDayNotTheWallClock() {
        val longAgo = LocalDate.of(2020, 2, 29)
        assertEquals(
            EffectiveDateError.InFuture(longAgo),
            EffectiveDates.forManualEntry(LocalDate.of(2020, 3, 1), longAgo).errorOrFail()
        )
        val farAhead = LocalDate.of(2040, 1, 1)
        assertEquals(
            LocalDate.of(2039, 12, 31),
            EffectiveDates.forManualEntry(LocalDate.of(2039, 12, 31), farAhead).valueOrFail()
        )
    }

    @Test
    fun aMonthCanBeSelectedUpToTheCurrentOneOnly() {
        fun check(month: YearMonth) = EffectiveDates.forSelection(month, today)
        assertEquals(YearMonth.of(2026, 10), check(YearMonth.of(2026, 10)).valueOrFail())
        assertEquals(YearMonth.of(2026, 9), check(YearMonth.of(2026, 9)).valueOrFail())
        assertEquals(YearMonth.of(2025, 12), check(YearMonth.of(2025, 12)).valueOrFail())
        assertEquals(
            EffectiveDateError.InFuture(today),
            check(YearMonth.of(2026, 11)).errorOrFail()
        )
        assertEquals(
            EffectiveDateError.InFuture(today),
            check(YearMonth.of(2027, 1)).errorOrFail()
        )
        // The last day of a month still selects that month, and the first does not unlock the next.
        val lastDay = LocalDate.of(2026, 10, 31)
        assertEquals(
            EffectiveDateError.InFuture(lastDay),
            EffectiveDates.forSelection(YearMonth.of(2026, 11), lastDay).errorOrFail()
        )
    }

    @Test
    fun aDateIsStoredAsFourDigitYearMonthAndDay() {
        assertEquals("2026-10-03", EffectiveDates.storedText(LocalDate.of(2026, 10, 3)))
        assertEquals("0000-01-01", EffectiveDates.storedText(LocalDate.of(0, 1, 1)))
        assertEquals("0987-06-05", EffectiveDates.storedText(LocalDate.of(987, 6, 5)))
        assertEquals("9999-12-31", EffectiveDates.storedText(LocalDate.of(9999, 12, 31)))
        assertEquals("2026-10", EffectiveDates.storedText(YearMonth.of(2026, 10)))
        assertEquals("0000-01", EffectiveDates.storedText(YearMonth.of(0, 1)))
        assertEquals("9999-12", EffectiveDates.storedText(YearMonth.of(9999, 12)))
    }

    @Test
    fun aDateTheStoredTextCannotHoldIsNotAcceptedForAnEntry() {
        // Stored, year -1 would be "-0001-01-01", which is never read back.
        for (date in listOf(
            LocalDate.of(-1, 1, 1),
            LocalDate.of(-1, 12, 31),
            LocalDate.of(-2026, 10, 9),
            LocalDate.MIN
        )) {
            assertEquals(
                "$date",
                EffectiveDateError.OutOfRange,
                EffectiveDates.forManualEntry(date, today).errorOrFail()
            )
        }
    }

    @Test
    fun everyDateAcceptedForAnEntryReadsBackAsItself() {
        val accepted = listOf(
            LocalDate.of(0, 1, 1), // the first day four digits can write
            LocalDate.of(1, 1, 1),
            LocalDate.of(999, 12, 31),
            LocalDate.of(1900, 2, 28),
            LocalDate.of(2024, 2, 29),
            today
        )
        for (date in accepted) {
            val entered = EffectiveDates.forManualEntry(date, today).valueOrFail()
            assertEquals(
                "$date",
                date,
                EffectiveDates.parseDate(EffectiveDates.storedText(entered)).valueOrFail()
            )
        }
        // The last day four digits can write, on a day when it is not the future.
        val last = LocalDate.of(9999, 12, 31)
        assertEquals(last, EffectiveDates.forManualEntry(last, last).valueOrFail())
        assertEquals(last, EffectiveDates.parseDate(EffectiveDates.storedText(last)).valueOrFail())
    }

    @Test
    fun aDateAfterTodayIsTheFutureEvenWhenItAlsoCouldNotBeStored() {
        assertEquals(
            EffectiveDateError.InFuture(today),
            EffectiveDates.forManualEntry(LocalDate.of(10000, 1, 1), today).errorOrFail()
        )
        // With the day itself past year 9999, "today" is not the future and cannot be stored.
        val beyond = LocalDate.of(10000, 1, 1)
        assertEquals(
            EffectiveDateError.OutOfRange,
            EffectiveDates.forManualEntry(beyond, beyond).errorOrFail()
        )
    }

    @Test
    fun aMonthTheStoredTextCannotHoldCannotBeSelectedOrGivenALimit() {
        for (month in listOf(YearMonth.of(-1, 1), YearMonth.of(-1, 12), YearMonth.of(-2026, 10))) {
            assertEquals(
                "$month",
                EffectiveDateError.OutOfRange,
                EffectiveDates.forSelection(month, today).errorOrFail()
            )
        }
        for (month in listOf(YearMonth.of(0, 1), YearMonth.of(1, 1), YearMonth.of(2026, 10))) {
            val selected = EffectiveDates.forSelection(month, today).valueOrFail()
            assertEquals(
                "$month",
                month,
                EffectiveDates.parseMonth(EffectiveDates.storedText(selected)).valueOrFail()
            )
        }
        val beyond = LocalDate.of(10000, 1, 15)
        assertEquals(
            EffectiveDateError.OutOfRange,
            EffectiveDates.forSelection(YearMonth.of(10000, 1), beyond).errorOrFail()
        )
    }
}
