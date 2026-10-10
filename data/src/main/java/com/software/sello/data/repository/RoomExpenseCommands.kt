package com.software.sello.data.repository

import com.software.sello.data.local.SelloDatabase
import com.software.sello.data.mapper.StoredReceipt
import com.software.sello.data.mapper.toDomain
import com.software.sello.data.mapper.toEntity
import com.software.sello.data.mapper.toStored
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
    override suspend fun create(command: CreateExpense): ExpenseCommandOutcome =
        when (val run = database.runCommand { decide(command) }) {
            is CommandRun.Finished -> run.value
            is CommandRun.RolledBack -> Rejected(ExpenseRejection.StorageFailed(run.failure))
            CommandRun.Unknown -> OutcomeUnknown(command.operationId)
        }

    private fun failed(failure: StorageFailure) = Rejected(ExpenseRejection.StorageFailed(failure))

    private fun StoredReceipt.committedExpense() =
        CommittedExpense(receipt, checkNotNull(expenseId) { "Not an expense receipt" })

    /** Runs inside the transaction. Every rejection returns before the first write. */
    private suspend fun decide(command: CreateExpense): ExpenseCommandOutcome {
        val kind = OperationKind.CreateExpense
        val digest = inputDigest(command)
        val start = database.startCommand(command.operationId, command.generation, kind, digest)
        val profile = when (start) {
            is CommandStart.Proceed -> start.profile

            is CommandStart.Stale -> return Rejected(
                ExpenseRejection.StaleGeneration(start.current)
            )

            is CommandStart.Replay -> return Committed(start.original.committedExpense(), true)

            CommandStart.Conflict -> return Rejected(ExpenseRejection.OperationConflict)

            is CommandStart.Damaged -> return failed(start.failure)
        }

        val category = database.categoryDao().byId(command.categoryId.value)
            ?: return Rejected(ExpenseRejection.CategoryMissing)
        when (val stored = category.toDomain()) {
            is Outcome.Failure -> return failed(stored.error)

            is Outcome.Success -> if (stored.value.archived) {
                return Rejected(ExpenseRejection.CategoryArchived)
            }
        }
        val today = clock.today.value.date
        if (EffectiveDates.forManualEntry(command.date, today) is Outcome.Failure) {
            return Rejected(ExpenseRejection.FutureDate(today))
        }

        val now = audit.now()
        val expenseId = when (val id = ExpenseId.of(ids.next())) {
            is Outcome.Success -> id.value
            is Outcome.Failure -> error("The identifier source returned a malformed identifier")
        }
        val receipt = database.commitCommand(
            profile,
            command.operationId,
            kind,
            digest,
            expenseId.value,
            now
        )
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
        return Committed(receipt.committedExpense(), replayed = false)
    }

    override suspend fun find(
        operationId: OperationId
    ): Outcome<CommittedExpense?, StorageFailure> = reading {
        when (val receipt = database.operationReceiptDao().find(operationId.value)?.toStored()) {
            null -> Outcome.Success(null)

            is Outcome.Failure -> receipt

            is Outcome.Success ->
                Outcome.Success(receipt.value.expenseId?.let { receipt.value.committedExpense() })
        }
    }
}
