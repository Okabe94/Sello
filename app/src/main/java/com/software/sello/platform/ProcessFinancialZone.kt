package com.software.sello.platform

import java.time.ZoneId

/**
 * The financial zone for this process. It is set once, explicitly, at startup;
 * a later device zone (travel) does not replace it, so effective dates do not move.
 *
 * Nothing is persisted here. SELLO-011 stores the zone in Room and supplies it at
 * startup instead of the device zone; until then each process start initializes it again.
 */
class ProcessFinancialZone {
    private var initialized: ZoneId? = null

    val zone: ZoneId
        get() = checkNotNull(initialized) { "Financial zone was read before initialize()" }

    /** Returns the financial zone in effect: [deviceZone] on the first call, the existing zone afterwards. */
    @Synchronized
    fun initialize(deviceZone: ZoneId): ZoneId = initialized ?: deviceZone.also { initialized = it }
}
