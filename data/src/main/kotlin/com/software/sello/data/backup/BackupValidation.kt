package com.software.sello.data.backup

import com.software.sello.data.repository.MAX_DEFAULT_HISTORY
import com.software.sello.domain.model.AppearancePreference
import com.software.sello.domain.model.AutomaticBudget
import com.software.sello.domain.model.BudgetLimit
import com.software.sello.domain.model.Category
import com.software.sello.domain.model.CategoryId
import com.software.sello.domain.model.CategoryName
import com.software.sello.domain.model.CategoryOrder
import com.software.sello.domain.model.CategoryOrderDirection
import com.software.sello.domain.model.CategoryOrderingPreference
import com.software.sello.domain.model.Currency
import com.software.sello.domain.model.DefaultLimit
import com.software.sello.domain.model.Expense
import com.software.sello.domain.model.ExpenseId
import com.software.sello.domain.model.IconKey
import com.software.sello.domain.model.Income
import com.software.sello.domain.model.IncomeId
import com.software.sello.domain.model.IncomeSource
import com.software.sello.domain.model.InkPreference
import com.software.sello.domain.model.Money
import com.software.sello.domain.model.MonthLimit
import com.software.sello.domain.model.Note
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.ThemePreference
import com.software.sello.domain.model.TransactionAmount
import com.software.sello.domain.model.TransactionAmountError
import com.software.sello.domain.policy.EffectiveDates
import java.time.DateTimeException
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeParseException
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

/*
 * The wire spellings of version 1. They belong to the file, not to Room or to the
 * domain's Kotlin names, so renaming either cannot change what a backup says.
 */
internal object WireNames {
    const val FINITE = "finite"
    const val UNLIMITED = "unlimited"
    const val PAUSED = "paused"

    val themes = mapOf(
        ThemePreference.System to "system",
        ThemePreference.Light to "light",
        ThemePreference.Dark to "dark"
    )
    val inks = mapOf(InkPreference.Cobalto to "cobalto", InkPreference.Violeta to "violeta")
    val orders = mapOf(
        CategoryOrder.MostUsed to "most_used",
        CategoryOrder.Alphabetical to "alphabetical",
        CategoryOrder.AmountSpent to "amount_spent",
        CategoryOrder.Limit to "limit",
        CategoryOrder.LatestExpenseAdded to "latest_expense_added"
    )
    val directions = mapOf(
        CategoryOrderDirection.Descending to "descending",
        CategoryOrderDirection.Ascending to "ascending"
    )
}

/** Thrown and caught inside [validate] only, carrying the first problem found. */
private class Rejected(val rejection: BackupRejection) : RuntimeException() {
    override fun fillInStackTrace(): Throwable = this
}

private fun reject(rejection: BackupRejection): Nothing = throw Rejected(rejection)

private fun invalid(path: String): Nothing = reject(BackupRejection.InvalidValue(path))

private fun <T> Outcome<T, *>.or(path: String): T = when (this) {
    is Outcome.Success -> value
    is Outcome.Failure -> invalid(path)
}

private fun whole(element: JsonElement, path: String): Long =
    element.wholeNumberOrNull() ?: invalid(path)

/** [whole], or an explicit `null`. */
private fun optionalWhole(element: JsonElement, path: String): Long? =
    if (element is JsonNull) null else whole(element, path)

/** A bare `true` or `false`, never the quoted words. */
private fun flag(element: JsonElement, path: String): Boolean {
    val primitive = element as? JsonPrimitive
    if (primitive == null || primitive.isString) invalid(path)
    return when (primitive.content) {
        "true" -> true
        "false" -> false
        else -> invalid(path)
    }
}

/** Checks a decoded version 1 file completely; the first problem rejects all of it. */
internal fun validate(file: BackupFileV1): Outcome<BackupContents, BackupRejection> = try {
    Outcome.Success(contentsOf(file))
} catch (rejected: Rejected) {
    Outcome.Failure(rejected.rejection)
}

private fun contentsOf(file: BackupFileV1): BackupContents {
    val financial = file.financial
    val revision = whole(financial.sourceRevision, "financial.sourceRevision")
    val zone = try {
        ZoneId.of(financial.zone).also { if (it.id != financial.zone) invalid("financial.zone") }
    } catch (_: DateTimeException) {
        invalid("financial.zone")
    }
    val categories = categories(financial.categories)
    val known = categories.mapTo(HashSet()) { it.id }
    return BackupContents(
        exportedAt = instant(file.exportedAt, "exportedAt"),
        financial = FinancialSnapshot(
            zone = zone,
            revision = revision,
            categories = categories,
            defaultLimits = defaultLimits(financial.defaultLimits, known),
            monthLimits = monthLimits(financial.monthLimits, known),
            expenses = expenses(financial.expenses, known),
            incomes = incomes(financial.incomes)
        ),
        preferences = file.preferences?.let(::preferences)
    )
}

private const val NANOS_PER_MILLI = 1_000_000

/** Exactly the text [Instant.toString] writes, so one moment has one spelling. */
private fun instant(text: String, path: String): Instant {
    val parsed = try {
        Instant.parse(text)
    } catch (_: DateTimeParseException) {
        invalid(path)
    }
    // Stored time keeps milliseconds, so finer time could not be restored as written.
    if (parsed.toString() != text || parsed.nano % NANOS_PER_MILLI != 0) invalid(path)
    return parsed
}

private fun recordId(text: String, path: String) = CategoryId.of(text).or(path)

private fun categories(wire: List<CategoryV1>): List<Category> {
    val ids = HashSet<CategoryId>()
    val names = HashSet<String>()
    return wire.mapIndexed { index, row ->
        val path = "financial.categories[$index]"
        val id = recordId(row.id, "$path.id")
        val name = CategoryName.of(row.name).or("$path.name")
        // A name must already be in the form the app stores, or two spellings of one
        // name could slip past the uniqueness check.
        if (name.value != row.name) invalid("$path.name")
        if (!ids.add(id)) reject(BackupRejection.DuplicateId("$path.id"))
        if (!names.add(name.uniquenessKey)) {
            reject(BackupRejection.DuplicateCategoryName("$path.name"))
        }
        Category(
            id = id,
            name = name,
            icon = IconKey.of(row.icon).or("$path.icon"),
            archived = flag(row.archived, "$path.archived"),
            version = 1,
            createdAt = instant(row.createdAt, "$path.createdAt"),
            updatedAt = instant(row.updatedAt, "$path.updatedAt")
        )
    }
}

private fun reference(text: String, known: Set<CategoryId>, path: String): CategoryId {
    val id = recordId(text, path)
    if (id !in known) reject(BackupRejection.DanglingReference(path))
    return id
}

private fun month(text: String, path: String): YearMonth = EffectiveDates.parseMonth(text).or(path)

private fun currency(code: String, path: String): Currency {
    val currency = Currency.entries.firstOrNull { it.code == code }
    if (currency == null || !currency.isMvpEntryCurrency) {
        reject(BackupRejection.UnsupportedCurrency(path))
    }
    return currency
}

/** A limit's amount and currency come together, and only for a finite limit. */
private fun limit(
    kind: String,
    rawAmount: JsonElement,
    currencyCode: String?,
    path: String
): BudgetLimit {
    val amount = optionalWhole(rawAmount, "$path.amount")
    return when (kind) {
        WireNames.FINITE -> {
            if (amount == null) invalid("$path.amount")
            if (currencyCode == null) invalid("$path.currency")
            val money = Money(amount, currency(currencyCode, "$path.currency"))
            BudgetLimit.finite(money).or("$path.amount")
        }

        WireNames.UNLIMITED -> {
            if (amount != null) invalid("$path.amount")
            if (currencyCode != null) invalid("$path.currency")
            BudgetLimit.Unlimited
        }

        else -> invalid("$path.kind")
    }
}

private fun defaultLimits(wire: List<DefaultLimitV1>, known: Set<CategoryId>): List<DefaultLimit> {
    val entries = HashSet<Pair<CategoryId, YearMonth>>()
    val perCategory = HashMap<CategoryId, Int>()
    return wire.mapIndexed { index, row ->
        val path = "financial.defaultLimits[$index]"
        val categoryId = reference(row.categoryId, known, "$path.categoryId")
        val from = month(row.fromMonth, "$path.from")
        if (!entries.add(categoryId to from)) reject(BackupRejection.DuplicateEntry(path))
        val count = perCategory.merge(categoryId, 1, Int::plus)!!
        if (count > MAX_DEFAULT_HISTORY) reject(BackupRejection.HistoryTooLong(path))
        val budget = if (row.kind == WireNames.PAUSED) {
            if (row.amount !is JsonNull) invalid("$path.amount")
            if (row.currency != null) invalid("$path.currency")
            AutomaticBudget.Paused
        } else {
            AutomaticBudget.Limit(limit(row.kind, row.amount, row.currency, path))
        }
        DefaultLimit(categoryId, from, budget)
    }
}

private fun monthLimits(wire: List<MonthLimitV1>, known: Set<CategoryId>): List<MonthLimit> {
    val entries = HashSet<Pair<CategoryId, YearMonth>>()
    return wire.mapIndexed { index, row ->
        val path = "financial.monthLimits[$index]"
        val categoryId = reference(row.categoryId, known, "$path.categoryId")
        val month = month(row.month, "$path.month")
        if (!entries.add(categoryId to month)) reject(BackupRejection.DuplicateEntry(path))
        MonthLimit(categoryId, month, limit(row.kind, row.amount, row.currency, path))
    }
}

/** One expense or income amount: an amount outside the range rejects the file (ADR 0003). */
private fun amount(units: JsonElement, currencyCode: String, path: String): TransactionAmount {
    val money = Money(whole(units, "$path.amount"), currency(currencyCode, "$path.currency"))
    return when (val amount = TransactionAmount.of(money)) {
        is Outcome.Success -> amount.value

        is Outcome.Failure -> when (amount.error) {
            TransactionAmountError.NotPositive, TransactionAmountError.AboveMaximum ->
                reject(BackupRejection.AmountOutOfRange("$path.amount"))

            is TransactionAmountError.UnsupportedCurrency ->
                reject(BackupRejection.UnsupportedCurrency("$path.currency"))
        }
    }
}

/** Absent, or exactly the text the note rule stores; an empty note is written as null. */
private fun note(text: String?, path: String): Note? {
    if (text == null) return null
    val note = Note.of(text).or(path)
    if (note?.value != text) invalid(path)
    return note
}

private fun expenses(wire: List<ExpenseV1>, known: Set<CategoryId>): List<Expense> {
    val ids = HashSet<String>()
    val sequences = HashSet<Long>()
    return wire.mapIndexed { index, row ->
        val path = "financial.expenses[$index]"
        val id = ExpenseId.of(row.id).or("$path.id")
        if (!ids.add(id.value)) reject(BackupRejection.DuplicateId("$path.id"))
        val sequence = whole(row.sequence, "$path.sequence")
        if (!sequences.add(sequence)) reject(BackupRejection.DuplicateEntry("$path.sequence"))
        Expense(
            id = id,
            categoryId = reference(row.categoryId, known, "$path.categoryId"),
            date = EffectiveDates.parseDate(row.date).or("$path.date"),
            sequence = sequence,
            amount = amount(row.amount, row.currency, path),
            note = note(row.note, "$path.note"),
            version = 1,
            createdAt = instant(row.createdAt, "$path.createdAt"),
            updatedAt = instant(row.updatedAt, "$path.updatedAt")
        )
    }
}

private fun incomes(wire: List<IncomeV1>): List<Income> {
    val ids = HashSet<String>()
    val sequences = HashSet<Long>()
    return wire.mapIndexed { index, row ->
        val path = "financial.incomes[$index]"
        val id = IncomeId.of(row.id).or("$path.id")
        if (!ids.add(id.value)) reject(BackupRejection.DuplicateId("$path.id"))
        val sequence = whole(row.sequence, "$path.sequence")
        if (!sequences.add(sequence)) reject(BackupRejection.DuplicateEntry("$path.sequence"))
        if (row.source !in IncomeSource.keys) invalid("$path.source")
        val source = IncomeSource.of(row.source, row.sourceName).or("$path.sourceName")
        if ((source as? IncomeSource.Other)?.name?.value != row.sourceName) {
            invalid("$path.sourceName")
        }
        Income(
            id = id,
            date = EffectiveDates.parseDate(row.date).or("$path.date"),
            sequence = sequence,
            amount = amount(row.amount, row.currency, path),
            source = source,
            note = note(row.note, "$path.note"),
            version = 1,
            createdAt = instant(row.createdAt, "$path.createdAt"),
            updatedAt = instant(row.updatedAt, "$path.updatedAt")
        )
    }
}

private fun <T> Map<T, String>.decode(text: String, path: String): T =
    entries.firstOrNull { it.value == text }?.key ?: invalid(path)

private fun preferences(wire: PreferencesV1) = PortablePreferences(
    appearance = AppearancePreference(
        theme = WireNames.themes.decode(wire.theme, "preferences.theme"),
        ink = WireNames.inks.decode(wire.ink, "preferences.ink")
    ),
    ordering = CategoryOrderingPreference(
        order = WireNames.orders.decode(wire.categoryOrder, "preferences.categoryOrder"),
        direction = WireNames.directions.decode(
            wire.categoryDirection,
            "preferences.categoryDirection"
        )
    )
)
