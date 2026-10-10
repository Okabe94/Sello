package com.software.sello.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.software.sello.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ProfileDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    protected abstract suspend fun insertIfAbsent(profile: ProfileEntity): Long

    /** At most two rows: enough for the mapper to tell "one" from "more than one". */
    @Query("SELECT * FROM profile ORDER BY id LIMIT 2")
    abstract suspend fun rows(): List<ProfileEntity>

    /** The profile rows now and again after every committed change to them. */
    @Query("SELECT * FROM profile ORDER BY id LIMIT 2")
    abstract fun changes(): Flow<List<ProfileEntity>>

    /**
     * Moves the revision from [from] to [to] only if the profile is still at
     * [generation] and [from]. Returns the number of rows changed: 1, or 0 if not.
     */
    @Query(
        "UPDATE profile SET revision = :to " +
            "WHERE id = 1 AND generation = :generation AND revision = :from"
    )
    abstract suspend fun advanceRevision(generation: Long, from: Long, to: Long): Int

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
