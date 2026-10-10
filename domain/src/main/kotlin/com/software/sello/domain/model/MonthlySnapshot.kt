package com.software.sello.domain.model

import com.software.sello.domain.policy.MonthBudgetState
import java.math.BigInteger
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

/**
 * Estimated spending for the whole month at the pace recorded so far, kept as the
 * exact fraction [numerator] / [denominator] pesos. Verdicts compare this exact
 * value; [rounded] is only for showing it and is never compared.
 */
data class Forecast(val numerator: BigInteger, val denominator: Int, val rounded: Money)

/** The current month, judged on the forecast. Actual overspending is always [Over]. */
enum class Verdict {
    /** Forecast at or below 95% of the limit. "Al día". */
    OnTrack,

    /** Forecast above 95% of the limit and not above the limit. "Justo". */
    Tight,

    /** Spent or forecast above the limit. "Pasado". */
    Over
}

/** A finished month, judged on what was actually spent. */
enum class PastVerdict {
    /** "Bajo el límite". Spending exactly the limit is within it. */
    WithinLimit,

    /** "Pasado". */
    Over
}

sealed interface BudgetStanding {
    /** The month in progress: an estimate, to be labelled as one. */
    data class Current(val forecast: Forecast, val verdict: Verdict) : BudgetStanding

    /** A finished month: actuals only, no forecast and no allowance. */
    data class Past(val verdict: PastVerdict) : BudgetStanding
}

/** A finite limit against spending. [remaining] is negative when overspent; it is never clamped. */
data class LimitedBudget(
    val limit: Money,
    val spent: Money,
    val remaining: Money,
    val standing: BudgetStanding
)

/**
 * What can be spent per day from today to the end of the month, today included.
 * [amount] is rounded down to a multiple of 50 pesos and is zero when nothing remains.
 * It is a planning figure: it is not recorded money and nothing else is rounded like it.
 */
data class DailyAllowance(val amount: Money, val availableDays: Int)

/** The month as a whole. */
sealed interface OverallBudget {
    /** No category has a finite limit this month: "Has gastado / Sin límite". Not a zero budget. */
    data object NoLimit : OverallBudget

    /**
     * [budget] is the sum of the finite limits against everything spent, including
     * spending in categories without a limit. [allowance] exists only in the current month.
     */
    data class Limited(val budget: LimitedBudget, val allowance: DailyAllowance?) : OverallBudget
}

/**
 * One category in the month. [budget] is present only when the category has a finite
 * limit that month; its spending counts towards the month's total either way.
 */
data class CategoryMonth(
    val category: Category,
    val state: MonthBudgetState,
    val spent: Money,
    val budget: LimitedBudget?
)

/**
 * Everything the screens show about one month, worked out once from rows read
 * together at [revision] of [generation], as of the financial day [asOf] in [zone].
 *
 * [spent] and every figure here count expenses dated up to [asOf] only;
 * [futureDatedExpenses] says how many later ones in the month were left out. A
 * snapshot kept after a failed refresh still carries its own [asOf] and [revision],
 * so it can be shown as what it is.
 */
data class MonthlySnapshot(
    val month: YearMonth,
    val asOf: LocalDate,
    val zone: ZoneId,
    val generation: Long,
    val revision: Long,
    val categories: List<CategoryMonth>,
    val spent: Money,
    val overall: OverallBudget,
    val futureDatedExpenses: Int
)

sealed interface SnapshotFailure {
    data class Storage(val failure: StorageFailure) : SnapshotFailure

    /** A total does not fit an exact amount. It is reported, never clipped or approximated. */
    data object Unrepresentable : SnapshotFailure

    /** Months after the current financial month cannot be viewed. */
    data class FutureMonth(val currentMonth: YearMonth) : SnapshotFailure
}
