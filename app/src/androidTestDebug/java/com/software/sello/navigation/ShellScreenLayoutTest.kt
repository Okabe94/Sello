package com.software.sello.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.software.sello.designsystem.component.SCAFFOLD_BAR_TAG
import com.software.sello.designsystem.component.SCAFFOLD_DOCK_TAG
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

    /**
     * A window of [width] × [height] dp. With [shrink], one dp is one pixel, so that a
     * window wider than the test screen still fits on it; otherwise the screen's own density.
     */
    private fun show(
        width: Int,
        height: Int,
        pickerOpen: Boolean,
        shrink: Boolean = false,
        fontScale: Float = 1f
    ) {
        rule.setContent {
            val density = if (shrink) 1f else LocalDensity.current.density
            CompositionLocalProvider(LocalDensity provides Density(density, fontScale)) {
                SelloTheme(reducedMotion = true) {
                    Box(Modifier.requiredSize(width.dp, height.dp)) {
                        ShellScreen(state(pickerOpen), monthNames(), { actions += it }) { padding ->
                            Text("contenido", Modifier.padding(padding).testTag("content"))
                        }
                    }
                }
            }
        }
    }

    @Test
    fun theTitleIsTheSelectedMonthAndOpensThePicker() {
        show(360, 700, pickerOpen = false)

        rule.onNodeWithText("Agosto 2026").assertIsDisplayed()
        rule.onNodeWithTag(MONTH_SWITCHER_TAG).performClick()

        assertEquals(listOf<ShellAction>(ShellAction.OpenMonthPicker), actions)
        rule.onNodeWithTag(MONTH_PICKER_TAG).assertDoesNotExist()
    }

    @Test
    fun withOneTabThereIsNoTabBarRailOrDockInAnyWindow() {
        show(900, 700, pickerOpen = false, shrink = true)

        rule.onNodeWithTag(SCAFFOLD_BAR_TAG).assertDoesNotExist()
        rule.onNodeWithTag(SCAFFOLD_RAIL_TAG).assertDoesNotExist()
        rule.onNodeWithTag(SCAFFOLD_DOCK_TAG).assertDoesNotExist()
        rule.onNodeWithTag("content").assertIsDisplayed()
    }

    @Test
    fun onAPhoneThePickerIsASheetOverTheContent() {
        show(360, 700, pickerOpen = true)

        rule.onNodeWithTag(MONTH_PICKER_TAG).assertIsDisplayed()
        // A month is one control, announced by its full name.
        rule.onNodeWithContentDescription("Agosto 2026").performClick()

        assertEquals(listOf<ShellAction>(ShellAction.PickMonth(YearMonth.of(2026, 8))), actions)
    }

    @Test
    fun inAnExpandedWindowThePickerIsAPanelBesideTheContentNotOverIt() {
        show(900, 700, pickerOpen = true, shrink = true)

        val content = rule.onNodeWithTag("content").assertIsDisplayed().getUnclippedBoundsInRoot()
        val picker = rule.onNodeWithTag(MONTH_PICKER_TAG).assertIsDisplayed()
            .getUnclippedBoundsInRoot()

        assertTrue("content $content, picker $picker", content.right <= picker.left)
    }

    @Test
    fun inAShortLandscapeWindowThePickerStillFitsAndIsUsable() {
        show(760, 400, pickerOpen = true, shrink = true)

        rule.onNodeWithTag(MONTH_PICKER_TAG).assertIsDisplayed()
        rule.onNodeWithText("2026").assertIsDisplayed()
    }

    @Test
    fun atTwiceTheFontSizeTheTitleAndPickerAreStillThere() {
        show(360, 700, pickerOpen = true, fontScale = 2f)

        rule.onNodeWithTag(MONTH_PICKER_TAG).assertIsDisplayed()
        // Every month can be reached, scrolling if it has to.
        rule.onNodeWithContentDescription("Diciembre 2026").performScrollTo().assertIsDisplayed()
        rule.onNodeWithContentDescription("Enero 2026").performScrollTo().assertIsDisplayed()
    }
}
