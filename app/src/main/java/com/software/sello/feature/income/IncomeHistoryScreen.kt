package com.software.sello.feature.income

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.software.sello.R
import com.software.sello.designsystem.component.ErrorSlip
import com.software.sello.designsystem.component.LeaderLine
import com.software.sello.designsystem.component.MoneyText
import com.software.sello.designsystem.component.SelloButton
import com.software.sello.designsystem.component.Slip
import com.software.sello.designsystem.component.SlipSkeleton
import com.software.sello.designsystem.theme.SelloTheme
import com.software.sello.presentation.date.dateLabels
import com.software.sello.presentation.income.incomeSourceLabel
import com.software.sello.presentation.month.monthNames
import java.time.LocalDate

@Composable
fun IncomeHistoryScreen(
    state: IncomeHistoryState,
    today: LocalDate,
    onAction: (IncomeHistoryAction) -> Unit,
    onCreate: () -> Unit,
    onEdit: (String) -> Unit,
    padding: PaddingValues
) {
    val dates = dateLabels()
    LazyColumn(
        contentPadding = padding,
        modifier = Modifier.padding(horizontal = SelloTheme.spacing.lg).testTag("income.history")
    ) {
        item {
            SelloButton(stringResource(R.string.income_entry_title), onCreate)
        }
        when (state) {
            IncomeHistoryState.Loading -> item { SlipSkeleton() }

            IncomeHistoryState.Failed -> item {
                ErrorSlip(stringResource(R.string.income_load_failed), {
                    onAction(IncomeHistoryAction.Retry)
                })
            }

            is IncomeHistoryState.Ready -> {
                item {
                    Slip {
                        Text(stringResource(R.string.income_month_total))
                        MoneyText(state.total)
                        if (state.lines.isEmpty()) {
                            Text(
                                stringResource(
                                    R.string.income_empty,
                                    monthNames().title(state.month)
                                )
                            )
                        }
                    }
                }
                items(state.lines, key = { it.id }) { line ->
                    Slip(pinked = false) {
                        val source = incomeSourceLabel(line.sourceKey, line.sourceName)
                        val date = dates.day(line.date, today)
                        val description =
                            stringResource(
                                R.string.income_edit_description,
                                source,
                                line.amount.spoken,
                                date
                            )
                        LeaderLine(
                            label = source,
                            caption = date,
                            onClick = { onEdit(line.id) },
                            modifier = Modifier.testTag("income.line.${line.id}")
                                .semantics { contentDescription = description }
                        ) { MoneyText(line.amount) }
                        line.note?.let { Text(it) }
                    }
                }
                if (state.pageFailed) {
                    item {
                        ErrorSlip(stringResource(R.string.income_load_failed), {
                            onAction(IncomeHistoryAction.More)
                        })
                    }
                }
                if (state.more) {
                    item {
                        SelloButton(
                            stringResource(R.string.income_more),
                            { onAction(IncomeHistoryAction.More) },
                            loading = state.busy,
                            modifier = Modifier.testTag("income.more")
                        )
                    }
                }
            }
        }
    }
}
