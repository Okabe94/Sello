package com.software.sello.feature.recibo

import com.software.sello.designsystem.component.MoneyTextValue

/** What Recibo shows. A failed read is its own state; it is never shown as zero. */
sealed interface ReciboState {
    data object Loading : ReciboState

    /** Read successfully and there are no categories yet. */
    data object FirstRun : ReciboState

    /** Read successfully. [spent] is the month's exact total. */
    data class Spending(val spent: MoneyTextValue, val categories: List<ReciboCategory>) :
        ReciboState

    data object Failed : ReciboState
}

/** One category as Recibo lists it. [iconKey] is the stored key; the screen picks the drawing. */
data class ReciboCategory(
    val id: String,
    val name: String,
    val iconKey: String,
    val archived: Boolean,
    val limit: ReciboLimit
)

sealed interface ReciboLimit {
    data class Amount(val value: MoneyTextValue) : ReciboLimit

    data object Unlimited : ReciboLimit

    /** Archived that month: no budget. */
    data object Paused : ReciboLimit

    /** Nothing was configured for the category that month. */
    data object NotSet : ReciboLimit
}

sealed interface ReciboAction {
    data object Retry : ReciboAction

    /** Open the new-category form. Navigation is the root's, not the view model's. */
    data object CreateCategory : ReciboAction
}
