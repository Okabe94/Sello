package com.software.sello.designsystem.theme

import androidx.compose.ui.graphics.Color
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SelloColorsTest {
    private val schemes = SelloInk.entries.flatMap { ink ->
        listOf(false, true).map { dark -> selloColors(ink, dark) }
    }

    @Test
    fun eachSchemeCarriesTheReferenceBrandFamily() {
        // Typed from the reference's foundations table, not derived from the code.
        val expected = mapOf(
            (SelloInk.Cobalto to false) to listOf(
                0x1F3BE0, 0xFFFFFF, 0xDCE2FF, 0x0B1C7A, 0xD9E1F7, 0xFFFFFF, 0xEEF2FC,
                0xD3DCF3, 0x0E1630, 0x434E72, 0x6975A0, 0xB9C5E6, 0x1F3BE0, 0x566287
            ),
            (SelloInk.Cobalto to true) to listOf(
                0x8FA6FF, 0x06175C, 0x23368F, 0xDDE4FF, 0x090D1C, 0x151C36, 0x1D2646,
                0x2A3560, 0xEEF1FB, 0xA9B3D6, 0x8490BA, 0x2F3B66, 0x2238B8, 0xA7B2D2
            ),
            (SelloInk.Violeta to false) to listOf(
                0x5B2BD6, 0xFFFFFF, 0xE6DCFF, 0x2A0F78, 0xE2DAF6, 0xFFFFFF, 0xF3EEFC,
                0xDCD2F2, 0x150F2E, 0x4A4370, 0x75679F, 0xC4B7E6, 0x5B2BD6, 0x5F5887
            ),
            (SelloInk.Violeta to true) to listOf(
                0xB79CFF, 0x22085E, 0x3A1F8F, 0xE9DFFF, 0x0E0A1C, 0x1A1433, 0x231B45,
                0x30265C, 0xF1EEFB, 0xB3ABD6, 0x8E82B8, 0x372C66, 0x4420B0, 0xABA3CF
            )
        )
        for ((key, hex) in expected) {
            val c = selloColors(key.first, key.second)
            val actual = listOf(
                c.brand, c.onBrand, c.brandSoft, c.onBrandSoft, c.desk, c.paper, c.paperAlt,
                c.paperDim, c.ink, c.inkSoft, c.outline, c.rule, c.band, c.pastBar
            )
            assertEquals("$key", hex.map(::rgb), actual)
            assertEquals(key.first, c.family)
            assertEquals(key.second, c.isDark)
        }
    }

    @Test
    fun semanticColoursMatchTheReference() {
        val light = selloColors(SelloInk.Cobalto, isDark = false).semantic
        assertEquals(rgb(0x0A6B4A), light.gain)
        assertEquals(rgb(0xCDEFE0), light.gainSoft)
        assertEquals(rgb(0xB81E2A), light.loss)
        assertEquals(rgb(0xFBD9DB), light.lossSoft)
        assertEquals(rgb(0x8A4A00), light.warn)
        assertEquals(rgb(0x6A3FC0), light.dividend)

        val dark = selloColors(SelloInk.Cobalto, isDark = true).semantic
        assertEquals(rgb(0x4FD6A0), dark.gain)
        assertEquals(rgb(0x0E4A35), dark.gainSoft)
        assertEquals(rgb(0xFF8A8A), dark.loss)
        assertEquals(rgb(0x5A171B), dark.lossSoft)
        assertEquals(rgb(0xFFB45C), dark.warn)
        assertEquals(rgb(0xB7A0F5), dark.dividend)
    }

    @Test
    fun changingInkChangesBrandButNeverMeaning() {
        for (dark in listOf(false, true)) {
            val cobalto = selloColors(SelloInk.Cobalto, dark)
            val violeta = selloColors(SelloInk.Violeta, dark)
            assertNotEquals(cobalto.brand, violeta.brand)
            assertNotEquals(cobalto.band, violeta.band)
            assertNotEquals(cobalto.desk, violeta.desk)
            assertEquals(cobalto.semantic, violeta.semantic)
            assertEquals(cobalto.categories, violeta.categories)
        }
    }

    @Test
    fun twelveCategoryColoursPerTheme() {
        schemes.forEach { assertEquals(12, it.categories.distinct().size) }
    }

    @Test
    fun contrastHelperAgreesWithPublishedRatios() {
        val light = selloColors(SelloInk.Cobalto, isDark = false)
        assertEquals(17.9, contrast(light.ink, light.paper), 0.05)
        assertEquals(7.6, contrast(light.onBrand, light.brand), 0.05)
        assertEquals(21.0, contrast(Color.Black, Color.White), 0.001)
    }

    @Test
    fun everyReferencePairMeetsItsContrastNeed() {
        val failures = schemes.flatMap { c ->
            val s = c.semantic
            val text = 4.5
            val mark = 3.0
            val pairs = buildList {
                listOf(c.desk, c.paper, c.paperAlt, c.paperDim).forEach {
                    add(Triple(c.ink, it, text))
                }
                listOf(c.desk, c.paper, c.paperAlt).forEach {
                    add(Triple(c.inkSoft, it, text))
                    add(Triple(c.brand, it, text))
                }
                add(Triple(c.onBrand, c.brand, text))
                add(Triple(c.onBrandSoft, c.brandSoft, text))
                add(Triple(c.onBand, c.band, text))
                listOf(c.desk, c.paper).forEach {
                    add(Triple(s.gain, it, text))
                    add(Triple(s.loss, it, text))
                    add(Triple(s.warn, it, text))
                    add(Triple(c.outline, it, mark))
                }
                add(Triple(c.pastBar, c.paper, mark))
                add(Triple(s.onGain, s.gain, text))
                add(Triple(s.onLoss, s.loss, text))
                c.categories.forEach { category ->
                    listOf(c.desk, c.paper, c.paperAlt).forEach { add(Triple(category, it, mark)) }
                }
            }
            pairs.filter { (fg, bg, need) -> contrast(fg, bg) < need }
                .map { (fg, bg, need) -> "${c.family} dark=${c.isDark}: $fg on $bg needs $need" }
        }
        assertTrue(failures.joinToString("\n"), failures.isEmpty())
    }
}

internal fun rgb(hex: Int): Color = Color(0xFF000000 or hex.toLong())

/** WCAG 2.x contrast ratio, written here independently of production code. */
internal fun contrast(a: Color, b: Color): Double {
    fun channel(v: Float): Double {
        val c = v.toDouble()
        return if (c <= 0.04045) c / 12.92 else ((c + 0.055) / 1.055).pow(2.4)
    }
    fun luminance(c: Color) =
        0.2126 * channel(c.red) + 0.7152 * channel(c.green) + 0.0722 * channel(c.blue)
    val la = luminance(a)
    val lb = luminance(b)
    return (max(la, lb) + 0.05) / (min(la, lb) + 0.05)
}
