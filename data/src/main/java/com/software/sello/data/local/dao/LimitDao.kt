package com.software.sello.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
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

    /** Inserts the entry, or replaces the one for the same category and month. */
    @Upsert
    suspend fun put(limit: DefaultLimitEntity)

    @Upsert
    suspend fun put(limit: MonthLimitEntity)

    @Query("DELETE FROM default_limit WHERE category_id = :categoryId AND effective_month = :month")
    suspend fun removeDefault(categoryId: String, month: String): Int

    /**
     * For each category after [afterCategoryId], the default in force in [month]: its
     * latest entry effective that month or earlier. A category with none is absent.
     */
    @Query(
        "SELECT d.* FROM default_limit d WHERE d.category_id > :afterCategoryId " +
            "AND d.effective_month = (SELECT MAX(e.effective_month) FROM default_limit e " +
            "WHERE e.category_id = d.category_id AND e.effective_month <= :month) " +
            "ORDER BY d.category_id LIMIT :limit"
    )
    suspend fun inForce(
        month: String,
        afterCategoryId: String,
        limit: Int
    ): List<DefaultLimitEntity>

    /** The limits set for [month] itself, after [afterCategoryId] in category order. */
    @Query(
        "SELECT * FROM month_limit WHERE month = :month AND category_id > :afterCategoryId " +
            "ORDER BY category_id LIMIT :limit"
    )
    suspend fun forMonth(month: String, afterCategoryId: String, limit: Int): List<MonthLimitEntity>
}
