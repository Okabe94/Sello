package com.software.sello.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogExamplesTest {
    @Test
    fun exampleIdsAreStable() {
        // Tests and deep references rely on these; changing one is a deliberate edit here.
        assertEquals(
            listOf(
                "foundations.colors",
                "foundations.typography",
                "foundations.money-text",
                "foundations.shape-spacing",
                "foundations.motion",
                "foundations.icons"
            ),
            catalogExamples.map { it.id }
        )
    }

    @Test
    fun everyIdIsUniqueAndNamedAfterItsGroup() {
        val ids = catalogExamples.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
        catalogExamples.forEach {
            assertTrue(it.id, Regex("${it.group.id}\\.[a-z]+(-[a-z]+)*").matches(it.id))
        }
    }

    @Test
    fun groupsCoverTheNineComponentTypesInOrder() {
        assertEquals(
            listOf(
                "foundations", "paper", "stamps", "money", "categories",
                "inputs", "navigation", "charts", "feedback"
            ),
            CatalogGroup.entries.map { it.id }
        )
    }
}
