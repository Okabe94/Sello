package com.software.sello.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.software.sello.designsystem.component.SCAFFOLD_BAR_TAG
import com.software.sello.designsystem.component.SCAFFOLD_RAIL_TAG
import com.software.sello.designsystem.theme.SelloTheme
import com.software.sello.presentation.month.monthNames
import java.time.YearMonth
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** The frame alone, given state by hand, in windows of chosen sizes and font scales. */
@RunWith(AndroidJUnit4::class)
class ShellScreenLayoutTest {
    @get:Rule
    val rule = createComposeRule()

    private val october = YearMonth.of(2026, 10)
    private val actions = mutableListOf<ShellAction>()

    private fun state(pickerOpen: Boolean) = ShellState(
        currentMonth = october,
        selectedMonth = YearMonth.of(2026, 8),
        monthPickerOpen = pickerOpen,
        pickerYear = 2026,
        pendingEntry = null
    )

    private fun stateWith(pickerOpen: Boolean) = state(pickerOpen)

    /** The frame in the test device's own window, which on a phone is a compact one. */
    private fun showOnThisScreen(pickerOpen: Boolean, fontScale: Float = 1f) {
        rule.setContent {
            val density = LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale)) {
                SelloTheme(reducedMotion = true) { Frame(pickerOpen) }
            }
        }
    }

    /**
     * The frame in a window of [width] × [height] dp, one dp drawn as one pixel. Such a
     * window can be larger than the device's screen, so these cases check what exists
     * and where it is laid out, not whether it is visible on the screen.
     */
    private fun showInWindow(width: Int, height: Int, pickerOpen: Boolean) {
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, 1f)) {
                SelloTheme(reducedMotion = true) {
                    Box(Modifier.requiredSize(width.dp, height.dp)) { Frame(pickerOpen) }
                }
            }
        }
    }

    @Composable
    private fun Frame(pickerOpen: Boolean) {
        ShellScreen(stateWith(pickerOpen), monthNames(), { actions += it }) { padding ->
            Text("contenido", Modifier.padding(padding).testTag("content"))
        }
    }

    @Test
    fun theTitleIsTheSelectedMonthAndOpensThePicker() {
        showOnThisScreen(pickerOpen = false)

        rule.onNodeWithText("Agosto 2026").assertIsDisplayed()
        rule.onNodeWithTag(MONTH_SWITCHER_TAG).performClick()

        assertEquals(listOf<ShellAction>(ShellAction.OpenMonthPicker), actions)
        rule.onNodeWithTag(MONTH_PICKER_TAG).assertDoesNotExist()
    }

    @Test
    fun withOneTabThereIsNoTabBarOrRailAndTheDockAsksToRecordAnExpense() {
        showInWindow(900, 700, pickerOpen = false)

        rule.onNodeWithTag(SCAFFOLD_BAR_TAG).assertDoesNotExist()
        rule.onNodeWithTag(SCAFFOLD_RAIL_TAG).assertDoesNotExist()
        rule.onNodeWithTag("content").assertExists()
        rule.onNodeWithContentDescription("Anotar un gasto")
            .performSemanticsAction(SemanticsActions.OnClick)

        assertEquals(listOf<ShellAction>(ShellAction.StartEntry), actions)
    }

    @Test
    fun onAPhoneThePickerIsASheetOverTheContent() {
        showOnThisScreen(pickerOpen = true)

        rule.onNodeWithTag(MONTH_PICKER_TAG).assertIsDisplayed()
        // A month is one control, announced by its full name.
        rule.onNodeWithContentDescription("Agosto 2026").performScrollTo().performClick()

        assertEquals(listOf<ShellAction>(ShellAction.PickMonth(YearMonth.of(2026, 8))), actions)
    }

    @Test
    fun inAnExpandedWindowThePickerIsAPanelBesideTheContentNotOverIt() {
        showInWindow(900, 700, pickerOpen = true)

        val content = rule.onNodeWithTag("content").assertExists().getUnclippedBoundsInRoot()
        val picker = rule.onNodeWithTag(MONTH_PICKER_TAG).assertExists().getUnclippedBoundsInRoot()

        assertTrue("content $content, picker $picker", content.right <= picker.left)
    }

    @Test
    fun inAShortLandscapeWindowEveryMonthOfThePickerCanBeReached() {
        showInWindow(760, 400, pickerOpen = true)

        rule.onNodeWithTag(MONTH_PICKER_TAG).assertExists()
        rule.onNodeWithContentDescription("Diciembre 2026").performScrollTo().assertExists()
    }

    @Test
    fun atTwiceTheFontSizeTheTitleAndPickerAreStillThere() {
        showOnThisScreen(pickerOpen = true, fontScale = 2f)

        rule.onNodeWithTag(MONTH_PICKER_TAG).assertIsDisplayed()
        // Every month can be reached, scrolling if it has to.
        rule.onNodeWithContentDescription("Diciembre 2026").performScrollTo().assertIsDisplayed()
        rule.onNodeWithContentDescription("Enero 2026").performScrollTo().assertIsDisplayed()
    }
}
