package com.software.sello.composition

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.software.sello.MainActivity
import com.software.sello.domain.port.AuditClock
import com.software.sello.domain.port.FinancialClock
import com.software.sello.domain.port.MonotonicClock
import com.software.sello.platform.SystemFinancialClock
import com.software.sello.platform.TimeSignals
import java.time.ZoneId
import kotlin.time.Duration
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext

@RunWith(AndroidJUnit4::class)
class ProductionGraphOnDeviceTest {
    private val koin get() = GlobalContext.get()

    @Test
    fun applicationStartsTheProductionGraph() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val application = context.applicationContext

        assertTrue(application is SelloApplication)
        assertTrue(koin.get<FinancialClock>() is SystemFinancialClock)
    }

    @Test
    fun financialDayIsTheRealDateInTheDeviceZoneAtStartup() {
        val today = koin.get<FinancialClock>().today.value
        val expected = koin.get<AuditClock>().now().atZone(ZoneId.systemDefault()).toLocalDate()

        assertEquals(ZoneId.systemDefault(), today.zone)
        assertEquals(expected, today.date)
    }

    @Test
    fun monotonicClockMovesForward() {
        val monotonic = koin.get<MonotonicClock>()
        val first = monotonic.elapsed()

        assertTrue(first > Duration.ZERO)
        assertTrue(monotonic.elapsed() >= first)
    }

    @Test
    fun bringingTheAppToTheForegroundEmitsATimeSignal() {
        ActivityScenario.launch(MainActivity::class.java).use {
            runBlocking { withTimeout(10_000) { koin.get<TimeSignals>().changes().first() } }
        }
    }
}
