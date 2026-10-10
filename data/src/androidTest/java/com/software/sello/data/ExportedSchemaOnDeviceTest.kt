package com.software.sello.data

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
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
 * The migration harness, with the only version there is. It builds a database from
 * the committed `data/schemas/.../1.json` alone, then opens that file with this
 * build. Room refuses the file if the exported schema and the entities disagree.
 * Later versions add their migrations and upgrade cases here.
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

    @Test
    fun aDatabaseBuiltFromTheExportedVersionOneSchemaOpensAndKeepsItsRows() = runBlocking {
        helper.createDatabase(name, 1).use { exported ->
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
