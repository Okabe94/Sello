package com.software.sello.feature.income

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.software.sello.R
import com.software.sello.designsystem.component.ButtonKind
import com.software.sello.designsystem.component.EmptySlip
import com.software.sello.designsystem.component.ErrorSlip
import com.software.sello.designsystem.component.LeaderLine
import com.software.sello.designsystem.component.MoneyStyle
import com.software.sello.designsystem.component.MoneyText
import com.software.sello.designsystem.component.SelloButton
import com.software.sello.designsystem.component.Slip
import com.software.sello.designsystem.component.SlipSkeleton
import com.software.sello.designsystem.component.StampInk
import com.software.sello.designsystem.component.TearLine
import com.software.sello.designsystem.component.TotalLine
import com.software.sello.designsystem.component.v2.AnswerSlip
import com.software.sello.designsystem.component.v2.SplitBar
import com.software.sello.designsystem.component.v2.SplitSegment
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.theme.SelloTheme
import com.software.sello.presentation.date.dateLabels
import com.software.sello.presentation.income.incomeSourceLabel
import com.software.sello.presentation.month.monthNames
import java.time.LocalDate

/**
 * The month's income: what came in and where it went, from the same summary Resumen
 * shows, then each income. Planning from the person's own records, never a balance.
 */
@Composable
fun IncomeHistoryScreen(
    state: IncomeHistoryState,
    split: IncomeSplit?,
    today: LocalDate,
    onAction: (IncomeHistoryAction) -> Unit,
    onCreate: () -> Unit,
    onEdit: (String) -> Unit,
    padding: PaddingValues
) {
    val dates = dateLabels()
    val months = monthNames()
    LazyColumn(
        contentPadding = padding,
        verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.md),
        modifier = Modifier.testTag("income.history")
    ) {
        when (state) {
            IncomeHistoryState.Loading -> item {
                Column(verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.md)) {
                    SlipSkeleton()
                    SlipSkeleton()
                }
            }

            IncomeHistoryState.Failed -> item {
                ErrorSlip(
                    message = stringResource(R.string.recibo_failed),
                    onRetry = { onAction(IncomeHistoryAction.Retry) },
                    title = stringResource(R.string.income_failed_title),
                    icon = SelloIcon.Error
                )
            }

            is IncomeHistoryState.Ready -> if (state.lines.isEmpty() && !state.more) {
                item {
                    EmptySlip(
                        title = stringResource(
                            R.string.income_empty_title,
                            months.inSentence(state.month)
                        ),
                        body = stringResource(R.string.income_empty_body),
                        actionLabel = stringResource(R.string.income_save),
                        onAction = onCreate,
                        icon = SelloIcon.AccountBalance,
                        modifier = Modifier.testTag("income.empty")
                    )
                }
            } else {
                item { CameIn(state, split, months.inSentence(state.month)) }
                item {
                    Slip(Modifier.fillMaxWidth().testTag("income.list"), pinked = false) {
                        Text(
                            stringResource(R.string.income_list_title),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.semantics { heading() }
                        )
                        state.lines.forEach { line ->
                            val source = incomeSourceLabel(line.sourceKey, line.sourceName)
                            val date = dates.day(line.date, today)
                            val description = stringResource(
                                R.string.income_edit_description,
                                source,
                                line.amount.spoken,
                                date
                            )
                            LeaderLine(
                                label = source,
                                caption = listOfNotNull(date, line.note).joinToString(" · "),
                                onClick = { onEdit(line.id) },
                                modifier = Modifier.testTag("income.line.${line.id}")
                                    .semantics { contentDescription = description }
                            ) { MoneyText(line.amount) }
                        }
                        if (state.pageFailed) {
                            ErrorSlip(stringResource(R.string.income_load_failed), {
                                onAction(IncomeHistoryAction.More)
                            })
                        }
                        if (state.more) {
                            SelloButton(
                                stringResource(R.string.income_more),
                                { onAction(IncomeHistoryAction.More) },
                                kind = ButtonKind.Text,
                                loading = state.busy,
                                modifier = Modifier.testTag("income.more")
                            )
                        }
                        TotalLine(stringResource(R.string.income_list_total)) {
                            MoneyText(state.total, style = MoneyStyle.Title)
                        }
                    }
                }
            }
        }
    }
}

/**
 * "Entró en octubre" and the recorded total; under the tear line, the split between
 * category budgets and what is unassigned. A negative unassigned amount is signed and
 * red, with the two facts behind it, and draws no segment of its own. Without the
 * summary, only the total is shown: no figure stands in for one that was not read.
 */
@Composable
private fun CameIn(state: IncomeHistoryState.Ready, split: IncomeSplit?, month: String) {
    AnswerSlip(
        label = stringResource(R.string.income_came_in, month),
        amount = state.total,
        stamp = null,
        ink = StampInk.Neutral,
        modifier = Modifier.testTag("income.hero")
    ) {
        if (split != null) {
            val colors = SelloTheme.colors
            TearLine()
            SplitBar(
                listOf(
                    SplitSegment(
                        split.allocatedShare,
                        stringResource(R.string.income_split_in_budgets, split.allocated.spoken),
                        colors.brand
                    ),
                    SplitSegment(
                        1f - split.allocatedShare,
                        stringResource(R.string.income_split_unassigned, split.unallocated.spoken),
                        colors.brand,
                        striped = true
                    )
                ),
                Modifier.padding(vertical = SelloTheme.spacing.sm).testTag("income.split")
            )
            LeaderLine(stringResource(R.string.income_in_budgets)) { MoneyText(split.allocated) }
            LeaderLine(
                stringResource(R.string.income_unassigned),
                modifier = Modifier.testTag("income.unassigned")
            ) {
                MoneyText(
                    split.unallocated,
                    color = if (split.over) colors.semantic.loss else colors.ink
                )
            }
            if (split.over) {
                Text(
                    stringResource(
                        R.string.income_over_basis,
                        split.allocated.digits,
                        split.income.digits
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.inkSoft,
                    modifier = Modifier.testTag("income.overBasis")
                )
            }
        }
    }
}
