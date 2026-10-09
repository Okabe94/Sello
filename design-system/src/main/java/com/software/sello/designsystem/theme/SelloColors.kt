package com.software.sello.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** The brand ink the user picks. It never changes what gain, loss or warn mean. */
enum class SelloInk { Cobalto, Violeta }

/**
 * Sello's colour tokens. The four schemes (two inks × light/dark) share every name;
 * values come from the foundations of the approved visual reference.
 */
@Immutable
data class SelloColors(
    val family: SelloInk,
    val isDark: Boolean,
    val brand: Color,
    val onBrand: Color,
    val brandSoft: Color,
    val onBrandSoft: Color,
    val desk: Color,
    val paper: Color,
    val paperAlt: Color,
    val paperDim: Color,
    val ink: Color,
    val inkSoft: Color,
    val outline: Color,
    val rule: Color,
    val band: Color,
    val onBand: Color,
    val pastBar: Color,
    val onPastBar: Color,
    val pastBarSoft: Color,
    val onPastBarSoft: Color,
    val semantic: SelloSemanticColors,
    val categories: List<Color>
)

/** Meanings that stay the same in both inks, so a stamp reads the same everywhere. */
@Immutable
data class SelloSemanticColors(
    val gain: Color,
    val onGain: Color,
    val gainSoft: Color,
    val onGainSoft: Color,
    val loss: Color,
    val onLoss: Color,
    val lossSoft: Color,
    val onLossSoft: Color,
    val warn: Color,
    val warnSoft: Color,
    val dividend: Color
)

fun selloColors(ink: SelloInk, isDark: Boolean): SelloColors = when (ink) {
    SelloInk.Cobalto -> if (isDark) CobaltoDark else CobaltoLight
    SelloInk.Violeta -> if (isDark) VioletaDark else VioletaLight
}

private val LightSemantic = SelloSemanticColors(
    gain = Color(0xFF0A6B4A),
    onGain = Color(0xFFFFFFFF),
    gainSoft = Color(0xFFCDEFE0),
    onGainSoft = Color(0xFF053B28),
    loss = Color(0xFFB81E2A),
    onLoss = Color(0xFFFFFFFF),
    lossSoft = Color(0xFFFBD9DB),
    onLossSoft = Color(0xFF5C0B12),
    warn = Color(0xFF8A4A00),
    warnSoft = Color(0xFFFFE3BF),
    dividend = Color(0xFF6A3FC0)
)

private val DarkSemantic = SelloSemanticColors(
    gain = Color(0xFF4FD6A0),
    onGain = Color(0xFF04382A),
    gainSoft = Color(0xFF0E4A35),
    onGainSoft = Color(0xFFC9F2E0),
    loss = Color(0xFFFF8A8A),
    onLoss = Color(0xFF4D0A0A),
    lossSoft = Color(0xFF5A171B),
    onLossSoft = Color(0xFFFFDADA),
    warn = Color(0xFFFFB45C),
    warnSoft = Color(0xFF55340C),
    dividend = Color(0xFFB7A0F5)
)

private val LightCategories = listOf(
    Color(0xFFC4343A),
    Color(0xFFB8521A),
    Color(0xFF9A6400),
    Color(0xFF7C6C00),
    Color(0xFF4F7F18),
    Color(0xFF1B7D45),
    Color(0xFF0B7F73),
    Color(0xFF1773AA),
    Color(0xFF4456C4),
    Color(0xFF7C4AC4),
    Color(0xFFB83A84),
    Color(0xFF5F6878)
)

private val DarkCategories = listOf(
    Color(0xFFF2726B),
    Color(0xFFF28B4E),
    Color(0xFFE8A63A),
    Color(0xFFD4BC3E),
    Color(0xFF9CCB5A),
    Color(0xFF5BC489),
    Color(0xFF45C7B8),
    Color(0xFF5AB6EA),
    Color(0xFF8C9CF2),
    Color(0xFFB693F0),
    Color(0xFFEE82BE),
    Color(0xFFA3ADB8)
)

private val CobaltoLight = SelloColors(
    family = SelloInk.Cobalto,
    isDark = false,
    brand = Color(0xFF1F3BE0),
    onBrand = Color(0xFFFFFFFF),
    brandSoft = Color(0xFFDCE2FF),
    onBrandSoft = Color(0xFF0B1C7A),
    desk = Color(0xFFD9E1F7),
    paper = Color(0xFFFFFFFF),
    paperAlt = Color(0xFFEEF2FC),
    paperDim = Color(0xFFD3DCF3),
    ink = Color(0xFF0E1630),
    inkSoft = Color(0xFF434E72),
    outline = Color(0xFF6975A0),
    rule = Color(0xFFB9C5E6),
    band = Color(0xFF1F3BE0),
    onBand = Color(0xFFFFFFFF),
    pastBar = Color(0xFF566287),
    onPastBar = Color(0xFFFFFFFF),
    pastBarSoft = Color(0xFFE1E7F6),
    onPastBarSoft = Color(0xFF17203C),
    semantic = LightSemantic,
    categories = LightCategories
)

private val CobaltoDark = SelloColors(
    family = SelloInk.Cobalto,
    isDark = true,
    brand = Color(0xFF8FA6FF),
    onBrand = Color(0xFF06175C),
    brandSoft = Color(0xFF23368F),
    onBrandSoft = Color(0xFFDDE4FF),
    desk = Color(0xFF090D1C),
    paper = Color(0xFF151C36),
    paperAlt = Color(0xFF1D2646),
    paperDim = Color(0xFF2A3560),
    ink = Color(0xFFEEF1FB),
    inkSoft = Color(0xFFA9B3D6),
    outline = Color(0xFF8490BA),
    rule = Color(0xFF2F3B66),
    band = Color(0xFF2238B8),
    onBand = Color(0xFFFFFFFF),
    pastBar = Color(0xFFA7B2D2),
    onPastBar = Color(0xFF17203C),
    pastBarSoft = Color(0xFF2A3560),
    onPastBarSoft = Color(0xFFE1E7F6),
    semantic = DarkSemantic,
    categories = DarkCategories
)

private val VioletaLight = CobaltoLight.copy(
    family = SelloInk.Violeta,
    brand = Color(0xFF5B2BD6),
    brandSoft = Color(0xFFE6DCFF),
    onBrandSoft = Color(0xFF2A0F78),
    desk = Color(0xFFE2DAF6),
    paperAlt = Color(0xFFF3EEFC),
    paperDim = Color(0xFFDCD2F2),
    ink = Color(0xFF150F2E),
    inkSoft = Color(0xFF4A4370),
    outline = Color(0xFF75679F),
    rule = Color(0xFFC4B7E6),
    band = Color(0xFF5B2BD6),
    pastBar = Color(0xFF5F5887),
    pastBarSoft = Color(0xFFE7E1F6)
)

private val VioletaDark = CobaltoDark.copy(
    family = SelloInk.Violeta,
    brand = Color(0xFFB79CFF),
    onBrand = Color(0xFF22085E),
    brandSoft = Color(0xFF3A1F8F),
    onBrandSoft = Color(0xFFE9DFFF),
    desk = Color(0xFF0E0A1C),
    paper = Color(0xFF1A1433),
    paperAlt = Color(0xFF231B45),
    paperDim = Color(0xFF30265C),
    ink = Color(0xFFF1EEFB),
    inkSoft = Color(0xFFB3ABD6),
    outline = Color(0xFF8E82B8),
    rule = Color(0xFF372C66),
    band = Color(0xFF4420B0),
    pastBar = Color(0xFFABA3CF),
    pastBarSoft = Color(0xFF30265C)
)

internal val LocalSelloColors = staticCompositionLocalOf { CobaltoLight }
