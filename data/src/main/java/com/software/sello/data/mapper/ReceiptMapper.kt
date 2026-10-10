package com.software.sello.data.mapper

import com.software.sello.data.local.entity.OperationReceiptEntity
import com.software.sello.domain.model.CategoryId
import com.software.sello.domain.model.ExpenseId
import com.software.sello.domain.model.OperationId
import com.software.sello.domain.model.OperationKind
import com.software.sello.domain.model.OperationReceipt
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure
import java.time.Instant

/**
 * A stored receipt, decoded. The subject is the record the operation changed: an
 * expense for [OperationKind.CreateExpense], a category for every other kind.
 */
internal class StoredReceipt(
    val receipt: OperationReceipt,
    val inputDigest: String,
    val expenseId: ExpenseId?,
    val categoryId: CategoryId?
)

private val sha256Hex = Regex("[0-9a-f]{64}")

internal fun OperationReceiptEntity.toStored(): Outcome<StoredReceipt, StorageFailure.Integrity> =
    decodeRow(OperationReceiptEntity.TABLE, operationId) {
        val kind = OperationKind.entries.firstOrNull { it.key == kind } ?: reject("kind")
        check("generation", generation >= 1)
        // The first commit produces revision 1; a receipt cannot describe revision 0.
        check("revision", revision >= 1)
        check("input_digest", sha256Hex.matches(inputDigest))
        val receipt = OperationReceipt(
            operationId = valid("operation_id", OperationId.of(operationId)),
            kind = kind,
            generation = generation,
            revision = revision,
            committedAt = Instant.ofEpochMilli(committedAt)
        )
        val subject = subjectId.orEmpty()
        if (kind == OperationKind.CreateExpense) {
            StoredReceipt(receipt, inputDigest, valid("subject_id", ExpenseId.of(subject)), null)
        } else {
            StoredReceipt(receipt, inputDigest, null, valid("subject_id", CategoryId.of(subject)))
        }
    }
