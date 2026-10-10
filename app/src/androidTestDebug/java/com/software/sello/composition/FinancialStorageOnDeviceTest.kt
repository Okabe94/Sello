package com.software.sello.composition

import android.database.sqlite.SQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.port.FinancialClock
import com.software.sello.domain.port.FinancialProfileStore
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.GlobalContext

/** The installed app's own database file, as its startup left it. */
@RunWith(AndroidJUnit4::class)
class FinancialStorageOnDeviceTest {
    private val koin get() = GlobalContext.get()
    private val file
        get() = InstrumentationRegistry.getInstrumentation().targetContext
            .getDatabasePath("sello.db")

    private fun <T> stored(read: (SQLiteDatabase) -> T): T =
        SQLiteDatabase.openDatabase(file.path, null, SQLiteDatabase.OPEN_READONLY).use(read)

    private fun SQLiteDatabase.count(table: String): Long =
        rawQuery("SELECT COUNT(*) FROM $table", null).use {
            it.moveToFirst()
            it.getLong(0)
        }

    @Test
    fun startupCreatedTheRealDatabaseWithExactlyOneProfile() {
        assertTrue(file.exists())
        val zone = stored { database ->
            assertEquals(1, database.version)
            assertEquals(1, database.count("profile"))
            database.rawQuery("SELECT financial_zone FROM profile WHERE id = 1", null).use {
                it.moveToFirst()
                it.getString(0)
            }
        }

        assertEquals(koin.get<FinancialClock>().today.value.zone, ZoneId.of(zone))
    }

    @Test
    fun theStoredZoneWinsOverADifferentDeviceZoneOnceTheAppHasStarted() = runBlocking {
        val elsewhere = ZoneId.of("Pacific/Kiritimati")
        val running = koin.get<FinancialClock>().today.value.zone
        assertNotEquals(elsewhere, running)

        val profile = koin.get<FinancialProfileStore>().establish(elsewhere)

        assertEquals(running, (profile as Outcome.Success).value.zone)
        assertEquals(1, stored { it.count("profile") })
    }

    @Test
    fun noFinancialRecordIsSeeded() {
        val tables = listOf(
            "category",
            "default_limit",
            "month_limit",
            "expense",
            "income",
            "operation_receipt"
        )

        val counts = stored { database -> tables.associateWith { database.count(it) } }

        assertEquals(tables.associateWith { 0L }, counts)
    }
}
