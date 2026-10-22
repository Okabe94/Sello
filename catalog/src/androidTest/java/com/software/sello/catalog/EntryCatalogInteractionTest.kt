package com.software.sello.catalog

import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
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

class EntryCatalogInteractionTest {
    @get:Rule
    val rule = createAndroidComposeRule<CatalogActivity>()

    // A chip sits in its strip's sideways scroll inside the entry's vertical one, and
    // performScrollTo moves only the closest: bring the strip into view, then the chip.
    private fun tapChip(strip: String, chip: String) {
        rule.onNodeWithTag(strip).performScrollTo()
        rule.onNodeWithTag(chip).performScrollTo().performClick()
    }

    @Test
    fun theEntryOpensFromTheInputsGroup() {
        rule.onNodeWithTag("link:inputs.entry").performScrollTo().performClick()
        rule.onNodeWithTag("example:inputs.entry").assertIsDisplayed()
        rule.onNodeWithTag("entry:amount").assertIsDisplayed()
    }

    @Test
    fun theScaffoldShowsTheMonthBarWithItsRibbonSlotFilledAndEmptied() {
        rule.onNodeWithTag("link:navigation.scaffold").performScrollTo().performClick()
        rule.onNodeWithTag("scaffold:ribbon").assertIsDisplayed()
        rule.onNodeWithContentDescription("Cambiar de mes. Mes seleccionado: Septiembre 2026")
            .assertIsDisplayed()

        rule.onNodeWithText("Volver a octubre").performClick()
        rule.onNodeWithTag("scaffold:ribbon").assertDoesNotExist()
        rule.onNodeWithContentDescription("Cambiar de mes. Mes actual: Octubre 2026")
            .assertIsDisplayed()

        rule.onNodeWithTag("scaffold:month").performClick()
        rule.onNodeWithTag("scaffold:ribbon").assertIsDisplayed()
    }

    @Test
    fun noteComposerCountsUnicodeCharactersAndSavesThem() {
        rule.onNodeWithTag("link:inputs.note-composer").performScrollTo().performClick()
        rule.onNodeWithTag("composer:open").performClick()
        val note = "🙂".repeat(31)
        rule.onNode(hasSetTextAction()).performTextInput(note)
        rule.onNodeWithText("31/60").assertExists()
        rule.onNodeWithContentDescription("Listo").assertIsEnabled().performClick()
        rule.onNodeWithTag("composer:saved").assertTextContains(note)
    }

    @Test
    fun expenseNoteSheetNamesItsEntryAndMarksTheNoteOptional() {
        rule.onNodeWithTag("link:inputs.entry").performScrollTo().performClick()
        tapChip("entry:choices", "entry:choice:1")
        rule.onNodeWithTag("entry:note").performScrollTo().performClick()
        rule.onNodeWithText("Gasto en Comida", substring = true).assertExists()
        rule.onNodeWithText("Opcional").assertExists()
        rule.onNodeWithContentDescription("Listo").assertIsEnabled()
    }

    @Test
    fun otherSourceIsAFullWidthLineWithAChangeAction() {
        rule.onNodeWithTag("link:inputs.entry").performScrollTo().performClick()
        rule.onNodeWithText("Ingreso").performClick()
        tapChip("entry:choices", "entry:choice:4")
        rule.onNodeWithTag("entry:source").performScrollTo().performClick()
        rule.onNode(hasSetTextAction()).performTextInput("Venta de la bicicleta")
        rule.onNodeWithContentDescription("Listo").performClick()
        val line = rule.onNodeWithTag("entry:source").getUnclippedBoundsInRoot()
        val amount = rule.onNodeWithTag("entry:amount").getUnclippedBoundsInRoot()
        assertEquals(amount.left, line.left)
        assertEquals(amount.right, line.right)
        rule.onNodeWithText("Cambiar").assertExists()
    }

    @Test
    fun expenseCategoriesEndWithANuevaButtonThatIsNotAChoice() {
        rule.onNodeWithTag("link:inputs.entry").performScrollTo().performClick()
        rule.onNodeWithTag("entry:choices").performScrollTo()
        rule.onNodeWithText("Nueva").performScrollTo()
            .assert(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button))
            .assert(SemanticsMatcher.keyNotDefined(SemanticsProperties.Selected))
        rule.onNodeWithText("Ingreso").performScrollTo().performClick()
        rule.onNodeWithText("Nueva").assertDoesNotExist()
    }

    @Test
    fun unknownOutcomeOffersCheckingAndLocksTheKeypad() {
        rule.onNodeWithTag("link:inputs.entry").performScrollTo().performClick()
        tapChip("entry:states", "entry:state:3")
        rule.onNodeWithTag(
            "entry:submit"
        ).assertTextContains("Comprobar si se guardó").assertIsEnabled()
        rule.onNodeWithText("1").assertIsNotEnabled()
    }

    @Test
    fun restoredDraftSurvivesRecreationWithoutSubmitting() {
        rule.onNodeWithTag("link:inputs.entry").performScrollTo().performClick()
        tapChip("entry:states", "entry:state:4")
        rule.activityRule.scenario.recreate()
        rule.onNodeWithText("Almuerzo").assertExists()
        rule.onNodeWithTag("entry:submit").assertIsEnabled()
    }
}
