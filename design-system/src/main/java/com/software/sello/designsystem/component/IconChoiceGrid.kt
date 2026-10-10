package com.software.sello.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.icon.painter
import com.software.sello.designsystem.theme.SelloTheme

/** One icon that can be chosen. [label] is what a screen reader calls it. */
@Immutable
data class IconChoice(val id: String, val icon: SelloIcon, val label: String)

/**
 * A grid of icons where exactly one can be chosen, as for a category. It shows what it
 * is given and reports a pick; which icons exist and which is chosen are the app's.
 * Up to [maxColumns] per row, fewer when the width or a large font leaves less room,
 * and every cell stays at least the minimum touch size.
 */
@Composable
fun IconChoiceGrid(
    choices: List<IconChoice>,
    selectedId: String?,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    maxColumns: Int = 7
) {
    val colors = SelloTheme.colors
    val cell = SelloTheme.spacing.minTouchTarget
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val columns = iconColumns(maxWidth, cell, maxColumns)
        Column(
            verticalArrangement = Arrangement.spacedBy(IconGap),
            modifier = Modifier.selectableGroup()
        ) {
            choices.chunked(columns).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(IconGap)) {
                    row.forEach { choice ->
                        val selected = choice.id == selectedId
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = cell)
                                .clip(SelloTheme.shapes.control)
                                .background(if (selected) colors.brand else colors.paper)
                                .border(
                                    1.5.dp,
                                    if (selected) colors.brand else colors.rule,
                                    SelloTheme.shapes.control
                                )
                                .selectable(
                                    selected = selected,
                                    enabled = enabled,
                                    role = Role.RadioButton,
                                    onClick = { onPick(choice.id) }
                                )
                                .semantics { contentDescription = choice.label }
                        ) {
                            Icon(
                                painter = choice.icon.painter(),
                                contentDescription = null,
                                tint = if (selected) colors.onBrand else colors.ink,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    // Keeps the cells of a short last row the same width as the others.
                    repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

/** As many columns as fit at the minimum cell size, between one and [maxColumns]. */
fun iconColumns(width: Dp, cell: Dp, maxColumns: Int): Int {
    val fitting = ((width + IconGap) / (cell + IconGap)).toInt()
    return fitting.coerceIn(1, maxColumns)
}

private val IconGap = 6.dp
