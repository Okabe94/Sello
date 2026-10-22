package com.software.sello.devtools

import com.software.sello.domain.port.FinancialDay
import java.time.LocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The sandbox's time as its controls show it. */
data class SandboxTimeState(
    val day: FinancialDay,
    /** A financial change was saved here; the date can only move forward until a reset. */
    val changed: Boolean,
    /** Days still to come while playing; zero when paused. */
    val stepsLeft: Int = 0
) {
    val playing: Boolean get() = stepsLeft > 0
}

/** Whether a date could be set. */
enum class DateChange { Moved, RefusedBeforeSavedHistory }

/**
 * The sandbox's financial date. Every change goes through [clock], so rollover, the
 * month's figures and Recibo recompute exactly as they do at a real midnight, and is
 * kept in [store] for the next process. Nothing here reads or moves the device clock,
 * audit time or elapsed time: undo windows, animations and timeouts run in real time.
 *
 * Playing advances one day every [stepMillis] real milliseconds, at most [maxSteps]
 * days or up to a chosen date, then stops by itself. A new process starts paused.
 */
class SandboxTime(
    val clock: ControlledFinancialClock,
    private val store: SandboxSessionStore,
    private val scope: CoroutineScope,
    private val stepMillis: Long = STEP_MILLIS,
    private val maxSteps: Int = MAX_STEPS
) {
    private val mutable = MutableStateFlow(
        SandboxTimeState(clock.today.value, changed = store.read().changed)
    )
    val state: StateFlow<SandboxTimeState> = mutable.asStateFlow()

    private var player: Job? = null

    init {
        persist()
    }

    fun advanceDays(days: Long) {
        require(days > 0) { "the sandbox moves back only through a reset" }
        clock.advanceDays(days)
        persist()
    }

    fun advanceMonths(months: Long) {
        require(months > 0) { "the sandbox moves back only through a reset" }
        clock.advanceMonths(months)
        persist()
    }

    /**
     * Sets [date]. An earlier date is refused once a change was saved: showing it would
     * pretend the later history had not happened. Before any change, any date is fine.
     */
    fun jumpTo(date: LocalDate): DateChange {
        if (mutable.value.changed && date < clock.today.value.date) {
            return DateChange.RefusedBeforeSavedHistory
        }
        clock.jumpTo(date)
        persist()
        return DateChange.Moved
    }

    /** Plays forward one day per step, up to [until] when given, else [maxSteps] days. */
    fun play(until: LocalDate? = null) {
        val today = clock.today.value.date
        val steps = until?.let { (it.toEpochDay() - today.toEpochDay()).toInt() }
            ?.coerceIn(0, maxSteps) ?: maxSteps
        if (steps == 0) return
        player?.cancel()
        mutable.update { it.copy(stepsLeft = steps) }
        player = scope.launch {
            repeat(steps) { step ->
                delay(stepMillis)
                clock.advanceDays(1)
                persist(stepsLeft = steps - step - 1)
            }
        }
    }

    fun pause() {
        player?.cancel()
        player = null
        mutable.update { it.copy(stepsLeft = 0) }
    }

    /** Called when a change is written in the sandbox. */
    fun markChanged() {
        if (mutable.value.changed) return
        store.write(store.read().copy(changed = true))
        mutable.update { it.copy(changed = true) }
    }

    private fun persist(stepsLeft: Int = mutable.value.stepsLeft) {
        val day = clock.today.value
        store.write(store.read().copy(date = day.date))
        mutable.update { it.copy(day = day, stepsLeft = stepsLeft) }
    }

    companion object {
        const val STEP_MILLIS = 2_000L
        const val MAX_STEPS = 31
    }
}
