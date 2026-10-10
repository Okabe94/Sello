package com.software.sello.feature.recibo

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.software.sello.designsystem.component.MoneySign
import com.software.sello.designsystem.component.MoneyTextValue
import com.software.sello.designsystem.theme.SelloTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReciboScreenTest {
    @get:Rule
    val rule = createComposeRule()

    private val actions = mutableListOf<ReciboAction>()

    private fun show(state: ReciboState) {
        rule.setContent {
            SelloTheme(reducedMotion = true) {
                ReciboScreen(state, { actions += it }, PaddingValues(12.dp))
            }
        }
    }

    @Test
    fun loadingShowsASkeletonAndNoFigure() {
        show(ReciboState.Loading)

        rule.onNodeWithTag(RECIBO_LOADING_TAG).assertIsDisplayed()
        rule.onNodeWithText("0").assertDoesNotExist()
    }

    @Test
    fun theFirstRunOffersToCreateCategoriesAndNothingThatDoesNotExistYet() {
        show(ReciboState.FirstRun)

        rule.onNodeWithText("Tu recibo está en blanco").assertIsDisplayed()
        rule.onNodeWithText("Crear categorías").performClick()

        assertEquals(listOf<ReciboAction>(ReciboAction.CreateCategory), actions)
        // No backup or entry action before those exist.
        rule.onNodeWithText("Traer mis datos de un respaldo").assertDoesNotExist()
        rule.onNodeWithTag(RECIBO_SPENDING_TAG).assertDoesNotExist()
    }

    @Test
    fun spendingIsReadAloudAsTheFullExactAmountAndCategoriesShowTheirLimit() {
        show(
            ReciboState.Spending(
                MoneyTextValue(MoneySign.None, "", "1.234.567", "1.234.567 pesos"),
                listOf(
                    ReciboCategory(
                        "a",
                        "Mercado",
                        "shopping_cart",
                        archived = false,
                        ReciboLimit.Amount(
                            MoneyTextValue(MoneySign.None, "", "300.000", "300.000 pesos")
                        )
                    ),
                    ReciboCategory("b", "Café", "local_cafe", false, ReciboLimit.Unlimited),
                    ReciboCategory("c", "Vieja", "una_clave_nueva", true, ReciboLimit.Paused),
                    ReciboCategory("d", "Nueva", "home", false, ReciboLimit.NotSet)
                )
            )
        )

        rule.onNodeWithText("Has gastado").assertIsDisplayed()
        rule.onNodeWithContentDescription("1.234.567 pesos").assertIsDisplayed()
        rule.onNodeWithText("Mercado").assertIsDisplayed()
        rule.onNodeWithContentDescription("300.000 pesos").assertIsDisplayed()
        rule.onNodeWithText("Sin límite").assertIsDisplayed()
        rule.onNodeWithText("En pausa").assertIsDisplayed()
        rule.onNodeWithText("Archivada").assertIsDisplayed()
        rule.onNodeWithText("Sin configurar").assertIsDisplayed()
        rule.onNodeWithTag(RECIBO_ADD_CATEGORY_TAG).performScrollTo().performClick()

        assertEquals(listOf<ReciboAction>(ReciboAction.CreateCategory), actions)
    }

    @Test
    fun aFailureOffersRetryAndShowsNoAmount() {
        show(ReciboState.Failed)

        rule.onNodeWithTag(RECIBO_FAILED_TAG).assertIsDisplayed()
        rule.onNodeWithText("No pudimos leer tus datos de este mes.").assertIsDisplayed()
        rule.onNodeWithText("Reintentar").performClick()

        assertEquals(listOf<ReciboAction>(ReciboAction.Retry), actions)
        rule.onNodeWithTag(RECIBO_SPENDING_TAG).assertDoesNotExist()
    }
}
