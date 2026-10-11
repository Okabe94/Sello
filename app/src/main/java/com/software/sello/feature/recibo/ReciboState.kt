package com.software.sello.feature.recibo

import com.software.sello.designsystem.component.MoneyTextValue

/** What Recibo shows. A failed read is its own state; it is never shown as zero. */
sealed interface ReciboState {
    data object Loading : ReciboState

    /** Read successfully and there are no categories yet. */
    data object FirstRun : ReciboState

    /**
     * A month that was read. With [stale] the latest read failed and this is the last
     * one that worked, shown as older data and never as current.
     */
    data class Month(
        val summary: ReciboSummary,
        val categories: List<ReciboCategory>,
        val stale: Boolean
    ) : ReciboState

    /** The read failed and there is nothing earlier to show. */
    data object Failed : ReciboState
}

/** Which sentence an amount completes. */
enum class ReciboHeadline {
    /** "Te quedan": what is left of the limit in the month in progress. */
    Remaining,

    /** "Te pasaste por": how far over the limit, in any month. */
    Overspent,

    /** "Te sobraron": what was left of the limit in a finished month. */
    LeftOver,

    /** "Has gastado": there is no limit to compare with. */
    Spent
}

/** The verdict, in words on the screen and in what a screen reader says. */
enum class ReciboStamp {
    OnTrack,
    Tight,
    Over,

    /** A finished month that stayed within its limit. */
    WithinLimit,

    /** A finished month that went over its limit. */
    OverLimit,
    NoLimit,

    /** Archived that month: no budget. */
    Paused,

    /** Nothing was configured for the category that month. */
    NotSet
}

/** An amount with the sentence it completes and its verdict. [amount] carries no sign. */
data class BudgetFigure(
    val headline: ReciboHeadline,
    val amount: MoneyTextValue,
    val stamp: ReciboStamp
)

/**
 * The month as a whole. [days] and [perDay] exist only in the month in progress with a
 * limit; [perDayExhausted] says the limit is already passed, so nothing is left per day.
 */
data class ReciboSummary(
    val figure: BudgetFigure,
    val days: Int?,
    val perDay: MoneyTextValue?,
    val perDayExhausted: Boolean
)

/**
 * One category in the month. [cellAmount] is what its cell shows: what is left, with a
 * minus when overspent, or what was spent when it has no limit. [fraction] is how full
 * to draw its circle and nothing else; every amount here is exact.
 */
data class ReciboCategory(
    val id: String,
    val name: String,
    val iconKey: String,
    val archived: Boolean,
    val figure: BudgetFigure,
    val cellAmount: MoneyTextValue,
    val spent: MoneyTextValue,
    val limit: MoneyTextValue?,
    val fraction: Float?,
    val over: Boolean,
    val expenseCount: Int
)

sealed interface ReciboAction {
    data object Retry : ReciboAction

    /** Open the new-category form. Navigation is the root's, not the view model's. */
    data object CreateCategory : ReciboAction

    /** Show one category's detail. Which one is shown is the frame's to remember. */
    data class OpenCategory(val id: String) : ReciboAction
}
