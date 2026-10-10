package com.software.sello.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.software.sello.data.local.entity.IncomeEntity

@Dao
interface IncomeDao {
    @Insert
    suspend fun insert(income: IncomeEntity)

    /** Same paging contract as [ExpenseDao.page]. */
    @Query(
        "SELECT * FROM income WHERE effective_date >= :from AND effective_date < :until " +
            "AND (effective_date > :afterDate " +
            "OR (effective_date = :afterDate AND sequence > :afterSequence)) " +
            "ORDER BY effective_date, sequence LIMIT :limit"
    )
    suspend fun page(
        from: String,
        until: String,
        afterDate: String,
        afterSequence: Long,
        limit: Int
    ): List<IncomeEntity>
}
