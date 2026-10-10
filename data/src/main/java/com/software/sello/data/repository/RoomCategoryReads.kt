package com.software.sello.data.repository

import androidx.room.withTransaction
import com.software.sello.data.local.SelloDatabase
import com.software.sello.data.mapper.decodeAll
import com.software.sello.data.mapper.profileFrom
import com.software.sello.data.mapper.toDomain
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure
import com.software.sello.domain.policy.CategoryBudgetPolicy
import com.software.sello.domain.port.CategoryBudget
import com.software.sello.domain.port.CategoryReads
import com.software.sello.domain.port.MonthBudget
import java.time.YearMonth

class RoomCategoryReads(private val database: SelloDatabase) : CategoryReads {
    /**
     * One transaction, so the categories, defaults, month limits and revision all
     * belong to the same moment. Rows are fetched a page at a time; a damaged row
     * fails the whole read.
     */
    override suspend fun monthBudget(month: YearMonth): Outcome<MonthBudget, StorageFailure> =
        reading { database.withTransaction { read(month) } }

    private suspend fun read(month: YearMonth): Outcome<MonthBudget, StorageFailure> {
        val profile = when (val stored = profileFrom(database.profileDao().rows())) {
            is Outcome.Success -> stored.value
            is Outcome.Failure -> return stored
        }
        val key = month.toString()
        val categories = when (
            val rows = pages({ it.id }) { after -> database.categoryDao().page(after, PAGE) }
                .decodeAll { it.toDomain() }
        ) {
            is Outcome.Success -> rows.value
            is Outcome.Failure -> return rows
        }
        val defaults = when (
            val rows = pages({ it.categoryId }) { after ->
                database.limitDao().inForce(key, after, PAGE)
            }.decodeAll { it.toDomain() }
        ) {
            is Outcome.Success -> rows.value.associateBy { it.categoryId }
            is Outcome.Failure -> return rows
        }
        val overrides = when (
            val rows = pages({ it.categoryId }) { after ->
                database.limitDao().forMonth(key, after, PAGE)
            }.decodeAll { it.toDomain() }
        ) {
            is Outcome.Success -> rows.value.associateBy { it.categoryId }
            is Outcome.Failure -> return rows
        }
        return Outcome.Success(
            MonthBudget(
                month = month,
                generation = profile.generation,
                revision = profile.revision,
                categories = categories.map { category ->
                    val state = CategoryBudgetPolicy.stateFor(
                        month,
                        listOfNotNull(defaults[category.id]),
                        overrides[category.id]
                    )
                    CategoryBudget(category, state)
                }
            )
        )
    }

    /** Reads every row in key order, [PAGE] at a time, continuing after the last key seen. */
    private suspend fun <E> pages(key: (E) -> String, page: suspend (String) -> List<E>): List<E> {
        val all = mutableListOf<E>()
        var after = ""
        while (true) {
            val rows = page(after)
            all += rows
            if (rows.size < PAGE) return all
            after = key(rows.last())
        }
    }

    private companion object {
        const val PAGE = 200
    }
}
