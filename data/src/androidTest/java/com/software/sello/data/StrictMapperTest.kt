package com.software.sello.data

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.software.sello.data.local.SelloDatabase
import com.software.sello.data.mapper.decodeAll
import com.software.sello.data.mapper.profileFrom
import com.software.sello.data.mapper.toDomain
import com.software.sello.data.mapper.toEntity
import com.software.sello.data.repository.RoomFinancialProfileStore
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Rows are written straight into the real database, as damage or a faulty writer
 * would leave them, then read back through the real queries and mappers.
 */
@RunWith(AndroidJUnit4::class)
class StrictMapperTest {
    @get:Rule
    val files = DatabaseFiles()

    private val millis = recordedAt.toEpochMilli()
    private val allDates = "0000-01-01" to "9999-12-32"

    private fun damaged(record: String, key: String?, field: String) =
        Outcome.Failure(StorageFailure.Integrity(record, key, field))

    private fun columns(base: List<Pair<String, Any?>>, change: Pair<String, Any?>) =
        base.map { if (it.first == change.first) change else it }.toTypedArray()

    private fun SelloDatabase.clear(table: String) =
        openHelper.writableDatabase.execSQL("DELETE FROM $table")

    private val expenseRow = listOf<Pair<String, Any?>>(
        "id" to uuid(10),
        "category_id" to uuid(1),
        "effective_date" to "2026-10-03",
        "sequence" to 1L,
        "amount_minor" to 10_000L,
        "currency" to "COP",
        "note" to "Pan",
        "version" to 1L,
        "created_at" to millis,
        "updated_at" to millis
    )

    @Test
    fun aDamagedExpenseRowIsAnIntegrityFailureNamingItsColumn() = runBlocking {
        val database = files.open()
        database.categoryDao().insert(category(1, "Mercado").toEntity())
        database.insertRaw("expense", *expenseRow.toTypedArray())
        assertEquals(
            Outcome.Success(expense(10, 1, "2026-10-03", 1, 10_000, "Pan")),
            database.expenseDao().byId(uuid(10))!!.toDomain()
        )
        val cases = listOf<Pair<Pair<String, Any?>, String>>(
            ("currency" to "USD") to "currency",
            ("currency" to "cop") to "currency",
            ("currency" to "XXX") to "currency",
            ("currency" to "") to "currency",
            ("amount_minor" to 0L) to "amount_minor",
            ("amount_minor" to -1L) to "amount_minor",
            ("amount_minor" to 1_000_000_000_000L) to "amount_minor",
            ("amount_minor" to "diez mil") to "amount_minor",
            ("effective_date" to "2026-02-30") to "effective_date",
            ("effective_date" to "2026-2-3") to "effective_date",
            ("effective_date" to "20261003") to "effective_date",
            ("effective_date" to "2026-10-03T00:00") to "effective_date",
            ("effective_date" to "") to "effective_date",
            ("note" to "") to "note",
            ("note" to "   ") to "note",
            ("note" to " Pan") to "note",
            ("note" to "a".repeat(61)) to "note",
            ("note" to "Pan\ny leche") to "note",
            ("version" to 0L) to "version",
            ("sequence" to -1L) to "sequence"
        )

        for ((change, field) in cases) {
            database.clear("expense")
            database.insertRaw("expense", *columns(expenseRow, change))

            assertEquals(
                "$change",
                damaged("expense", uuid(10), field),
                database.expenseDao().byId(uuid(10))!!.toDomain()
            )
        }
    }

    @Test
    fun aMalformedIdentifierIsAnIntegrityFailure() = runBlocking {
        val database = files.open()
        // A real identifier in a spelling the app never writes.
        val upperCase = "ABCDEF00-0000-4000-8000-000000000010"
        database.categoryDao().insert(category(1, "Mercado").toEntity())
        database.insertRaw("category", *columns(categoryRow, "id" to "MERCADO"))
        database.insertRaw("expense", *columns(expenseRow, "id" to upperCase))
        database.insertRaw(
            "expense",
            *expenseRow.map {
                when (it.first) {
                    "id" -> "id" to uuid(11)
                    "category_id" -> "category_id" to "MERCADO"
                    "sequence" -> "sequence" to 2L
                    else -> it
                }
            }.toTypedArray()
        )

        assertEquals(
            damaged("expense", upperCase, "id"),
            database.expenseDao().byId(upperCase)!!.toDomain()
        )
        assertEquals(
            damaged("expense", uuid(11), "category_id"),
            database.expenseDao().byId(uuid(11))!!.toDomain()
        )
        assertEquals(
            damaged("category", "MERCADO", "id"),
            database.categoryDao().byId("MERCADO")!!.toDomain()
        )
    }

    @Test
    fun oneDamagedRowFailsTheWholeReadInsteadOfShorteningIt() = runBlocking {
        val database = files.open()
        val (from, until) = allDates
        database.categoryDao().insert(category(1, "Mercado").toEntity())
        database.expenseDao().insert(expense(10, 1, "2026-10-01", 1, 10_000).toEntity())
        database.expenseDao().insert(expense(12, 1, "2026-10-03", 3, 5_000).toEntity())
        val intact = database.expenseDao().page(from, until, "", 0, 10).decodeAll { it.toDomain() }
        database.insertRaw(
            "expense",
            "id" to uuid(11),
            "category_id" to uuid(1),
            "effective_date" to "2026-10-02",
            "sequence" to 2L,
            "amount_minor" to 15_000L,
            "currency" to "BTC",
            "note" to null,
            "version" to 1L,
            "created_at" to millis,
            "updated_at" to millis
        )

        val read = database.expenseDao().page(from, until, "", 0, 10).decodeAll { it.toDomain() }

        assertEquals(
            Outcome.Success(
                listOf(
                    expense(10, 1, "2026-10-01", 1, 10_000),
                    expense(12, 1, "2026-10-03", 3, 5_000)
                )
            ),
            intact
        )
        assertEquals(damaged("expense", uuid(11), "currency"), read)
    }

    private val incomeRow = listOf<Pair<String, Any?>>(
        "id" to uuid(20),
        "effective_date" to "2026-10-03",
        "sequence" to 1L,
        "amount_minor" to 10_000L,
        "currency" to "COP",
        "source_key" to "other",
        "source_name" to "Venta",
        "note" to null,
        "version" to 1L,
        "created_at" to millis,
        "updated_at" to millis
    )

    @Test
    fun aDamagedIncomeRowIsAnIntegrityFailureNamingItsColumn() = runBlocking {
        val database = files.open()
        val (from, until) = allDates
        val cases = listOf<Pair<List<Pair<String, Any?>>, String>>(
            listOf("source_key" to "bonus") to "source_key",
            listOf("source_key" to "Salary") to "source_key",
            listOf("source_key" to "") to "source_key",
            listOf("source_name" to null) to "source_name",
            listOf("source_name" to "") to "source_name",
            listOf("source_name" to " Venta") to "source_name",
            listOf("source_name" to "a".repeat(25)) to "source_name",
            listOf("source_key" to "salary") to "source_name",
            listOf("currency" to "EUR") to "currency",
            listOf("amount_minor" to 0L) to "amount_minor",
            listOf("amount_minor" to 1_000_000_000_000L) to "amount_minor",
            listOf("effective_date" to "2026-13-01") to "effective_date",
            listOf("note" to "a".repeat(61)) to "note",
            listOf("version" to 0L) to "version",
            listOf("id" to "20") to "id"
        )

        for ((changes, field) in cases) {
            database.clear("income")
            val row = changes.fold(incomeRow) { row, change -> columns(row, change).toList() }
            database.insertRaw("income", *row.toTypedArray())
            val key = row.first { it.first == "id" }.second as String

            assertEquals(
                "$changes",
                damaged("income", key, field),
                database.incomeDao().page(from, until, "", 0, 10).decodeAll { it.toDomain() }
            )
        }
    }

    private val limitRow = listOf<Pair<String, Any?>>(
        "category_id" to uuid(1),
        "kind" to "finite",
        "limit_minor" to 100_000L,
        "currency" to "COP"
    )

    private val limitCases = listOf<Pair<List<Pair<String, Any?>>, String>>(
        listOf("kind" to "soft") to "kind",
        listOf("kind" to "FINITE") to "kind",
        listOf("kind" to "") to "kind",
        listOf("limit_minor" to null) to "limit_minor",
        listOf("limit_minor" to -1L) to "limit_minor",
        listOf("kind" to "unlimited") to "limit_minor",
        listOf("currency" to "USD") to "currency",
        listOf("currency" to "XXX") to "currency"
    )

    @Test
    fun aDamagedMonthLimitRowIsAnIntegrityFailureNamingItsColumn() = runBlocking {
        val database = files.open()
        database.categoryDao().insert(category(1, "Mercado").toEntity())
        val cases = limitCases + listOf(
            listOf<Pair<String, Any?>>("month" to "2026-13") to "month",
            listOf<Pair<String, Any?>>("month" to "2026-1") to "month",
            listOf<Pair<String, Any?>>("month" to "octubre") to "month"
        )

        for ((changes, field) in cases) {
            database.clear("month_limit")
            val row = changes.fold(limitRow + ("month" to "2026-10")) { row, change ->
                columns(row, change).toList()
            }
            database.insertRaw("month_limit", *row.toTypedArray())
            val month = row.first { it.first == "month" }.second as String

            assertEquals(
                "$changes",
                damaged("month_limit", "${uuid(1)}/$month", field),
                database.limitDao().forMonth(month, 10).decodeAll { it.toDomain() }
            )
        }
    }

    @Test
    fun aDamagedDefaultLimitRowIsAnIntegrityFailureNamingItsColumn() = runBlocking {
        val database = files.open()
        database.categoryDao().insert(category(1, "Mercado").toEntity())
        val cases = limitCases + listOf(
            listOf<Pair<String, Any?>>("effective_month" to "2026-00") to "effective_month",
            listOf<Pair<String, Any?>>("effective_month" to "2026-10-01") to "effective_month"
        )

        for ((changes, field) in cases) {
            database.clear("default_limit")
            val row = changes.fold(limitRow + ("effective_month" to "2026-10")) { row, change ->
                columns(row, change).toList()
            }
            database.insertRaw("default_limit", *row.toTypedArray())
            val month = row.first { it.first == "effective_month" }.second as String

            assertEquals(
                "$changes",
                damaged("default_limit", "${uuid(1)}/$month", field),
                database.limitDao().defaults(uuid(1), 10).decodeAll { it.toDomain() }
            )
        }
    }

    private val categoryRow = listOf<Pair<String, Any?>>(
        "id" to uuid(2),
        "name" to "Café",
        "name_key" to "cafe",
        "icon" to "local_cafe",
        "archived" to 0L,
        "version" to 1L,
        "created_at" to millis,
        "updated_at" to millis
    )

    @Test
    fun aDamagedCategoryRowIsAnIntegrityFailureNamingItsColumn() = runBlocking {
        val database = files.open()
        database.insertRaw("category", *categoryRow.toTypedArray())
        val intact = database.categoryDao().byId(uuid(2))!!.toDomain().valueOrFail()
        assertEquals("Café", intact.name.value)
        val cases = listOf<Pair<Pair<String, Any?>, String>>(
            ("name" to "") to "name",
            ("name" to "a".repeat(25)) to "name",
            ("name" to " Café") to "name",
            // The same word with a separate combining accent: not the stored form.
            ("name" to "Cafe\u0301") to "name",
            ("name" to "Caf\u0000") to "name",
            ("name_key" to "café") to "name_key",
            ("name_key" to "te") to "name_key",
            ("name_key" to "") to "name_key",
            ("icon" to "Home") to "icon",
            ("icon" to "") to "icon",
            ("archived" to 2L) to "archived",
            ("archived" to -1L) to "archived",
            ("version" to 0L) to "version"
        )

        for ((change, field) in cases) {
            database.clear("category")
            database.insertRaw("category", *columns(categoryRow, change))

            assertEquals(
                "$change",
                damaged("category", uuid(2), field),
                database.categoryDao().byId(uuid(2))!!.toDomain()
            )
        }
    }

    @Test
    fun aDamagedProfileIsAFailureAndNeverReplacedByTheDeviceZone() = runBlocking {
        val cases = listOf<Pair<List<Pair<String, Any?>>, String>>(
            listOf("financial_zone" to "Mars/Olympus") to "financial_zone",
            listOf("financial_zone" to "") to "financial_zone",
            listOf("financial_zone" to "america/bogota") to "financial_zone",
            // Readable as a zone, but not the spelling a zone is stored in.
            listOf("financial_zone" to "UTC+5") to "financial_zone",
            listOf("generation" to 0L) to "generation",
            listOf("revision" to -1L) to "revision",
            listOf("id" to 2L) to "id"
        )
        val profileRow = listOf<Pair<String, Any?>>(
            "id" to 1L,
            "financial_zone" to "America/Bogota",
            "generation" to 3L,
            "revision" to 41L,
            "created_at" to millis
        )
        val database = files.open()
        val store = RoomFinancialProfileStore(database) { recordedAt }

        for ((changes, field) in cases) {
            database.clear("profile")
            val row = changes.fold(profileRow) { row, change -> columns(row, change).toList() }
            database.insertRaw("profile", *row.toTypedArray())

            assertEquals(
                "$changes",
                damaged("profile", null, field),
                store.establish(ZoneId.of("Europe/Madrid"))
            )
        }
        assertEquals(damaged("profile", null, "id"), profileFrom(emptyList()))
    }
}
