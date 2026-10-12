package com.software.sello.data

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Reads the committed schema export and the module's sources; Gradle runs it in `data/`. */
class ExportedSchemaTest {
    private val schemas = File("schemas/com.software.sello.data.local.SelloDatabase")
    private val versionOne = File(schemas, "1.json").readText()
    private val current = File(schemas, "2.json").readText()

    private fun values(schema: String, key: String) =
        Regex("\"$key\"\\s*:\\s*\"?([\\w]+)\"?").findAll(schema).map { it.groupValues[1] }

    private val mvpTables = setOf(
        "profile",
        "category",
        "default_limit",
        "month_limit",
        "expense",
        "income",
        "operation_receipt"
    )

    @Test
    fun everyVersionThatShippedIsStillExported() {
        assertEquals(listOf("1.json", "2.json"), schemas.list()!!.sorted())
        assertEquals(listOf("1"), values(versionOne, "version").toList())
        assertEquals(listOf("2"), values(current, "version").toList())
    }

    @Test
    fun versionOneStillHoldsOnlyTheTablesItShippedWith() {
        assertEquals(mvpTables, values(versionOne, "tableName").toSet())
    }

    @Test
    fun versionTwoAddsOnlyTheTableForDeletedExpenses() {
        assertEquals(mvpTables + "deleted_expense", values(current, "tableName").toSet())
    }

    @Test
    fun noForeignKeyCascadesOrDetaches() {
        for (schema in listOf(versionOne, current)) {
            val actions = (values(schema, "onDelete") + values(schema, "onUpdate")).toList()

            assertEquals(6, actions.size)
            assertEquals(setOf("RESTRICT"), actions.toSet())
        }
    }

    @Test
    fun noSourceAsksRoomForADestructiveFallback() {
        val sources = File("src/main").walkTopDown().filter { it.extension == "kt" }.toList()

        assertTrue(sources.isNotEmpty())
        for (source in sources) {
            val text = source.readText()
            assertFalse(source.path, "fallbackToDestructive" in text)
            assertFalse(source.path, "allowDataLossOnRecovery(true)" in text)
            assertFalse(source.path, "deleteDatabase" in text)
        }
    }
}
