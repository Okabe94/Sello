package com.software.sello.presentation.corrections

import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure
import com.software.sello.domain.port.ExpenseCommandOutcome
import com.software.sello.domain.port.ExpenseCommandOutcome.OutcomeUnknown
import com.software.sello.domain.port.ExpenseCommandOutcome.Rejected
import com.software.sello.domain.port.ExpenseRejection
import com.software.sello.domain.port.IncomeCommandOutcome
import com.software.sello.domain.port.IncomeRejection
import com.software.sello.domain.port.IncomeUndoOffer
import com.software.sello.domain.port.MonotonicClock
import com.software.sello.domain.port.UndoOffer
import com.software.sello.feature.expense.HandExpenseCommands
import com.software.sello.feature.expense.HandIncomeCommands
import com.software.sello.feature.expense.expenseId
import com.software.sello.feature.expense.incomeId
import com.software.sello.feature.expense.uuid
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Deleting, undoing and what is said about it, over scripted commands. Time is the
 * test's own: the monotonic clock reads five hours plus the virtual time that passed,
 * unless a test moves it apart to stand for a device that slept.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseCorrectionsTest {
    private val scope = TestScope()
    private val commands = HandExpenseCommands()
    private val incomes = HandIncomeCommands()
    private var slept: Duration = Duration.ZERO
    private val monotonic =
        MonotonicClock { 5.hours + scope.testScheduler.currentTime.milliseconds + slept }
    private var nextId = 500
    private val corrections =
        ExpenseCorrections(commands, incomes, monotonic, { uuid(nextId++) }, scope.backgroundScope)

    private val storageFailed =
        Rejected(ExpenseRejection.StorageFailed(StorageFailure.Unavailable("SQLiteException")))

    init {
        // As the real commands do: a committed deletion is on offer for six seconds.
        commands.offer = {
            commands.deleted.lastOrNull()?.takeIf { stillOffered }?.let {
                UndoOffer(it.operationId, it.expenseId, deletedAt + 6.seconds)
            }?.takeIf { monotonic.elapsed() < it.until }
        }
    }

    private var stillOffered = true
    private var deletedAt: Duration = Duration.ZERO

    private fun test(body: suspend TestScope.() -> Unit) = scope.runTest { body() }

    private suspend fun delete(
        expense: Int = 101,
        version: Long = 3,
        generation: Long = 7
    ): DeleteResult {
        deletedAt = monotonic.elapsed()
        return corrections.delete(expenseId(expense), version, generation)
    }

    private val notice get() = corrections.notice.value

    // --- Deleting ---

    @Test
    fun deletingSendsOneDeletionOfThatVersionAndOffersUndo() = test {
        val result = delete()

        assertEquals(DeleteResult.Deleted, result)
        val sent = commands.deleted.single()
        assertEquals(
            listOf(uuid(500), uuid(101), 3L, 7L),
            listOf(
                sent.operationId.value,
                sent.expenseId.value,
                sent.expectedVersion,
                sent.generation
            )
        )
        assertEquals(CorrectionNotice.Deleted(canUndo = true), notice)
    }

    @Test
    fun aDeletionThatWasRefusedSaysWhyAndOffersNothing() = test {
        val answers = listOf(
            Rejected(ExpenseRejection.ExpenseMissing) to DeleteResult.Gone,
            Rejected(ExpenseRejection.VersionConflict(current = 4)) to DeleteResult.Changed,
            Rejected(ExpenseRejection.StaleGeneration(current = 8)) to DeleteResult.DataChanged,
            storageFailed to DeleteResult.Failed
        )

        val results = answers.map { (answer, _) ->
            commands.deleteAnswer = { answer }
            delete() to notice
        }

        assertEquals(answers.map { it.second to null }, results)
    }

    @Test
    fun aDeletionWhoseOutcomeIsUnknownIsLookedUpAndNeverSentAgain() = test {
        commands.deleteAnswer = { OutcomeUnknown(it.operationId) }
        commands.found = { Outcome.Success(null) }
        val notSaved = delete()
        // It did commit, but its time was not started: nothing can be undone.
        stillOffered = false
        commands.found = { Outcome.Success(commands.committed(it)) }
        val saved = delete()
        val savedNotice = notice
        commands.found = { Outcome.Failure(StorageFailure.Unavailable("SQLiteException")) }
        val unreadable = delete()

        assertEquals(
            listOf(DeleteResult.Failed, DeleteResult.Deleted, DeleteResult.Failed),
            listOf(notSaved, saved, unreadable)
        )
        assertEquals(CorrectionNotice.Deleted(canUndo = false), savedNotice)
        assertEquals(3, commands.deleted.size)
        assertEquals(commands.deleted.map { it.operationId }, commands.looked)
    }

    @Test
    fun aDeletionFinishesAndIsOfferedEvenIfTheScreenThatAskedIsGone() = test {
        commands.hold = CompletableDeferred()
        val screen = launch { delete() }
        runCurrent()

        screen.cancelAndJoin()
        commands.hold!!.complete(Unit)
        runCurrent()

        assertEquals(1, commands.deleted.size)
        assertEquals(CorrectionNotice.Deleted(canUndo = true), notice)
    }

    // --- The six seconds ---

    @Test
    fun theOfferIsThereJustBeforeSixSecondsAndGoneAtSixWithNothingAsked() = test {
        delete()

        advanceTimeBy(5_999)
        runCurrent()
        val before = notice
        advanceTimeBy(1)
        runCurrent()

        assertEquals(CorrectionNotice.Deleted(canUndo = true), before)
        assertNull(notice)
        assertEquals(emptyList<Any>(), commands.restored)
    }

    @Test
    fun aSecondDeletionTakesTheFirstOnesPlaceAndKeepsItsOwnSixSeconds() = test {
        delete(expense = 101)
        advanceTimeBy(4_000)
        delete(expense = 102)

        advanceTimeBy(2_500)
        runCurrent()
        val afterTheFirstWouldHaveEnded = notice
        corrections.undo()
        runCurrent()

        assertEquals(CorrectionNotice.Deleted(canUndo = true), afterTheFirstWouldHaveEnded)
        assertEquals(commands.deleted[1].operationId, commands.restored.single().deletionId)
    }

    @Test
    fun whenTheDeviceSleptPastTheTimeComingBackRemovesTheOffer() = test {
        delete()
        advanceTimeBy(1_000)
        // Real time went on while nothing here ran.
        slept = 30.seconds

        corrections.refresh()

        assertNull(notice)
    }

    @Test
    fun comingBackInTimeLeavesTheOfferAndItStillEndsWhenItShould() = test {
        delete()
        advanceTimeBy(1_000)
        slept = 3.seconds

        corrections.refresh()
        val back = notice
        advanceTimeBy(2_000)
        runCurrent()

        assertEquals(CorrectionNotice.Deleted(canUndo = true), back)
        assertNull(notice)
    }

    // --- Undo ---

    @Test
    fun undoPutsBackThatDeletionInItsHistoryAndSaysSoForAMoment() = test {
        delete(generation = 7)
        advanceTimeBy(2_000)

        corrections.undo()
        runCurrent()
        val said = notice
        advanceTimeBy(4_000)
        runCurrent()

        val sent = commands.restored.single()
        assertEquals(
            listOf(uuid(501), 7L, uuid(500)),
            listOf(sent.operationId.value, sent.generation, sent.deletionId.value)
        )
        assertEquals(CorrectionNotice.Restored, said)
        assertNull(notice)
    }

    @Test
    fun undoPressedAgainWhileItIsBeingDoneAsksOnce() = test {
        delete()
        commands.hold = CompletableDeferred()

        corrections.undo()
        runCurrent()
        val during = notice
        corrections.undo()
        runCurrent()
        // The time running out meanwhile does not take away an undo already asked.
        advanceTimeBy(10_000)
        runCurrent()
        val stillDuring = notice
        commands.hold!!.complete(Unit)
        runCurrent()

        assertEquals(CorrectionNotice.Deleted(canUndo = true, undoing = true), during)
        assertEquals(during, stillDuring)
        assertEquals(1, commands.restored.size)
        assertEquals(CorrectionNotice.Restored, notice)
    }

    @Test
    fun anUndoThatCameTooLateOrIsBlockedSaysSoAndRestoresNothing() = test {
        commands.restoreAnswer = { Rejected(ExpenseRejection.UndoExpired) }
        delete()
        corrections.undo()
        runCurrent()
        val late = notice
        commands.restoreAnswer = { Rejected(ExpenseRejection.UndoConflict) }
        delete()
        corrections.undo()
        runCurrent()
        val conflict = notice
        commands.restoreAnswer = { Rejected(ExpenseRejection.StaleGeneration(current = 9)) }
        delete()
        corrections.undo()
        runCurrent()

        assertEquals(CorrectionNotice.UndoTooLate, late)
        assertEquals(CorrectionNotice.UndoBlocked, conflict)
        assertEquals(CorrectionNotice.UndoBlocked, notice)
    }

    @Test
    fun anUndoThatFailedCanBeTriedAgainInTimeAsTheSameOperation() = test {
        delete()
        commands.restoreAnswer = { storageFailed }
        corrections.undo()
        runCurrent()
        val failed = notice
        commands.restoreAnswer =
            {
                ExpenseCommandOutcome.Committed(
                    commands.committed(it.operationId),
                    replayed = false
                )
            }

        corrections.undo()
        runCurrent()

        assertEquals(CorrectionNotice.Deleted(canUndo = true, undoFailed = true), failed)
        assertEquals(CorrectionNotice.Restored, notice)
        assertEquals(listOf(uuid(501), uuid(501)), commands.restored.map { it.operationId.value })
    }

    @Test
    fun anUndoWhoseOutcomeIsUnknownIsLookedUpBeforeAnythingIsSaid() = test {
        delete()
        commands.restoreAnswer = { OutcomeUnknown(it.operationId) }
        commands.found = { Outcome.Success(null) }
        corrections.undo()
        runCurrent()
        val notDone = notice
        commands.found = { Outcome.Success(commands.committed(it)) }

        corrections.undo()
        runCurrent()

        assertEquals(CorrectionNotice.Deleted(canUndo = true, undoFailed = true), notDone)
        assertEquals(CorrectionNotice.Restored, notice)
        assertEquals(commands.restored.map { it.operationId }, commands.looked)
    }

    @Test
    fun undoWithNothingToUndoAsksNothing() = test {
        corrections.undo()
        delete()
        advanceTimeBy(6_000)
        runCurrent()
        corrections.undo()
        runCurrent()

        assertEquals(emptyList<Any>(), commands.restored)
        assertNull(notice)
    }

    // --- Other notices ---

    @Test
    fun aSavedEditIsSaidForAMomentAndCanBeDismissed() = test {
        corrections.updated()
        val said = notice
        advanceTimeBy(3_999)
        runCurrent()
        val stillSaid = notice
        advanceTimeBy(1)
        runCurrent()
        val gone = notice
        corrections.updated()
        corrections.dismiss()

        assertEquals(
            listOf(CorrectionNotice.Updated, CorrectionNotice.Updated, null),
            listOf(said, stillSaid, gone)
        )
        assertNull(notice)
    }

    // --- Income: the same offer, the income's own commands and words ---

    private suspend fun deleteIncome(
        income: Int = 201,
        version: Long = 2,
        generation: Long = 7
    ): DeleteResult {
        deletedAt = monotonic.elapsed()
        incomes.offer = {
            incomes.deleted.lastOrNull()?.let {
                IncomeUndoOffer(it.operationId, it.incomeId, deletedAt + 6.seconds)
            }?.takeIf { monotonic.elapsed() < it.until }
        }
        return corrections.deleteIncome(incomeId(income), version, generation)
    }

    @Test
    fun deletingAnIncomeSendsOneIncomeDeletionAndOffersUndoInItsOwnWords() = test {
        val result = deleteIncome()

        assertEquals(DeleteResult.Deleted, result)
        val sent = incomes.deleted.single()
        assertEquals(
            listOf(uuid(500), uuid(201), 2L, 7L),
            listOf(
                sent.operationId.value,
                sent.incomeId.value,
                sent.expectedVersion,
                sent.generation
            )
        )
        assertEquals(CorrectionNotice.IncomeDeleted(canUndo = true), notice)
        assertEquals(emptyList<Any>(), commands.deleted)
    }

    @Test
    fun undoingADeletedIncomeRestoresItThroughTheIncomeCommandsOnly() = test {
        deleteIncome(generation = 7)
        advanceTimeBy(2_000)

        corrections.undo()
        runCurrent()

        val sent = incomes.restored.single()
        assertEquals(
            listOf(uuid(501), 7L, uuid(500)),
            listOf(sent.operationId.value, sent.generation, sent.deletionId.value)
        )
        assertEquals(emptyList<Any>(), commands.restored)
        assertEquals(CorrectionNotice.IncomeRestored, notice)
    }

    @Test
    fun anIncomeOfferEndsAtSixSecondsAndALateOrBlockedUndoSaysSoAboutTheIncome() = test {
        deleteIncome()
        advanceTimeBy(5_999)
        runCurrent()
        val before = notice
        advanceTimeBy(1)
        runCurrent()
        val after = notice
        incomes.restoreAnswer = { IncomeCommandOutcome.Rejected(IncomeRejection.UndoExpired) }
        deleteIncome()
        corrections.undo()
        runCurrent()
        val late = notice
        incomes.restoreAnswer = { IncomeCommandOutcome.Rejected(IncomeRejection.UndoConflict) }
        deleteIncome()
        corrections.undo()
        runCurrent()

        assertEquals(CorrectionNotice.IncomeDeleted(canUndo = true), before)
        assertNull(after)
        assertEquals(CorrectionNotice.IncomeUndoTooLate, late)
        assertEquals(CorrectionNotice.IncomeUndoBlocked, notice)
    }

    @Test
    fun anIncomeDeletionTakesAnExpenseOffersPlaceAndUndoThenGoesToTheIncome() = test {
        delete(expense = 101)
        advanceTimeBy(1_000)

        deleteIncome()
        corrections.undo()
        runCurrent()

        assertEquals(1, incomes.restored.size)
        assertEquals(emptyList<Any>(), commands.restored)
        assertEquals(CorrectionNotice.IncomeRestored, notice)
    }

    @Test
    fun aRefusedIncomeDeletionSaysWhyAndASavedIncomeEditIsSaidForAMoment() = test {
        val answers = listOf(
            IncomeRejection.IncomeMissing to DeleteResult.Gone,
            IncomeRejection.VersionConflict(current = 4) to DeleteResult.Changed,
            IncomeRejection.StaleGeneration(current = 8) to DeleteResult.DataChanged,
            IncomeRejection.StorageFailed(StorageFailure.Unavailable("x")) to DeleteResult.Failed
        )

        val results = answers.map { (reason, _) ->
            incomes.deleteAnswer = { IncomeCommandOutcome.Rejected(reason) }
            deleteIncome() to notice
        }
        corrections.incomeUpdated()

        assertEquals(answers.map { it.second to null }, results)
        assertEquals(CorrectionNotice.IncomeUpdated, notice)
    }
}
