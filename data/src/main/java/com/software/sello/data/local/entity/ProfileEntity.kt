package com.software.sello.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * The single profile row. It is never deleted: a restore or reset replaces the
 * financial rows and advances [generation] here, which is what lets a stale command be
 * refused afterwards.
 */
@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: Long,
    @ColumnInfo(name = "financial_zone")
    val financialZone: String,
    @ColumnInfo(name = "generation")
    val generation: Long,
    @ColumnInfo(name = "revision")
    val revision: Long,
    @ColumnInfo(name = "created_at")
    val createdAt: Long
) {
    companion object {
        const val TABLE = "profile"
        const val SINGLE_ID = 1L
    }
}
