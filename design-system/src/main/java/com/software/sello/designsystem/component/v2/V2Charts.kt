package com.software.sello.designsystem.component.v2

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.software.sello.designsystem.component.CategoryCircle
import com.software.sello.designsystem.component.MoneyText
import com.software.sello.designsystem.component.MoneyTextValue
import com.software.sello.designsystem.component.Slip
import com.software.sello.designsystem.component.TearLine
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.icon.painter
import com.software.sello.designsystem.theme.SelloTheme

/** Fractions are approximate geometry only. The caller provides exact speech and adjacent figures. */
@Immutable
data class SplitSegment(
    val fraction: Float,
    val spoken: String,
    val color: Color,
    val striped: Boolean = false
)

@Composable
fun SplitBar(segments: List<SplitSegment>, modifier: Modifier = Modifier) {
    require(segments.all { it.fraction.isFinite() && it.fraction >= 0f })
    val total = segments.sumOf { it.fraction.toDouble() }.toFloat()
    val summary = segments.joinToString(". ") { it.spoken }
    Canvas(
        modifier.fillMaxWidth().height(14.dp).clip(SelloTheme.shapes.control).semantics {
            contentDescription =
                summary
        }
    ) {
        if (total == 0f) return@Canvas
        var start = 0f
        segments.forEach { segment ->
            val width = size.width * (segment.fraction / total)
            if (segment.striped) {
                clipRect(start, 0f, start + width, size.height) {
                    drawRect(
                        segment.color.copy(alpha = 0.15f),
                        Offset(start, 0f),
                        androidx.compose.ui.geometry.Size(width, size.height)
                    )
                    var x = start - size.height
                    while (x < start + width) {
                        drawLine(
                            segment.color,
                            Offset(x, size.height),
                            Offset(x + size.height, 0f),
                            2.dp.toPx()
                        )
                        x += 7.dp.toPx()
                    }
                }
            } else {
                drawRect(
                    segment.color,
                    Offset(start, 0f),
                    androidx.compose.ui.geometry.Size(width, size.height)
                )
            }
            start += width
        }
    }
}

@Immutable
data class ComparisonBar(
    val label: String,
    val amount: MoneyTextValue,
    val fraction: Float,
    val current: Boolean
)

@Composable
fun ComparisonSlip(
    question: String,
    difference: MoneyTextValue,
    explanation: String,
    trend: String,
    basis: String,
    bars: List<ComparisonBar>,
    modifier: Modifier = Modifier
) {
    val colors = SelloTheme.colors
    Slip(modifier.fillMaxWidth()) {
        Text(question, style = MaterialTheme.typography.labelMedium, color = colors.inkSoft)
        FittedMoneyText(difference)
        Text(explanation, fontWeight = FontWeight.W800)
        Text(trend, color = colors.inkSoft)
        TearLine()
        Text(basis, style = MaterialTheme.typography.labelSmall, color = colors.inkSoft)
        bars.forEach { bar ->
            Column(Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(bar.label)
                    MoneyText(bar.amount)
                }
                Box(
                    Modifier.fillMaxWidth().height(
                        14.dp
                    ).background(colors.paperDim, SelloTheme.shapes.control)
                ) {
                    Box(
                        Modifier.fillMaxWidth(
                            bar.fraction.coerceIn(0f, 1f)
                        ).height(
                            14.dp
                        ).background(
                            if (bar.current) colors.brand else colors.pastBar,
                            SelloTheme.shapes.control
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun SpendingRankRow(
    name: String,
    icon: SelloIcon,
    amount: MoneyTextValue,
    share: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier.fillMaxWidth().heightIn(
            min = 52.dp
        ).clickable(role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        CategoryCircle(icon, 1f, size = 36.dp, animate = false)
        Column(Modifier.weight(1f)) {
            Text(name, fontWeight = FontWeight.W700)
            Box(
                Modifier.fillMaxWidth().height(
                    6.dp
                ).background(SelloTheme.colors.paperDim, SelloTheme.shapes.control)
            ) {
                Box(
                    Modifier.fillMaxWidth(
                        share.coerceIn(0f, 1f)
                    ).height(6.dp).background(SelloTheme.colors.brand, SelloTheme.shapes.control)
                )
            }
        }
        MoneyText(amount)
        Icon(
            SelloIcon.ChevronRight.painter(),
            null,
            Modifier.size(18.dp),
            SelloTheme.colors.outline
        )
    }
}

/** Caller-mapped chart coordinates; the design system never derives a financial return. */
@Immutable
data class RecordedMark(val id: String, val x: Float, val y: Float, val spoken: String)

@Composable
fun RecordedValuesChart(
    values: List<RecordedMark>,
    contributions: List<RecordedMark>,
    summary: String,
    selectedId: String?,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = SelloTheme.colors
    Column(modifier.fillMaxWidth()) {
        Canvas(Modifier.fillMaxWidth().height(150.dp).semantics { contentDescription = summary }) {
            fun point(mark: RecordedMark) = Offset(
                mark.x.coerceIn(0f, 1f) * (size.width - 16.dp.toPx()) + 8.dp.toPx(),
                (1f - mark.y.coerceIn(0f, 1f)) * (size.height - 16.dp.toPx()) + 8.dp.toPx()
            )
            val steps = Path()
            contributions.forEachIndexed { index, mark ->
                val p = point(mark)
                if (index == 0) {
                    steps.moveTo(p.x, p.y)
                } else {
                    steps.lineTo(p.x, point(contributions[index - 1]).y)
                    steps.lineTo(p.x, p.y)
                }
            }
            drawPath(
                steps,
                colors.inkSoft,
                style = Stroke(
                    1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 4.dp.toPx()))
                )
            )
            val line = Path()
            values.forEachIndexed { index, mark ->
                val p = point(mark)
                if (index == 0) line.moveTo(p.x, p.y) else line.lineTo(p.x, p.y)
            }
            drawPath(line, colors.brand, style = Stroke(1.5.dp.toPx()))
            values.forEach { mark ->
                drawCircle(
                    if (mark.id ==
                        selectedId
                    ) {
                        colors.ink
                    } else {
                        colors.brand
                    },
                    if (mark.id ==
                        selectedId
                    ) {
                        6.dp.toPx()
                    } else {
                        4.dp.toPx()
                    },
                    point(mark)
                )
            }
        }
        // Named controls expose each real recorded value to keyboard and screen-reader users.
        Column(Modifier.selectableGroup()) {
            values.forEach { mark ->
                Text(
                    mark.spoken,
                    Modifier.fillMaxWidth().heightIn(
                        min = 48.dp
                    ).selectable(mark.id == selectedId, role = Role.RadioButton) {
                        onSelect(mark.id)
                    }.padding(8.dp),
                    color = colors.ink
                )
            }
        }
    }
}
