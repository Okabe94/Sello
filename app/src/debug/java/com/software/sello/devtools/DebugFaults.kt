package com.software.sello.devtools

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * A fault the sandbox can put at one boundary. None changes a figure or a receipt:
 * each one changes only what a caller is told, or whether a write is attempted.
 */
enum class Fault(val label: String) {
    /** Recibo and Resumen get one failed read; the last good figures stay with the error. */
    ReadFailure("Lectura fallida"),

    /** The write really commits; its caller is told the outcome is unknown. */
    LostAnswer("Respuesta perdida tras guardar"),

    /** The write really commits; then its caller's work is cancelled. */
    InterruptAfterSave("Interrupción tras guardar"),

    /**
     * Nothing is written and the caller is told the outcome is unknown. It happens
     * before storage, so it says nothing about how a transaction rolls back.
     */
    FailBeforeStorage("Falla antes de guardar")
}

/**
 * The faults armed in this process, each with how many more times it fires. They live
 * only in memory, so a restart, a profile switch or [clear] removes them all, and the
 * debug composition installs them only in the sandbox.
 */
class DebugFaults {
    private val armed = MutableStateFlow<Map<Fault, Int>>(emptyMap())
    val state: StateFlow<Map<Fault, Int>> = armed.asStateFlow()

    private val reads = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    /** Fires once for every read failure armed, to whichever reads are open. */
    val readFailures: SharedFlow<Unit> = reads.asSharedFlow()

    fun arm(fault: Fault, times: Int = 1) {
        require(times > 0)
        if (fault == Fault.ReadFailure) {
            repeat(times) { reads.tryEmit(Unit) }
            return
        }
        armed.update { it + (fault to (it[fault] ?: 0) + times) }
    }

    fun clear() = armed.update { emptyMap() }

    /** The command fault to apply now, if any, counted as used. */
    fun takeCommandFault(): Fault? = synchronized(this) {
        val now = armed.value
        val fault = listOf(Fault.FailBeforeStorage, Fault.LostAnswer, Fault.InterruptAfterSave)
            .firstOrNull { (now[it] ?: 0) > 0 } ?: return null
        val left = now.getValue(fault) - 1
        armed.value = if (left == 0) now - fault else now + (fault to left)
        fault
    }
}
