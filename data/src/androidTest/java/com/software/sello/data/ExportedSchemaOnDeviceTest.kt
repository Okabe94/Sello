package com.software.sello.data

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.software.sello.data.local.Migration1To2
import com.software.sello.data.local.SelloDatabase
import com.software.sello.data.local.openSelloDatabase
import com.software.sello.data.repository.RoomFinancialProfileStore
import com.software.sello.domain.model.FinancialProfile
import com.software.sello.domain.model.Outcome
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The migration harness. It builds a database from a committed
 * `data/schemas/.../N.json` alone, then upgrades or opens that file with this build.
 * Room refuses the file if the exported schema and the entities disagree. Each later
 * version adds its migration and its upgrade cases here.
 */
@RunWith(AndroidJUnit4::class)
class ExportedSchemaOnDeviceTest {
    private val name = "schema-harness.db"
    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    @get:Rule
    val helper = MigrationTestHelper(instrumentation, SelloDatabase::class.java)

    @After
    fun removeFile() {
        instrumentation.targetContext.deleteDatabase(name)
    }

    private val versionOneTables =
        listOf(
            "profile",
            "category",
            "default_limit",
            "month_limit",
            "expense",
            "income",
            "operation_receipt"
        )

    private fun SupportSQLiteDatabase.everyRow(): Map<String, List<String>> =
        versionOneTables.associateWith { table ->
            query("SELECT * FROM $table").use { cursor ->
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

    /**
     * A version 1 database holding one of every kind of row a person could have by
     * then. Returns what it held, read before the file was closed.
     */
    private fun versionOneWithHistory(): Map<String, List<String>> =
        helper.createDatabase(name, 1).use { exported ->
            exported.execSQL(
                "INSERT INTO profile (id, financial_zone, generation, revision, created_at) " +
                    "VALUES (1, 'America/Bogota', 4, 17, 0)"
            )
            exported.execSQL(
                "INSERT INTO category VALUES ('${uuid(
                    1
                )}', 'Mercado', 'mercado', 'shopping_cart', 0, 3, 10, 20)"
            )
            exported.execSQL(
                "INSERT INTO default_limit VALUES ('${uuid(
                    1
                )}', '2026-09', 'finite', 300000, 'COP')"
            )
            exported.execSQL(
                "INSERT INTO month_limit VALUES ('${uuid(1)}', '2026-10', 'unlimited', NULL, 'COP')"
            )
            exported.execSQL(
                "INSERT INTO expense VALUES ('${uuid(2)}', '${uuid(1)}', '2026-10-03', 5, 48700, " +
                    "'COP', 'Pan', 2, 30, 40)"
            )
            exported.execSQL(
                "INSERT INTO operation_receipt VALUES ('${uuid(9)}', 'expense.create', " +
                    "'${"ab".repeat(32)}', 4, 17, '${uuid(2)}', 50)"
            )
            exported.everyRow()
        }

    @Test
    fun upgradingFromVersionOneKeepsEveryRowAndAddsOnlyTheEmptyTableForDeletedExpenses() {
        val before = versionOneWithHistory()

        // Room compares the result with the exported version 2 schema and fails on any difference.
        helper.runMigrationsAndValidate(name, 2, true, Migration1To2).use { upgraded ->
            val kept = upgraded.query("SELECT COUNT(*) FROM deleted_expense").use { count ->
                count.moveToFirst()
                count.getInt(0)
            }

            assertEquals(6, before.values.sumOf { rows -> rows.size })
            assertEquals(before, upgraded.everyRow())
            assertEquals(0, kept)
        }
    }

    @Test
    fun aVersionOneDatabaseOpenedByThisBuildIsUpgradedAndReadsTheSameProfile() = runBlocking {
        versionOneWithHistory()

        val database = openSelloDatabase(instrumentation.targetContext, Dispatchers.IO, name)
        val profile =
            RoomFinancialProfileStore(database) { recordedAt }.establish(ZoneId.of("Asia/Tokyo"))
        val expenses = database.expenseDao().page("0000-01-01", "9999-12-32", "", 0, 10)
        val version = database.openHelper.readableDatabase.version
        database.close()

        assertEquals(
            Outcome.Success(FinancialProfile(ZoneId.of("America/Bogota"), 4, 17)),
            profile
        )
        assertEquals(listOf(48_700L), expenses.map { it.amountMinor })
        assertEquals(2, version)
    }

    @Test
    fun aDatabaseBuiltFromTheExportedCurrentSchemaOpensAndKeepsItsRows() = runBlocking {
        helper.createDatabase(name, 2).use { exported ->
            exported.execSQL(
                "INSERT INTO profile (id, financial_zone, generation, revision, created_at) " +
                    "VALUES (1, 'America/Bogota', 4, 17, 0)"
            )
        }

        val database = openSelloDatabase(instrumentation.targetContext, Dispatchers.IO, name)
        val profile =
            RoomFinancialProfileStore(database) { recordedAt }.establish(ZoneId.of("Asia/Tokyo"))
        database.close()

        assertEquals(
            Outcome.Success(FinancialProfile(ZoneId.of("America/Bogota"), 4, 17)),
            profile
        )
    }
}
