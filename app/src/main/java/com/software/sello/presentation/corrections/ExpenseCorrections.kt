package com.software.sello.presentation.corrections

import com.software.sello.domain.model.ExpenseId
import com.software.sello.domain.model.OperationId
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.port.DeleteExpense
import com.software.sello.domain.port.ExpenseCommandOutcome
import com.software.sello.domain.port.ExpenseCommands
import com.software.sello.domain.port.ExpenseRejection
import com.software.sello.domain.port.MonotonicClock
import com.software.sello.domain.port.RecordIdSource
import com.software.sello.domain.port.RestoreExpense
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** What is said, for a moment, about the last correction of an expense. */
sealed interface CorrectionNotice {
    /**
     * An expense was deleted. With [canUndo] it can still be put back; [undoing] while
     * that has been asked, and [undoFailed] when the last attempt did not work and
     * another can be made.
     */
    data class Deleted(
        val canUndo: Boolean,
        val undoing: Boolean = false,
        val undoFailed: Boolean = false
    ) : CorrectionNotice

    /** The deleted expense is back. */
    data object Restored : CorrectionNotice

    /** The time to undo was over when undo was asked. The deletion stands. */
    data object UndoTooLate : CorrectionNotice

    /** Putting the expense back would contradict what is recorded now. The deletion stands. */
    data object UndoBlocked : CorrectionNotice

    /** An edit was saved. */
    data object Updated : CorrectionNotice
}

/** How a deletion ended. Only [Deleted] means the expense is gone. */
enum class DeleteResult { Deleted, Gone, Changed, DataChanged, Failed }

/**
 * Deleting an expense, undoing that, and what is said about the last correction. One
 * instance serves the whole process, so the offer to undo is the same whatever screen
 * is showing and however often it is redrawn, and a deletion is finished and offered
 * even if the screen that asked for it is gone.
 *
 * The time to undo belongs to [ExpenseCommands]; this only stops showing the offer
 * when that time is over, by the same [monotonic] clock. [scope] must outlive screens
 * and run everything on the one thread these functions are called from.
 */
class ExpenseCorrections(
    private val commands: ExpenseCommands,
    private val monotonic: MonotonicClock,
    private val ids: RecordIdSource,
    private val scope: CoroutineScope
) {
    /**
     * The deletion on offer. [undoOperation] names every attempt to undo it, so an
     * attempt whose outcome was not known is asked again and never done twice.
     */
    private class Pending(
        val deletionId: OperationId,
        val generation: Long,
        val until: Duration,
        val undoOperation: OperationId
    )

    private val shown = MutableStateFlow<CorrectionNotice?>(null)
    val notice: StateFlow<CorrectionNotice?> = shown

    private var pending: Pending? = null
    private var ending: Job? = null

    /**
     * Deletes the expense as the caller read it. It runs to its end even if the caller
     * is cancelled meanwhile; the caller then gets the cancellation, not a result.
     */
    suspend fun delete(
        expenseId: ExpenseId,
        expectedVersion: Long,
        generation: Long
    ): DeleteResult = scope.async { deleteOnce(expenseId, expectedVersion, generation) }.await()

    private suspend fun deleteOnce(
        expenseId: ExpenseId,
        expectedVersion: Long,
        generation: Long
    ): DeleteResult {
        val operation = operation()
        val command = DeleteExpense(operation, generation, expenseId, expectedVersion)
        val committed = when (val outcome = commands.delete(command)) {
            is ExpenseCommandOutcome.Committed -> true

            // Asked, never sent again: only a receipt says it was deleted.
            is ExpenseCommandOutcome.OutcomeUnknown ->
                (commands.find(operation) as? Outcome.Success)?.value != null

            is ExpenseCommandOutcome.Rejected -> return when (outcome.reason) {
                ExpenseRejection.ExpenseMissing -> DeleteResult.Gone
                is ExpenseRejection.VersionConflict -> DeleteResult.Changed
                is ExpenseRejection.StaleGeneration -> DeleteResult.DataChanged
                else -> DeleteResult.Failed
            }
        }
        if (!committed) return DeleteResult.Failed
        val offer = commands.undoOffer()?.takeIf { it.deletionId == operation }
        if (offer == null) {
            pending = null
            show(CorrectionNotice.Deleted(canUndo = false), monotonic.elapsed() + NOTICE)
        } else {
            pending = Pending(operation, generation, offer.until, operation())
            show(CorrectionNotice.Deleted(canUndo = true), offer.until)
        }
        return DeleteResult.Deleted
    }

    /** Puts back the deletion on offer. Asked again while that is being done, it does nothing. */
    fun undo() {
        val offered = pending ?: return
        val now = shown.value as? CorrectionNotice.Deleted ?: return
        if (!now.canUndo || now.undoing) return
        // Once asked, the answer decides what is said next, not the time running out.
        ending?.cancel()
        shown.value = now.copy(undoing = true, undoFailed = false)
        scope.launch { settle(offered, restore(offered)) }
    }

    /** How the undo ended, or null when that could not be found out and it can be asked again. */
    private suspend fun restore(offered: Pending): ExpenseCommandOutcome? {
        val command = RestoreExpense(offered.undoOperation, offered.generation, offered.deletionId)
        val outcome = commands.restore(command)
        if (outcome !is ExpenseCommandOutcome.OutcomeUnknown) return outcome
        val found = (commands.find(offered.undoOperation) as? Outcome.Success)?.value ?: return null
        return ExpenseCommandOutcome.Committed(found, replayed = true)
    }

    private fun settle(offered: Pending, outcome: ExpenseCommandOutcome?) {
        // A later deletion took this one's place while it was being undone.
        if (pending !== offered) return
        val rejection = (outcome as? ExpenseCommandOutcome.Rejected)?.reason
        when {
            outcome is ExpenseCommandOutcome.Committed -> said(CorrectionNotice.Restored)

            rejection == ExpenseRejection.UndoExpired -> said(CorrectionNotice.UndoTooLate)

            rejection == ExpenseRejection.UndoConflict ||
                rejection is ExpenseRejection.StaleGeneration -> said(CorrectionNotice.UndoBlocked)

            // Nothing was restored and nothing forbids it: it can be asked again while
            // the time lasts.
            monotonic.elapsed() < offered.until ->
                show(CorrectionNotice.Deleted(canUndo = true, undoFailed = true), offered.until)

            else -> said(CorrectionNotice.UndoTooLate)
        }
    }

    /** A screen reports that an edit was saved. */
    fun updated() = said(CorrectionNotice.Updated)

    fun dismiss() {
        val now = shown.value
        if (now is CorrectionNotice.Deleted && now.undoing) return
        ending?.cancel()
        pending = null
        shown.value = null
    }

    /**
     * Call when the app is back in view. Waiting does not advance while a device
     * sleeps but the time to undo does, so the offer is checked against the clock again.
     */
    fun refresh() {
        val offered = pending ?: return
        val now = shown.value as? CorrectionNotice.Deleted ?: return
        if (now.canUndo && !now.undoing) show(now, offered.until)
    }

    /** Something that is over: said for a moment, with nothing left to undo. */
    private fun said(notice: CorrectionNotice) {
        pending = null
        show(notice, monotonic.elapsed() + NOTICE)
    }

    /** Shows [notice] until the clock reads [until]. */
    private fun show(notice: CorrectionNotice, until: Duration) {
        ending?.cancel()
        if (monotonic.elapsed() >= until) {
            pending = null
            shown.value = null
            return
        }
        shown.value = notice
        ending = scope.launch {
            var left = until - monotonic.elapsed()
            while (left > Duration.ZERO) {
                delay(left)
                left = until - monotonic.elapsed()
            }
            pending = null
            shown.value = null
        }
    }

    private fun operation(): OperationId = when (val id = OperationId.of(ids.next())) {
        is Outcome.Success -> id.value
        is Outcome.Failure -> error("The identifier source returned a malformed identifier")
    }

    private companion object {
        /** How long something that needs no answer stays on screen. */
        val NOTICE = 4.seconds
    }
}
