package com.software.sello.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Version 4 adds goals: their history, the copy kept for undoing a deleted movement and
 * Recibo's chosen goal, all empty. Nothing that existed in version 3 is read, changed or
 * removed.
 */
internal val Migration3To4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `goal` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, " +
                "`icon_key` TEXT NOT NULL, `target_month` TEXT, `completed_on` TEXT, " +
                "`next_sequence` INTEGER NOT NULL, `version` INTEGER NOT NULL, " +
                "`created_at` INTEGER NOT NULL, `updated_at` INTEGER NOT NULL, PRIMARY KEY(`id`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `goal_event` (`id` TEXT NOT NULL, " +
                "`goal_id` TEXT NOT NULL, `sequence` INTEGER NOT NULL, `kind` TEXT NOT NULL, " +
                "`amount_minor` INTEGER NOT NULL, `currency` TEXT NOT NULL, " +
                "`effective_date` TEXT, `recorded_on` TEXT NOT NULL, `note` TEXT, " +
                "`version` INTEGER NOT NULL, `created_at` INTEGER NOT NULL, " +
                "`updated_at` INTEGER NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`goal_id`) " +
                "REFERENCES `goal`(`id`) ON UPDATE RESTRICT ON DELETE RESTRICT )"
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_goal_event_goal_id_sequence` " +
                "ON `goal_event` (`goal_id`, `sequence`)"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_goal_event_goal_id_effective_date` " +
                "ON `goal_event` (`goal_id`, `effective_date`)"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `deleted_goal_event` (" +
                "`deletion_id` TEXT NOT NULL, `generation` INTEGER NOT NULL, " +
                "`deleted_at` INTEGER NOT NULL, `event_id` TEXT NOT NULL, " +
                "`goal_id` TEXT NOT NULL, `sequence` INTEGER NOT NULL, `kind` TEXT NOT NULL, " +
                "`amount_minor` INTEGER NOT NULL, `currency` TEXT NOT NULL, " +
                "`effective_date` TEXT, `recorded_on` TEXT NOT NULL, `note` TEXT, " +
                "`version` INTEGER NOT NULL, `created_at` INTEGER NOT NULL, " +
                "`updated_at` INTEGER NOT NULL, PRIMARY KEY(`deletion_id`))"
        )
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `featured_goal` (`id` INTEGER NOT NULL, " +
                "`goal_id` TEXT NOT NULL, PRIMARY KEY(`id`), FOREIGN KEY(`goal_id`) " +
                "REFERENCES `goal`(`id`) ON UPDATE RESTRICT ON DELETE RESTRICT )"
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_featured_goal_goal_id` " +
                "ON `featured_goal` (`goal_id`)"
        )
    }
}

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
