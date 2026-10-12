package com.software.sello.designsystem.theme

import android.os.ParcelFileDescriptor
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * The theme follows the system's "remove animations" setting while it is on screen,
 * and stops listening when it leaves. A value passed to the theme always wins.
 */
@RunWith(AndroidJUnit4::class)
class LiveReducedMotionTest {
    @get:Rule
    val rule = createComposeRule()

    /** A setting a test changes by hand, counting who is listening. */
    private class HandScale(var off: Boolean) : AnimatorScale {
        val listeners = mutableListOf<() -> Unit>()
        var watches = 0

        override fun isOff() = off

        override fun watch(onChange: () -> Unit): AutoCloseable {
            watches++
            listeners += onChange
            return AutoCloseable { listeners -= onChange }
        }

        fun set(value: Boolean) {
            off = value
            listeners.toList().forEach { it() }
        }
    }

    private fun shell(command: String): String {
        val out = InstrumentationRegistry.getInstrumentation().uiAutomation
            .executeShellCommand(command)
        return ParcelFileDescriptor.AutoCloseInputStream(out).bufferedReader().readText().trim()
    }

    private var systemBefore = "1"

    @Before
    fun rememberTheDevicesSetting() {
        systemBefore = shell("settings get global animator_duration_scale")
    }

    @After
    fun putTheDevicesSettingBack() {
        shell("settings put global animator_duration_scale $systemBefore")
    }

    @Test
    fun aChangeWhileOnScreenIsFollowedInBothDirections() {
        val scale = HandScale(off = false)
        var reduced: Boolean? = null
        rule.setContent { reduced = reducedMotion(scale) }
        rule.waitForIdle()
        val atFirst = reduced

        rule.runOnUiThread { scale.set(true) }
        rule.waitForIdle()
        val turnedOff = reduced
        rule.runOnUiThread { scale.set(false) }
        rule.waitForIdle()

        assertEquals(listOf(false, true, false), listOf(atFirst, turnedOff, reduced))
    }

    @Test
    fun drawingAgainNeverListensTwiceAndLeavingTheScreenStopsListening() {
        val scale = HandScale(off = false)
        var tick by mutableIntStateOf(0)
        var shown by mutableStateOf(true)
        rule.setContent {
            if (shown) {
                tick
                reducedMotion(scale)
            }
        }
        repeat(5) {
            tick++
            rule.waitForIdle()
        }
        val whileShown = scale.watches to scale.listeners.size

        shown = false
        rule.waitForIdle()

        assertEquals(1 to 1, whileShown)
        assertEquals(0, scale.listeners.size)
    }

    @Test
    fun aChangeMadeJustBeforeListeningStartedIsNotMissed() {
        // The setting flips after it is first read and before anyone is listening.
        val scale = object : AnimatorScale {
            var reads = 0

            override fun isOff() = reads++ > 0

            override fun watch(onChange: () -> Unit) = AutoCloseable {}
        }
        var reduced: Boolean? = null

        rule.setContent { reduced = reducedMotion(scale) }
        rule.waitForIdle()

        assertEquals(true, reduced)
    }

    @Test
    fun theRealSystemSettingIsFollowedWhileTheThemeIsOnScreen() {
        shell("settings put global animator_duration_scale 1")
        var reduced: Boolean? = null
        rule.setContent { SelloTheme { reduced = SelloTheme.reducedMotion } }
        rule.waitForIdle()
        val atFirst = reduced

        shell("settings put global animator_duration_scale 0")
        rule.waitUntil(10_000) { reduced == true }
        shell("settings put global animator_duration_scale 1")
        rule.waitUntil(10_000) { reduced == false }

        assertEquals(false, atFirst)
    }

    @Test
    fun aValueGivenToTheThemeWinsOverTheSystemSettingWhateverItBecomes() {
        shell("settings put global animator_duration_scale 0")
        var full: Boolean? = null
        var reduced: Boolean? = null
        rule.setContent {
            SelloTheme(reducedMotion = false) { full = SelloTheme.reducedMotion }
            SelloTheme(reducedMotion = true) { reduced = SelloTheme.reducedMotion }
        }
        rule.waitForIdle()
        val systemOff = full to reduced

        shell("settings put global animator_duration_scale 1")
        rule.waitForIdle()

        assertEquals(false to true, systemOff)
        assertEquals(false to true, full to reduced)
    }
}
