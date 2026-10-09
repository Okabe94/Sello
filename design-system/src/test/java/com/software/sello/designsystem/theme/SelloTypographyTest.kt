package com.software.sello.designsystem.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SelloTypographyTest {
    private val roles: Map<String, TextStyle> = with(SelloTypography) {
        mapOf(
            "displayLarge" to displayLarge,
            "displayMedium" to displayMedium,
            "displaySmall" to displaySmall,
            "headlineLarge" to headlineLarge,
            "headlineMedium" to headlineMedium,
            "headlineSmall" to headlineSmall,
            "titleLarge" to titleLarge,
            "titleMedium" to titleMedium,
            "titleSmall" to titleSmall,
            "bodyLarge" to bodyLarge,
            "bodyMedium" to bodyMedium,
            "bodySmall" to bodySmall,
            "labelLarge" to labelLarge,
            "labelMedium" to labelMedium,
            "labelSmall" to labelSmall
        )
    }

    @Test
    fun referenceRolesHaveTheirSizeLineWeightAndTracking() {
        // size, line height, weight, tracking in em — typed from the reference table.
        val expected = mapOf(
            "displayLarge" to listOf(60.0, 61.0, 900.0, -0.04),
            "displayMedium" to listOf(44.0, 46.0, 900.0, -0.04),
            "displaySmall" to listOf(32.0, 34.0, 900.0, -0.035),
            "headlineSmall" to listOf(23.0, 26.0, 800.0, -0.02),
            "titleLarge" to listOf(19.0, 22.0, 800.0, -0.01),
            "titleMedium" to listOf(16.0, 20.0, 800.0, 0.0),
            "bodyLarge" to listOf(14.0, 19.0, 400.0, 0.0),
            "bodyMedium" to listOf(13.5, 18.0, 400.0, 0.0),
            "labelLarge" to listOf(17.0, 20.0, 800.0, 0.0),
            "labelMedium" to listOf(12.5, 16.0, 400.0, 0.0),
            "labelSmall" to listOf(12.0, 14.0, 600.0, 0.0)
        )
        for ((role, v) in expected) {
            val style = roles.getValue(role)
            assertEquals(role, v[0].sp, style.fontSize)
            assertEquals(role, v[1].sp, style.lineHeight)
            assertEquals(role, FontWeight(v[2].toInt()), style.fontWeight)
            assertEquals(role, v[3].em, style.letterSpacing)
        }
    }

    @Test
    fun everyMaterialRoleIsSchibstedWithTabularFiguresAndAtLeast12sp() {
        assertEquals(15, roles.size)
        for ((role, style) in roles) {
            assertEquals(role, SchibstedGrotesk, style.fontFamily)
            assertEquals(role, "tnum", style.fontFeatureSettings)
            assertTrue("$role is ${style.fontSize}", style.fontSize.value >= 12f)
        }
    }

    @Test
    fun stencilTypeIsLimitedToStampStyles() {
        val type = SelloType()
        val stamps = listOf(type.stampSmall, type.stampLarge, type.stampHero)
        assertEquals(listOf(15.sp, 34.sp, 52.sp), stamps.map { it.fontSize })
        stamps.forEach {
            assertEquals(SairaStencilOne, it.fontFamily)
            assertEquals(0.05.em, it.letterSpacing)
        }
        roles.forEach { (role, style) -> assertTrue(role, style.fontFamily != SairaStencilOne) }
    }
}
