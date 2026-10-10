package com.software.sello.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.software.sello.data.local.entity.DefaultLimitEntity
import com.software.sello.data.local.entity.MonthLimitEntity

@Dao
interface LimitDao {
    @Insert
    suspend fun insert(limit: DefaultLimitEntity)

    @Insert
    suspend fun insert(limit: MonthLimitEntity)

    /** A category's default-limit history, oldest first, at most [limit] entries. */
    @Query(
        "SELECT * FROM default_limit WHERE category_id = :categoryId " +
            "ORDER BY effective_month LIMIT :limit"
    )
    suspend fun defaults(categoryId: String, limit: Int): List<DefaultLimitEntity>

    /** The limits set for [month], in category order, at most [limit]. */
    @Query("SELECT * FROM month_limit WHERE month = :month ORDER BY category_id LIMIT :limit")
    suspend fun forMonth(month: String, limit: Int): List<MonthLimitEntity>
}
