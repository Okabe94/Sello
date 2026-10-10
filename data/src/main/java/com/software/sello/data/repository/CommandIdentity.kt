package com.software.sello.data.repository

import com.software.sello.domain.model.OperationKind
import com.software.sello.domain.port.CreateExpense
import java.security.MessageDigest

/**
 * The identity of a command's input, used to tell a replay from a conflict. It is a
 * SHA-256 over the fields that carry meaning, each written as its UTF-8 byte length,
 * a colon and its bytes, so no two different inputs can produce the same sequence.
 * The operation identifier is not part of it. The leading version changes if the
 * field list ever does.
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

private fun digest(vararg fields: String): String {
    val sha256 = MessageDigest.getInstance("SHA-256")
    for (field in fields) {
        val bytes = field.toByteArray(Charsets.UTF_8)
        sha256.update("${bytes.size}:".toByteArray(Charsets.UTF_8))
        sha256.update(bytes)
    }
    return sha256.digest().joinToString("") { "%02x".format(it) }
}
