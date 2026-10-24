package com.software.sello.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.software.sello.data.local.entity.GoalEntity
import com.software.sello.data.local.entity.GoalEventEntity

@Dao
interface GoalDao {
    @Insert
    suspend fun insert(goal: GoalEntity)

    @Insert
    suspend fun insertEvent(event: GoalEventEntity)

    @Query("SELECT * FROM goal WHERE id = :id")
    suspend fun byId(id: String): GoalEntity?

    /** Every goal, the earliest created first. */
    @Query("SELECT * FROM goal ORDER BY created_at, id")
    suspend fun all(): List<GoalEntity>

    /** A goal's history in recording order. */
    @Query("SELECT * FROM goal_event WHERE goal_id = :goalId ORDER BY sequence")
    suspend fun events(goalId: String): List<GoalEventEntity>
}
