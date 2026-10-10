package com.software.sello

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.platform.app.InstrumentationRegistry

/**
 * The installed debug app's own database, which every test in a run shares. Tests
 * that need a new installation, or that create records, start and end from here.
 */
object TestData {
    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private val tables =
        listOf("expense", "income", "month_limit", "default_limit", "category", "operation_receipt")

    private fun <T> database(flags: Int, use: (SQLiteDatabase) -> T): T =
        SQLiteDatabase.openDatabase(context.getDatabasePath("sello.db").path, null, flags)
            .use(use)

    /** Back to what a new installation holds: a profile at revision 0 and nothing else. */
    fun reset() = database(SQLiteDatabase.OPEN_READWRITE) { database ->
        database.beginTransaction()
        try {
            tables.forEach { database.execSQL("DELETE FROM $it") }
            database.execSQL("UPDATE profile SET revision = 0")
            database.setTransactionSuccessful()
        } finally {
            database.endTransaction()
        }
    }

    fun count(table: String): Long = long("SELECT COUNT(*) FROM $table")

    fun financialRows(): Long = tables.sumOf(::count)

    fun long(sql: String): Long = database(SQLiteDatabase.OPEN_READONLY) { database ->
        database.rawQuery(sql, null).use {
            it.moveToFirst()
            it.getLong(0)
        }
    }

    /** Each row of [sql] with its columns joined by "|". */
    fun rows(sql: String): List<String> = database(SQLiteDatabase.OPEN_READONLY) { database ->
        database.rawQuery(sql, null).use { cursor ->
            buildList {
                while (cursor.moveToNext()) {
                    add(
                        (0 until cursor.columnCount).joinToString("|") {
                            cursor.getString(it)
                                ?: "null"
                        }
                    )
                }
            }
        }
    }
}
