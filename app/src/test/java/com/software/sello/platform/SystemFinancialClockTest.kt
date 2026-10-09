package com.software.sello.platform

import com.software.sello.domain.port.FinancialDay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SystemFinancialClockTest {
    private val bogota = ZoneId.of("America/Bogota")
    private val signals = RecordingTimeSignals()

    // 2026-10-31 23:59:00 in Bogota (UTC-5) is 2026-11-01 04:59:00 UTC.
    private val oneMinuteBeforeNovember = Instant.parse("2026-11-01T04:59:00Z")
    private val october31 = FinancialDay(LocalDate.of(2026, 10, 31), bogota)
    private val november1 = FinancialDay(LocalDate.of(2026, 11, 1), bogota)

    private fun TestScope.clockAt(start: Instant): Pair<SystemFinancialClock, VirtualAuditClock> {
        val audit = VirtualAuditClock(testScheduler, start)
        val zone = ProcessFinancialZone().apply { initialize(bogota) }
        val clock = SystemFinancialClock(zone, audit, signals, backgroundScope)
        runCurrent()
        return clock to audit
    }

    @Test
    fun startsOnTheDateOfTheFinancialZoneNotUtc() = runTest {
        val (clock, _) = clockAt(oneMinuteBeforeNovember)

        assertEquals(october31, clock.today.value)
    }

    @Test
    fun rollsOverAtMidnightWhileRunning() = runTest {
        val (clock, _) = clockAt(oneMinuteBeforeNovember)

        advanceTimeBy(59_999)
        runCurrent()
        assertEquals(october31, clock.today.value)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(november1, clock.today.value)
    }

    @Test
    fun keepsRollingOverOnFollowingDays() = runTest {
        val (clock, _) = clockAt(oneMinuteBeforeNovember)

        advanceTimeBy(60_000 + 24 * 60 * 60_000L)
        runCurrent()

        assertEquals(FinancialDay(LocalDate.of(2026, 11, 2), bogota), clock.today.value)
    }

    @Test
    fun monthRolloverWhileBackgroundedIsObservedOnResume() = runTest {
        val (clock, audit) = clockAt(oneMinuteBeforeNovember)

        // The process was frozen: real time moved to the next month but no timer ran.
        audit.setTo(Instant.parse("2026-11-01T15:00:00Z"))
        assertEquals(october31, clock.today.value)

        signals.signal()
        runCurrent()
        assertEquals(november1, clock.today.value)
    }

    @Test
    fun deviceClockMovedBackIsObservedAndMidnightIsRescheduled() = runTest {
        val (clock, audit) = clockAt(Instant.parse("2026-11-01T15:00:00Z"))
        assertEquals(november1, clock.today.value)

        audit.setTo(oneMinuteBeforeNovember)
        signals.signal()
        runCurrent()
        assertEquals(october31, clock.today.value)

        advanceTimeBy(60_000)
        runCurrent()
        assertEquals(november1, clock.today.value)
    }

    @Test
    fun travelDoesNotChangeTheFinancialZoneOrDate() = runTest {
        val (clock, _) = clockAt(oneMinuteBeforeNovember)

        // Device zone changes to Madrid, where it is already 1 November. The signal
        // arrives, but the financial zone was initialized once and stays Bogota.
        signals.signal()
        runCurrent()

        assertEquals(october31, clock.today.value)
    }

    @Test
    fun unchangedDayIsNotRepublished() = runTest {
        val (clock, _) = clockAt(oneMinuteBeforeNovember)
        val first = clock.today.value

        signals.signal()
        runCurrent()

        assertEquals(first, clock.today.value)
    }
}
