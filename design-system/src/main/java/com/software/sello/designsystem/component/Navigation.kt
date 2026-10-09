package com.software.sello.designsystem.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.icon.painter
import com.software.sello.designsystem.theme.SelloTheme

/** One top-level place. [id] is what a selection reports; the app decides what it opens. */
@Immutable
data class SelloDestination(val id: String, val label: String, val icon: SelloIcon)

/**
 * The top-level destinations of the app, however many there are. The same data feeds
 * the tab bar and the rail; neither keeps a selection or navigates on its own.
 */
@Immutable
class SelloNavigation(
    val destinations: List<SelloDestination>,
    val selectedId: String,
    val onSelect: (String) -> Unit
)

/**
 * The action that starts an entry from anywhere: the dock above the tabs, or the "+" on
 * a rail. [label] is also what it is called by a screen reader.
 */
@Immutable
class SelloDock(
    val label: String,
    val actionLabel: String,
    val onClick: () -> Unit,
    val onLongClick: (() -> Unit)? = null,
    val longClickLabel: String? = null
)

/** The tab bar. Selecting the current tab is reported too, so the app can scroll to top. */
@Composable
fun SelloNavigationBar(navigation: SelloNavigation, modifier: Modifier = Modifier) {
    val colors = SelloTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.paper)
            .drawBehind {
                val stroke = 1.5.dp.toPx()
                drawLine(
                    color = colors.rule,
                    start = Offset(0f, stroke / 2),
                    end = Offset(size.width, stroke / 2),
                    strokeWidth = stroke,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 5.dp.toPx()))
                )
            }
            .selectableGroup()
            .height(BarHeight)
    ) {
        navigation.destinations.forEach { destination ->
            val selected = destination.id == navigation.selectedId
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .selectable(selected, role = Role.Tab) { navigation.onSelect(destination.id) }
            ) {
                SelectionMark(selected, Modifier.width(44.dp).height(3.dp))
                DestinationContent(destination, selected, Modifier.weight(1f))
            }
        }
    }
}

/**
 * The rail that replaces the tab bar in short and expanded windows. With a [dock] its
 * "+" button replaces the dock.
 */
@Composable
fun SelloNavigationRail(
    navigation: SelloNavigation,
    modifier: Modifier = Modifier,
    dock: SelloDock? = null
) {
    val colors = SelloTheme.colors
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .fillMaxHeight()
            .width(RailWidth)
            .background(colors.paper)
            .padding(vertical = 8.dp)
    ) {
        if (dock != null) {
            IconButton(
                onClick = dock.onClick,
                modifier = Modifier
                    .size(SelloTheme.spacing.minTouchTarget)
                    .background(colors.brand, SelloTheme.shapes.button)
            ) {
                Icon(SelloIcon.Add.painter(), dock.label, tint = colors.onBrand)
            }
        }
        Column(modifier = Modifier.selectableGroup()) {
            navigation.destinations.forEach { destination ->
                val selected = destination.id == navigation.selectedId
                Row(
                    modifier = Modifier
                        .width(RailWidth)
                        .heightIn(min = 56.dp)
                        .selectable(selected, role = Role.Tab) {
                            navigation.onSelect(destination.id)
                        }
                ) {
                    SelectionMark(selected, Modifier.width(3.dp).height(56.dp))
                    DestinationContent(destination, selected, Modifier.weight(1f).height(56.dp))
                }
            }
        }
    }
}

@Composable
private fun SelectionMark(selected: Boolean, modifier: Modifier) {
    Box(modifier.background(if (selected) SelloTheme.colors.brand else SelloTheme.colors.paper))
}

@Composable
private fun DestinationContent(
    destination: SelloDestination,
    selected: Boolean,
    modifier: Modifier
) {
    val colors = SelloTheme.colors
    val tint = if (selected) colors.brand else colors.inkSoft
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Icon(destination.icon.painter(filled = selected), null, Modifier.size(22.dp), tint)
        Text(
            text = destination.label,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

internal val BarHeight = 64.dp
internal val RailWidth = 80.dp

/**
 * The line above the tabs that starts an entry. The whole dock is one button; the
 * coloured part at the end is decoration, not a second control.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EntryDock(dock: SelloDock, modifier: Modifier = Modifier) {
    val colors = SelloTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(SelloTheme.shapes.button)
            .background(colors.paper)
            .border(2.dp, colors.ink, SelloTheme.shapes.button)
            .combinedClickable(
                role = Role.Button,
                onLongClickLabel = dock.longClickLabel,
                onLongClick = dock.onLongClick,
                onClick = dock.onClick
            )
            .clearAndSetSemantics { contentDescription = dock.label }
            .padding(start = 16.dp, end = 7.dp)
    ) {
        Text(
            text = dock.label,
            style = MaterialTheme.typography.bodyLarge,
            color = colors.inkSoft,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .heightIn(min = 42.dp)
                .background(colors.brand, SelloTheme.shapes.control)
                .padding(start = 10.dp, end = 14.dp)
        ) {
            Icon(SelloIcon.Add.painter(), null, Modifier.size(20.dp), colors.onBrand)
            Text(
                text = dock.actionLabel,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
                color = colors.onBrand
            )
        }
    }
}

/** The month as a screen title that opens a month picker. It takes its ink from the bar. */
@Composable
fun MonthSwitcher(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .heightIn(min = SelloTheme.spacing.minTouchTarget)
            .clip(SelloTheme.shapes.control)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(end = 4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge.copy(fontSize = 21.sp, lineHeight = 24.sp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Icon(SelloIcon.KeyboardArrowDown.painter(), null, Modifier.size(22.dp))
    }
}

/**
 * One month in a [MonthGrid]. A month that is not [enabled] cannot be picked: the app
 * disables future months. [hasData] only dims a month that has nothing recorded.
 */
@Immutable
data class MonthCell(
    val id: String,
    val label: String,
    val spoken: String = label,
    val enabled: Boolean = true,
    val hasData: Boolean = true,
    val isCurrent: Boolean = false
)

/**
 * The twelve months of one year. It shows what it is given and reports a pick; which
 * months exist, which is current and which year comes next are the app's to decide. A
 * null year callback disables that arrow.
 */
@Composable
fun MonthGrid(
    yearLabel: String,
    months: List<MonthCell>,
    selectedId: String?,
    onPick: (String) -> Unit,
    previousYearLabel: String,
    nextYearLabel: String,
    modifier: Modifier = Modifier,
    onPreviousYear: (() -> Unit)? = null,
    onNextYear: (() -> Unit)? = null
) {
    val colors = SelloTheme.colors
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            YearArrow(SelloIcon.ChevronLeft, previousYearLabel, onPreviousYear)
            Text(
                text = yearLabel,
                style = MaterialTheme.typography.titleLarge,
                color = colors.ink,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f).semantics { heading() }
            )
            YearArrow(SelloIcon.ChevronRight, nextYearLabel, onNextYear)
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.selectableGroup()
        ) {
            months.chunked(MONTHS_PER_ROW).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    row.forEach { month ->
                        val selected = month.id == selectedId
                        val outline = when {
                            selected || month.isCurrent -> colors.brand
                            else -> colors.rule
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = SelloTheme.spacing.minTouchTarget)
                                .alpha(if (month.enabled) 1f else DISABLED_MONTH_ALPHA)
                                .clip(SelloTheme.shapes.control)
                                .background(if (selected) colors.brand else colors.paper)
                                .border(
                                    if (month.isCurrent) 2.dp else 1.5.dp,
                                    outline,
                                    SelloTheme.shapes.control
                                )
                                .selectable(selected, month.enabled, Role.RadioButton) {
                                    onPick(month.id)
                                }
                                .clearAndSetSemantics { contentDescription = month.spoken }
                        ) {
                            Text(
                                text = month.label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.W600
                                ),
                                color = when {
                                    selected -> colors.onBrand
                                    month.hasData -> colors.ink
                                    else -> colors.inkSoft
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun YearArrow(icon: SelloIcon, label: String, onClick: (() -> Unit)?) {
    IconButton(onClick = { onClick?.invoke() }, enabled = onClick != null) {
        Icon(
            painter = icon.painter(),
            contentDescription = label,
            tint = LocalContentColor.current.takeIf { onClick == null } ?: SelloTheme.colors.brand
        )
    }
}

private const val MONTHS_PER_ROW = 3
private const val DISABLED_MONTH_ALPHA = 0.38f
