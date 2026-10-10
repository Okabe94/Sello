package com.software.sello.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.software.sello.data.local.entity.CategoryEntity

@Dao
interface CategoryDao {
    /** Fails on a duplicate identifier or name key; nothing is replaced. */
    @Insert
    suspend fun insert(category: CategoryEntity)

    @Query("SELECT * FROM category WHERE id = :id")
    suspend fun byId(id: String): CategoryEntity?

    /** Categories after [afterId] in identifier order, at most [limit]. */
    @Query("SELECT * FROM category WHERE id > :afterId ORDER BY id LIMIT :limit")
    suspend fun page(afterId: String, limit: Int): List<CategoryEntity>
}
