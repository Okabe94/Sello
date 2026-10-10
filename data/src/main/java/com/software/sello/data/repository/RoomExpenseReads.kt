package com.software.sello.data.repository

import com.software.sello.data.local.SelloDatabase
import com.software.sello.data.local.entity.ExpenseEntity
import com.software.sello.data.mapper.toDomain
import com.software.sello.domain.model.CategoryId
import com.software.sello.domain.model.Expense
import com.software.sello.domain.model.ExpenseId
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure
import com.software.sello.domain.port.ExpenseReads

class RoomExpenseReads(private val database: SelloDatabase) : ExpenseReads {
    override suspend fun byId(id: ExpenseId): Outcome<Expense?, StorageFailure> = reading {
        when (val row = database.expenseDao().byId(id.value)) {
            null -> Outcome.Success(null)
            else -> row.toDomain()
        }
    }

    /** Sequences only grow, so the highest one is the expense added last. */
    override suspend fun lastUsedCategory(): Outcome<CategoryId?, StorageFailure> = reading {
        when (val stored = database.expenseDao().lastAddedCategory()) {
            null -> Outcome.Success(null)

            else -> when (val id = CategoryId.of(stored)) {
                is Outcome.Success -> id

                is Outcome.Failure -> Outcome.Failure(
                    StorageFailure.Integrity(ExpenseEntity.TABLE, null, "category_id")
                )
            }
        }
    }
}
