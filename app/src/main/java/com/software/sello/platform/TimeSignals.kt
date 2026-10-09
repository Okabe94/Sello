package com.software.sello.platform

import kotlinx.coroutines.flow.Flow

/** Emits whenever financial "today" may have changed: app resume, or a device time, date or zone change. */
fun interface TimeSignals {
    fun changes(): Flow<Unit>
}
