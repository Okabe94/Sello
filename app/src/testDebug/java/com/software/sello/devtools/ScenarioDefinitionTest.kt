package com.software.sello.devtools

import com.software.sello.devtools.ScenarioStep.AdvanceTo
import com.software.sello.devtools.ScenarioStep.Category
import com.software.sello.devtools.ScenarioStep.Expense
import com.software.sello.devtools.ScenarioStep.Income
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ScenarioDefinitionTest {
    @Test
    fun eachStepHasItsOwnOperationAndTheSameOneEveryTime() {
        Scenarios.all.forEach { scenario ->
            val ids = scenario.steps.indices.map(scenario::operation)
            assertEquals(scenario.key, ids.size, ids.toSet().size)
            assertEquals(ids, scenario.steps.indices.map(scenario::operation))
            ids.forEach {
                assertTrue(it, Regex("[0-9a-f]{8}(-[0-9a-f]{4}){3}-[0-9a-f]{12}").matches(it))
            }
        }
        // A new version of a scenario is a new set of operations.
        val other = Scenarios.G01.copy(version = 2)
        assertTrue(Scenarios.G01.operation(1) != other.operation(1))
    }

    @Test
    fun datesNeverGoBackAndEveryEntryHasACategoryCreatedBeforeIt() {
        Scenarios.all.forEach { scenario ->
            var today = scenario.start
            val categories = mutableSetOf<String>()
            scenario.steps.forEach { step ->
                when (step) {
                    is AdvanceTo -> {
                        assertTrue("${scenario.key} $step", step.date > today)
                        today = step.date
                    }

                    is Category -> assertTrue(categories.add(step.key))

                    is Expense -> assertTrue("${scenario.key} $step", step.category in categories)

                    is Income -> Unit
                }
            }
        }
    }

    @Test
    fun g01IsTheApprovedExample() {
        val steps = Scenarios.G01.steps
        assertEquals(LocalDate.of(2026, 10, 1), Scenarios.G01.start)
        assertEquals(
            listOf(10_000L, 15_000L, 5_000L),
            steps.filterIsInstance<Expense>().map {
                it.pesos
            }
        )
        assertEquals(listOf(200_000L), steps.filterIsInstance<Income>().map { it.pesos })
        assertEquals(100_000L, steps.filterIsInstance<Category>().single().limit)
    }

    @Test
    fun theSixMonthsAreTheSameOnEveryBuildAndCoverMayToOctober() {
        val scenario = Scenarios.SixMonths
        val expenses = scenario.steps.filterIsInstance<Expense>()
        assertEquals(36, expenses.size)
        assertEquals(6, scenario.steps.filterIsInstance<Income>().size)
        // Pinned: a change to the seed or the steps must come with a new version.
        assertEquals(3_004_000L, expenses.sumOf { it.pesos })
        val months = scenario.steps.filterIsInstance<AdvanceTo>().map {
            YearMonth.from(it.date)
        }.toSet()
        assertEquals((5..10).map { YearMonth.of(2026, it) }.toSet(), months + YearMonth.of(2026, 5))
    }
}
