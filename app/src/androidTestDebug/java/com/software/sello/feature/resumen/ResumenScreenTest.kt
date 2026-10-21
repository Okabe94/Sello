package com.software.sello.feature.resumen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.software.sello.designsystem.theme.SelloInk
import com.software.sello.designsystem.theme.SelloTheme
import com.software.sello.domain.model.Currency
import com.software.sello.domain.model.Money
import com.software.sello.presentation.money.MoneyFormatter
import com.software.sello.presentation.money.MoneyLabels
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ResumenScreenTest {
    @get:Rule
    val rule = createComposeRule()
    private val money = MoneyFormatter(object : MoneyLabels {
        override fun amount(currency: Currency, digits: String, isOne: Boolean) = "$digits pesos"
        override fun negative(amount: String) = "menos $amount"
        override fun positive(amount: String) = "más $amount"
    })
    private val state = mutableStateOf<ResumenState>(resumenExample(money))
    private val actions = mutableListOf<ResumenAction>()
    private var incomeClicks = 0
    private var expenseClicks = 0
    private var category: String? = null

    private fun show(
        ink: SelloInk = SelloInk.Cobalto,
        dark: Boolean = false,
        font: Float = 1f,
        width: Dp? = null,
        height: Dp? = null,
        density: Float? = null
    ) {
        rule.setContent {
            val deviceDensity = LocalDensity.current.density
            CompositionLocalProvider(
                LocalDensity provides Density(density ?: deviceDensity, font)
            ) {
                SelloTheme(ink = ink, darkTheme = dark, reducedMotion = true) {
                    Box(
                        if (width != null && height != null) {
                            Modifier.requiredSize(width, height)
                        } else {
                            Modifier.fillMaxSize()
                        }
                    ) {
                        ResumenScreen(
                            state.value,
                            { actions += it },
                            { incomeClicks++ },
                            { category = it },
                            { expenseClicks++ },
                            PaddingValues()
                        )
                    }
                }
            }
        }
    }

    private fun figures(ink: SelloInk, dark: Boolean) {
        show(ink, dark)
        // Income first, as receipt lines with exact amounts.
        rule.onNodeWithText("Ingresos anotados").assertIsDisplayed()
        rule.onNodeWithContentDescription("150.000 pesos").assertIsDisplayed()
        rule.onNodeWithText("Ingresos menos gastos").assertIsDisplayed()
        rule.onNodeWithContentDescription("120.000 pesos").assertIsDisplayed()
        rule.onNodeWithText("Sin asignar a categorías").assertIsDisplayed()
        rule.onNodeWithContentDescription("50.000 pesos").assertIsDisplayed()
        // Nothing projected, and the month's status lives on Recibo.
        rule.onNodeWithText("Proyección de gasto (estimada)").assertDoesNotExist()
        rule.onNodeWithText("Gasto real").assertDoesNotExist()
    }

    @Test
    fun cobaltoLight() = figures(SelloInk.Cobalto, false)

    @Test
    fun cobaltoDark() = figures(SelloInk.Cobalto, true)

    @Test
    fun violetaLight() = figures(SelloInk.Violeta, false)

    @Test
    fun violetaDark() = figures(SelloInk.Violeta, true)

    @Test
    fun chartsSelectSourceIdsAndSourceRowsOpenImplementedHistories() {
        show()
        rule.onNodeWithTag("resumen.daily.chart").performScrollTo()
        rule.onNodeWithContentDescription("2 oct, 25.000 pesos").performClick()
        rule.runOnIdle { assertEquals(listOf(ResumenAction.SelectDaily("2026-10-02")), actions) }
        rule.runOnIdle { state.value = resumenExample(money).copy(selectedDaily = "2026-10-02") }
        rule.onNodeWithTag("resumen.daily.selected").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Gasto de ese día").assertIsDisplayed()
        rule.onNodeWithTag("resumen.history.chart").performScrollTo()
        rule.onNodeWithContentDescription("oct, 30.000 pesos, mes en curso, hasta hoy")
            .performClick()
        rule.runOnIdle { assertEquals(ResumenAction.SelectHistory("2026-10"), actions.last()) }
        rule.onNodeWithTag("resumen.category.00000000-0000-4000-8000-000000000001")
            .performScrollTo().performClick()
        rule.runOnIdle { assertEquals("00000000-0000-4000-8000-000000000001", category) }
        rule.onNodeWithText("Ver ingresos y su destino").performScrollTo().performClick()
        rule.runOnIdle { assertEquals(1, incomeClicks) }
    }

    @Test
    fun readFailureIsNeverAnEmptySuccessAndStaleDataHasARetry() {
        state.value = ResumenState.Failed
        show()
        rule.onNodeWithTag("resumen.failed").assertIsDisplayed()
        rule.onNodeWithTag("resumen.empty").assertDoesNotExist()
        rule.onNodeWithTag("resumen.flow").assertDoesNotExist()
        rule.runOnIdle { state.value = resumenExample(money).copy(stale = true) }
        rule.onNodeWithTag("resumen.stale").assertIsDisplayed()
    }

    @Test
    fun successfulEmptyHistoryOffersOnlyAWorkingExpenseAction() {
        state.value = resumenExample(money).copy(
            empty = true,
            daily = emptyList(),
            history = emptyList(),
            categories = emptyList()
        )
        show()
        rule.onNodeWithText("Anotar un gasto").performScrollTo().performClick()
        rule.runOnIdle { assertEquals(1, expenseClicks) }
        rule.onNodeWithTag("resumen.daily").assertDoesNotExist()
    }

    @Test
    fun largeFontKeepsExactSelectedAmountsAndControlsReachable() {
        state.value = resumenExample(money).copy(selectedDaily = "2026-10-02")
        show(font = 2f)
        rule.onNodeWithTag("resumen.daily.selected").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Ver ingresos y su destino").performScrollTo().performClick()
        rule.runOnIdle { assertEquals(1, incomeClicks) }
    }

    @Test
    fun shortCompactWidthKeepsSummaryAndChartsInOneColumn() {
        show(width = 500.dp, height = 400.dp)

        val summary = rule.onNodeWithTag("resumen.flow").getUnclippedBoundsInRoot()
        val chart = rule.onNodeWithTag("resumen.daily").getUnclippedBoundsInRoot()

        assertTrue(chart.top >= summary.bottom)
    }

    @Test
    fun expandedWidthUsesIndependentSideBySidePanes() {
        show(width = 840.dp, height = 900.dp, density = 1f)

        val summary = rule.onNodeWithTag("resumen.flow").getUnclippedBoundsInRoot()
        val chart = rule.onNodeWithTag("resumen.daily").getUnclippedBoundsInRoot()

        assertTrue(chart.left > summary.left)
    }

    @Test
    fun largeSignedFiguresAndSelectedChartValuesRemainExact() {
        val huge = money.format(Money.cop(9_007_199_254_740_993))
        val original = resumenExample(money)
        val point = original.daily.last().copy(
            chart = original.daily.last().chart.copy(
                value = 9_007_199_254_740_993,
                spoken = "3 oct, ${huge.spoken}"
            ),
            amount = huge,
            detail = huge
        )
        state.value = original.copy(
            income = money.format(Money.cop(0)),
            spent = huge,
            netFlow = money.format(Money.cop(-9_007_199_254_740_993)),
            unallocated = money.format(Money.cop(-100_000)),
            daily = listOf(point),
            selectedDaily = point.chart.id
        )
        show(font = 2f)
        rule.onNodeWithContentDescription("menos 9.007.199.254.740.993 pesos")
            .performScrollTo().assertIsDisplayed()
        rule.onNodeWithContentDescription(
            "menos 100.000 pesos"
        ).performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("resumen.daily.selected").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("resumen.daily.chart").performScrollTo()
        rule.onNodeWithContentDescription("3 oct, 9.007.199.254.740.993 pesos").assertIsDisplayed()
    }

    @Test
    fun theSlipsComeInTheV2Order() {
        show(width = 360.dp, height = 3000.dp, density = 1f)

        val tops = listOf("resumen.flow", "resumen.history", "resumen.categories", "resumen.daily")
            .map { rule.onNodeWithTag(it).getUnclippedBoundsInRoot().top }

        assertEquals(tops.sorted(), tops)
    }

    @Test
    fun theMonthInProgressIsMarkedAndExplainedAndTheLimitIsOneLine() {
        show()

        rule.onNodeWithTag("resumen.history.chart").performScrollTo()
        rule.onNodeWithText("oct*", useUnmergedTree = true).assertExists()
        rule.onNodeWithContentDescription("oct, 30.000 pesos, mes en curso, hasta hoy")
            .assertExists()
        rule.onNodeWithText("* Mes en curso: lo gastado hasta hoy.").performScrollTo()
            .assertIsDisplayed()
        rule.onNodeWithText("Límite 100.000").assertExists()
    }

    @Test
    fun whereTheMoneyGoesIsRankedWithExactAmountsAndATotal() {
        show()

        rule.onNodeWithText("¿Dónde se va?").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("resumen.category.00000000-0000-4000-8000-000000000001")
            .performScrollTo().assertTextContains("Comida").performClick()
        rule.onNodeWithText("Total").performScrollTo().assertIsDisplayed()

        rule.runOnIdle { assertEquals("00000000-0000-4000-8000-000000000001", category) }
    }
}
