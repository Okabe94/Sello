package com.software.sello.catalog.foundations

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.software.sello.catalog.R
import com.software.sello.designsystem.theme.SelloColors
import com.software.sello.designsystem.theme.SelloInk
import com.software.sello.designsystem.theme.SelloTheme

/** A token, its colour and the Material slot it feeds (empty when it is Sello-only). */
private class Swatch(val name: String, val color: Color, val slot: String = "")

private fun brandSwatches(c: SelloColors) = listOf(
    Swatch("brand", c.brand, "primary"),
    Swatch("onBrand", c.onBrand, "onPrimary"),
    Swatch("brandSoft", c.brandSoft, "primaryContainer"),
    Swatch("onBrandSoft", c.onBrandSoft, "onPrimaryContainer"),
    Swatch("desk", c.desk, "background"),
    Swatch("paper", c.paper, "surface"),
    Swatch("paperAlt", c.paperAlt, "surfaceContainer"),
    Swatch("paperDim", c.paperDim, "surfaceContainerHigh"),
    Swatch("ink", c.ink, "onSurface"),
    Swatch("inkSoft", c.inkSoft, "onSurfaceVariant"),
    Swatch("outline", c.outline, "outline"),
    Swatch("rule", c.rule, "outlineVariant"),
    Swatch("band", c.band),
    Swatch("pastBar", c.pastBar, "secondary")
)

private fun semanticSwatches(c: SelloColors) = with(c.semantic) {
    listOf(
        Swatch("gain", gain, "tertiary"),
        Swatch("gainSoft", gainSoft, "tertiaryContainer"),
        Swatch("loss", loss, "error"),
        Swatch("lossSoft", lossSoft, "errorContainer"),
        Swatch("warn", warn),
        Swatch("warnSoft", warnSoft),
        Swatch("dividend", dividend)
    )
}

@Composable
fun ColorsExample() {
    val colors = SelloTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.slipGap)) {
        SwatchList(stringResource(R.string.catalog_colors_brand), brandSwatches(colors))
        SwatchList(stringResource(R.string.catalog_colors_semantic), semanticSwatches(colors))
        SwatchList(
            stringResource(R.string.catalog_colors_categories),
            colors.categories.mapIndexed { index, color -> Swatch("category$index", color) }
        )
    }
}

@Composable
private fun SwatchList(title: String, swatches: List<Swatch>) {
    val colors = SelloTheme.colors
    val spacing = SelloTheme.spacing
    Column(
        verticalArrangement = Arrangement.spacedBy(spacing.sm),
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.paper, SelloTheme.shapes.control)
            .padding(spacing.slipPadding)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = colors.ink)
        swatches.forEach { swatch ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(spacing.md)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(swatch.color, SelloTheme.shapes.stampSmall)
                        .border(1.dp, colors.rule, SelloTheme.shapes.stampSmall)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        swatch.name,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.ink
                    )
                    if (swatch.slot.isNotEmpty()) {
                        Text(
                            swatch.slot,
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.inkSoft
                        )
                    }
                }
                Text(
                    text = hex(swatch.color),
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.ink,
                    modifier = Modifier.testTag(tokenTag(swatch.name))
                )
            }
        }
    }
}

fun tokenTag(name: String) = "token:$name"

private fun hex(color: Color) = "#%06X".format(color.toArgb() and 0xFFFFFF)

@Composable
private fun ColorsPreview(ink: SelloInk, dark: Boolean) {
    SelloTheme(ink = ink, darkTheme = dark) {
        Box(modifier = Modifier.background(SelloTheme.colors.desk).padding(12.dp)) {
            ColorsExample()
        }
    }
}

@Preview(name = "Cobalto, light")
@Composable
private fun ColorsCobaltoLightPreview() = ColorsPreview(SelloInk.Cobalto, dark = false)

@Preview(name = "Cobalto, dark")
@Composable
private fun ColorsCobaltoDarkPreview() = ColorsPreview(SelloInk.Cobalto, dark = true)

@Preview(name = "Violeta, light")
@Composable
private fun ColorsVioletaLightPreview() = ColorsPreview(SelloInk.Violeta, dark = false)

@Preview(name = "Violeta, dark")
@Composable
private fun ColorsVioletaDarkPreview() = ColorsPreview(SelloInk.Violeta, dark = true)
