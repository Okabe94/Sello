package com.software.sello.catalog.foundations

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.software.sello.catalog.R
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.icon.painter
import com.software.sello.designsystem.theme.SelloMotion
import com.software.sello.designsystem.theme.SelloTheme
import kotlinx.coroutines.launch

@Composable
private fun Sheet(content: @Composable () -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.md),
        modifier = Modifier
            .fillMaxWidth()
            .background(SelloTheme.colors.paper, SelloTheme.shapes.control)
            .padding(SelloTheme.spacing.slipPadding)
    ) { content() }
}

@Composable
private fun Caption(text: String) {
    Text(text, style = MaterialTheme.typography.labelMedium, color = SelloTheme.colors.inkSoft)
}

@Composable
fun TypographyExample() {
    val t = MaterialTheme.typography
    val roles: List<Pair<String, TextStyle>> = listOf(
        "displayLarge" to t.displayLarge,
        "displayMedium" to t.displayMedium,
        "displaySmall" to t.displaySmall,
        "headlineSmall" to t.headlineSmall,
        "titleLarge" to t.titleLarge,
        "titleMedium" to t.titleMedium,
        "bodyLarge" to t.bodyLarge,
        "bodyMedium" to t.bodyMedium,
        "labelLarge" to t.labelLarge,
        "labelMedium" to t.labelMedium,
        "labelSmall" to t.labelSmall
    )
    val stamps = listOf(
        "stampSmall" to SelloTheme.type.stampSmall,
        "stampLarge" to SelloTheme.type.stampLarge,
        "stampHero" to SelloTheme.type.stampHero
    )
    Sheet {
        roles.forEach { (name, style) ->
            Column {
                Caption(name)
                Text(
                    text = stringResource(R.string.catalog_type_sample),
                    style = style,
                    color = SelloTheme.colors.ink,
                    modifier = Modifier.testTag(typeTag(name))
                )
            }
        }
        stamps.forEach { (name, style) ->
            Column {
                Caption(name)
                Text(
                    text = stringResource(R.string.catalog_type_stamp_sample).uppercase(),
                    style = style,
                    color = SelloTheme.colors.semantic.gain,
                    modifier = Modifier.testTag(typeTag(name))
                )
            }
        }
    }
}

fun typeTag(name: String) = "type:$name"

/** Synthetic figures at the lengths that break layouts. No real amounts. */
private val LongMoneySamples = listOf(
    "$ 937.200",
    "+$ 6.050.000",
    "\u2212$ 1.234.567.890",
    "$ 9.223.372.036.854.775.807",
    "USD 2.340,00"
)

@Composable
fun MoneyTextExample() {
    val t = MaterialTheme.typography
    Sheet {
        listOf(t.displayLarge, t.displayMedium, t.titleLarge, t.bodyLarge).forEach { style ->
            LongMoneySamples.forEach { sample ->
                Text(sample, style = style, color = SelloTheme.colors.ink)
            }
        }
    }
}

@Composable
fun ShapeSpacingExample() {
    val shapes = SelloTheme.shapes
    val spacing = SelloTheme.spacing
    val namedShapes: List<Pair<String, CornerBasedShape>> = listOf(
        "slip" to shapes.slip,
        "control" to shapes.control,
        "button" to shapes.button,
        "bar" to shapes.bar,
        "stampSmall" to shapes.stampSmall,
        "stampLarge" to shapes.stampLarge,
        "stampHero" to shapes.stampHero,
        "sheet" to shapes.sheet
    )
    val distances: List<Pair<String, Dp>> = listOf(
        "xs" to spacing.xs,
        "sm" to spacing.sm,
        "md" to spacing.md,
        "lg" to spacing.lg,
        "xl" to spacing.xl,
        "deskMargin" to spacing.deskMargin,
        "slipPadding" to spacing.slipPadding,
        "slipGap" to spacing.slipGap,
        "minTouchTarget" to spacing.minTouchTarget
    )
    Column(verticalArrangement = Arrangement.spacedBy(spacing.slipGap)) {
        Sheet {
            namedShapes.forEach { (name, shape) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.md)
                ) {
                    Box(Modifier.size(72.dp, 40.dp).background(SelloTheme.colors.brandSoft, shape))
                    Caption(name)
                }
            }
        }
        Sheet {
            distances.forEach { (name, distance) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(spacing.md)
                ) {
                    Box(Modifier.width(distance).height(12.dp).background(SelloTheme.colors.brand))
                    Caption("$name ${distance.value.toInt()}dp")
                }
            }
        }
    }
}

@Composable
fun MotionExample() {
    val tokens: List<Pair<String, AnimationSpec<Float>>> = listOf(
        "quick" to SelloMotion.quick(),
        "standard" to SelloMotion.standard(),
        "emphasized" to SelloMotion.emphasized(),
        "count" to SelloMotion.count(),
        "fill" to SelloMotion.fill(),
        "settle" to SelloMotion.settle(),
        "stamp" to SelloMotion.stamp(),
        "feed" to SelloMotion.feed()
    )
    Sheet {
        tokens.forEach { (name, spec) -> MotionRow(name, spec) }
    }
}

@Composable
private fun MotionRow(name: String, spec: AnimationSpec<Float>) {
    val progress = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "motion.$name",
                style = MaterialTheme.typography.bodyLarge,
                color = SelloTheme.colors.ink,
                modifier = Modifier.weight(1f)
            )
            TextButton(
                onClick = {
                    scope.launch {
                        progress.snapTo(0f)
                        progress.animateTo(1f, spec)
                    }
                }
            ) { Text(stringResource(R.string.catalog_motion_play)) }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .background(SelloTheme.colors.paperDim, SelloTheme.shapes.bar)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress.value.coerceIn(0f, 1f))
                    .height(8.dp)
                    .background(SelloTheme.colors.brand, SelloTheme.shapes.bar)
            )
        }
    }
}

@Composable
fun IconsExample() {
    Sheet {
        SelloIcon.entries.forEach { icon ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(SelloTheme.spacing.md)
            ) {
                Icon(
                    painter = icon.painter(),
                    contentDescription = icon.name,
                    tint = SelloTheme.colors.brand,
                    modifier = Modifier.size(22.dp)
                )
                if (icon.outlined != null) {
                    Icon(
                        painter = icon.painter(filled = false),
                        contentDescription = stringResource(
                            R.string.catalog_icons_outlined,
                            icon.name
                        ),
                        tint = SelloTheme.colors.inkSoft,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Caption(icon.name)
            }
        }
    }
}
