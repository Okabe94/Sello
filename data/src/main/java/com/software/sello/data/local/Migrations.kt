package com.software.sello.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Version 3 adds the table that keeps what a deleted income was until its undo time is
 * over. Nothing that existed in version 2 is read, changed or removed.
 */
internal val Migration2To3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `deleted_income` (" +
                "`deletion_id` TEXT NOT NULL, `generation` INTEGER NOT NULL, " +
                "`deleted_at` INTEGER NOT NULL, `income_id` TEXT NOT NULL, " +
                "`effective_date` TEXT NOT NULL, `sequence` INTEGER NOT NULL, " +
                "`amount_minor` INTEGER NOT NULL, `currency` TEXT NOT NULL, " +
                "`source_key` TEXT NOT NULL, `source_name` TEXT, `note` TEXT, " +
                "`version` INTEGER NOT NULL, `created_at` INTEGER NOT NULL, " +
                "`updated_at` INTEGER NOT NULL, PRIMARY KEY(`deletion_id`))"
        )
    }
}

/**
 * Version 2 adds the table that keeps what a deleted expense was until its undo time
 * is over. Nothing that existed in version 1 is read, changed or removed.
 */
internal val Migration1To2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `deleted_expense` (" +
                "`deletion_id` TEXT NOT NULL, `generation` INTEGER NOT NULL, " +
                "`deleted_at` INTEGER NOT NULL, `expense_id` TEXT NOT NULL, " +
                "`category_id` TEXT NOT NULL, `effective_date` TEXT NOT NULL, " +
                "`sequence` INTEGER NOT NULL, `amount_minor` INTEGER NOT NULL, " +
                "`currency` TEXT NOT NULL, `note` TEXT, `version` INTEGER NOT NULL, " +
                "`created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, " +
                "PRIMARY KEY(`deletion_id`))"
        )
    }
}
