package com.software.sello.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.software.sello.R
import com.software.sello.designsystem.component.MonthCell
import com.software.sello.designsystem.component.MonthGrid
import com.software.sello.designsystem.theme.SelloTheme
import com.software.sello.presentation.month.MonthNames
import java.time.YearMonth

/**
 * The twelve months of [year]. A month after [current] cannot be picked. A cell's
 * identifier is its month as `yyyy-MM`.
 */
fun monthCells(year: Int, current: YearMonth, names: MonthNames): List<MonthCell> =
    (1..12).map { number ->
        val month = YearMonth.of(year, number)
        MonthCell(
            id = month.toString(),
            label = names.short(month),
            spoken = names.title(month),
            enabled = month <= current,
            isCurrent = month == current
        )
    }

/** The picker's content, the same in a bottom sheet and in a side panel. */
@Composable
fun MonthPicker(
    state: ShellState,
    names: MonthNames,
    onAction: (ShellAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val year = state.pickerYear
    Column(modifier = modifier.fillMaxWidth().testTag(MONTH_PICKER_TAG).padding(16.dp)) {
        Text(
            text = stringResource(R.string.month_picker_title),
            style = MaterialTheme.typography.titleMedium,
            color = SelloTheme.colors.ink,
            modifier = Modifier.padding(bottom = 8.dp).semantics { heading() }
        )
        MonthGrid(
            yearLabel = year.toString(),
            months = monthCells(year, state.currentMonth, names),
            selectedId = state.selectedMonth.toString(),
            onPick = { onAction(ShellAction.PickMonth(YearMonth.parse(it))) },
            previousYearLabel = stringResource(R.string.month_picker_previous_year),
            nextYearLabel = stringResource(R.string.month_picker_next_year),
            onPreviousYear = if (year > ShellViewModel.EARLIEST_YEAR) {
                { onAction(ShellAction.ShowPickerYear(year - 1)) }
            } else {
                null
            },
            onNextYear = if (year < state.currentMonth.year) {
                { onAction(ShellAction.ShowPickerYear(year + 1)) }
            } else {
                null
            }
        )
    }
}

const val MONTH_PICKER_TAG = "shell:month-picker"
