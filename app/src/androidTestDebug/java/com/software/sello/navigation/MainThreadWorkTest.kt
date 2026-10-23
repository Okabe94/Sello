package com.software.sello.navigation

import android.os.StrictMode
import android.os.strictmode.Violation
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.software.sello.TestCommands
import com.software.sello.TestData
import com.software.sello.awaitDescription
import com.software.sello.awaitTag
import com.software.sello.composition.SelloApplication
import com.software.sello.designsystem.theme.SelloTheme
import com.software.sello.feature.expense.ENTRY_CATEGORIES_TAG
import com.software.sello.feature.expense.ENTRY_KEYPAD_TAG
import com.software.sello.feature.expense.ENTRY_SUBMIT_TAG
import com.software.sello.feature.recibo.CATEGORY_DETAIL_HEADER_TAG
import com.software.sello.feature.recibo.RECIBO_SUMMARY_TAG
import com.software.sello.feature.recibo.reciboCategoryTag
import com.software.sello.press
import com.software.sello.readyViewModels
import java.util.Collections
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Reading and saving happen off the main thread: walking the daily screens of the
 * real app, with its own database, and saving an expense on the way, makes no disk
 * read or write on the main thread from Sello's code. StrictMode records every such
 * access; one whose stack passes through Sello is a failure.
 */
class MainThreadWorkTest {
    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    private val violations: MutableList<Violation> = Collections.synchronizedList(mutableListOf())
    private lateinit var previous: StrictMode.ThreadPolicy

    @Before
    fun history() {
        TestData.reset()
        TestCommands.showCurrentMonth()
        TestCommands.createCategory("Mercado", 300_000)
        TestCommands.createExpense("Mercado", TestCommands.today, 48_700, "Plaza de mercado")
        TestCommands.createIncome(TestCommands.today, 4_200_000)
    }

    @After
    fun leaveNothingBehind() {
        rule.runOnUiThread { StrictMode.setThreadPolicy(previous) }
        TestData.reset()
        TestCommands.showCurrentMonth()
    }

    @Test
    fun theDailyScreensAndASaveDoNoDiskWorkOnTheMainThread() {
        val viewModels = (rule.activity.application as SelloApplication).readyViewModels()
        rule.runOnUiThread {
            previous = StrictMode.getThreadPolicy()
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .penaltyListener({ it.run() }) { violations += it }
                    .build()
            )
        }
        rule.setContent { SelloTheme(reducedMotion = true) { SelloAppRoot(viewModels) } }

        rule.awaitTag(RECIBO_SUMMARY_TAG)
        rule.onNodeWithTag(reciboCategoryTag(TestCommands.categoryId("Mercado"))).performClick()
        rule.awaitTag(CATEGORY_DETAIL_HEADER_TAG)
        rule.activityRule.scenario.onActivity { it.onBackPressedDispatcher.onBackPressed() }

        rule.awaitDescription("Anotar un gasto")
        rule.onNodeWithContentDescription("Anotar un gasto").performClick()
        rule.awaitTag(ENTRY_KEYPAD_TAG)
        rule.onNode(hasText("Mercado") and hasAnyAncestor(hasTestTag(ENTRY_CATEGORIES_TAG)))
            .performClick()
        listOf("1", "2", "0", "0", "0").forEach { key ->
            rule.onNode(
                hasText(key) and hasAnyAncestor(hasTestTag(ENTRY_KEYPAD_TAG))
            ).performClick()
        }
        rule.onNodeWithTag(ENTRY_SUBMIT_TAG).press()
        rule.awaitTag(RECIBO_SUMMARY_TAG)
        rule.waitUntil(15_000) {
            TestData.rows("SELECT id FROM expense").size == 2
        }

        rule.onNode(hasText("Resumen") and hasClickAction()).performClick()
        rule.awaitTag("resumen.flow")
        rule.waitForIdle()

        val ours = violations.filter { violation ->
            violation.stackTrace.any {
                it.className.startsWith("com.software.sello.") &&
                    !it.className.startsWith("com.software.sello.navigation.MainThreadWorkTest")
            }
        }
        assertTrue(
            ours.joinToString("\n\n") { violation ->
                violation.toString() + "\n" +
                    violation.stackTrace.take(25).joinToString("\n") { "  at $it" }
            },
            ours.isEmpty()
        )
    }
}
