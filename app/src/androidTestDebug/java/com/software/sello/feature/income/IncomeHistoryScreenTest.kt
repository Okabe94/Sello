package com.software.sello.feature.income

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.software.sello.designsystem.component.MoneySign
import com.software.sello.designsystem.component.MoneyTextValue
import com.software.sello.designsystem.theme.SelloTheme
import java.time.LocalDate
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** The month's income as drawn from state given by hand: what came in, where it went, each one. */
@RunWith(AndroidJUnit4::class)
class IncomeHistoryScreenTest {
    @get:Rule
    val rule = createComposeRule()

    private val october = YearMonth.of(2026, 10)
    private val today = LocalDate.of(2026, 10, 22)
    private val actions = mutableListOf<IncomeHistoryAction>()
    private var created = 0
    private val edited = mutableListOf<String>()

    private fun pesos(digits: String) = MoneyTextValue(MoneySign.None, "", digits, "$digits pesos")

    private fun minus(digits: String) =
        MoneyTextValue(MoneySign.Minus, "", digits, "menos $digits pesos")

    private val salary = IncomeLine("i1", pesos("150.000"), "salary", null, today, "Quincena")

    private fun ready(lines: List<IncomeLine> = listOf(salary), more: Boolean = false) =
        IncomeHistoryState.Ready(october, pesos("150.000"), lines, more)

    private val split = IncomeSplit(
        income = pesos("150.000"),
        allocated = pesos("100.000"),
        unallocated = pesos("50.000"),
        over = false,
        allocatedShare = 2f / 3f
    )

    private fun show(state: IncomeHistoryState, split: IncomeSplit?, fontScale: Float = 1f) {
        rule.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale)) {
                SelloTheme(reducedMotion = true) {
                    IncomeHistoryScreen(
                        state,
                        split,
                        today,
                        { actions += it },
                        { created++ },
                        { edited += it },
                        PaddingValues(12.dp)
                    )
                }
            }
        }
    }

    @Test
    fun theHeroSaysWhatCameInAndWhereItWentInExactAmounts() {
        show(ready(), split)

        rule.onNodeWithText("Entró en octubre").assertIsDisplayed()
        rule.onNodeWithTag("income.split").assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.ContentDescription,
                listOf(
                    "En presupuestos de categorías: 100.000 pesos. Sin asignar: 50.000 pesos"
                )
            )
        )
        rule.onNodeWithText("En presupuestos de categorías").assertIsDisplayed()
        rule.onNodeWithText("Sin asignar").assertIsDisplayed()
        rule.onNodeWithTag("income.overBasis").assertDoesNotExist()
        // No plan, no expected income and no suggestion: recurrence does not exist.
        rule.onNodeWithText("Plan del mes").assertDoesNotExist()
        rule.onNodeWithText("Una sugerencia").assertDoesNotExist()
    }

    @Test
    fun moreBudgetedThanCameInIsSignedAndSaysTheTwoFactsBehindIt() {
        show(
            ready(),
            split.copy(
                income = pesos("60.000"),
                unallocated = minus("40.000"),
                over = true,
                allocatedShare = 1f
            )
        )

        rule.onNodeWithContentDescription("menos 40.000 pesos").assertIsDisplayed()
        rule.onNodeWithText("Tus presupuestos suman 100.000; anotaste 60.000.").assertIsDisplayed()
        rule.onNodeWithTag("income.split").assert(
            SemanticsMatcher.expectValue(
                SemanticsProperties.ContentDescription,
                listOf(
                    "En presupuestos de categorías: 100.000 pesos. Sin asignar: menos 40.000 pesos"
                )
            )
        )
    }

    @Test
    fun withoutTheMonthsSummaryOnlyTheTotalIsShownNeverAZeroSplit() {
        show(ready(), split = null)

        rule.onNodeWithText("Entró en octubre").assertIsDisplayed()
        rule.onNodeWithTag("income.split").assertDoesNotExist()
        rule.onNodeWithText("Sin asignar").assertDoesNotExist()
        rule.onNodeWithTag("income.line.i1").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun eachIncomeIsOneRowThatOpensItForEditingAndTheListEndsWithItsTotal() {
        show(ready(), split)

        rule.onNodeWithText("Lo que entró").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Hoy, jue 22 oct · Quincena").performScrollTo().assertIsDisplayed()
        rule.onNodeWithText("Total").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("income.line.i1").performScrollTo().performClick()

        assertEquals(listOf("i1"), edited)
    }

    @Test
    fun moreRowsArePagedAndAFailedPageCanBeAskedForAgain() {
        show(ready(more = true).copy(pageFailed = true), split)

        rule.onNodeWithText("Reintentar").performScrollTo().performClick()
        rule.onNodeWithTag("income.more").performScrollTo().performClick()

        assertEquals(listOf(IncomeHistoryAction.More, IncomeHistoryAction.More), actions)
    }

    @Test
    fun noIncomeIsTheEmptyStateWithAWayToRecordOne() {
        show(ready(lines = emptyList()).copy(total = pesos("0")), split = null)

        rule.onNodeWithText("Aún no anotas ingresos en octubre").assertIsDisplayed()
        rule.onNodeWithText(
            "Sello funciona sin ingresos. Si los anotas, verás cuánto tiene un presupuesto y cuánto no."
        ).assertIsDisplayed()
        rule.onNodeWithTag("income.hero").assertDoesNotExist()
        rule.onNodeWithText("Anotar ingreso").performClick()

        assertEquals(1, created)
    }

    @Test
    fun aFailedReadSaysSoAndNeverShowsZero() {
        show(IncomeHistoryState.Failed, split = null)

        rule.onNodeWithText("No pudimos leer tus ingresos").assertIsDisplayed()
        rule.onNodeWithContentDescription("0 pesos").assertDoesNotExist()
        rule.onNodeWithText("Reintentar").performClick()

        assertEquals(listOf<IncomeHistoryAction>(IncomeHistoryAction.Retry), actions)
    }

    @Test
    fun atTwiceTheFontSizeTheHeroAndARowCanBeReached() {
        show(ready(), split, fontScale = 2f)

        rule.onNodeWithText("Entró en octubre").assertIsDisplayed()
        rule.onNodeWithTag("income.unassigned").performScrollTo().assertIsDisplayed()
        rule.onNodeWithTag("income.line.i1").performScrollTo().assertIsDisplayed()
    }
}
