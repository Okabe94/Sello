package com.software.sello.platform

import com.software.sello.domain.port.AuditClock
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.test.TestCoroutineScheduler

/** Real time that advances with the test scheduler's virtual time, and can be changed like a device clock. */
@OptIn(ExperimentalCoroutinesApi::class)
class VirtualAuditClock(private val scheduler: TestCoroutineScheduler, start: Instant) :
    AuditClock {
    private var origin = start

    override fun now(): Instant = origin.plusMillis(scheduler.currentTime)

    /** Simulates the user or network changing the device clock. */
    fun setTo(instant: Instant) {
        origin = instant.minusMillis(scheduler.currentTime)
    }
}

class RecordingTimeSignals : TimeSignals {
    private val signals = MutableSharedFlow<Unit>(extraBufferCapacity = 16)
    var subscriptions = 0
        private set
    var cancellations = 0
        private set

    override fun changes(): Flow<Unit> = signals.onStart {
        subscriptions++
    }.onCompletion { cancellations++ }

    fun signal() {
        check(signals.tryEmit(Unit))
    }
}
