package com.software.sello.domain.port

import com.software.sello.domain.model.CategoryId
import com.software.sello.domain.model.Expense
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure
import java.time.YearMonth

enum class ExpenseOrder { Date, Amount, Name }

data class ExpenseSearchPage(
    val expenses: List<Expense>,
    val more: Boolean,
    val generation: Long,
    val revision: Long,
    val totalCount: Long
)

/** Search applies to the whole selected category/month, before bounded paging. */
interface CategoryExpenseSearch {
    suspend fun read(
        categoryId: CategoryId,
        month: YearMonth,
        order: ExpenseOrder,
        query: String,
        offset: Int,
        limit: Int
    ): Outcome<ExpenseSearchPage, StorageFailure>
}
