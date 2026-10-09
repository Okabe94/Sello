package com.software.sello.devtools

import com.software.sello.domain.port.FinancialDay
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ControlledFinancialClockTest {
    private val bogota = ZoneId.of("America/Bogota")
    private val clock = ControlledFinancialClock(FinancialDay(LocalDate.of(2028, 2, 28), bogota))

    @Test
    fun everyChangeIsPublishedToObservers() = runTest {
        val seen = mutableListOf<LocalDate>()
        backgroundScope.launch { clock.today.collect { seen += it.date } }
        runCurrent()

        clock.advanceDays(1)
        runCurrent()
        clock.advanceDays(1)
        runCurrent()
        clock.jumpTo(LocalDate.of(2028, 12, 31))
        runCurrent()
        clock.advanceDays(1)
        runCurrent()

        assertEquals(
            listOf(
                LocalDate.of(2028, 2, 28),
                LocalDate.of(2028, 2, 29),
                LocalDate.of(2028, 3, 1),
                LocalDate.of(2028, 12, 31),
                LocalDate.of(2029, 1, 1)
            ),
            seen
        )
    }

    @Test
    fun advancingAMonthClampsToTheMonthEnd() {
        clock.jumpTo(LocalDate.of(2027, 1, 31))

        clock.advanceMonths(1)

        assertEquals(LocalDate.of(2027, 2, 28), clock.today.value.date)
    }

    @Test
    fun steppingNeverChangesTheFinancialZone() {
        clock.advanceDays(400)
        clock.jumpTo(LocalDate.of(2030, 6, 15))

        assertEquals(bogota, clock.today.value.zone)
    }
}
