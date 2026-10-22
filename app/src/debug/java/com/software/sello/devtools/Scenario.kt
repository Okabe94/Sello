package com.software.sello.devtools

import java.time.LocalDate
import java.time.YearMonth
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

/**
 * What a step must lead to for the scenario to go on. A rejection the step expects is
 * a pass, so edge cases can be scripted; anything else stops the run and says why.
 */
sealed interface Expect {
    /** Committed, now or by an earlier try of the same operation. */
    data object Saved : Expect

    /** Committed by an earlier submission of the same operation; nothing new written. */
    data object AlreadySaved : Expect

    /** Refused by the command with this reason, such as `VersionConflict`. */
    data class Rejected(val reason: String) : Expect

    /** Refused by input validation, so no command was sent at all. */
    data object InvalidInput : Expect
}

/** A figure the month snapshots report, as Recibo and Resumen show it. */
enum class Figure(val label: String) {
    Spent("gastado"),
    Limit("límite"),
    Remaining("restante"),

    /** No category has a finite limit that month: no budget at all, not a zero one. */
    NoLimit("sin límite"),
    Income("ingresos"),
    NetFlow("ingresos menos gastos"),
    Unallocated("sin asignar")
}

/** [pesos] in [month] for [figure], as given by [source]: an approved example ID or a hand derivation. */
data class Check(val month: YearMonth, val figure: Figure, val pesos: Long, val source: String)

/** One action of a scenario. Amounts are whole pesos; entries are dated on the sandbox's day. */
sealed interface ScenarioStep {
    val expect: Expect get() = Expect.Saved

    /** Whether the step submits an operation; a date step or a check does not. */
    val writes: Boolean get() = this !is AdvanceTo && this !is Verify

    /** Moves the sandbox's date forward to [date]. */
    data class AdvanceTo(val date: LocalDate) : ScenarioStep

    /** A category the later steps call [key], with [limit] pesos a month or no limit. */
    data class Category(val key: String, val name: String, val icon: String, val limit: Long?) :
        ScenarioStep

    /**
     * [pesos] may be beyond what an entry allows, to script [Expect.InvalidInput]. [on]
     * dates it earlier than the sandbox's day, a backdated entry; by default it is today.
     */
    data class Expense(
        val category: String,
        val pesos: Long,
        val note: String? = null,
        override val expect: Expect = Expect.Saved,
        val on: LocalDate? = null
    ) : ScenarioStep

    /** The category's default limit from the sandbox's month on, as read at [version]; null is none. */
    data class SetDefaultLimit(val category: String, val pesos: Long?, val version: Long) :
        ScenarioStep

    /** The limit of [month] only, as read at [version]; null is none. */
    data class SetMonthLimit(
        val category: String,
        val month: YearMonth,
        val pesos: Long?,
        val version: Long
    ) : ScenarioStep

    data class Archive(val category: String, val version: Long) : ScenarioStep

    /**
     * Reads the real month snapshots and compares each of [checks]. Every expected value
     * names where it comes from; none is computed by the code being checked.
     */
    data class Verify(val checks: List<Check>) : ScenarioStep

    /** [source] is a stored income source key, such as `salary`. */
    data class Income(val pesos: Long, val source: String) : ScenarioStep

    /** Submits step [of] again: the same operation and input, as a double tap would. */
    data class Repeat(val of: Int, override val expect: Expect = Expect.AlreadySaved) :
        ScenarioStep

    /** Changes the expense step [of] recorded to [pesos], as read at [version]. */
    data class EditExpense(
        val of: Int,
        val pesos: Long,
        val version: Long,
        override val expect: Expect = Expect.Saved
    ) : ScenarioStep

    /** Deletes the expense step [of] recorded, as read at [version]. */
    data class DeleteExpense(
        val of: Int,
        val version: Long,
        override val expect: Expect = Expect.Saved
    ) : ScenarioStep

    /** Undoes the deletion made by step [of]. */
    data class UndoDelete(val of: Int, override val expect: Expect = Expect.Saved) : ScenarioStep
}
