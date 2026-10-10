package com.software.sello.feature.expense

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.software.sello.designsystem.component.MoneySign
import com.software.sello.designsystem.component.MoneyTextValue
import com.software.sello.designsystem.theme.SelloTheme
import com.software.sello.presentation.date.dateLabels
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** The form alone, given state by hand: what each state shows and what each control reports. */
@RunWith(AndroidJUnit4::class)
class ExpenseEntryScreenTest {
    @get:Rule
    val rule = createComposeRule()

    private val actions = mutableListOf<ExpenseEntryAction>()
    private val today = LocalDate.of(2026, 10, 9)
    private val food = EntryCategory("a", "Alimentación", "restaurant")
    private val coffee = EntryCategory("b", "Café", "local_cafe")

    private fun shown(digits: String) = MoneyTextValue(MoneySign.None, "", digits, "$digits pesos")

    private val empty = ExpenseEntryState(
        loading = false,
        categories = listOf(food, coffee),
        date = today,
        today = today
    )
    private val complete = empty.copy(
        amount = shown("48.700"),
        categoryId = "a",
        missing = null,
        preview = EntryPreview(
            PreviewLine(PreviewLine.Kind.Remaining, shown("888.500")),
            PreviewLine(PreviewLine.Kind.Over, shown("12.300"))
        )
    )

    private fun show(state: ExpenseEntryState, fontScale: Float = 1f) {
        rule.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale)) {
                SelloTheme(reducedMotion = true) {
                    ExpenseEntryScreen(state, dateLabels(), { actions += it })
                }
            }
        }
    }

    private fun key(label: String) =
        rule.onNode(hasText(label) and hasAnyAncestor(hasTestTag(ENTRY_KEYPAD_TAG)))

    @Test
    fun anEmptyFormSaysWhatIsMissingAndCannotBeSubmitted() {
        show(empty)

        rule.onNodeWithText("Hoy, vie 9 oct, renglón nuevo").assertIsDisplayed()
        rule.onNodeWithText("Elige una categoría").assertIsDisplayed()
        rule.onNodeWithText("Escribe un monto.").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag(ENTRY_SUBMIT_TAG).performScrollTo().assertIsNotEnabled()
        rule.onNodeWithTag(ENTRY_PREVIEW_TAG).assertDoesNotExist()
        rule.onNodeWithTag(ENTRY_RECEIPT_TAG).assertDoesNotExist()
    }

    @Test
    fun everyControlReportsWhatWasDone() {
        show(complete)

        key("7").performScrollTo().performClick()
        key("000").performScrollTo().performClick()
        rule.onNodeWithText("Café").performScrollTo().performClick()
        rule.onNodeWithTag(ENTRY_DATE_TAG).performScrollTo().performClick()
        rule.onNodeWithTag(ENTRY_SUBMIT_TAG).performScrollTo().performClick()
        rule.onNodeWithContentDescription("Cerrar").performClick()

        assertEquals(
            listOf(
                ExpenseEntryAction.Digit(7),
                ExpenseEntryAction.TripleZero,
                ExpenseEntryAction.CategoryPicked("b"),
                ExpenseEntryAction.OpenDatePicker,
                ExpenseEntryAction.Submit,
                ExpenseEntryAction.Back
            ),
            actions
        )
    }

    @Test
    fun theAmountIsReadInFullTheChipsAreOneChoiceAndTheButtonSaysTheWholeSentence() {
        show(complete)

        rule.onNodeWithContentDescription("48.700 pesos").assertIsDisplayed()
        rule.onNodeWithText("Alimentación", useUnmergedTree = false)
        rule.onNode(hasText("Alimentación") and hasAnyAncestor(hasTestTag(ENTRY_CATEGORIES_TAG)))
            .performScrollTo().assertIsSelected()
        rule.onNode(hasText("Café") and hasAnyAncestor(hasTestTag(ENTRY_CATEGORIES_TAG)))
            .assertIsNotSelected()
        rule.onNodeWithContentDescription("Anotar gasto de 48.700 pesos en Alimentación")
            .performScrollTo().assertIsEnabled()
        rule.onNodeWithContentDescription("Fecha: Hoy, vie 9 oct").assertExists()
    }

    @Test
    fun thePreviewIsLabelledAsOneAndSaysHowFarOverACategoryWouldGo() {
        show(complete)

        rule.onNodeWithText("Vista previa, si lo anotas").assertIsDisplayed()
        rule.onNodeWithText("Te quedarán").assertIsDisplayed()
        rule.onNodeWithContentDescription("888.500 pesos").assertIsDisplayed()
        rule.onNodeWithText("En Alimentación te pasas por").assertIsDisplayed()
        rule.onNodeWithContentDescription("12.300 pesos").assertIsDisplayed()
    }

    @Test
    fun aRefusedPasteIsShownAsItWasWithTheReason() {
        show(empty.copy(amountText = "1e3", amountProblem = AmountProblem.NotAnAmount))

        rule.onNodeWithText("1e3", substring = true).assertIsDisplayed()
        rule.onNodeWithText(
            "Eso no es un monto. Escribe solo el número, por ejemplo 48700 o 48.700."
        ).assertIsDisplayed()
    }

    @Test
    fun whileSavingNothingCanBePressed() {
        show(complete.copy(busy = true))

        key("7").performScrollTo().performClick()
        rule.onNodeWithText("Café").performScrollTo().assertIsNotEnabled()
        rule.onNodeWithTag(ENTRY_SUBMIT_TAG).performScrollTo().performClick()

        assertEquals(emptyList<ExpenseEntryAction>(), actions)
    }

    @Test
    fun eachWayOfNotSavingSaysThatNothingWasRecorded() {
        show(
            complete.copy(
                error = EntryError.CategoryGone,
                categoryId = null,
                missing = Missing.Category
            )
        )

        rule.onNodeWithText("Esa categoría ya no acepta gastos. Elige otra; no se anotó nada.")
            .performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag(ENTRY_SUBMIT_TAG).performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun anUnknownOutcomeOffersToCheckNotToRecordAgain() {
        show(complete.copy(error = EntryError.Unknown))

        rule.onNodeWithText("Anotar gasto").assertDoesNotExist()
        rule.onNodeWithText("Comprobar si se guardó").performScrollTo().performClick()

        assertEquals(listOf<ExpenseEntryAction>(ExpenseEntryAction.Retry), actions)
    }

    @Test
    fun theReceiptShowsTheStoredExpenseItsStampAndOnlyAWayOut() {
        val stamped = SemanticsMatcher.expectValue(
            SemanticsProperties.ContentDescription,
            listOf("Recibido")
        )
        show(
            complete.copy(
                receipt = ExpenseReceiptView(
                    "e",
                    ExpenseReceiptView.Details(
                        shown("48.700"),
                        "Alimentación",
                        "restaurant",
                        LocalDate.of(2026, 10, 8),
                        "Pan y café"
                    )
                )
            )
        )

        rule.onNodeWithText("Gasto anotado").assertIsDisplayed()
        rule.onNode(stamped.or(hasText("Recibido", ignoreCase = true))).assertExists()
        rule.onNodeWithText("Alimentación").assertIsDisplayed()
        rule.onNodeWithText("jue 8 oct").assertIsDisplayed()
        rule.onNodeWithText("Pan y café").assertIsDisplayed()
        rule.onNodeWithContentDescription("48.700 pesos").assertIsDisplayed()
        rule.onNodeWithTag(ENTRY_SUBMIT_TAG).assertDoesNotExist()
        rule.onNodeWithTag(ENTRY_KEYPAD_TAG).assertDoesNotExist()
        rule.onNodeWithText("Listo").performScrollTo().performClick()

        assertEquals(listOf<ExpenseEntryAction>(ExpenseEntryAction.Done), actions)
    }

    @Test
    fun aSavedExpenseThatCouldNotBeReadBackStillSaysItIsSaved() {
        show(complete.copy(receipt = ExpenseReceiptView("e", details = null)))

        rule.onNodeWithText("Gasto anotado").assertIsDisplayed()
        rule.onNodeWithText("Tu gasto quedó guardado, pero no pudimos leer su detalle ahora.")
            .assertIsDisplayed()
    }

    @Test
    fun discardingIsAskedForAndEitherAnswerIsReported() {
        show(complete.copy(confirmDiscard = true))

        rule.onNodeWithText("¿Descartar este gasto?").assertIsDisplayed()
        rule.onNodeWithText("Seguir anotando").performClick()
        rule.onNodeWithText("Descartar").performClick()

        assertEquals(
            listOf(ExpenseEntryAction.KeepEditing, ExpenseEntryAction.DiscardConfirmed),
            actions
        )
    }

    @Test
    fun whenCategoriesCannotBeReadThereIsNoFormAndRetryIsOffered() {
        show(empty.copy(loadFailed = true))

        rule.onNodeWithTag(ENTRY_LOAD_FAILED_TAG).assertIsDisplayed()
        rule.onNodeWithTag(ENTRY_SUBMIT_TAG).assertDoesNotExist()
        rule.onNodeWithText("Reintentar").performClick()

        assertEquals(listOf<ExpenseEntryAction>(ExpenseEntryAction.Retry), actions)
    }

    @Test
    fun atTwiceTheFontSizeEveryControlCanStillBeReached() {
        show(complete, fontScale = 2f)

        rule.onNodeWithContentDescription("48.700 pesos").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Café").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag(ENTRY_DATE_TAG).performScrollTo().assertIsDisplayed()
        key("000").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag(ENTRY_SUBMIT_TAG).performScrollTo().assertIsDisplayed().assertIsEnabled()
    }
}
