package com.software.sello.feature.startup

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.software.sello.designsystem.theme.SelloTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** The screen alone, given state by hand: what each state shows and what each control reports. */
@RunWith(AndroidJUnit4::class)
class StartupFailureScreenTest {
    @get:Rule
    val rule = createComposeRule()

    private val actions = mutableListOf<StartupFailureAction>()

    private fun show(state: StartupFailureState, fontScale: Float = 1f) {
        rule.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale)) {
                SelloTheme(reducedMotion = true) {
                    StartupFailureScreen(state, { actions += it })
                }
            }
        }
    }

    /** The field is found by its label, which is also what a screen reader calls it. */
    private val word get() = rule.onNodeWithContentDescription("Escribe BORRAR para confirmar")

    @Test
    fun itSaysTheDataCouldNotBeOpenedAndThatNothingWasDeleted() {
        show(StartupFailureState())

        rule.onNodeWithText("No pudimos abrir tus datos").assertIsDisplayed()
        rule.onNodeWithText("Sello no pudo leer lo que tienes guardado. No se borró nada.")
            .assertIsDisplayed()
        rule.onNodeWithTag(STARTUP_STILL_FAILING_TAG).assertDoesNotExist()
        rule.onNodeWithTag(STARTUP_NOT_SET_ASIDE_TAG).assertDoesNotExist()
        rule.onNodeWithTag(STARTUP_WARNING_TAG).assertDoesNotExist()
    }

    @Test
    fun tryingAgainAndStartingOverAreSeparateChoicesAndNeitherConfirmsAnything() {
        show(StartupFailureState())

        rule.onNodeWithTag(STARTUP_RETRY_TAG).performClick()
        rule.onNodeWithTag(STARTUP_START_OVER_TAG).performClick()

        assertEquals(
            listOf(StartupFailureAction.Retry, StartupFailureAction.StartOver),
            actions
        )
    }

    @Test
    fun aRetryThatDidNotWorkIsSaid() {
        show(StartupFailureState(stillFailing = true))

        rule.onNodeWithTag(STARTUP_STILL_FAILING_TAG).assertIsDisplayed()
        rule.onNodeWithText("Todavía no se pueden abrir tus datos.").assertIsDisplayed()
    }

    @Test
    fun theWarningSaysWhatStartingOverDoesAndCannotBeConfirmedWithoutTheWord() {
        show(StartupFailureState(confirming = true, typed = "BORRA"))

        rule.onNodeWithTag(STARTUP_WARNING_TAG).performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("¿Empezar de cero?").assertIsDisplayed()
        rule.onNodeWithText("quedan guardados aparte", substring = true).assertIsDisplayed()
        rule.onNodeWithTag(STARTUP_CONFIRM_TAG).performScrollTo().assertIsNotEnabled()
        rule.onNodeWithTag(STARTUP_CONFIRM_TAG).performClick()

        assertEquals(emptyList<StartupFailureAction>(), actions)
    }

    @Test
    fun whatIsTypedIsReportedExactly() {
        show(StartupFailureState(confirming = true))

        word.performScrollTo().performTextInput("borrar")

        assertEquals(listOf<StartupFailureAction>(StartupFailureAction.Typed("borrar")), actions)
    }

    @Test
    fun withTheWordTypedTheConfirmationAndTheWayOutBothWork() {
        show(StartupFailureState(confirming = true, typed = "BORRAR"))

        rule.onNodeWithTag(STARTUP_CONFIRM_TAG).performScrollTo().assertIsEnabled().performClick()
        rule.onNodeWithTag(STARTUP_CANCEL_TAG).performScrollTo().performClick()

        assertEquals(
            listOf(StartupFailureAction.Confirm, StartupFailureAction.Cancel),
            actions
        )
    }

    @Test
    fun dataThatCouldNotBeMovedIsSaidAndNothingIsClaimed() {
        show(StartupFailureState(notSetAside = true))

        rule.onNodeWithTag(STARTUP_NOT_SET_ASIDE_TAG).performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("No pudimos apartar tus datos. No se cambió nada.").assertIsDisplayed()
    }

    @Test
    fun atTwiceTheFontSizeEveryChoiceCanStillBeReached() {
        show(StartupFailureState(confirming = true, typed = "BORRAR"), fontScale = 2f)

        rule.onNodeWithTag(STARTUP_RETRY_TAG).performScrollTo().assertIsDisplayed()
        word.performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag(STARTUP_CANCEL_TAG).performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag(STARTUP_CONFIRM_TAG).performScrollTo().assertIsDisplayed().performClick()

        assertEquals(listOf<StartupFailureAction>(StartupFailureAction.Confirm), actions)
    }
}
