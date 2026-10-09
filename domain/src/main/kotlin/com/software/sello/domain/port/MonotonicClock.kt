package com.software.sello.domain.port

import kotlin.time.Duration

/**
 * Elapsed time that only moves forward, for deadlines such as undo expiry.
 * Unaffected by wall-clock changes and by simulated financial time.
 */
fun interface MonotonicClock {
    fun elapsed(): Duration
}
