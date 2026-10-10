package com.software.sello.feature.category

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
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
import com.software.sello.presentation.category.CategoryIcons
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** The form alone, given state by hand: what each state shows and what each control reports. */
@RunWith(AndroidJUnit4::class)
class CategoryEditorScreenTest {
    @get:Rule
    val rule = createComposeRule()

    private val actions = mutableListOf<CategoryEditorAction>()
    private val ready = CategoryEditorState(
        loading = false,
        iconKeys = CategoryIcons.keys,
        iconKey = "home",
        suggestions = listOf(
            LimitSuggestion(200_000, "200.000"),
            LimitSuggestion(300_000, "300.000"),
            LimitSuggestion(500_000, "500.000")
        )
    )

    private fun show(state: CategoryEditorState, fontScale: Float = 1f) {
        rule.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale)) {
                SelloTheme(reducedMotion = true) {
                    CategoryEditorScreen(state, { actions += it })
                }
            }
        }
    }

    /** A field is found by its label, which is also what a screen reader calls it. */
    private val name get() = rule.onNodeWithContentDescription("Nombre")
    private val limit get() = rule.onNodeWithContentDescription("Límite en pesos")

    @Test
    fun whileItLoadsThereIsNoFormAndAFailureOffersRetry() {
        show(CategoryEditorState(loading = false, loadFailed = true))

        rule.onNodeWithTag(EDITOR_LOAD_FAILED_TAG).assertIsDisplayed()
        rule.onNodeWithTag(EDITOR_SUBMIT_TAG).assertDoesNotExist()
        rule.onNodeWithText("Reintentar").performClick()

        assertEquals(listOf<CategoryEditorAction>(CategoryEditorAction.Retry), actions)
    }

    @Test
    fun anEmptyFormCannotBeSubmittedAndStartsUnlimitedWithTheAmountOff() {
        show(ready)

        rule.onNodeWithTag(EDITOR_SUBMIT_TAG).performScrollTo().assertIsNotEnabled()
        rule.onNodeWithTag(EDITOR_UNLIMITED_TAG).assertIsOn()
        limit.assertIsNotEnabled()
        rule.onNodeWithContentDescription("Hogar").assertIsSelected()
        rule.onNodeWithTag(EDITOR_PREREQUISITE_TAG).assertDoesNotExist()
    }

    @Test
    fun everyControlReportsWhatWasDoneAndChangesNothingItself() {
        show(ready.copy(name = "Mer", unlimited = false))

        name.performTextInput("c")
        rule.onNodeWithContentDescription("Mascotas").performScrollTo().performClick()
        rule.onNodeWithTag(EDITOR_UNLIMITED_TAG).performScrollTo().performClick()
        limit.performScrollTo().performTextInput("5")
        rule.onNodeWithText("300.000").performScrollTo().performClick()
        rule.onNodeWithTag(EDITOR_SUBMIT_TAG).performScrollTo().performClick()
        rule.onNodeWithContentDescription("Volver").performClick()

        // The form is given the same state again, so the field goes back to "Mer" and
        // says so; what matters is that the typed text was reported first, as typed.
        assertEquals(CategoryEditorAction.NameChanged("cMer"), actions.first())
        assertEquals(
            listOf(
                CategoryEditorAction.IconPicked("pets"),
                CategoryEditorAction.UnlimitedChanged(true),
                CategoryEditorAction.LimitChanged("5"),
                CategoryEditorAction.SuggestionPicked(300_000),
                CategoryEditorAction.Submit,
                CategoryEditorAction.Back
            ),
            actions.filterNot { it is CategoryEditorAction.NameChanged }
        )
        name.assertTextContains("Mer")
    }

    @Test
    fun errorsAreWrittenOutNextToWhatWasTypedWhichStaysAsItWas() {
        show(
            ready.copy(
                name = "CAFE",
                nameError = NameError.Taken,
                unlimited = false,
                limitText = "1e3",
                limitError = LimitError.NotAnAmount,
                saveError = SaveError.NotSaved
            )
        )

        rule.onNodeWithText("Ya tienes una categoría con ese nombre.").assertIsDisplayed()
        name.assertTextContains("CAFE")
        rule.onNodeWithText("Escribe solo el número, por ejemplo 300000 o 300.000.")
            .performScrollTo().assertIsDisplayed()
        limit.assertTextContains("1e3")
        rule.onNodeWithTag(EDITOR_SAVE_ERROR_TAG).performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag(EDITOR_SUBMIT_TAG).performScrollTo().assertIsEnabled()
    }

    @Test
    fun whileSavingNothingCanBeChangedOrSentAgain() {
        show(ready.copy(name = "Mercado", busy = true))

        name.assertIsNotEnabled()
        rule.onNodeWithContentDescription("Mascotas").performScrollTo().assertIsNotEnabled()
        rule.onNodeWithTag(EDITOR_SUBMIT_TAG).performScrollTo().performClick()

        assertEquals(emptyList<CategoryEditorAction>(), actions)
    }

    @Test
    fun anUnknownOutcomeOffersToCheckNotToCreateAgain() {
        show(ready.copy(name = "Mercado", saveError = SaveError.Unknown))

        rule.onNodeWithText("Crear categoría").assertDoesNotExist()
        name.assertIsNotEnabled()
        rule.onNodeWithText("Comprobar si se guardó").performScrollTo().performClick()

        assertEquals(listOf<CategoryEditorAction>(CategoryEditorAction.Retry), actions)
    }

    @Test
    fun openedForAnEntryItExplainsWhyACategoryComesFirst() {
        show(ready.copy(forEntry = true))

        rule.onNodeWithTag(EDITOR_PREREQUISITE_TAG).assertIsDisplayed()
        rule.onNodeWithText(
            "Para anotar un gasto primero necesitas una categoría. Crea la primera aquí."
        )
            .assertIsDisplayed()
    }

    @Test
    fun discardingADraftIsAskedForAndEitherAnswerIsReported() {
        show(ready.copy(name = "Mer", confirmDiscard = true))

        rule.onNodeWithText("¿Descartar los cambios?").assertIsDisplayed()
        rule.onNodeWithText("Seguir editando").performClick()
        rule.onNodeWithText("Descartar").performClick()

        assertEquals(
            listOf(CategoryEditorAction.KeepEditing, CategoryEditorAction.DiscardConfirmed),
            actions
        )
    }

    @Test
    fun atTwiceTheFontSizeEveryControlCanStillBeReached() {
        show(ready.copy(name = "Mercado de plaza", unlimited = false), fontScale = 2f)

        name.performScrollTo().assertIsDisplayed()
        rule.onNodeWithContentDescription("Hijos").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag(EDITOR_UNLIMITED_TAG).performScrollTo().assertIsOff()
        rule.onNodeWithText("500.000").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag(
            EDITOR_SUBMIT_TAG
        ).performScrollTo().assertIsDisplayed().assertIsEnabled()
    }
}
