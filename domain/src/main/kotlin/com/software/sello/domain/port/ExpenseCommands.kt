package com.software.sello.domain.port

import com.software.sello.domain.model.CategoryId
import com.software.sello.domain.model.ExpenseId
import com.software.sello.domain.model.Note
import com.software.sello.domain.model.OperationId
import com.software.sello.domain.model.OperationReceipt
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure
import com.software.sello.domain.model.TransactionAmount
import java.time.LocalDate

/**
 * A request to record one new expense. [operationId] names this attempt and
 * [generation] is the history the caller was looking at when it was prepared.
 * Everything except [operationId] is the input a replay is compared against.
 */
data class CreateExpense(
    val operationId: OperationId,
    val generation: Long,
    val categoryId: CategoryId,
    val amount: TransactionAmount,
    val date: LocalDate,
    val note: Note?
)

/** Why nothing was written. Each of these is certain: the expense does not exist. */
sealed interface ExpenseRejection {
    /** The history was replaced (restore or reset) since the command was prepared. */
    data class StaleGeneration(val current: Long) : ExpenseRejection

    data object CategoryMissing : ExpenseRejection

    data object CategoryArchived : ExpenseRejection

    /** The date is after the financial current date. */
    data class FutureDate(val today: LocalDate) : ExpenseRejection

    /** This operation already committed with different input. The original is unchanged. */
    data object OperationConflict : ExpenseRejection

    /** Storage failed and the write was rolled back. */
    data class StorageFailed(val failure: StorageFailure) : ExpenseRejection
}

/** A committed expense and the receipt that proves it. */
data class CommittedExpense(val receipt: OperationReceipt, val expenseId: ExpenseId)

sealed interface ExpenseCommandOutcome {
    /**
     * The expense is saved. [replayed] is true when this operation had already
     * committed with the same input and this call wrote nothing new.
     */
    data class Committed(val expense: CommittedExpense, val replayed: Boolean) :
        ExpenseCommandOutcome

    data class Rejected(val reason: ExpenseRejection) : ExpenseCommandOutcome

    /**
     * It is not known whether the expense was saved. Ask [ExpenseCommands.find] with
     * the same [operationId], or submit the same command again; never a new one.
     */
    data class OutcomeUnknown(val operationId: OperationId) : ExpenseCommandOutcome
}

interface ExpenseCommands {
    /**
     * Records the expense once. Validation, the expense, the revision and the receipt
     * are one transaction: all of it happens or none of it. Submitting the same
     * operation and input again returns the original receipt.
     *
     * A cancelled caller gets the cancellation, which says nothing about whether the
     * expense was saved; [find] does.
     */
    suspend fun create(command: CreateExpense): ExpenseCommandOutcome

    /** The committed result of [operationId], or null when it did not commit. */
    suspend fun find(operationId: OperationId): Outcome<CommittedExpense?, StorageFailure>
}
