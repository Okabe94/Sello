package com.software.sello.devtools

import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

/**
 * A synthetic history to replay in the sandbox, one step at a time, through the app's
 * own commands. [id] and [version] name it; change [version] whenever its steps
 * change, because the operation of each step is derived from them, the [seed] and the
 * step's place. The same scenario therefore always writes the same operations, and a
 * step run twice is recognised rather than recorded twice.
 *
 * It starts in a new sandbox at [start] in [zone]. Recurrence does not exist in the
 * app, so no step stands for a scheduled entry; every entry is an explicit step.
 */
data class Scenario(
    val id: String,
    val version: Int,
    val seed: Long,
    val zone: ZoneId,
    val start: LocalDate,
    val title: String,
    val summary: String,
    val steps: List<ScenarioStep>
) {
    val key: String get() = "$id@$version"

    /** The operation step [index] always uses. */
    fun operation(index: Int): String =
        UUID.nameUUIDFromBytes("sello-scenario:$id:$version:$seed:$index".toByteArray())
            .toString()
}

/** One action of a scenario. Amounts are whole pesos; entries are dated on the sandbox's day. */
sealed interface ScenarioStep {
    /** Moves the sandbox's date forward to [date]. */
    data class AdvanceTo(val date: LocalDate) : ScenarioStep

    /** A category the later steps call [key], with [limit] pesos a month or no limit. */
    data class Category(val key: String, val name: String, val icon: String, val limit: Long?) :
        ScenarioStep

    data class Expense(val category: String, val pesos: Long, val note: String? = null) :
        ScenarioStep

    /** [source] is a stored income source key, such as `salary`. */
    data class Income(val pesos: Long, val source: String) : ScenarioStep
}
