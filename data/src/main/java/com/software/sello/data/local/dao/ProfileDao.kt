package com.software.sello.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.software.sello.data.local.entity.ProfileEntity

@Dao
abstract class ProfileDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertIfAbsent(profile: ProfileEntity): Long

    /** At most two rows: enough for the mapper to tell "one" from "more than one". */
    @Query("SELECT * FROM profile ORDER BY id LIMIT 2")
    abstract suspend fun rows(): List<ProfileEntity>

    /**
     * Stores [candidate] only if no profile exists, then reads what is stored. One
     * transaction, so zone, generation and revision appear together or not at all.
     */
    @Transaction
    open suspend fun establish(candidate: ProfileEntity): List<ProfileEntity> {
        insertIfAbsent(candidate)
        return rows()
    }
}
