package com.software.sello.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.software.sello.data.local.entity.ExpenseEntity

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insert(expense: ExpenseEntity)

    /** The highest sequence in use, or null when there is no expense. */
    @Query("SELECT MAX(sequence) FROM expense")
    suspend fun highestSequence(): Long?

    @Query("SELECT * FROM expense WHERE id = :id")
    suspend fun byId(id: String): ExpenseEntity?

    /**
     * Expenses dated from [from] up to but not including [until], in date then
     * sequence order, starting after the position ([afterDate], [afterSequence]) and
     * returning at most [limit]. Pass the last row of one page to get the next.
     */
    @Query(
        "SELECT * FROM expense WHERE effective_date >= :from AND effective_date < :until " +
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
    ): List<ExpenseEntity>
}
