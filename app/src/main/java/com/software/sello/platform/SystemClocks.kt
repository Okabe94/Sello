package com.software.sello.platform

import android.os.SystemClock
import com.software.sello.domain.port.AuditClock
import com.software.sello.domain.port.DispatcherProvider
import com.software.sello.domain.port.MonotonicClock
import java.time.Instant
import java.time.ZoneId
import kotlin.time.Duration
import kotlin.time.Duration.Companion.nanoseconds
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** The device's current zone. Read once at startup to initialize the financial zone. */
fun deviceZone(): ZoneId = ZoneId.systemDefault()

class SystemAuditClock : AuditClock {
    override fun now(): Instant = Instant.now()
}

/** Time since boot, including sleep, so a deadline is not paused or moved by clock changes. */
class SystemMonotonicClock : MonotonicClock {
    override fun elapsed(): Duration = SystemClock.elapsedRealtimeNanos().nanoseconds
}

class SystemDispatcherProvider : DispatcherProvider {
    override val main: CoroutineDispatcher get() = Dispatchers.Main
    override val default: CoroutineDispatcher get() = Dispatchers.Default
    override val io: CoroutineDispatcher get() = Dispatchers.IO
}
