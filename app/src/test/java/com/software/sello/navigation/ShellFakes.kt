package com.software.sello.navigation

import com.software.sello.domain.port.FinancialClock
import com.software.sello.domain.port.FinancialDay
import com.software.sello.domain.port.MonotonicClock
import java.time.LocalDate
import java.time.ZoneId
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.flow.MutableStateFlow

/** Financial time a test moves by hand, as a month change or the sandbox would. */
class HandFinancialClock(date: String) : FinancialClock {
    private val zone = ZoneId.of("America/Bogota")
    override val today = MutableStateFlow(FinancialDay(LocalDate.parse(date), zone))

    fun goTo(date: String) {
        today.value = FinancialDay(LocalDate.parse(date), zone)
    }
}

/** Elapsed time a test moves by hand. It starts well after "boot". */
class HandMonotonicClock : MonotonicClock {
    var now: Duration = 5.hours

    override fun elapsed(): Duration = now
}
