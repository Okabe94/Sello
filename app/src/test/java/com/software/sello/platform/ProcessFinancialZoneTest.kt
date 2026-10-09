package com.software.sello.platform

import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ProcessFinancialZoneTest {
    private val bogota = ZoneId.of("America/Bogota")
    private val madrid = ZoneId.of("Europe/Madrid")

    @Test
    fun readingBeforeInitializationIsAWiringError() {
        assertThrows(IllegalStateException::class.java) { ProcessFinancialZone().zone }
    }

    @Test
    fun firstInitializationWins() {
        val financialZone = ProcessFinancialZone()

        assertEquals(bogota, financialZone.initialize(bogota))
        assertEquals(bogota, financialZone.zone)
    }

    @Test
    fun laterDeviceZoneDoesNotReplaceTheFinancialZone() {
        val financialZone = ProcessFinancialZone()
        financialZone.initialize(bogota)

        assertEquals(bogota, financialZone.initialize(madrid))
        assertEquals(bogota, financialZone.zone)
    }
}
