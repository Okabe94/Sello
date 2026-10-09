package com.software.sello.catalog

import androidx.annotation.StringRes
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.software.sello.designsystem.theme.SelloInk

enum class CatalogMode(@StringRes val label: Int) {
    System(R.string.catalog_mode_system),
    Light(R.string.catalog_mode_light),
    Dark(R.string.catalog_mode_dark)
}

enum class CatalogFontScale(val scale: Float) {
    Normal(1.0f),
    Large(1.3f),
    Largest(2.0f)
}

enum class CatalogMotion(@StringRes val label: Int) {
    Full(R.string.catalog_motion_full),
    Reduced(R.string.catalog_motion_reduced)
}

/** Widths at the reference's breakpoints; [Device] uses whatever the screen offers. */
enum class CatalogWindow(@StringRes val label: Int, val width: Dp?, val height: Dp? = null) {
    Device(R.string.catalog_window_device, null),
    Compact(R.string.catalog_window_compact, 360.dp),
    Landscape(R.string.catalog_window_landscape, 760.dp, 400.dp),
    Medium(R.string.catalog_window_medium, 600.dp),
    Expanded(R.string.catalog_window_expanded, 840.dp)
}

/** Catalog-only viewing choices. They live here, never in the design-system API. */
data class CatalogSettings(
    val ink: SelloInk = SelloInk.Cobalto,
    val mode: CatalogMode = CatalogMode.System,
    val fontScale: CatalogFontScale = CatalogFontScale.Normal,
    val window: CatalogWindow = CatalogWindow.Device,
    val motion: CatalogMotion = CatalogMotion.Full
) {
    companion object {
        val Saver: Saver<CatalogSettings, Any> = listSaver(
            save = {
                listOf(it.ink.name, it.mode.name, it.fontScale.name, it.window.name, it.motion.name)
            },
            restore = {
                CatalogSettings(
                    ink = SelloInk.valueOf(it[0]),
                    mode = CatalogMode.valueOf(it[1]),
                    fontScale = CatalogFontScale.valueOf(it[2]),
                    window = CatalogWindow.valueOf(it[3]),
                    motion = CatalogMotion.valueOf(it[4])
                )
            }
        )
    }
}
