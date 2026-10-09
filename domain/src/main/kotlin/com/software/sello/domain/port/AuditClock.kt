package com.software.sello.domain.port

import java.time.Instant

/** Real time for audit timestamps. Simulated financial time never changes it. */
fun interface AuditClock {
    fun now(): Instant
}
