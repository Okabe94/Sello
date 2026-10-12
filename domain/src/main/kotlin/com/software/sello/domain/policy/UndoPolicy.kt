package com.software.sello.domain.policy

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/** Approved D07: how long a deletion can be undone, in real elapsed time. */
object UndoPolicy {
    val WINDOW: Duration = 6.seconds

    /**
     * Whether a deletion made at [deletedAt] can still be undone at [now], both readings
     * of the same monotonic clock. The last instant it can is just before the window
     * ends: at six seconds exactly it has ended.
     */
    fun canUndo(deletedAt: Duration, now: Duration): Boolean = now - deletedAt < WINDOW
}
