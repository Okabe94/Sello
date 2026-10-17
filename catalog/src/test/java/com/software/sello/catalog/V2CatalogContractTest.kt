package com.software.sello.catalog

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V2CatalogContractTest {
    @Test
    fun v2SectionCoversTheReviewedComponentFamilies() {
        assertEquals(
            listOf(
                "entry", "amount", "keypad", "chips", "note-composer", "buttons", "dock",
                "navigation", "month-bar", "past-month-ribbon", "stamps", "footnotes",
                "summary", "comparison", "ranking", "suggestion", "charts", "milestones",
                "goal-pill", "goal-cards", "movements", "investments", "icon-choice",
                "feedback", "celebration", "states", "foundations"
            ).map { "v2.$it" },
            catalogExamples.filter { it.group.id == "v2" }.map { it.id }
        )
    }

    @Test
    fun existingExamplesRemainAvailableAlongsideV2() {
        assertEquals(38, catalogExamples.count { !it.group.id.startsWith("v2") })
        assertTrue(catalogExamples.any { it.id == "inputs.expense-entry" })
    }

    @Test
    fun v2ScreensSectionMirrorsTheEvidenceScreensInFull() {
        val screens = catalogExamples.filter { it.group.id == "v2-screens" }
        assertEquals(
            listOf("recibo", "metas", "inversiones", "celebration", "metas-undo")
                .map { "v2-screens.$it" },
            screens.map { it.id }
        )
        assertTrue(screens.all { it.fullBleed })
    }
}
