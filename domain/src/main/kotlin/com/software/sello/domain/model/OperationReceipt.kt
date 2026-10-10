package com.software.sello.domain.model

import java.time.Instant

/** What an operation did. [key] is the stable stored spelling. */
enum class OperationKind(val key: String) {
    CreateExpense("expense.create")
}

/**
 * Durable proof that an operation committed. [generation] is the history it was
 * committed into and [revision] the revision it produced. [committedAt] is real audit
 * time, never simulated financial time.
 */
data class OperationReceipt(
    val operationId: OperationId,
    val kind: OperationKind,
    val generation: Long,
    val revision: Long,
    val committedAt: Instant
)
