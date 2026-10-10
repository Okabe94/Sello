package com.software.sello.data.repository

import android.database.sqlite.SQLiteException
import androidx.room.withTransaction
import com.software.sello.data.local.SelloDatabase
import com.software.sello.data.local.entity.OperationReceiptEntity
import com.software.sello.data.mapper.profileFrom
import com.software.sello.data.mapper.toCommittedExpense
import com.software.sello.data.mapper.toDomain
import com.software.sello.data.mapper.toEntity
import com.software.sello.domain.model.Expense
import com.software.sello.domain.model.ExpenseId
import com.software.sello.domain.model.OperationId
import com.software.sello.domain.model.OperationKind
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure
import com.software.sello.domain.policy.EffectiveDates
import com.software.sello.domain.port.AuditClock
import com.software.sello.domain.port.CommittedExpense
import com.software.sello.domain.port.CreateExpense
import com.software.sello.domain.port.ExpenseCommandOutcome
import com.software.sello.domain.port.ExpenseCommandOutcome.Committed
import com.software.sello.domain.port.ExpenseCommandOutcome.OutcomeUnknown
import com.software.sello.domain.port.ExpenseCommandOutcome.Rejected
import com.software.sello.domain.port.ExpenseCommands
import com.software.sello.domain.port.ExpenseRejection
import com.software.sello.domain.port.FinancialClock
import com.software.sello.domain.port.RecordIdSource
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/**
 * Records expenses in one short transaction each: check the generation, answer a
 * replay, validate, then write the expense, the revision and the receipt together.
 *
 * [audit] stamps the rows and the receipt with real time. [clock] is only asked which
 * day it is, to refuse a future date; simulated financial time never reaches a timestamp.
 */
class RoomExpenseCommands(
    private val database: SelloDatabase,
    private val audit: AuditClock,
    private val clock: FinancialClock,
    private val ids: RecordIdSource
) : ExpenseCommands {
    override suspend fun create(command: CreateExpense): ExpenseCommandOutcome {
        currentCoroutineContext().ensureActive()
        // Once started, the transaction finishes whatever happens to the caller, so a
        // cancellation can never stop it halfway. Until every statement has run, a
        // failure means the transaction rolled back. A failure after that came from the
        // commit itself, and whether it took effect is not known here.
        var everyStatementRan = false
        val outcome = try {
            withContext(NonCancellable) {
                database.withTransaction { decide(command).also { everyStatementRan = true } }
            }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: SQLiteException) {
            if (everyStatementRan) OutcomeUnknown(command.operationId) else rolledBack(failure)
        } catch (failure: IllegalStateException) {
            if (everyStatementRan) OutcomeUnknown(command.operationId) else rolledBack(failure)
        }
        // A caller cancelled meanwhile is told so, not handed a result it may never see.
        // The receipt is already durable; `find` or the same command recovers it.
        currentCoroutineContext().ensureActive()
        return outcome
    }

    private fun rolledBack(failure: Exception) =
        Rejected(failed(StorageFailure.Unavailable(failure.javaClass.simpleName)))

    private fun failed(failure: StorageFailure) = ExpenseRejection.StorageFailed(failure)

    /** Runs inside the transaction. Every rejection returns before the first write. */
    private suspend fun decide(command: CreateExpense): ExpenseCommandOutcome {
        val profile = when (val stored = profileFrom(database.profileDao().rows())) {
            is Outcome.Success -> stored.value
            is Outcome.Failure -> return Rejected(failed(stored.error))
        }
        if (profile.generation != command.generation) {
            return Rejected(ExpenseRejection.StaleGeneration(profile.generation))
        }

        val digest = inputDigest(command)
        database.operationReceiptDao().find(command.operationId.value)?.let { receipt ->
            return when (val original = receipt.toCommittedExpense()) {
                is Outcome.Failure -> Rejected(failed(original.error))

                is Outcome.Success -> if (receipt.inputDigest == digest) {
                    Committed(original.value, replayed = true)
                } else {
                    Rejected(ExpenseRejection.OperationConflict)
                }
            }
        }

        val category = database.categoryDao().byId(command.categoryId.value)
            ?: return Rejected(ExpenseRejection.CategoryMissing)
        when (val stored = category.toDomain()) {
            is Outcome.Failure -> return Rejected(failed(stored.error))

            is Outcome.Success -> if (stored.value.archived) {
                return Rejected(ExpenseRejection.CategoryArchived)
            }
        }
        val today = clock.today.value.date
        if (EffectiveDates.forManualEntry(command.date, today) is Outcome.Failure) {
            return Rejected(ExpenseRejection.FutureDate(today))
        }

        val now = audit.now()
        val revision = Math.addExact(profile.revision, 1)
        val advanced =
            database.profileDao().advanceRevision(profile.generation, profile.revision, revision)
        check(advanced == 1) { "The profile changed inside the transaction that read it" }
        val expenseId = when (val id = ExpenseId.of(ids.next())) {
            is Outcome.Success -> id.value
            is Outcome.Failure -> error("The identifier source returned a malformed identifier")
        }
        val expense = Expense(
            id = expenseId,
            categoryId = command.categoryId,
            date = command.date,
            sequence = Math.addExact(database.expenseDao().highestSequence() ?: 0, 1),
            amount = command.amount,
            note = command.note,
            version = 1,
            createdAt = now,
            updatedAt = now
        )
        database.expenseDao().insert(expense.toEntity())
        val receipt = OperationReceiptEntity(
            operationId = command.operationId.value,
            kind = OperationKind.CreateExpense.key,
            inputDigest = digest,
            generation = profile.generation,
            revision = revision,
            subjectId = expenseId.value,
            committedAt = now.toEpochMilli()
        )
        database.operationReceiptDao().insert(receipt)
        return when (val committed = receipt.toCommittedExpense()) {
            is Outcome.Success -> Committed(committed.value, replayed = false)
            is Outcome.Failure -> error("A receipt written by this command did not read back")
        }
    }

    override suspend fun find(
        operationId: OperationId
    ): Outcome<CommittedExpense?, StorageFailure> = try {
        when (val receipt = database.operationReceiptDao().find(operationId.value)) {
            null -> Outcome.Success(null)
            else -> receipt.toCommittedExpense()
        }
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (failure: SQLiteException) {
        Outcome.Failure(StorageFailure.Unavailable(failure.javaClass.simpleName))
    } catch (failure: IllegalStateException) {
        Outcome.Failure(StorageFailure.Unavailable(failure.javaClass.simpleName))
    }
}
