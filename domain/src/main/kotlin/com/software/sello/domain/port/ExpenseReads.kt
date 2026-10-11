package com.software.sello.domain.port

import com.software.sello.domain.model.CategoryId
import com.software.sello.domain.model.Expense
import com.software.sello.domain.model.ExpenseId
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure
import java.time.LocalDate
import java.time.YearMonth

/** Where a page of expenses ended, to ask for the next one. */
data class ExpenseCursor(val date: LocalDate, val sequence: Long)

/**
 * Some of a category's expenses, newest first. [next] is null when these are the
 * last; otherwise it is what to pass to get the following page. A page is never the
 * whole answer unless [next] is null, and no total is ever worked out from a page.
 */
data class ExpensePage(val expenses: List<Expense>, val next: ExpenseCursor?)

interface ExpenseReads {
    /** The stored expense, exactly as recorded, or null when there is none with that identifier. */
    suspend fun byId(id: ExpenseId): Outcome<Expense?, StorageFailure>

    /**
     * Up to [limit] expenses of [categoryId] dated in [month], newest day first and,
     * within a day, the one added last first, starting after [after].
     */
    suspend fun inCategory(
        categoryId: CategoryId,
        month: YearMonth,
        after: ExpenseCursor?,
        limit: Int
    ): Outcome<ExpensePage, StorageFailure>

    /** The category of the expense added most recently, or null when there are no expenses. */
    suspend fun lastUsedCategory(): Outcome<CategoryId?, StorageFailure>
}
