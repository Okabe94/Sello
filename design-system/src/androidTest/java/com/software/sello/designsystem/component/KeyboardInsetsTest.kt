package com.software.sello.designsystem.component

import android.graphics.Insets
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets as ComposeInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.theme.SelloTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The system keyboard is reported to the window the way the system reports it, as
 * window insets, so the result does not depend on a keyboard actually sliding in.
 */
@RunWith(AndroidJUnit4::class)
class KeyboardInsetsTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    /** A keyboard covering [fraction] of the window, whatever the device's screen is. */
    private fun keyboard(fraction: Float) {
        rule.runOnUiThread {
            val heightPx = (rule.activity.window.decorView.height * fraction).toInt()
            val insets = WindowInsets.Builder()
                .setInsets(WindowInsets.Type.ime(), Insets.of(0, 0, 0, heightPx))
                .setVisible(WindowInsets.Type.ime(), heightPx > 0)
                .build()
            // Straight to the Compose view: the window's own frame would consume them.
            val content = rule.activity.findViewById<ViewGroup>(android.R.id.content)
            content.getChildAt(0).dispatchApplyWindowInsets(insets)
        }
        rule.waitForIdle()
    }

    @Test
    fun theDockAndTabBarStepAsideForTheKeyboardAndComeBackAfterIt() {
        rule.setContent {
            // A phone-sized stage drawn at one pixel per dp, so the scaffold arranges
            // itself as on an upright phone whatever the test device's screen is.
            CompositionLocalProvider(LocalDensity provides Density(1f, 1f)) {
                SelloTheme(reducedMotion = true) {
                    Box(Modifier.requiredSize(360.dp, 640.dp)) {
                        SelloScaffold(
                            title = "Octubre 2026",
                            navigation = SelloNavigation(
                                listOf(
                                    SelloDestination("recibo", "Recibo", SelloIcon.ReceiptLong),
                                    SelloDestination("resumen", "Resumen", SelloIcon.Insights)
                                ),
                                "recibo"
                            ) {},
                            dock = SelloDock("Anotar un gasto", "Anotar", {}),
                            windowInsets = ComposeInsets(0, 0, 0, 0)
                        ) { padding ->
                            Column(Modifier.fillMaxSize().padding(padding)) {
                                BasicTextField("Nota", {}, Modifier.testTag("field"))
                            }
                        }
                    }
                }
            }
        }
        // "Exists", not "is displayed": the test device may be a very small screen, and
        // what is being checked is whether the scaffold lays these controls out at all.
        rule.onNodeWithTag(SCAFFOLD_DOCK_TAG).assertExists()
        rule.onNodeWithTag(SCAFFOLD_BAR_TAG).assertExists()

        keyboard(fraction = 0.4f)

        rule.onNodeWithTag(SCAFFOLD_DOCK_TAG).assertDoesNotExist()
        rule.onNodeWithTag(SCAFFOLD_BAR_TAG).assertDoesNotExist()
        rule.onNodeWithTag("field").assertExists()

        keyboard(fraction = 0f)

        rule.onNodeWithTag(SCAFFOLD_DOCK_TAG).assertExists()
        rule.onNodeWithTag(SCAFFOLD_BAR_TAG).assertExists()
    }
}
