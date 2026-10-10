package com.software.sello.domain.port

import com.software.sello.domain.model.CategoryId
import com.software.sello.domain.model.Expense
import com.software.sello.domain.model.ExpenseId
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure

interface ExpenseReads {
    /** The stored expense, exactly as recorded, or null when there is none with that identifier. */
    suspend fun byId(id: ExpenseId): Outcome<Expense?, StorageFailure>

    /** The category of the expense added most recently, or null when there are no expenses. */
    suspend fun lastUsedCategory(): Outcome<CategoryId?, StorageFailure>
}
