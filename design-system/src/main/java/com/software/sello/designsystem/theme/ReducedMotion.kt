package com.software.sello.designsystem.theme

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

/**
 * True when the system asks for no animations (animator duration scale 0, which is
 * what "Remove animations" sets). Components then show their end state at once.
 */
@Composable
fun systemReducedMotion(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f
    }
}

internal val LocalSelloReducedMotion = staticCompositionLocalOf { false }
