package com.software.sello.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.software.sello.data.local.entity.DeletedGoalEventEntity

@Dao
interface DeletedGoalEventDao {
    @Insert
    suspend fun insert(kept: DeletedGoalEventEntity)

    @Query("SELECT * FROM deleted_goal_event WHERE deletion_id = :deletionId")
    suspend fun find(deletionId: String): DeletedGoalEventEntity?

    @Query("DELETE FROM deleted_goal_event WHERE deletion_id = :deletionId")
    suspend fun remove(deletionId: String): Int

    @Query("DELETE FROM deleted_goal_event")
    suspend fun clear()
}
