package com.software.sello.devtools

import com.software.sello.composition.HostSubstitutes
import com.software.sello.composition.initializedZone
import com.software.sello.composition.platformModule
import com.software.sello.domain.port.AuditClock
import com.software.sello.domain.port.FinancialClock
import com.software.sello.domain.port.FinancialDay
import com.software.sello.domain.port.MonotonicClock
import com.software.sello.platform.SystemAuditClock
import com.software.sello.platform.SystemMonotonicClock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.time.Duration.Companion.seconds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.koin.dsl.koinApplication
import org.koin.dsl.module

class DebugGraphTest {
    private val host = HostSubstitutes()
    private val controlled =
        ControlledFinancialClock(
            FinancialDay(LocalDate.of(2026, 10, 9), ZoneId.of("America/Bogota"))
        )

    @Test
    fun overrideReplacesFinancialTimeAndKeepsTheRealAuditAndMonotonicClocks() {
        val application = koinApplication {
            allowOverride(true)
            modules(
                platformModule(initializedZone()),
                host.module,
                financialTimeOverride(controlled)
            )
        }
        host.scheduler.runCurrent()
        val koin = application.koin

        assertSame(controlled, koin.get<FinancialClock>())
        assertTrue(koin.get<AuditClock>() is SystemAuditClock)
        assertTrue(koin.get<MonotonicClock>() is SystemMonotonicClock)
        application.close()
    }

    @Test
    fun productionFinancialObserverIsNotStartedBesideTheSimulatedClock() {
        val application = koinApplication {
            allowOverride(true)
            modules(
                platformModule(initializedZone()),
                host.module,
                financialTimeOverride(controlled)
            )
        }
        host.scheduler.runCurrent()

        assertEquals(0, host.signals.subscriptions)
        application.close()
    }

    @Test
    fun steppingFinancialTimeDoesNotMoveAuditOrMonotonicTime() {
        val auditInstant = Instant.parse("2026-10-09T15:00:00Z")
        val application = koinApplication {
            allowOverride(true)
            modules(
                platformModule(initializedZone()),
                host.module,
                financialTimeOverride(controlled),
                module {
                    single<AuditClock> { AuditClock { auditInstant } }
                    single<MonotonicClock> { MonotonicClock { 42.seconds } }
                }
            )
        }
        val koin = application.koin

        controlled.advanceMonths(14)

        assertEquals(LocalDate.of(2027, 12, 9), koin.get<FinancialClock>().today.value.date)
        assertEquals(auditInstant, koin.get<AuditClock>().now())
        assertEquals(42.seconds, koin.get<MonotonicClock>().elapsed())
        application.close()
    }
}
