package com.software.sello.catalog

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performTextInput
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
        rule.onNodeWithText("Listo").assertIsEnabled().performClick()
        rule.onNodeWithTag("v2:composer:saved").assertTextContains(note)
    }

    @Test
    fun unknownOutcomeOffersCheckingAndLocksTheKeypad() {
        rule.onNodeWithTag("link:v2.entry").performScrollTo().performClick()
        rule.onNodeWithTag("v2:entry:states").performScrollTo().performScrollToIndex(3)
        rule.onNodeWithTag("v2:entry:state:3").performClick()
        rule.onNodeWithTag(
            "v2:entry:submit"
        ).assertTextContains("Comprobar si se guardó").assertIsEnabled()
        rule.onNodeWithText("1").assertIsNotEnabled()
    }

    @Test
    fun restoredDraftSurvivesRecreationWithoutSubmitting() {
        rule.onNodeWithTag("link:v2.entry").performScrollTo().performClick()
        rule.onNodeWithTag("v2:entry:states").performScrollTo().performScrollToIndex(4)
        rule.onNodeWithTag("v2:entry:state:4").performClick()
        rule.activityRule.scenario.recreate()
        rule.onNodeWithText("Almuerzo").assertExists()
        rule.onNodeWithTag("v2:entry:submit").assertIsEnabled()
    }
}
