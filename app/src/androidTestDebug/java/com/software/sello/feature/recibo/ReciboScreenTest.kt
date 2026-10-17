package com.software.sello.feature.recibo

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.software.sello.designsystem.component.MoneySign
import com.software.sello.designsystem.component.MoneyTextValue
import com.software.sello.designsystem.theme.SelloTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReciboScreenTest {
    @get:Rule
    val rule = createComposeRule()

    private val actions = mutableListOf<ReciboAction>()

    private fun show(state: ReciboState, fontScale: Float = 1f, selected: String? = null) {
        rule.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale)) {
                SelloTheme(reducedMotion = true) {
                    ReciboScreen(
                        state,
                        { actions += it },
                        PaddingValues(12.dp),
                        selectedCategoryId = selected
                    )
                }
            }
        }
    }

    private fun plain(digits: String) = MoneyTextValue(MoneySign.None, "", digits, "$digits pesos")

    private fun limited(
        id: String,
        name: String,
        headline: ReciboHeadline,
        amount: String,
        stamp: ReciboStamp,
        limit: String,
        over: Boolean = false
    ) = ReciboCategory(
        id = id,
        name = name,
        iconKey = "restaurant",
        archived = false,
        figure = BudgetFigure(headline, plain(amount), stamp),
        cellAmount = if (over) {
            MoneyTextValue(MoneySign.Minus, "", amount, "menos $amount pesos")
        } else {
            plain(amount)
        },
        spent = plain("1"),
        limit = plain(limit),
        fraction = 0.5f,
        over = over,
        expenseCount = 1
    )

    private fun open(id: String, name: String, stamp: ReciboStamp, archived: Boolean = false) =
        ReciboCategory(
            id = id,
            name = name,
            iconKey = "una_clave_nueva",
            archived = archived,
            figure = BudgetFigure(ReciboHeadline.Spent, plain("80.000"), stamp),
            cellAmount = plain("80.000"),
            spent = plain("80.000"),
            limit = null,
            fraction = null,
            over = false,
            expenseCount = 8
        )

    private val categories = listOf(
        limited(
            "a",
            "Alimentación",
            ReciboHeadline.Remaining,
            "287.600",
            ReciboStamp.OnTrack,
            "900.000"
        ),
        limited(
            "b",
            "Transporte",
            ReciboHeadline.Remaining,
            "55.000",
            ReciboStamp.Tight,
            "200.000"
        ),
        limited(
            "c",
            "Entretenimiento",
            ReciboHeadline.Overspent,
            "21.500",
            ReciboStamp.Over,
            "100.000",
            over = true
        ),
        open("d", "Arte", ReciboStamp.NoLimit),
        open("e", "Vieja", ReciboStamp.Paused, archived = true),
        open("f", "Nueva", ReciboStamp.NotSet)
    )

    private fun month(
        headline: ReciboHeadline,
        amount: String,
        stamp: ReciboStamp,
        days: Int? = null,
        perDay: String? = null,
        exhausted: Boolean = false,
        stale: Boolean = false
    ) = ReciboState.Month(
        ReciboSummary(
            BudgetFigure(headline, plain(amount), stamp),
            days,
            perDay?.let(::plain),
            exhausted
        ),
        categories,
        stale
    )

    @Test
    fun loadingShowsSkeletonsAndNoFigure() {
        show(ReciboState.Loading)

        rule.onNodeWithTag(RECIBO_LOADING_TAG).assertIsDisplayed()
        rule.onNodeWithTag(RECIBO_SUMMARY_TAG).assertDoesNotExist()
        rule.onNodeWithTag(RECIBO_CATEGORIES_TAG).assertDoesNotExist()
    }

    @Test
    fun theFirstRunOffersToCreateCategoriesAndNothingThatDoesNotExistYet() {
        show(ReciboState.FirstRun)

        rule.onNodeWithText("Dale forma a tu mes").assertIsDisplayed()
        rule.onNodeWithText(
            "Crea una categoría para lo que importa: la casa, la comida, el ocio o tu ahorro."
        ).assertIsDisplayed()
        rule.onNodeWithTag(RECIBO_FAILED_TAG).assertDoesNotExist()
        rule.onNodeWithText("Crear categoría").performClick()

        assertEquals(listOf<ReciboAction>(ReciboAction.CreateCategory), actions)
        // No backup or entry action before those exist.
        rule.onNodeWithText("Traer mis datos de un respaldo").assertDoesNotExist()
        rule.onNodeWithTag(RECIBO_SUMMARY_TAG).assertDoesNotExist()
    }

    @Test
    fun theMonthInProgressIsReadAsOneSentenceWithItsVerdictDaysAndDailyAmount() {
        show(month(ReciboHeadline.Remaining, "937.200", ReciboStamp.OnTrack, 9, "99.100"))

        rule.onNodeWithContentDescription("Te quedan 937.200 pesos. Al día.").assertIsDisplayed()
        rule.onNodeWithText("Para 9 días").assertIsDisplayed()
        rule.onNodeWithText("Puedes gastar al día").assertIsDisplayed()
        rule.onNodeWithContentDescription("99.100 pesos").assertIsDisplayed()
        rule.onNodeWithTag(RECIBO_STALE_TAG).assertDoesNotExist()
    }

    @Test
    fun theLastDaySaysTodayAndAnOverspentMonthSaysByHowMuch() {
        show(month(ReciboHeadline.Overspent, "50.000", ReciboStamp.Over, 1, "0", exhausted = true))

        rule.onNodeWithContentDescription(
            "Te pasaste por 50.000 pesos. Pasado."
        ).assertIsDisplayed()
        rule.onNodeWithText("Para hoy").assertIsDisplayed()
        rule.onNodeWithContentDescription("0 pesos").assertIsDisplayed()
    }

    @Test
    fun aFinishedMonthAndAMonthWithoutLimitsShowNoDailyAmount() {
        show(month(ReciboHeadline.LeftOver, "500.000", ReciboStamp.WithinLimit))

        rule.onNodeWithContentDescription("Te sobraron 500.000 pesos. Bajo el límite.")
            .assertIsDisplayed()
        rule.onNodeWithTag(RECIBO_PER_DAY_TAG).assertDoesNotExist()
        rule.onNodeWithText("Puedes gastar al día").assertDoesNotExist()
    }

    @Test
    fun aFinishedMonthOverItsLimitAndAMonthWithNoLimitSayItInWords() {
        show(month(ReciboHeadline.Spent, "80.000", ReciboStamp.NoLimit))

        rule.onNodeWithContentDescription("Has gastado 80.000 pesos. Sin límite.")
            .assertIsDisplayed()
        rule.onNodeWithTag(RECIBO_PER_DAY_TAG).assertDoesNotExist()
    }

    @Test
    fun everyCategoryIsOneButtonThatSaysItsExactAmountsAndItsVerdictInWords() {
        show(month(ReciboHeadline.Remaining, "937.200", ReciboStamp.OnTrack, 9, "99.100"))

        listOf(
            "Alimentación. Te quedan 287.600 pesos de 900.000 pesos. Al día.",
            "Transporte. Te quedan 55.000 pesos de 200.000 pesos. Justo.",
            "Entretenimiento. Te pasaste por 21.500 pesos de 100.000 pesos. Pasado.",
            "Arte. Has gastado 80.000 pesos. Sin límite.",
            "Vieja. Has gastado 80.000 pesos. En pausa. Archivada.",
            "Nueva. Has gastado 80.000 pesos. Sin configurar."
        ).forEach { rule.onNodeWithContentDescription(it).performScrollTo().assertIsDisplayed() }
        rule.onNodeWithTag(reciboCategoryTag("c")).performScrollTo().performClick()
        rule.onNodeWithTag(RECIBO_ADD_CATEGORY_TAG).performScrollTo().performClick()

        assertEquals(
            listOf(ReciboAction.OpenCategory("c"), ReciboAction.CreateCategory),
            actions
        )
    }

    @Test
    fun theCategoriesCardHeaderCarriesTheOnlyAddButton() {
        show(month(ReciboHeadline.Remaining, "937.200", ReciboStamp.OnTrack, 9, "99.100"))

        val add = rule.onNodeWithContentDescription("Agregar categoría")
        val heading = rule.onNodeWithText("Tus categorías").getUnclippedBoundsInRoot()
        val firstCell = rule.onNodeWithTag(reciboCategoryTag("a")).getUnclippedBoundsInRoot()
        val button = add.getUnclippedBoundsInRoot()
        // On the heading's line, above the grid.
        assertTrue(button.bottom <= firstCell.top)
        assertTrue(button.top < heading.bottom && button.bottom > heading.top)
        val node = add.fetchSemanticsNode()
        assertTrue(
            with(node.layoutInfo.density) {
                node.touchBoundsInRoot.height.toDp()
            } >= 47.9.dp
        )
        // No caption and no second add action under the grid.
        rule.onNodeWithText("queda en cada una").assertDoesNotExist()
        rule.onAllNodesWithText("Agregar categoría").assertCountEquals(0)
        add.performClick()

        assertEquals(listOf<ReciboAction>(ReciboAction.CreateCategory), actions)
    }

    @Test
    fun olderFiguresCarryANoticeAndAWayToReadAgain() {
        show(
            month(
                ReciboHeadline.Remaining,
                "937.200",
                ReciboStamp.OnTrack,
                9,
                "99.100",
                stale = true
            )
        )

        rule.onNodeWithTag(RECIBO_STALE_TAG).assertIsDisplayed()
        rule.onNodeWithText(
            "No pudimos actualizar. Esto es lo último que se leyó y puede no estar al día."
        ).assertIsDisplayed()
        rule.onNodeWithContentDescription("Te quedan 937.200 pesos. Al día.").assertIsDisplayed()
        rule.onNodeWithText("Reintentar").performClick()

        assertEquals(listOf<ReciboAction>(ReciboAction.Retry), actions)
    }

    @Test
    fun theCategoryShownBesideReciboIsMarkedAsSelectedAndNoOtherIs() {
        show(month(ReciboHeadline.Remaining, "937.200", ReciboStamp.OnTrack), selected = "b")

        rule.onNodeWithTag(reciboCategoryTag("b")).assertIsSelected()
        rule.onNodeWithTag(reciboCategoryTag("a")).assertIsNotSelected()
    }

    @Test
    fun atTwiceTheFontSizeWithVeryLargeAmountsEverythingCanStillBeReached() {
        val huge = "8.998.000.000.000.001"
        val state = month(ReciboHeadline.Remaining, huge, ReciboStamp.OnTrack, 31, huge).copy(
            categories = listOf(
                limited(
                    "a",
                    "Un nombre de categoría largo",
                    ReciboHeadline.Overspent,
                    huge,
                    ReciboStamp.Over,
                    huge,
                    over = true
                )
            )
        )

        show(state, fontScale = 2f)

        rule.onNodeWithContentDescription("Te quedan $huge pesos. Al día.").assertIsDisplayed()
        rule.onNodeWithTag(RECIBO_PER_DAY_TAG).performScrollTo().assertIsDisplayed()
        rule.onNodeWithContentDescription(
            "Un nombre de categoría largo. Te pasaste por $huge pesos de $huge pesos. Pasado."
        ).performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag(RECIBO_ADD_CATEGORY_TAG).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun aFailureOffersRetryAndShowsNoAmount() {
        show(ReciboState.Failed)

        rule.onNodeWithTag(RECIBO_FAILED_TAG).assertIsDisplayed()
        rule.onNodeWithText("No pudimos leer tu mes").assertIsDisplayed()
        rule.onNodeWithText("Tus registros siguen guardados. Vuelve a intentarlo.")
            .assertIsDisplayed()
        // A failed read is never shown as first use.
        rule.onNodeWithTag(RECIBO_FIRST_RUN_TAG).assertDoesNotExist()
        rule.onNodeWithText("Reintentar").performClick()

        assertEquals(listOf<ReciboAction>(ReciboAction.Retry), actions)
        rule.onNodeWithTag(RECIBO_SUMMARY_TAG).assertDoesNotExist()
        rule.onNodeWithTag(RECIBO_CATEGORIES_TAG).assertDoesNotExist()
    }

    @Test
    fun aLongPressOrAScreenReaderActionOnACategoryOffersToEditItAndPressingStillOpensIt() {
        show(month(ReciboHeadline.Remaining, "937.200", ReciboStamp.OnTrack, 9, "99.100"))

        rule.onNodeWithTag(reciboCategoryTag("a")).performScrollTo().performClick()
        rule.onNodeWithTag(reciboCategoryTag("a")).performTouchInput { longClick() }
        rule.onNodeWithText("Editar categoría").assertIsDisplayed().performClick()
        val offered = rule.onNodeWithTag(reciboCategoryTag("b")).fetchSemanticsNode()
            .config[SemanticsActions.CustomActions]
        rule.runOnUiThread { offered.single().action() }

        assertEquals(listOf("Editar categoría"), offered.map { it.label })
        assertEquals(
            listOf(
                ReciboAction.OpenCategory("a"),
                ReciboAction.EditCategory("a"),
                ReciboAction.EditCategory("b")
            ),
            actions
        )
    }
}
