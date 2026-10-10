package com.software.sello.data.mapper

import com.software.sello.data.local.entity.OperationReceiptEntity
import com.software.sello.domain.model.ExpenseId
import com.software.sello.domain.model.OperationId
import com.software.sello.domain.model.OperationKind
import com.software.sello.domain.model.OperationReceipt
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure
import com.software.sello.domain.port.CommittedExpense
import java.time.Instant

private fun RowDecoder.receipt(row: OperationReceiptEntity) = OperationReceipt(
    operationId = valid("operation_id", OperationId.of(row.operationId)),
    kind = OperationKind.entries.firstOrNull { it.key == row.kind } ?: reject("kind"),
    generation = row.generation.also { check("generation", it >= 1) },
    // The first commit produces revision 1; a receipt cannot describe revision 0.
    revision = row.revision.also { check("revision", it >= 1) },
    committedAt = Instant.ofEpochMilli(row.committedAt)
)

/** The receipt of a created expense: its subject is that expense's identifier. */
internal fun OperationReceiptEntity.toCommittedExpense(): Decoded<CommittedExpense> =
    decodeRow(OperationReceiptEntity.TABLE, operationId) {
        val receipt = receipt(this@toCommittedExpense)
        check("kind", receipt.kind == OperationKind.CreateExpense)
        check("input_digest", sha256Hex.matches(inputDigest))
        CommittedExpense(receipt, valid("subject_id", ExpenseId.of(subjectId.orEmpty())))
    }

private typealias Decoded<T> = Outcome<T, StorageFailure.Integrity>

private val sha256Hex = Regex("[0-9a-f]{64}")
