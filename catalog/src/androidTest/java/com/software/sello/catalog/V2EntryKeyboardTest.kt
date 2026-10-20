package com.software.sello.catalog

import android.os.ParcelFileDescriptor
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.DpRect
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** The note sheet's keyboard belongs to the sheet; the entry behind it must not move. */
class V2EntryKeyboardTest {
    @get:Rule
    val rule = createAndroidComposeRule<CatalogActivity>()

    private fun shell(command: String): String {
        val out = InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand(command)
        return ParcelFileDescriptor.AutoCloseInputStream(out).bufferedReader().readText().trim()
    }

    private var softKeyboardBefore = "0"

    // Emulators with a hardware keyboard hide the on-screen one unless asked to show it.
    @Before
    fun showTheOnScreenKeyboard() {
        softKeyboardBefore = shell("settings get secure show_ime_with_hard_keyboard")
        shell("settings put secure show_ime_with_hard_keyboard 1")
    }

    @After
    fun putTheDevicesSettingBack() {
        val value = softKeyboardBefore.takeIf { it == "0" || it == "1" } ?: "0"
        shell("settings put secure show_ime_with_hard_keyboard $value")
    }

    private fun bounds(tag: String): DpRect = rule.onNodeWithTag(tag).getUnclippedBoundsInRoot()

    private fun keyboardShown(): Boolean {
        val insets = ViewCompat.getRootWindowInsets(rule.activity.window.decorView)
        return insets?.isVisible(WindowInsetsCompat.Type.ime()) == true
    }

    /** Scrolls [line] into view, then checks that opening its sheet moves nothing behind. */
    private fun assertEntryStaysPutWhileTyping(line: String) {
        rule.onNodeWithTag(line).performScrollTo()
        rule.waitForIdle()
        val amount = bounds("v2:entry:amount")
        val submit = bounds("v2:entry:submit")
        rule.onNodeWithTag(line).performClick()
        rule.onNode(hasSetTextAction()).performClick()
        rule.waitUntil(5_000) { keyboardShown() }
        rule.waitForIdle()
        assertEquals(amount, bounds("v2:entry:amount"))
        assertEquals(submit, bounds("v2:entry:submit"))
    }

    @Test
    fun theExpenseNoteKeyboardLeavesTheEntryInPlace() {
        rule.onNodeWithTag("link:v2.entry").performScrollTo().performClick()
        assertEntryStaysPutWhileTyping("v2:entry:note")
    }

    @Test
    fun theIncomeSourceKeyboardLeavesTheEntryInPlace() {
        rule.onNodeWithTag("link:v2.entry").performScrollTo().performClick()
        rule.onNodeWithText("Ingreso").performClick()
        rule.onNodeWithTag("v2:entry:choices").performScrollTo()
        rule.onNodeWithTag("v2:entry:choice:4").performScrollTo().performClick()
        assertEntryStaysPutWhileTyping("v2:entry:source")
    }
}
