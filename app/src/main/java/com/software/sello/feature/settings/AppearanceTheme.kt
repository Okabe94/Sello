package com.software.sello.feature.settings

import com.software.sello.designsystem.theme.SelloInk
import com.software.sello.domain.model.AppearancePreference
import com.software.sello.domain.model.InkPreference
import com.software.sello.domain.model.ThemePreference

/** Pure mapping at the composition boundary: neutral preferences never leak into tokens. */
data class AppearanceTheme(val ink: SelloInk, val dark: Boolean)

fun AppearancePreference.asSelloTheme(systemDark: Boolean): AppearanceTheme = AppearanceTheme(
    ink = when (ink) {
        InkPreference.Cobalto -> SelloInk.Cobalto
        InkPreference.Violeta -> SelloInk.Violeta
    },
    dark = when (theme) {
        ThemePreference.System -> systemDark
        ThemePreference.Light -> false
        ThemePreference.Dark -> true
    }
)
