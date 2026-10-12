package com.software.sello.feature.recibo

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.software.sello.designsystem.component.ACTION_LINE_MENU_TAG
import com.software.sello.designsystem.component.MoneySign
import com.software.sello.designsystem.component.MoneyTextValue
import com.software.sello.designsystem.theme.SelloTheme
import com.software.sello.presentation.date.dateLabels
import com.software.sello.presentation.month.monthNames
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CategoryDetailScreenTest {
    @get:Rule
    val rule = createComposeRule()

    private val actions = mutableListOf<CategoryDetailAction>()
    private var recordHere = 0
    private val edited = mutableListOf<String>()

    private fun show(state: CategoryDetailState, fontScale: Float = 1f, showName: Boolean = false) {
        rule.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale)) {
                SelloTheme(reducedMotion = true) {
                    CategoryDetailScreen(
                        state = state,
                        months = monthNames(),
                        dates = dateLabels(),
                        showName = showName,
                        onAction = { actions += it },
                        onRecordHere = { recordHere++ },
                        contentPadding = PaddingValues(12.dp),
                        onEditExpense = { edited += it }
                    )
                }
            }
        }
    }

    private fun plain(digits: String) = MoneyTextValue(MoneySign.None, "", digits, "$digits pesos")

    private val food = ReciboCategory(
        id = "a",
        name = "Alimentación",
        iconKey = "restaurant",
        archived = false,
        figure = BudgetFigure(ReciboHeadline.Remaining, plain("287.600"), ReciboStamp.OnTrack),
        cellAmount = plain("287.600"),
        spent = plain("612.400"),
        limit = plain("900.000"),
        fraction = 0.68f,
        over = false,
        expenseCount = 13
    )

    private val rows = listOf(
        ExpenseRow("1", LocalDate.of(2026, 10, 21), "Rappi, almuerzo", plain("20.600"), false),
        ExpenseRow("2", LocalDate.of(2026, 10, 20), null, plain("33.500"), false),
        ExpenseRow("3", LocalDate.of(2026, 10, 28), "Adelantado", plain("1.000"), true)
    )

    private fun ready(
        expenses: ExpenseListState,
        category: ReciboCategory = food,
        stale: Boolean = false,
        canRecord: Boolean = true
    ) = CategoryDetailState.Ready(
        category = category,
        month = YearMonth.of(2026, 10),
        today = LocalDate.of(2026, 10, 21),
        stale = stale,
        canRecord = canRecord,
        expenses = expenses
    )

    private fun inRow(id: String, text: String) =
        hasText(text) and hasAnyAncestor(hasTestTag(categoryExpenseTag(id)))

    @Test
    fun theHeaderSaysWhatIsLeftSpentAndTheLimitExactlyAndInWords() {
        show(ready(ExpenseListState.Rows(rows, more = false)))

        rule.onNodeWithContentDescription("Te quedan aquí 287.600 pesos. Al día.")
            .assertIsDisplayed()
        // A line is read as one sentence, so its parts are looked for inside it.
        rule.onNode(
            hasContentDescription("612.400 pesos") and
                hasAnyAncestor(hasTestTag(CATEGORY_DETAIL_SPENT_TAG)),
            useUnmergedTree = true
        ).assertExists()
        rule.onNode(
            hasContentDescription("900.000 pesos") and
                hasAnyAncestor(hasTestTag(CATEGORY_DETAIL_LIMIT_TAG)),
            useUnmergedTree = true
        ).assertExists()
        // Its own screen is titled by the frame; beside Recibo it names itself.
        rule.onNodeWithText("Alimentación").assertDoesNotExist()
    }

    @Test
    fun besideReciboItNamesTheCategoryItself() {
        show(ready(ExpenseListState.Rows(rows, more = false)), showName = true)

        rule.onNodeWithText("Alimentación").assertIsDisplayed()
    }

    @Test
    fun eachExpenseShowsItsNoteDayAndExactAmountAndTheCountIsOfAllOfThem() {
        show(ready(ExpenseListState.Rows(rows, more = false)))

        rule.onNodeWithText("13 gastos").performScrollTo().assertIsDisplayed()
        rule.onNode(inRow("1", "Rappi, almuerzo"), useUnmergedTree = true).assertExists()
        rule.onNode(inRow("1", "Hoy, mié 21 oct"), useUnmergedTree = true).assertExists()
        rule.onNode(
            hasContentDescription("20.600 pesos") and
                hasAnyAncestor(hasTestTag(categoryExpenseTag("1"))),
            useUnmergedTree = true
        ).assertExists()
        rule.onNode(inRow("2", "Sin nota"), useUnmergedTree = true).assertExists()
        rule.onNode(inRow("2", "mar 20 oct"), useUnmergedTree = true).assertExists()
        rule.onNode(
            inRow("3", "mié 28 oct. Fecha posterior a hoy: todavía no cuenta"),
            useUnmergedTree = true
        ).assertExists()
        // Everything is listed, so there is nothing more to ask for.
        rule.onNodeWithTag(CATEGORY_DETAIL_MORE_TAG).assertDoesNotExist()
    }

    @Test
    fun aPartOfTheListSaysHowManyAreShownOfHowManyAndOffersTheRest() {
        show(ready(ExpenseListState.Rows(rows, more = true)))

        rule.onNodeWithText("Se muestran 3 de 13").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag(CATEGORY_DETAIL_MORE_TAG).performScrollTo().performClick()

        assertEquals(listOf<CategoryDetailAction>(CategoryDetailAction.ShowMore), actions)
        rule.onNodeWithTag(CATEGORY_DETAIL_MORE_FAILED_TAG).assertDoesNotExist()
    }

    @Test
    fun whileMoreIsBeingReadTheButtonCannotBePressedAndAFailureToReadMoreIsSaid() {
        show(ready(ExpenseListState.Rows(rows, more = true, loadingMore = true)))
        rule.onNodeWithTag(CATEGORY_DETAIL_MORE_TAG).performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun aFailureToReadMoreKeepsTheRowsAndSaysSo() {
        show(ready(ExpenseListState.Rows(rows, more = true, moreFailed = true)))

        rule.onNodeWithText("No pudimos leer más gastos. Inténtalo de nuevo.")
            .performScrollTo().assertIsDisplayed()
        rule.onNode(inRow("1", "Rappi, almuerzo"), useUnmergedTree = true).assertExists()
        rule.onNodeWithTag(CATEGORY_DETAIL_MORE_TAG).performScrollTo().performClick()

        assertEquals(listOf<CategoryDetailAction>(CategoryDetailAction.ShowMore), actions)
    }

    @Test
    fun noExpensesIsSaidForThatMonthWithAWayToRecordOneHere() {
        show(ready(ExpenseListState.Empty))

        rule.onNodeWithTag(CATEGORY_DETAIL_EMPTY_TAG).performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Sin gastos en octubre").assertIsDisplayed()
        rule.onNodeWithText("Anotar aquí").performScrollTo().performClick()

        assertEquals(1, recordHere)
        rule.onNodeWithTag(CATEGORY_DETAIL_LIST_FAILED_TAG).assertDoesNotExist()
    }

    @Test
    fun whereNothingCanBeRecordedTheEmptyListOffersNothing() {
        show(ready(ExpenseListState.Empty, canRecord = false))

        rule.onNodeWithText("Sin gastos en octubre").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Anotar aquí").assertDoesNotExist()
    }

    @Test
    fun aListThatCouldNotBeReadIsAnErrorWithTheFiguresStillShownAndNeverNoExpenses() {
        show(ready(ExpenseListState.Failed))

        rule.onNodeWithTag(CATEGORY_DETAIL_LIST_FAILED_TAG).performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("No pudimos leer los gastos de esta categoría.").assertIsDisplayed()
        rule.onNodeWithContentDescription("Te quedan aquí 287.600 pesos. Al día.").assertExists()
        rule.onNodeWithTag(CATEGORY_DETAIL_EMPTY_TAG).assertDoesNotExist()
        rule.onNodeWithText("Sin gastos en octubre").assertDoesNotExist()
        rule.onNodeWithText("Reintentar").performScrollTo().performClick()

        assertEquals(listOf<CategoryDetailAction>(CategoryDetailAction.Retry), actions)
    }

    @Test
    fun aCategoryWithoutALimitSaysWhatWasSpentAndWhyThereIsNothingToJudge() {
        val art = food.copy(
            name = "Arte",
            figure = BudgetFigure(ReciboHeadline.Spent, plain("80.000"), ReciboStamp.NoLimit),
            spent = plain("80.000"),
            limit = null,
            fraction = null,
            archived = true
        )

        show(ready(ExpenseListState.Rows(rows, more = false), category = art))

        rule.onNodeWithContentDescription("Has gastado aquí 80.000 pesos. Sin límite.")
            .assertIsDisplayed()
        rule.onNode(
            hasText("Sin límite") and hasAnyAncestor(hasTestTag(CATEGORY_DETAIL_LIMIT_TAG)),
            useUnmergedTree = true
        ).assertExists()
        rule.onNodeWithText("Archivada").assertIsDisplayed()
    }

    @Test
    fun anOverspentCategoryAndAFinishedMonthSayItInWords() {
        val over = food.copy(
            figure = BudgetFigure(ReciboHeadline.Overspent, plain("21.500"), ReciboStamp.OverLimit),
            over = true
        )

        show(ready(ExpenseListState.Rows(rows, more = false), category = over))

        rule.onNodeWithContentDescription("Te pasaste aquí por 21.500 pesos. Sobre el límite.")
            .assertIsDisplayed()
    }

    @Test
    fun olderFiguresCarryANoticeAndFailedGoneAndLoadingAreTheirOwnStates() {
        show(ready(ExpenseListState.Rows(rows, more = false), stale = true))

        rule.onNodeWithTag(CATEGORY_DETAIL_STALE_TAG).assertIsDisplayed()
        rule.onNodeWithText("Reintentar").performClick()

        assertEquals(listOf<CategoryDetailAction>(CategoryDetailAction.Retry), actions)
    }

    @Test
    fun aCategoryThatCouldNotBeReadShowsNoFigureAndOffersRetry() {
        show(CategoryDetailState.Failed)

        rule.onNodeWithTag(CATEGORY_DETAIL_FAILED_TAG).assertIsDisplayed()
        rule.onNodeWithText("No pudimos leer esta categoría.").assertIsDisplayed()
        rule.onNodeWithTag(CATEGORY_DETAIL_HEADER_TAG).assertDoesNotExist()
        rule.onNodeWithText("Reintentar").performClick()

        assertEquals(listOf<CategoryDetailAction>(CategoryDetailAction.Retry), actions)
    }

    @Test
    fun aCategoryWithNothingInTheMonthSaysSoAndLoadingShowsNoFigure() {
        show(CategoryDetailState.Gone)

        rule.onNodeWithTag(CATEGORY_DETAIL_GONE_TAG).assertIsDisplayed()
        rule.onNodeWithText("Nada aquí este mes").assertIsDisplayed()
        rule.onNodeWithTag(CATEGORY_DETAIL_HEADER_TAG).assertDoesNotExist()
    }

    @Test
    fun atTwiceTheFontSizeWithVeryLargeAmountsEverythingCanStillBeReached() {
        val huge = "8.998.000.000.000.001"
        val category = food.copy(
            figure = BudgetFigure(ReciboHeadline.Remaining, plain(huge), ReciboStamp.Tight),
            spent = plain(huge),
            limit = plain(huge)
        )
        val long = ExpenseRow(
            "9",
            LocalDate.of(2025, 12, 31),
            "Una nota tan larga como se permite escribir en un gasto, sí",
            plain("999.999.999.999"),
            false
        )

        show(
            ready(ExpenseListState.Rows(listOf(long), more = true), category = category),
            fontScale = 2f,
            showName = true
        )

        rule.onNodeWithContentDescription("Te quedan aquí $huge pesos. Justo.")
            .performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag(CATEGORY_DETAIL_SPENT_TAG).performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag(CATEGORY_DETAIL_LIMIT_TAG).performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag(categoryExpenseTag("9")).performScrollTo().assertIsDisplayed()
        rule.onNode(inRow("9", "mié 31 dic 2025"), useUnmergedTree = true).assertExists()
        rule.onNodeWithTag(CATEGORY_DETAIL_MORE_TAG).performScrollTo().assertIsDisplayed()
    }

    private fun numbered(numbers: List<Int>) = ExpenseListState.Rows(
        numbers.map {
            ExpenseRow(
                "n$it",
                LocalDate.of(2026, 10, 1 + it % 20),
                "Gasto $it",
                plain("${1000 + it}"),
                false
            )
        },
        more = false
    )

    /** Which piece of the screen is drawing each expense, by the expense's identifier. */
    private fun drawnBy(numbers: List<Int>): Map<Int, Int> = numbers.associateWith {
        rule.onNodeWithTag(categoryExpenseTag("n$it")).fetchSemanticsNode().id
    }

    private fun showChanging(first: ExpenseListState): (ExpenseListState) -> Unit {
        var state by mutableStateOf<CategoryDetailState>(ready(first))
        rule.setContent {
            SelloTheme(reducedMotion = true) {
                CategoryDetailScreen(
                    state = state,
                    months = monthNames(),
                    dates = dateLabels(),
                    showName = false,
                    onAction = { actions += it },
                    onRecordHere = { recordHere++ },
                    contentPadding = PaddingValues(12.dp)
                )
            }
        }
        return { next ->
            state = ready(next)
            rule.waitForIdle()
        }
    }

    @Test
    fun aNewExpenseAtTheTopLeavesEveryRowThatWasThereBeingTheSameRow() {
        val before = (1..30).toList()
        val change = showChanging(numbered(before))
        val drawn = drawnBy(before)

        // Saved while the list is open: it goes first and the others move down one.
        change(numbered(listOf(99) + before))

        assertEquals(drawn, drawnBy(before))
        rule.onNodeWithTag(categoryExpenseTag("n99")).assertExists()
    }

    @Test
    fun aRowTakenOutOfTheMiddleLeavesTheOthersBeingTheSameRows() {
        val before = (1..30).toList()
        val change = showChanging(numbered(before))
        val after = before - 12
        val drawn = drawnBy(after)

        change(numbered(after))

        assertEquals(drawn, drawnBy(after))
        rule.onNodeWithTag(categoryExpenseTag("n12")).assertDoesNotExist()
    }

    @Test
    fun rowsThatChangePlacesAreStillThemselves() {
        val before = (1..10).toList()
        val change = showChanging(numbered(before))
        val drawn = drawnBy(before)

        change(numbered(before.reversed()))

        assertEquals(drawn, drawnBy(before))
        // And each still shows its own text, in its new place.
        rule.onNode(inRow("n7", "Gasto 7"), useUnmergedTree = true).assertExists()
    }

    // --- Correcting an expense from the list ---

    @Test
    fun pressingAnExpenseOpensItToBeCorrectedAndDeletesNothing() {
        show(ready(ExpenseListState.Rows(rows, more = false)))

        rule.onNodeWithTag(categoryExpenseTag("2")).performScrollTo().performClick()

        assertEquals(listOf("2"), edited)
        assertEquals(emptyList<CategoryDetailAction>(), actions)
    }

    @Test
    fun aLongPressOffersToEditOrDeleteThatExpense() {
        show(ready(ExpenseListState.Rows(rows, more = false)))

        rule.onNodeWithTag(categoryExpenseTag("1")).performScrollTo()
            .performTouchInput { longClick() }
        rule.onNode(hasText("Borrar") and hasAnyAncestor(hasTestTag(ACTION_LINE_MENU_TAG)))
            .performClick()
        rule.onNodeWithTag(categoryExpenseTag("2")).performTouchInput { longClick() }
        rule.onNode(hasText("Editar") and hasAnyAncestor(hasTestTag(ACTION_LINE_MENU_TAG)))
            .performClick()

        assertEquals(listOf<CategoryDetailAction>(CategoryDetailAction.Delete("1")), actions)
        assertEquals(listOf("2"), edited)
    }

    @Test
    fun swipingAnExpenseAwayDeletesThatOneOnce() {
        show(ready(ExpenseListState.Rows(rows, more = false)))

        rule.onNodeWithTag(categoryExpenseTag("3")).performScrollTo()
            .performTouchInput { swipeLeft(startX = right - 4f, endX = left + 4f) }
        rule.waitForIdle()

        assertEquals(listOf<CategoryDetailAction>(CategoryDetailAction.Delete("3")), actions)
        assertEquals(emptyList<String>(), edited)
    }

    @Test
    fun aScreenReaderCanEditOrDeleteAnExpenseByNameAndHearsItWhole() {
        show(ready(ExpenseListState.Rows(rows, more = false)))

        val node = rule.onNodeWithTag(categoryExpenseTag("1")).fetchSemanticsNode()
        val offered = node.config[SemanticsActions.CustomActions]
        rule.runOnUiThread { offered.forEach { it.action() } }

        assertEquals(listOf("Editar", "Borrar"), offered.map { it.label })
        assertEquals(listOf("1"), edited)
        assertEquals(listOf<CategoryDetailAction>(CategoryDetailAction.Delete("1")), actions)
        // Read as one line: what, when and exactly how much.
        rule.onNode(inRow("1", "Rappi, almuerzo"), useUnmergedTree = true).assertExists()
        rule.onNode(
            hasContentDescription("20.600 pesos") and
                hasAnyAncestor(hasTestTag(categoryExpenseTag("1"))),
            useUnmergedTree = true
        ).assertExists()
    }

    @Test
    fun theListSaysHowAnExpenseIsCorrectedOrDeleted() {
        show(ready(ExpenseListState.Rows(rows, more = false)))

        rule.onNodeWithTag(CATEGORY_DETAIL_HINT_TAG).performScrollTo().assertIsDisplayed()
        rule.onNodeWithText(
            "Toca un gasto para corregirlo. Para borrarlo, deslízalo o mantenlo presionado."
        ).assertExists()
    }

    @Test
    fun aDeletionThatDidNotHappenIsSaidAndTheExpenseStaysListed() {
        val texts = mapOf(
            DeleteProblem.Gone to "Ese gasto ya estaba borrado.",
            DeleteProblem.Changed to "Ese gasto cambió mientras tanto. No se borró; revísalo.",
            DeleteProblem.DataChanged to "Tus datos cambiaron mientras tanto. No se borró nada.",
            DeleteProblem.Failed to "No se pudo borrar. El gasto sigue anotado."
        )
        var problem by mutableStateOf(DeleteProblem.Gone)
        rule.setContent {
            SelloTheme(reducedMotion = true) {
                CategoryDetailScreen(
                    state = ready(
                        ExpenseListState.Rows(rows, more = false)
                    ).copy(deleteProblem = problem),
                    months = monthNames(),
                    dates = dateLabels(),
                    showName = false,
                    onAction = { actions += it },
                    onRecordHere = {},
                    contentPadding = PaddingValues(12.dp)
                )
            }
        }

        texts.forEach { (which, text) ->
            problem = which
            rule.waitForIdle()
            rule.onNodeWithTag(
                CATEGORY_DETAIL_DELETE_PROBLEM_TAG
            ).performScrollTo().assertIsDisplayed()
            rule.onNodeWithText(text).assertExists()
            rule.onNodeWithTag(categoryExpenseTag("1")).assertExists()
        }
    }
}
