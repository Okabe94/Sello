package com.software.sello.feature.recibo

import com.software.sello.designsystem.component.MoneyTextValue
import java.time.LocalDate
import java.time.YearMonth

/** One category in the selected month, and the expenses behind its figures. */
sealed interface CategoryDetailState {
    /** No category is being shown. */
    data object Closed : CategoryDetailState

    data object Loading : CategoryDetailState

    /**
     * [category] comes from the same snapshot Recibo shows, so the two agree. With
     * [stale] it is the last month that could be read, not the current one. [today]
     * is the day the figures are as of.
     */
    data class Ready(
        val category: ReciboCategory,
        val month: YearMonth,
        val today: LocalDate,
        val stale: Boolean,
        val canRecord: Boolean,
        val expenses: ExpenseListState
    ) : CategoryDetailState

    /** The month was read and this category has nothing in it. */
    data object Gone : CategoryDetailState

    data object Failed : CategoryDetailState
}

/** The expenses listed. An empty list and a list that could not be read are different. */
sealed interface ExpenseListState {
    data object Loading : ExpenseListState

    /** Read successfully: there are none. */
    data object Empty : ExpenseListState

    /**
     * The newest [rows]; with [more] there are older ones not listed yet, so these
     * rows are never the source of a total. [moreFailed] says the last attempt to list
     * more did not work.
     */
    data class Rows(
        val rows: List<ExpenseRow>,
        val more: Boolean,
        val loadingMore: Boolean = false,
        val moreFailed: Boolean = false
    ) : ExpenseListState

    data object Failed : ExpenseListState
}

/** [notCountedYet] is an expense dated after today: listed, and not yet in the figures. */
data class ExpenseRow(
    val id: String,
    val date: LocalDate,
    val note: String?,
    val amount: MoneyTextValue,
    val notCountedYet: Boolean
)

sealed interface CategoryDetailAction {
    /** Show this category, or none. */
    data class Show(val categoryId: String?) : CategoryDetailAction

    data object Retry : CategoryDetailAction

    /** List the next older expenses. */
    data object ShowMore : CategoryDetailAction
}
