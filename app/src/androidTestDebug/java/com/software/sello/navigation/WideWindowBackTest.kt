package com.software.sello.navigation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.software.sello.TestData
import com.software.sello.composition.SelloApplication
import com.software.sello.designsystem.component.SCAFFOLD_SECONDARY_TAG
import com.software.sello.designsystem.theme.SelloTheme
import com.software.sello.feature.recibo.RECIBO_FIRST_RUN_TAG
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The real root, with the app's own view models, in a window as wide as a tablet. One
 * dp is drawn as one pixel so that 900dp fits the test device's screen.
 */
@RunWith(AndroidJUnit4::class)
class WideWindowBackTest {
    @Before
    fun newInstallation() = TestData.reset()

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun backClosesTheMonthPanelBesideTheContentWithoutLeaving() {
        val viewModels = (rule.activity.application as SelloApplication).viewModels
        rule.setContent {
            CompositionLocalProvider(LocalDensity provides Density(1f, 1f)) {
                SelloTheme(reducedMotion = true) {
                    Box(Modifier.requiredSize(900.dp, 700.dp)) { SelloAppRoot(viewModels) }
                }
            }
        }
        rule.onNodeWithTag(MONTH_SWITCHER_TAG).performClick()
        // The window may be wider than the device's screen, so: present, not "on screen".
        rule.onNodeWithTag(SCAFFOLD_SECONDARY_TAG).assertExists()
        rule.onNodeWithTag(MONTH_PICKER_TAG).assertExists()
        rule.onNodeWithTag(RECIBO_FIRST_RUN_TAG).assertExists()

        Espresso.pressBack()

        rule.onNodeWithTag(MONTH_PICKER_TAG).assertDoesNotExist()
        rule.onNodeWithTag(RECIBO_FIRST_RUN_TAG).assertExists()
        assertEquals(Lifecycle.State.RESUMED, rule.activityRule.scenario.state)
    }
}
