package com.software.sello.devtools

import com.software.sello.domain.port.FinancialDay
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class InMemorySessionStore(var session: SandboxSession = SandboxSession(active = true)) :
    SandboxSessionStore {
    override fun read() = session

    override fun write(session: SandboxSession) {
        this.session = session
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class SandboxTimeTest {
    private val bogota = ZoneId.of("America/Bogota")
    private val store = InMemorySessionStore()

    private fun TestScope.time(date: LocalDate = LocalDate.of(2026, 10, 31)) = SandboxTime(
        ControlledFinancialClock(FinancialDay(date, bogota)),
        store,
        backgroundScope
    )

    @Test
    fun everyStepIsPublishedOnTheClockAndKeptForTheNextProcess() = runTest {
        val time = time()

        time.advanceDays(1)
        assertEquals(LocalDate.of(2026, 11, 1), time.clock.today.value.date)
        time.advanceMonths(1)
        assertEquals(LocalDate.of(2026, 12, 1), time.state.value.day.date)

        assertEquals(LocalDate.of(2026, 12, 1), store.session.date)
        // A new process reads the date back and goes on from it.
        val reopened = SandboxTime(
            ControlledFinancialClock(FinancialDay(store.session.date!!, bogota)),
            store,
            backgroundScope
        )
        assertEquals(LocalDate.of(2026, 12, 1), reopened.state.value.day.date)
        assertEquals(bogota, reopened.state.value.day.zone)
    }

    @Test
    fun beforeAnyChangeIsSavedTheDateCanGoAnywhere() = runTest {
        val time = time()

        assertEquals(DateChange.Moved, time.jumpTo(LocalDate.of(2025, 1, 15)))
        assertEquals(LocalDate.of(2025, 1, 15), time.clock.today.value.date)
    }

    @Test
    fun onceAChangeIsSavedAnEarlierDateIsRefusedAndLaterOnesAreNot() = runTest {
        val time = time()
        time.markChanged()

        assertEquals(DateChange.RefusedBeforeSavedHistory, time.jumpTo(LocalDate.of(2026, 10, 30)))
        assertEquals(LocalDate.of(2026, 10, 31), time.clock.today.value.date)
        assertEquals(DateChange.Moved, time.jumpTo(LocalDate.of(2027, 2, 1)))
        assertTrue(store.session.changed)

        // The refusal holds in the next process too.
        val reopened = time(store.session.date!!)
        assertEquals(
            DateChange.RefusedBeforeSavedHistory,
            reopened.jumpTo(LocalDate.of(2026, 12, 1))
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun stepsOnlyGoForward() = runTest {
        time().advanceDays(-1)
    }

    @Test
    fun playingMovesOneDayEveryTwoRealSecondsAndStopsByItselfAfterAMonthOfDays() = runTest {
        val time = time(LocalDate.of(2026, 10, 1))

        time.play()
        runCurrent()
        assertTrue(time.state.value.playing)
        advanceTimeBy(1_999)
        assertEquals(LocalDate.of(2026, 10, 1), time.clock.today.value.date)
        advanceTimeBy(2)
        assertEquals(LocalDate.of(2026, 10, 2), time.clock.today.value.date)
        assertEquals(30, time.state.value.stepsLeft)

        // Far longer than 31 steps: it stops by itself at the 31st day.
        advanceTimeBy(120_000)
        assertEquals(LocalDate.of(2026, 11, 1), time.clock.today.value.date)
        assertFalse(time.state.value.playing)
        assertEquals(LocalDate.of(2026, 11, 1), store.session.date)
    }

    @Test
    fun playingUpToADateStopsThereAndPauseStopsAtOnce() = runTest {
        val time = time(LocalDate.of(2026, 10, 30))

        time.play(until = LocalDate.of(2026, 11, 2))
        advanceTimeBy(120_000)
        assertEquals(LocalDate.of(2026, 11, 2), time.clock.today.value.date)
        assertFalse(time.state.value.playing)

        time.play()
        advanceTimeBy(4_001)
        time.pause()
        advanceTimeBy(120_000)
        assertEquals(LocalDate.of(2026, 11, 4), time.clock.today.value.date)
        assertFalse(time.state.value.playing)
    }
}
