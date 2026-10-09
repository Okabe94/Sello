package com.software.sello.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.icon.painter
import com.software.sello.designsystem.theme.SelloTheme

/**
 * A receipt line: label, dotted leader, value. For two to six facts. Read as one node,
 * "label, value". Put a [MoneyText] in [value] for amounts.
 */
@Composable
fun LeaderLine(
    label: String,
    modifier: Modifier = Modifier,
    icon: SelloIcon? = null,
    caption: String? = null,
    onClick: (() -> Unit)? = null,
    value: @Composable () -> Unit
) {
    val minHeight = when {
        onClick != null && caption == null -> SelloTheme.spacing.minTouchTarget
        caption != null -> 52.dp
        else -> 36.dp
    }
    val interaction = if (onClick == null) {
        Modifier.semantics(mergeDescendants = true) {}
    } else {
        Modifier.clickable(role = Role.Button, onClick = onClick)
    }
    LeaderLayout(
        modifier = modifier.fillMaxWidth().then(interaction).heightIn(min = minHeight),
        start = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icon != null) {
                    Icon(
                        painter = icon.painter(),
                        contentDescription = null,
                        tint = SelloTheme.colors.brand,
                        modifier = Modifier.padding(end = 7.dp).size(18.dp)
                    )
                }
                Column {
                    Text(label, style = MaterialTheme.typography.bodyLarge)
                    if (caption != null) {
                        Text(
                            caption,
                            style = MaterialTheme.typography.labelSmall,
                            color = SelloTheme.colors.inkSoft
                        )
                    }
                }
            }
        },
        value = value
    )
}

/** A [LeaderLine] whose value is plain text, such as a date or a count. */
@Composable
fun LeaderLine(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: SelloIcon? = null,
    caption: String? = null,
    valueColor: Color = Color.Unspecified,
    onClick: (() -> Unit)? = null
) {
    LeaderLine(label, modifier, icon, caption, onClick) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.W700,
                fontFeatureSettings = "tnum"
            ),
            color = valueColor.takeOrElse { SelloTheme.colors.ink }
        )
    }
}

/** The closing line of a receipt: a double rule, a bold label and the total. */
@Composable
fun TotalLine(label: String, modifier: Modifier = Modifier, value: @Composable () -> Unit) {
    val ink = SelloTheme.colors.ink
    LeaderLayout(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp)
            .drawBehind {
                val line = 1.dp.toPx()
                drawLine(ink, Offset(0f, line / 2), Offset(size.width, line / 2), line)
                drawLine(ink, Offset(0f, line * 2.5f), Offset(size.width, line * 2.5f), line)
            }
            .padding(top = 10.dp, bottom = 8.dp)
            .semantics(mergeDescendants = true) {},
        start = {
            Text(
                label,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.W700)
            )
        },
        value = value
    )
}

/**
 * Label at the start, value at the end, dotted leader between. The value is never
 * squeezed; the leader gives way first and disappears before the label has to wrap.
 */
@Composable
private fun LeaderLayout(
    modifier: Modifier,
    start: @Composable () -> Unit,
    value: @Composable () -> Unit
) {
    val dots = SelloTheme.colors.outline.copy(alpha = 0.6f)
    Layout(
        modifier = modifier,
        content = {
            start()
            Canvas(Modifier) {
                val stroke = 2.dp.toPx()
                drawLine(
                    color = dots,
                    start = Offset(stroke / 2, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = stroke,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(0f, stroke * 2))
                )
            }
            value()
        }
    ) { measurables, constraints ->
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val gap = LeaderGap.roundToPx()
        val minLeader = LeaderMinWidth.roundToPx() + 2 * gap
        val valuePlaceable = measurables[2].measure(loose)
        val startMax = (constraints.maxWidth - valuePlaceable.width - gap).coerceAtLeast(0)
        val startPlaceable = measurables[0].measure(loose.copy(maxWidth = startMax))
        val free = constraints.maxWidth - startPlaceable.width - valuePlaceable.width
        val leaderWidth = if (free >= minLeader) free - 2 * gap else 0
        val leader = measurables[1].measure(
            androidx.compose.ui.unit.Constraints.fixed(leaderWidth, 2.dp.roundToPx())
        )
        val height = maxOf(startPlaceable.height, valuePlaceable.height, constraints.minHeight)
        layout(constraints.maxWidth, height) {
            startPlaceable.placeRelative(0, (height - startPlaceable.height) / 2)
            valuePlaceable.placeRelative(
                constraints.maxWidth - valuePlaceable.width,
                (height - valuePlaceable.height) / 2
            )
            if (leaderWidth > 0) {
                leader.placeRelative(startPlaceable.width + gap, height / 2 + 4.dp.roundToPx())
            }
        }
    }
}

private val LeaderGap = 6.dp
private val LeaderMinWidth = 12.dp

/** A small label with a larger value under it, used in twos and threes under a tear line. */
@Composable
fun KeyValue(
    label: String,
    modifier: Modifier = Modifier,
    icon: SelloIcon? = null,
    caption: String? = null,
    value: @Composable () -> Unit
) {
    Column(modifier = modifier.semantics(mergeDescendants = true) {}) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                Icon(
                    painter = icon.painter(),
                    contentDescription = null,
                    tint = SelloTheme.colors.brand,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                color = SelloTheme.colors.inkSoft
            )
        }
        value()
        if (caption != null) {
            Text(
                caption,
                style = MaterialTheme.typography.labelMedium,
                color = SelloTheme.colors.inkSoft,
                maxLines = 2
            )
        }
    }
}

/** A [KeyValue] whose value is plain text. */
@Composable
fun KeyValue(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: SelloIcon? = null,
    caption: String? = null,
    valueColor: Color = Color.Unspecified
) {
    KeyValue(label, modifier, icon, caption) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = valueColor.takeOrElse { SelloTheme.colors.ink }
        )
    }
}
