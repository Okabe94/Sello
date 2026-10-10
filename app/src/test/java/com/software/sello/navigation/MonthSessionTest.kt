package com.software.sello.navigation

import java.time.YearMonth
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MonthSessionTest {
    private val clock = HandFinancialClock("2026-10-09")
    private val monotonic = HandMonotonicClock()
    private val session = MonthSession(clock, monotonic)
    private val august = YearMonth.of(2026, 8)
    private val october = YearMonth.of(2026, 10)
    private val november = YearMonth.of(2026, 11)

    private fun away(time: kotlin.time.Duration) {
        session.enteredBackground()
        monotonic.now += time
        session.returnedToForeground()
    }

    @Test
    fun itStartsOnTheCurrentMonthAndFollowsItIntoTheNext() = runBlocking {
        assertEquals(october, session.selectedMonthNow)
        assertEquals(october, session.selectedMonth.first())

        clock.goTo("2026-11-01")

        assertEquals(november, session.selectedMonthNow)
        assertEquals(november, session.selectedMonth.first())
        assertEquals(november, session.currentMonth.first())
    }

    @Test
    fun aPickedPastMonthStaysWhenTheMonthChanges() {
        assertTrue(session.select(august))

        clock.goTo("2026-11-01")

        assertEquals(august, session.selectedMonthNow)
        assertEquals(november, session.currentMonthNow)
    }

    @Test
    fun aMonthAfterTheCurrentOneCannotBeSelected() {
        session.select(august)

        assertFalse(session.select(november))
        assertFalse(session.select(YearMonth.of(2027, 1)))

        assertEquals(august, session.selectedMonthNow)
    }

    @Test
    fun pickingTheCurrentMonthGoesBackToFollowingIt() {
        session.select(august)

        assertTrue(session.select(october))
        clock.goTo("2026-11-01")

        assertEquals(november, session.selectedMonthNow)
    }

    @Test
    fun exactlyThirtyMinutesAwayKeepsTheSelection() {
        session.select(august)

        away(30.minutes)

        assertEquals(august, session.selectedMonthNow)
    }

    @Test
    fun anythingOverThirtyMinutesAwayResetsToTheCurrentMonth() {
        session.select(august)

        away(30.minutes + 1.milliseconds)

        assertEquals(october, session.selectedMonthNow)
    }

    @Test
    fun severalShortAbsencesDoNotAddUp() {
        session.select(august)

        repeat(5) { away(20.minutes) }

        assertEquals(august, session.selectedMonthNow)
    }

    @Test
    fun movingFinancialTimeIsNotTimeInTheBackground() {
        session.select(august)

        // Months of financial time pass, in the foreground and during a short absence.
        clock.goTo("2027-03-15")
        session.enteredBackground()
        clock.goTo("2027-06-01")
        monotonic.now += 10.seconds
        session.returnedToForeground()

        assertEquals(august, session.selectedMonthNow)
        assertEquals(YearMonth.of(2027, 6), session.currentMonthNow)
    }

    @Test
    fun returningWithoutHavingLeftChangesNothing() {
        session.select(august)
        monotonic.now += 90.minutes

        session.returnedToForeground()

        assertEquals(august, session.selectedMonthNow)
    }

    @Test
    fun aDeviceRestartWhileAwayCountsAsALongAbsence() {
        session.select(august)
        session.enteredBackground()

        // Elapsed time starts again from zero after a restart.
        monotonic.now = 2.minutes
        session.returnedToForeground()

        assertEquals(october, session.selectedMonthNow)
    }

    @Test
    fun aPickThatIsNoLongerInThePastShowsTheCurrentMonth() {
        session.select(august)

        clock.goTo("2026-08-20")

        assertEquals(august, session.selectedMonthNow)
        assertEquals(august, session.currentMonthNow)
        clock.goTo("2026-07-31")
        assertEquals(YearMonth.of(2026, 7), session.selectedMonthNow)
    }

    @Test
    fun theSelectionAndTheTimeItLeftSurviveARestoredProcess() {
        session.select(august)
        session.enteredBackground()
        val saved = session.save()

        // A new process: new objects, the same saved values, 31 minutes later.
        monotonic.now += 31.minutes
        val restored = MonthSession(clock, monotonic)
        restored.restore(saved)
        val beforeReturning = restored.selectedMonthNow
        restored.returnedToForeground()

        assertEquals(SavedMonthSession("2026-08", saved.backgroundedAtMillis), saved)
        assertEquals(august, beforeReturning)
        assertEquals(october, restored.selectedMonthNow)
    }

    @Test
    fun aRestoredProcessWithinThirtyMinutesKeepsTheSelection() {
        session.select(august)
        session.enteredBackground()
        val saved = session.save()

        monotonic.now += 29.minutes
        val restored = MonthSession(clock, monotonic)
        restored.restore(saved)
        restored.returnedToForeground()

        assertEquals(august, restored.selectedMonthNow)
    }

    @Test
    fun aSavedMonthThatIsNotAMonthIsIgnored() {
        for (damaged in listOf("2026-13", "agosto", "", "2099-01")) {
            val restored = MonthSession(clock, monotonic)

            restored.restore(SavedMonthSession(damaged, null))

            assertEquals(damaged, october, restored.selectedMonthNow)
        }
    }
}
