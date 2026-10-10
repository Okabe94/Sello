package com.software.sello.platform

import java.time.ZoneId

/**
 * The financial zone for this process. It is set once, explicitly, at startup;
 * a later device zone (travel) does not replace it, so effective dates do not move.
 *
 * Nothing is persisted here: the composition root reads the zone stored in the
 * database and passes it in. Only the first start of a new installation passes the
 * device zone, at the moment it is stored.
 */
class ProcessFinancialZone {
    private var initialized: ZoneId? = null

    val zone: ZoneId
        get() = checkNotNull(initialized) { "Financial zone was read before initialize()" }

    /** Returns the financial zone in effect: [zone] on the first call, the existing zone afterwards. */
    @Synchronized
    fun initialize(zone: ZoneId): ZoneId = initialized ?: zone.also { initialized = it }
}
