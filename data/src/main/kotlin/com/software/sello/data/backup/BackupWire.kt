package com.software.sello.data.backup

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive

/*
 * The version 1 wire shape, documented in docs/data/backup-format.md. These classes
 * are the file and nothing else: they never become Room entities or domain values
 * without going through BackupValidation. Every property is required, including the
 * nullable ones, and the JSON configuration refuses unknown keys. Numbers and flags
 * are kept as raw JSON for validation to check strictly.
 */

/** Only the two keys every version must keep, read before the rest of the file. */
@Serializable
internal data class BackupHeaderWire(
    val format: JsonElement? = null,
    val version: JsonElement? = null
)

@Serializable
internal data class BackupFileV1(
    val format: String,
    val version: JsonElement,
    val exportedAt: String,
    val financial: FinancialV1,
    val preferences: PreferencesV1?
)

@Serializable
internal data class FinancialV1(
    val sourceRevision: JsonElement,
    val zone: String,
    val categories: List<CategoryV1>,
    val defaultLimits: List<DefaultLimitV1>,
    val monthLimits: List<MonthLimitV1>,
    val expenses: List<ExpenseV1>,
    val incomes: List<IncomeV1>
)

@Serializable
internal data class CategoryV1(
    val id: String,
    val name: String,
    val icon: String,
    val archived: JsonElement,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
internal data class DefaultLimitV1(
    val categoryId: String,
    @SerialName("from") val fromMonth: String,
    val kind: String,
    val amount: JsonElement,
    val currency: String?
)

@Serializable
internal data class MonthLimitV1(
    val categoryId: String,
    val month: String,
    val kind: String,
    val amount: JsonElement,
    val currency: String?
)

@Serializable
internal data class ExpenseV1(
    val id: String,
    val categoryId: String,
    val date: String,
    val sequence: JsonElement,
    val amount: JsonElement,
    val currency: String,
    val note: String?,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
internal data class IncomeV1(
    val id: String,
    val date: String,
    val sequence: JsonElement,
    val amount: JsonElement,
    val currency: String,
    val source: String,
    val sourceName: String?,
    val note: String?,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
internal data class PreferencesV1(
    val theme: String,
    val ink: String,
    val categoryOrder: String,
    val categoryDirection: String
)

private val plainWhole = Regex("0|[1-9][0-9]*")

/**
 * A bare, non-negative JSON integer: `125000`. The JSON library alone would also take
 * `"125000"`, `1.25e5` and `0125000` as a number, so numbers stay raw JSON until
 * validation checks the literal as written and knows where in the file it is.
 */
internal fun JsonElement.wholeNumberOrNull(): Long? {
    val primitive = this as? JsonPrimitive ?: return null
    if (primitive.isString || !plainWhole.matches(primitive.content)) return null
    return primitive.content.toLongOrNull()
}
