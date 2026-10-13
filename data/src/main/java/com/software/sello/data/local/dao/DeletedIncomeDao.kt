package com.software.sello.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.software.sello.data.local.entity.DeletedIncomeEntity

@Dao
interface DeletedIncomeDao {
    @Insert
    suspend fun insert(deleted: DeletedIncomeEntity)

    @Query("SELECT * FROM deleted_income WHERE deletion_id = :deletionId")
    suspend fun find(deletionId: String): DeletedIncomeEntity?

    /** Returns how many rows were removed. */
    @Query("DELETE FROM deleted_income WHERE deletion_id = :deletionId")
    suspend fun remove(deletionId: String): Int

    /** Makes every earlier income deletion final. Returns how many kept copies were removed. */
    @Query("DELETE FROM deleted_income")
    suspend fun clear(): Int

    /** The highest sequence a kept copy holds, or null when there is none. */
    @Query("SELECT MAX(sequence) FROM deleted_income")
    suspend fun highestSequence(): Long?
}
