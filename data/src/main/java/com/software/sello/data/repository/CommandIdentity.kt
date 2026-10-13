package com.software.sello.data.repository

import com.software.sello.domain.model.BudgetLimit
import com.software.sello.domain.model.IncomeSource
import com.software.sello.domain.model.OperationKind
import com.software.sello.domain.port.ArchiveCategory
import com.software.sello.domain.port.CategoryCommand
import com.software.sello.domain.port.CategoryEdit
import com.software.sello.domain.port.ChangeCategoryIcon
import com.software.sello.domain.port.CreateCategory
import com.software.sello.domain.port.CreateExpense
import com.software.sello.domain.port.CreateIncome
import com.software.sello.domain.port.DeleteExpense
import com.software.sello.domain.port.DeleteIncome
import com.software.sello.domain.port.EditExpense
import com.software.sello.domain.port.EditIncome
import com.software.sello.domain.port.RenameCategory
import com.software.sello.domain.port.RestoreExpense
import com.software.sello.domain.port.RestoreIncome
import com.software.sello.domain.port.SetDefaultLimit
import com.software.sello.domain.port.SetMonthLimit
import com.software.sello.domain.port.UnarchiveCategory
import java.security.MessageDigest

/*
 * The identity of a command's input, used to tell a replay from a conflict. It is a
 * SHA-256 over the fields that carry meaning, each written as its UTF-8 byte length,
 * a colon and its bytes, so no two different inputs can produce the same sequence.
 * Every identity starts with the kind, a format version that changes if that kind's
 * field list ever does, and the generation. The operation identifier is not part of it.
 */

internal fun inputDigest(command: CreateExpense): String = digest(
    OperationKind.CreateExpense.key,
    "1",
    command.generation.toString(),
    command.categoryId.value,
    command.amount.money.minorUnits.toString(),
    command.amount.money.currency.code,
    command.date.toString(),
    if (command.note == null) "0" else "1",
    command.note?.value.orEmpty()
)

internal fun inputDigest(command: EditExpense): String = digest(
    OperationKind.EditExpense.key,
    "1",
    command.generation.toString(),
    command.expenseId.value,
    command.expectedVersion.toString(),
    command.categoryId.value,
    command.amount.money.minorUnits.toString(),
    command.amount.money.currency.code,
    command.date.toString(),
    if (command.note == null) "0" else "1",
    command.note?.value.orEmpty()
)

internal fun inputDigest(command: DeleteExpense): String = digest(
    OperationKind.DeleteExpense.key,
    "1",
    command.generation.toString(),
    command.expenseId.value,
    command.expectedVersion.toString()
)

internal fun inputDigest(command: RestoreExpense): String = digest(
    OperationKind.RestoreExpense.key,
    "1",
    command.generation.toString(),
    command.deletionId.value
)

/** Zero or one name: the key says which, so "other" named "salary" is not salary. */
private val IncomeSource.fields: List<String>
    get() = listOf(key, (this as? IncomeSource.Other)?.name?.value.orEmpty())

internal fun inputDigest(command: CreateIncome): String = digest(
    OperationKind.CreateIncome.key,
    "1",
    command.generation.toString(),
    command.amount.money.minorUnits.toString(),
    command.amount.money.currency.code,
    *command.source.fields.toTypedArray(),
    command.date.toString(),
    if (command.note == null) "0" else "1",
    command.note?.value.orEmpty()
)

internal fun inputDigest(command: EditIncome): String = digest(
    OperationKind.EditIncome.key,
    "1",
    command.generation.toString(),
    command.incomeId.value,
    command.expectedVersion.toString(),
    command.amount.money.minorUnits.toString(),
    command.amount.money.currency.code,
    *command.source.fields.toTypedArray(),
    command.date.toString(),
    if (command.note == null) "0" else "1",
    command.note?.value.orEmpty()
)

internal fun inputDigest(command: DeleteIncome): String = digest(
    OperationKind.DeleteIncome.key,
    "1",
    command.generation.toString(),
    command.incomeId.value,
    command.expectedVersion.toString()
)

internal fun inputDigest(command: RestoreIncome): String = digest(
    OperationKind.RestoreIncome.key,
    "1",
    command.generation.toString(),
    command.deletionId.value
)

internal val CategoryCommand.kind: OperationKind
    get() = when (this) {
        is CreateCategory -> OperationKind.CreateCategory
        is RenameCategory -> OperationKind.RenameCategory
        is ChangeCategoryIcon -> OperationKind.ChangeCategoryIcon
        is ArchiveCategory -> OperationKind.ArchiveCategory
        is UnarchiveCategory -> OperationKind.UnarchiveCategory
        is SetDefaultLimit -> OperationKind.SetDefaultLimit
        is SetMonthLimit -> OperationKind.SetMonthLimit
    }

internal fun inputDigest(command: CategoryCommand): String {
    val subject = when (command) {
        is CategoryEdit -> listOf(command.categoryId.value, command.expectedVersion.toString())
        is CreateCategory -> emptyList()
    }
    val change = when (command) {
        is CreateCategory -> listOf(command.name.value, command.icon.value) + command.limit.fields
        is RenameCategory -> listOf(command.name.value)
        is ChangeCategoryIcon -> listOf(command.icon.value)
        is ArchiveCategory, is UnarchiveCategory -> emptyList()
        is SetDefaultLimit -> command.limit.fields
        is SetMonthLimit -> listOf(command.month.toString()) + command.limit.fields
    }
    val head = listOf(command.kind.key, "1", command.generation.toString())
    return digest(*(head + subject + change).toTypedArray())
}

/** Zero and unlimited must not collide, so the kind is a field of its own. */
private val BudgetLimit.fields: List<String>
    get() = when (this) {
        is BudgetLimit.Finite ->
            listOf("finite", amount.minorUnits.toString(), amount.currency.code)

        BudgetLimit.Unlimited -> listOf("unlimited", "", "")
    }

private fun digest(vararg fields: String): String {
    val sha256 = MessageDigest.getInstance("SHA-256")
    for (field in fields) {
        val bytes = field.toByteArray(Charsets.UTF_8)
        sha256.update("${bytes.size}:".toByteArray(Charsets.UTF_8))
        sha256.update(bytes)
    }
    return sha256.digest().joinToString("") { "%02x".format(it) }
}
