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
    private val versionTwo = File(schemas, "2.json").readText()
    private val versionThree = File(schemas, "3.json").readText()
    private val current = File(schemas, "4.json").readText()

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
        assertEquals(listOf("1.json", "2.json", "3.json", "4.json"), schemas.list()!!.sorted())
        assertEquals(listOf("1"), values(versionOne, "version").toList())
        assertEquals(listOf("2"), values(versionTwo, "version").toList())
        assertEquals(listOf("3"), values(versionThree, "version").toList())
        assertEquals(listOf("4"), values(current, "version").toList())
    }

    @Test
    fun versionOneStillHoldsOnlyTheTablesItShippedWith() {
        assertEquals(mvpTables, values(versionOne, "tableName").toSet())
    }

    @Test
    fun versionTwoAddsOnlyTheTableForDeletedExpenses() {
        assertEquals(mvpTables + "deleted_expense", values(versionTwo, "tableName").toSet())
    }

    @Test
    fun versionThreeAddsOnlyTheTableForDeletedIncome() {
        assertEquals(
            mvpTables + "deleted_expense" + "deleted_income",
            values(versionThree, "tableName").toSet()
        )
    }

    @Test
    fun versionFourAddsOnlyTheGoalTables() {
        assertEquals(
            mvpTables + "deleted_expense" + "deleted_income" + "goal" + "goal_event" +
                "deleted_goal_event" + "featured_goal",
            values(current, "tableName").toSet()
        )
    }

    @Test
    fun noForeignKeyCascadesOrDetaches() {
        for ((schema, keys) in listOf(
            versionOne to 3,
            versionTwo to 3,
            versionThree to 3,
            current to 5
        )) {
            val actions = (values(schema, "onDelete") + values(schema, "onUpdate")).toList()

            assertEquals(keys * 2, actions.size)
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
