package com.software.sello.data.repository

import androidx.room.withTransaction
import com.software.sello.data.local.SelloDatabase
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure
import com.software.sello.domain.port.CategoryReads
import com.software.sello.domain.port.MonthBudget
import java.time.YearMonth

class RoomCategoryReads(private val database: SelloDatabase) : CategoryReads {
    /** One transaction, so categories, defaults, month limits and revision belong together. */
    override suspend fun monthBudget(month: YearMonth): Outcome<MonthBudget, StorageFailure> =
        reading { database.withTransaction { database.monthBudget(month) } }
}
