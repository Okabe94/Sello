package com.software.sello.feature.reset

import com.software.sello.domain.model.OperationId
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.port.RecordIdSource
import com.software.sello.domain.port.ResetOutcome
import com.software.sello.domain.port.ResetPreview
import com.software.sello.domain.port.Resets
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Where an erase stands. Only [Erased] means the history is gone. */
sealed interface ResetStep {
    data object Idle : ResetStep

    data object Counting : ResetStep

    /** The counts could not be read; nothing was erased. */
    data object Unreadable : ResetStep

    /** Counted and waiting. [refreshed]: the history changed and these are the new counts. */
    data class Ready(val preview: ResetPreview, val refreshed: Boolean = false) : ResetStep

    data class Erasing(val preview: ResetPreview, val operationId: OperationId) : ResetStep

    /** Nothing was erased. */
    data object Failed : ResetStep

    /** The erase may have committed; asking again with the same operation finds out. */
    data class Unknown(val preview: ResetPreview, val operationId: OperationId) : ResetStep

    /** Erased. [preferencesPending]: the preferences are not back to first-run yet. */
    data class Erased(val preferencesPending: Boolean, val applying: Boolean = false) : ResetStep
}

/**
 * One erase at a time, for as long as the process lives, so leaving the screen never
 * cuts it short. Created with the graph: an erase an earlier process committed but did
 * not finish is finished then, and shown only if its preferences still fail.
 */
class ResetSession(
    private val resets: Resets,
    private val ids: RecordIdSource,
    private val scope: CoroutineScope
) {
    private val state = MutableStateFlow<ResetStep>(ResetStep.Idle)
    val step: StateFlow<ResetStep> = state.asStateFlow()

    init {
        scope.launch {
            resets.finishPending()?.takeIf { it.preferencesPending }?.let {
                // Shown over counts read meanwhile, never over an erase under way.
                state.update { now ->
                    if (now.counted) ResetStep.Erased(preferencesPending = true) else now
                }
            }
        }
    }

    /** On opening the screen: counts again unless an erase is under way or done. */
    fun open() {
        val now = state.value
        if (now == ResetStep.Idle || now == ResetStep.Unreadable || now == ResetStep.Failed ||
            now is ResetStep.Ready
        ) {
            count(refreshed = false)
        }
    }

    /** Counts again after a failure. */
    fun retry() {
        val now = state.value
        if (now == ResetStep.Unreadable || now == ResetStep.Failed) count(refreshed = false)
    }

    fun erase(preview: ResetPreview) {
        val operation = when (val id = OperationId.of(ids.next())) {
            is Outcome.Success -> id.value
            is Outcome.Failure -> error("The identifier source returned a malformed identifier")
        }
        begin(
            ResetStep.Erasing(preview, operation),
            from = { (it as? ResetStep.Ready)?.preview == preview }
        )
    }

    /** After an unknown outcome: the same erase, asked again. */
    fun check() {
        val unknown = state.value as? ResetStep.Unknown ?: return
        begin(ResetStep.Erasing(unknown.preview, unknown.operationId), from = { it == unknown })
    }

    fun applyPreferences() {
        val current = state.value as? ResetStep.Erased ?: return
        if (current.applying || !current.preferencesPending) return
        state.value = current.copy(applying = true)
        scope.launch {
            val finished = resets.finishPending()
            state.value = ResetStep.Erased(finished?.preferencesPending ?: true)
        }
    }

    /** The person is leaving an erased history; the next screen starts from the new one. */
    fun finish() {
        state.update {
            if (it is ResetStep.Erased || it is ResetStep.Unknown) ResetStep.Idle else it
        }
    }

    private fun begin(erasing: ResetStep.Erasing, from: (ResetStep) -> Boolean) {
        var started = false
        state.update { current ->
            if (from(current)) {
                started = true
                erasing
            } else {
                current
            }
        }
        if (!started) return
        scope.launch {
            when (val outcome = resets.reset(erasing.preview, erasing.operationId)) {
                is ResetOutcome.Erased -> state.value = ResetStep.Erased(outcome.preferencesPending)

                ResetOutcome.Stale -> count(refreshed = true)

                ResetOutcome.Failed -> state.value = ResetStep.Failed

                is ResetOutcome.OutcomeUnknown ->
                    state.value = ResetStep.Unknown(erasing.preview, erasing.operationId)
            }
        }
    }

    private fun count(refreshed: Boolean) {
        state.value = ResetStep.Counting
        scope.launch { load(refreshed) }
    }

    private suspend fun load(refreshed: Boolean) {
        val counted = when (val preview = resets.preview()) {
            is Outcome.Success -> ResetStep.Ready(preview.value, refreshed)
            is Outcome.Failure -> ResetStep.Unreadable
        }
        state.compareAndSet(ResetStep.Counting, counted)
    }

    /** Nothing is being or has been erased: counting, counted, or waiting to count. */
    private val ResetStep.counted: Boolean
        get() = this == ResetStep.Idle || this == ResetStep.Counting ||
            this == ResetStep.Unreadable || this == ResetStep.Failed || this is ResetStep.Ready
}
