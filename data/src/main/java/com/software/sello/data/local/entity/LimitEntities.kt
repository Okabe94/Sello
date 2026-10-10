package com.software.sello.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/** Stored spellings of a limit's kind. A finite limit has an amount; an unlimited one has none. */
object LimitKind {
    const val FINITE = "finite"
    const val UNLIMITED = "unlimited"
}

/** One entry in a category's history of default limits, effective from a month onwards. */
@Entity(
    tableName = "default_limit",
    primaryKeys = ["category_id", "effective_month"],
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.RESTRICT
        )
    ]
)
data class DefaultLimitEntity(
    @ColumnInfo(name = "category_id")
    val categoryId: String,
    @ColumnInfo(name = "effective_month")
    val effectiveMonth: String,
    @ColumnInfo(name = "kind")
    val kind: String,
    @ColumnInfo(name = "limit_minor")
    val limitMinor: Long?,
    @ColumnInfo(name = "currency")
    val currency: String
) {
    companion object {
        const val TABLE = "default_limit"
    }
}

/** The limit set for one category in one month. */
@Entity(
    tableName = "month_limit",
    primaryKeys = ["category_id", "month"],
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("month")]
)
data class MonthLimitEntity(
    @ColumnInfo(name = "category_id")
    val categoryId: String,
    @ColumnInfo(name = "month")
    val month: String,
    @ColumnInfo(name = "kind")
    val kind: String,
    @ColumnInfo(name = "limit_minor")
    val limitMinor: Long?,
    @ColumnInfo(name = "currency")
    val currency: String
) {
    companion object {
        const val TABLE = "month_limit"
    }
}
