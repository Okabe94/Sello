package com.software.sello.domain.port

import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.flow.StateFlow

/** The current effective date in the financial zone. */
data class FinancialDay(val date: LocalDate, val zone: ZoneId)

/**
 * Financial "today". Every financial rule reads the date from here, never from the
 * wall clock, and observes [today] so that rollover or simulated time reaches it.
 *
 * This is the only clock a sandbox may replace. It does not provide audit
 * timestamps ([AuditClock]) or elapsed-time deadlines ([MonotonicClock]).
 */
interface FinancialClock {
    val today: StateFlow<FinancialDay>
}
