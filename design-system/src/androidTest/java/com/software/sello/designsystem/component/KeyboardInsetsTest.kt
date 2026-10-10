package com.software.sello.designsystem.component

import android.graphics.Insets
import android.view.ViewGroup
import android.view.WindowInsets
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
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

    private fun keyboard(heightPx: Int) {
        rule.runOnUiThread {
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
            SelloTheme(reducedMotion = true) {
                SelloScaffold(
                    title = "Octubre 2026",
                    navigation = SelloNavigation(
                        listOf(
                            SelloDestination("recibo", "Recibo", SelloIcon.ReceiptLong),
                            SelloDestination("resumen", "Resumen", SelloIcon.Insights)
                        ),
                        "recibo"
                    ) {},
                    dock = SelloDock("Anotar un gasto", "Anotar", {})
                ) { padding ->
                    Column(Modifier.fillMaxSize().padding(padding)) {
                        BasicTextField("Nota", {}, Modifier.testTag("field"))
                    }
                }
            }
        }
        rule.onNodeWithTag(SCAFFOLD_DOCK_TAG).assertIsDisplayed()
        rule.onNodeWithTag(SCAFFOLD_BAR_TAG).assertIsDisplayed()

        keyboard(heightPx = 600)

        rule.onNodeWithTag(SCAFFOLD_DOCK_TAG).assertDoesNotExist()
        rule.onNodeWithTag(SCAFFOLD_BAR_TAG).assertDoesNotExist()
        rule.onNodeWithTag("field").assertIsDisplayed()

        keyboard(heightPx = 0)

        rule.onNodeWithTag(SCAFFOLD_DOCK_TAG).assertIsDisplayed()
        rule.onNodeWithTag(SCAFFOLD_BAR_TAG).assertIsDisplayed()
    }
}
