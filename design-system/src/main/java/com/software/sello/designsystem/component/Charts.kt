package com.software.sello.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import com.software.sello.designsystem.theme.SelloTheme

/** Past is a finished period, Current the one in progress, Over one above its limit, Upcoming one not yet happened. */
enum class ChartPeriod { Past, Current, Over, Upcoming }

/**
 * One mark of a chart.
 *
 * @param id what a selection reports. The app looks the exact amount up by it; nothing
 * is ever read back from where a tap landed.
 * @param value used only to size the mark. It may be drawn imprecisely.
 * @param spoken the mark in words with its exact amount, such as "sábado, 86.000 pesos".
 * @param valueLabel a short figure above the mark, for the few that carry one.
 */
@Immutable
data class ChartPoint(
    val id: String,
    val axisLabel: String,
    val value: Long,
    val spoken: String,
    val period: ChartPeriod = ChartPeriod.Past,
    val valueLabel: String? = null
)

/** The single reference line a chart may carry, with its own label. */
@Immutable
data class ChartReference(val value: Long, val label: String)

/** The smallest a chart may be. */
val ChartMinHeight = 130.dp

/**
 * Columns for a handful of periods. Each column is a selectable item that says its
 * [ChartPoint.spoken] text, so the chart works with a screen reader, a keyboard or
 * Switch Access; [summary] describes it as a whole. With no [points] it shows
 * [emptyText] at the same size.
 */
@Composable
fun ColumnsChart(
    points: List<ChartPoint>,
    summary: String,
    modifier: Modifier = Modifier,
    selectedId: String? = null,
    onSelect: ((String) -> Unit)? = null,
    reference: ChartReference? = null,
    height: Dp = 150.dp,
    emptyText: String? = null
) {
    ChartFrame(points, summary, modifier, selectedId, onSelect, reference, height, emptyText) {
            point,
            fraction,
            selected,
            dimmed,
            barArea
        ->
        val colors = SelloTheme.colors
        val ink = when (point.period) {
            ChartPeriod.Past -> colors.pastBar
            ChartPeriod.Current, ChartPeriod.Upcoming -> colors.brand
            ChartPeriod.Over -> colors.semantic.loss
        }.copy(alpha = if (dimmed) DIMMED_ALPHA else 1f)
        val barHeight = if (fraction > 0f) (barArea * fraction).coerceAtLeast(2.dp) else 0.dp
        Box(
            modifier = Modifier
                .fillMaxWidth(COLUMN_WIDTH_SHARE)
                .height(barHeight)
                .drawBehind {
                    val corner = CornerRadius(4.dp.toPx())
                    val bar = Path().apply {
                        addRoundRect(
                            RoundRect(
                                left = 0f,
                                top = 0f,
                                right = size.width,
                                bottom = size.height,
                                topLeftCornerRadius = corner,
                                topRightCornerRadius = corner
                            )
                        )
                    }
                    if (point.period == ChartPeriod.Upcoming) {
                        // Not yet happened: an outline, never a solid bar.
                        val dash = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 3.dp.toPx()))
                        drawPath(bar, ink, style = Stroke(1.5.dp.toPx(), pathEffect = dash))
                    } else {
                        drawPath(bar, ink)
                    }
                    if (selected) drawPath(bar, colors.ink, style = Stroke(2.dp.toPx()))
                }
        )
    }
}

/**
 * One series as a line, for values over time. Selection and speech work as in
 * [ColumnsChart]: each point is its own selectable item.
 */
@Composable
fun LineChart(
    points: List<ChartPoint>,
    summary: String,
    modifier: Modifier = Modifier,
    selectedId: String? = null,
    onSelect: ((String) -> Unit)? = null,
    reference: ChartReference? = null,
    height: Dp = 150.dp,
    emptyText: String? = null
) {
    val colors = SelloTheme.colors
    val max = scaleMax(points, reference)
    ChartFrame(
        points = points,
        summary = summary,
        modifier = modifier,
        selectedId = selectedId,
        onSelect = onSelect,
        reference = reference,
        height = height,
        emptyText = emptyText,
        // Clear of the point's own dot.
        labelGap = 9.dp,
        behind = { labelRoom ->
            Canvas(Modifier.fillMaxSize()) {
                val area = size.height - labelRoom.toPx()
                val step = size.width / points.size
                val line = Path()
                points.forEachIndexed { index, point ->
                    val x = step * (index + 0.5f)
                    val y = labelRoom.toPx() + area * (1f - chartFraction(point.value, max))
                    if (index == 0) line.moveTo(x, y) else line.lineTo(x, y)
                }
                drawPath(line, colors.brand, style = Stroke(2.5.dp.toPx(), cap = StrokeCap.Round))
            }
        }
    ) { _, fraction, selected, _, barArea ->
        // The mark sits where the line passes; the invisible column under it is the target.
        Box(modifier = Modifier.height((barArea * fraction).coerceAtLeast(0.dp)).fillMaxWidth()) {
            val radius = if (selected) 6.dp else 3.5.dp
            Canvas(Modifier.align(Alignment.TopCenter).offset(y = -radius)) {
                drawCircle(colors.paper, (radius + 1.5.dp).toPx())
                drawCircle(if (selected) colors.ink else colors.brand, radius.toPx())
            }
        }
    }
}

private fun scaleMax(points: List<ChartPoint>, reference: ChartReference?): Long =
    maxOf(points.maxOfOrNull { it.value } ?: 0L, reference?.value ?: 0L)

@Composable
private fun ChartFrame(
    points: List<ChartPoint>,
    summary: String,
    modifier: Modifier,
    selectedId: String?,
    onSelect: ((String) -> Unit)?,
    reference: ChartReference?,
    height: Dp,
    emptyText: String?,
    labelGap: Dp = 2.dp,
    behind: @Composable (labelRoom: Dp) -> Unit = {},
    mark: @Composable (
        ChartPoint,
        fraction: Float,
        selected: Boolean,
        dimmed: Boolean,
        barArea: Dp
    ) -> Unit
) {
    val colors = SelloTheme.colors
    val chartHeight = height.coerceAtLeast(ChartMinHeight)
    if (points.isEmpty()) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier.fillMaxWidth().height(chartHeight + AxisRoom)
        ) {
            Text(
                text = emptyText ?: summary,
                style = MaterialTheme.typography.bodyLarge,
                color = colors.inkSoft,
                textAlign = TextAlign.Center
            )
        }
        return
    }
    val max = scaleMax(points, reference)
    Column(modifier = modifier.fillMaxWidth().semantics { contentDescription = summary }) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth().height(chartHeight)) {
            val barArea = maxHeight - LabelRoom
            behind(LabelRoom)
            Row(modifier = Modifier.fillMaxSize().selectableGroup()) {
                points.forEach { point ->
                    val selected = point.id == selectedId
                    val pick = if (onSelect == null) {
                        Modifier
                    } else {
                        Modifier.selectable(selected, role = Role.RadioButton) {
                            onSelect(point.id)
                        }
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .then(pick)
                            .clearAndSetSemantics { contentDescription = point.spoken }
                    ) {
                        if (point.valueLabel != null) {
                            Text(
                                text = point.valueLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.ink,
                                maxLines = 1,
                                modifier = Modifier.padding(bottom = labelGap)
                            )
                        }
                        mark(
                            point,
                            chartFraction(point.value, max),
                            selected,
                            selectedId != null && !selected,
                            barArea
                        )
                    }
                }
            }
            if (reference != null) {
                val y = LabelRoom + barArea * (1f - chartFraction(reference.value, max))
                Canvas(Modifier.fillMaxWidth().offset(y = y)) {
                    drawLine(
                        color = colors.ink,
                        start = Offset.Zero,
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(5.dp.toPx(), 4.dp.toPx())
                        )
                    )
                }
                Text(
                    text = reference.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.ink,
                    modifier = Modifier
                        .offset(y = (y - LabelRoom / 2).coerceAtLeast(0.dp))
                        .background(colors.paper)
                        .padding(horizontal = 4.dp)
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth().clearAndSetSemantics {}) {
            points.forEach { point ->
                Text(
                    text = point.axisLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.inkSoft,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    modifier = Modifier.weight(1f).padding(top = 4.dp)
                )
            }
        }
    }
}

private val LabelRoom = 18.dp
private val AxisRoom = 20.dp
private const val COLUMN_WIDTH_SHARE = 0.62f
private const val DIMMED_ALPHA = 0.5f

/**
 * The heading of a slip that holds a chart: a small question and a large answer. A
 * chart's selected value belongs here, as its exact formatted amount.
 */
@Composable
fun SlipHeading(question: String, modifier: Modifier = Modifier, answer: @Composable () -> Unit) {
    Column(modifier = modifier.semantics(mergeDescendants = true) { heading() }) {
        Text(
            text = question,
            style = MaterialTheme.typography.labelMedium,
            color = SelloTheme.colors.inkSoft
        )
        answer()
    }
}
