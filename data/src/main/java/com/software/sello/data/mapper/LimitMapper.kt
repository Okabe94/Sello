package com.software.sello.data.mapper

import com.software.sello.data.local.entity.DefaultLimitEntity
import com.software.sello.data.local.entity.LimitKind
import com.software.sello.data.local.entity.MonthLimitEntity
import com.software.sello.domain.model.BudgetLimit
import com.software.sello.domain.model.CategoryId
import com.software.sello.domain.model.Currency
import com.software.sello.domain.model.DefaultLimit
import com.software.sello.domain.model.Money
import com.software.sello.domain.model.MonthLimit
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure
import com.software.sello.domain.policy.EffectiveDates

private val BudgetLimit.kind
    get() = when (this) {
        is BudgetLimit.Finite -> LimitKind.FINITE
        BudgetLimit.Unlimited -> LimitKind.UNLIMITED
    }

private val BudgetLimit.minor get() = (this as? BudgetLimit.Finite)?.amount?.minorUnits

// An unlimited entry has no amount but still says which currency the category budgets in.
private val BudgetLimit.currencyCode
    get() = ((this as? BudgetLimit.Finite)?.amount?.currency ?: Currency.COP).code

private fun RowDecoder.limit(kind: String, minor: Long?, currencyCode: String): BudgetLimit {
    val currency = currency("currency", currencyCode)
    check("currency", currency.isMvpEntryCurrency)
    return when (kind) {
        LimitKind.FINITE -> {
            check("limit_minor", minor != null)
            valid("limit_minor", BudgetLimit.finite(Money(minor!!, currency)))
        }

        LimitKind.UNLIMITED -> {
            check("limit_minor", minor == null)
            BudgetLimit.Unlimited
        }

        else -> reject("kind")
    }
}

internal fun DefaultLimit.toEntity() = DefaultLimitEntity(
    categoryId = categoryId.value,
    effectiveMonth = effectiveMonth.toString(),
    kind = limit.kind,
    limitMinor = limit.minor,
    currency = limit.currencyCode
)

internal fun MonthLimit.toEntity() = MonthLimitEntity(
    categoryId = categoryId.value,
    month = month.toString(),
    kind = limit.kind,
    limitMinor = limit.minor,
    currency = limit.currencyCode
)

internal fun DefaultLimitEntity.toDomain(): Outcome<DefaultLimit, StorageFailure.Integrity> =
    decodeRow(DefaultLimitEntity.TABLE, "$categoryId/$effectiveMonth") {
        DefaultLimit(
            categoryId = valid("category_id", CategoryId.of(categoryId)),
            effectiveMonth = valid("effective_month", EffectiveDates.parseMonth(effectiveMonth)),
            limit = limit(kind, limitMinor, currency)
        )
    }

internal fun MonthLimitEntity.toDomain(): Outcome<MonthLimit, StorageFailure.Integrity> =
    decodeRow(MonthLimitEntity.TABLE, "$categoryId/$month") {
        MonthLimit(
            categoryId = valid("category_id", CategoryId.of(categoryId)),
            month = valid("month", EffectiveDates.parseMonth(month)),
            limit = limit(kind, limitMinor, currency)
        )
    }
