package com.software.sello.devtools

import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first

/**
 * Lets the sandbox switch or reset only between financial writes. Every command runs
 * through [write]; [close] stops new ones from starting and returns once those already
 * running have finished, whatever their outcome.
 *
 * A command asked for after [close] never starts and never returns: nothing is
 * written, no outcome is invented, and the process is about to be replaced.
 */
class WriteFence {
    private val running = MutableStateFlow(0)
    private var closed = false

    suspend fun <T> write(block: suspend () -> T): T {
        val admitted = synchronized(this) {
            if (!closed) running.value += 1
            !closed
        }
        if (!admitted) awaitCancellation()
        try {
            return block()
        } finally {
            synchronized(this) { running.value -= 1 }
        }
    }

    suspend fun close() {
        synchronized(this) { closed = true }
        running.first { it == 0 }
    }
}
