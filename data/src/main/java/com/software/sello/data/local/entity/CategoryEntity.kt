package com.software.sello.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** [nameKey] is the domain's uniqueness key; the unique index is what refuses a duplicate name. */
@Entity(tableName = "category", indices = [Index(value = ["name_key"], unique = true)])
data class CategoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "name_key")
    val nameKey: String,
    @ColumnInfo(name = "icon")
    val icon: String,
    @ColumnInfo(name = "archived")
    val archived: Long,
    @ColumnInfo(name = "version")
    val version: Long,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long
) {
    companion object {
        const val TABLE = "category"
    }
}
