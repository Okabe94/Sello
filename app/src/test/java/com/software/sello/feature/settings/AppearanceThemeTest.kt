package com.software.sello.feature.settings

import com.software.sello.designsystem.theme.SelloInk
import com.software.sello.domain.model.AppearancePreference
import com.software.sello.domain.model.InkPreference
import com.software.sello.domain.model.ThemePreference
import org.junit.Assert.assertEquals
import org.junit.Test

class AppearanceThemeTest {
    @Test
    fun eachApprovedAppearanceMapsToTheMatchingSelloScheme() {
        assertEquals(
            AppearanceTheme(SelloInk.Cobalto, false),
            AppearancePreference(ThemePreference.Light, InkPreference.Cobalto).asSelloTheme(true)
        )
        assertEquals(
            AppearanceTheme(SelloInk.Cobalto, true),
            AppearancePreference(ThemePreference.Dark, InkPreference.Cobalto).asSelloTheme(false)
        )
        assertEquals(
            AppearanceTheme(SelloInk.Violeta, false),
            AppearancePreference(ThemePreference.Light, InkPreference.Violeta).asSelloTheme(true)
        )
        assertEquals(
            AppearanceTheme(SelloInk.Violeta, true),
            AppearancePreference(ThemePreference.Dark, InkPreference.Violeta).asSelloTheme(false)
        )
    }

    @Test
    fun systemAppearanceOnlyFollowsTheSystemDarkSignal() {
        val preference = AppearancePreference(ThemePreference.System, InkPreference.Violeta)
        assertEquals(AppearanceTheme(SelloInk.Violeta, false), preference.asSelloTheme(false))
        assertEquals(AppearanceTheme(SelloInk.Violeta, true), preference.asSelloTheme(true))
    }
}
