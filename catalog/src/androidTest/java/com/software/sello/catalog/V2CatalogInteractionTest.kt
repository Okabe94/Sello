package com.software.sello.catalog

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class V2CatalogInteractionTest {
    @get:Rule
    val rule = createAndroidComposeRule<CatalogActivity>()

    @Test
    fun v2EntryOpensFromItsOwnSection() {
        rule.onNodeWithTag("link:v2.entry").performScrollTo().performClick()
        rule.onNodeWithTag("example:v2.entry").assertIsDisplayed()
        rule.onNodeWithTag("v2:entry:amount").assertIsDisplayed()
    }

    @Test
    fun noteComposerCountsUnicodeCharactersAndSavesThem() {
        rule.onNodeWithTag("link:v2.note-composer").performScrollTo().performClick()
        rule.onNodeWithTag("v2:composer:open").performClick()
        val note = "🙂".repeat(31)
        rule.onNode(hasSetTextAction()).performTextInput(note)
        rule.onNodeWithText("31/60").assertExists()
        rule.onNodeWithContentDescription("Listo").assertIsEnabled().performClick()
        rule.onNodeWithTag("v2:composer:saved").assertTextContains(note)
    }

    @Test
    fun expenseNoteSheetNamesItsEntryAndMarksTheNoteOptional() {
        rule.onNodeWithTag("link:v2.entry").performScrollTo().performClick()
        rule.onNodeWithTag("v2:entry:choice:1").performScrollTo().performClick()
        rule.onNodeWithTag("v2:entry:note").performScrollTo().performClick()
        rule.onNodeWithText("Gasto en Comida", substring = true).assertExists()
        rule.onNodeWithText("Opcional").assertExists()
        rule.onNodeWithContentDescription("Listo").assertIsEnabled()
    }

    @Test
    fun otherSourceIsAFullWidthLineWithAChangeAction() {
        rule.onNodeWithTag("link:v2.entry").performScrollTo().performClick()
        rule.onNodeWithText("Ingreso").performClick()
        rule.onNodeWithTag("v2:entry:choice:4").performScrollTo().performClick()
        rule.onNodeWithTag("v2:entry:source").performScrollTo().performClick()
        rule.onNode(hasSetTextAction()).performTextInput("Venta de la bicicleta")
        rule.onNodeWithContentDescription("Listo").performClick()
        val line = rule.onNodeWithTag("v2:entry:source").getUnclippedBoundsInRoot()
        val amount = rule.onNodeWithTag("v2:entry:amount").getUnclippedBoundsInRoot()
        assertEquals(amount.left, line.left)
        assertEquals(amount.right, line.right)
        rule.onNodeWithText("Cambiar").assertExists()
    }

    @Test
    fun unknownOutcomeOffersCheckingAndLocksTheKeypad() {
        rule.onNodeWithTag("link:v2.entry").performScrollTo().performClick()
        rule.onNodeWithTag("v2:entry:state:3").performScrollTo().performClick()
        rule.onNodeWithTag(
            "v2:entry:submit"
        ).assertTextContains("Comprobar si se guardó").assertIsEnabled()
        rule.onNodeWithText("1").assertIsNotEnabled()
    }

    @Test
    fun restoredDraftSurvivesRecreationWithoutSubmitting() {
        rule.onNodeWithTag("link:v2.entry").performScrollTo().performClick()
        rule.onNodeWithTag("v2:entry:state:4").performScrollTo().performClick()
        rule.activityRule.scenario.recreate()
        rule.onNodeWithText("Almuerzo").assertExists()
        rule.onNodeWithTag("v2:entry:submit").assertIsEnabled()
    }
}
