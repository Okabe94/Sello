package com.software.sello.feature.resumen

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.software.sello.designsystem.component.ButtonKind
import com.software.sello.designsystem.component.ColumnsChart
import com.software.sello.designsystem.component.EmptySlip
import com.software.sello.designsystem.component.ErrorSlip
import com.software.sello.designsystem.component.LeaderLine
import com.software.sello.designsystem.component.LineChart
import com.software.sello.designsystem.component.MoneyStyle
import com.software.sello.designsystem.component.MoneyText
import com.software.sello.designsystem.component.SelloButton
import com.software.sello.designsystem.component.Slip
import com.software.sello.designsystem.component.SlipSkeleton
import com.software.sello.designsystem.component.Stamp
import com.software.sello.designsystem.component.StampInk
import com.software.sello.designsystem.component.StampSize
import com.software.sello.designsystem.component.selloWindowLayout
import com.software.sello.designsystem.theme.SelloTheme
import com.software.sello.domain.model.PastVerdict

@Composable
fun ResumenScreen(
    state: ResumenState,
    onAction: (ResumenAction) -> Unit,
    onIncome: () -> Unit,
    onCategory: (String) -> Unit,
    onExpense: () -> Unit,
    padding: PaddingValues
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val layout = selloWindowLayout(maxWidth, maxHeight)
        val content = Modifier.fillMaxSize().padding(padding).padding(SelloTheme.spacing.lg)
        when (state) {
            ResumenState.Loading -> ResumenPane(content) {
                SlipSkeleton(Modifier.testTag("resumen.loading"))
            }

            ResumenState.Failed -> ResumenPane(content) {
                ErrorSlip(
                    stringResource(R.string.resumen_load_failed),
                    { onAction(ResumenAction.Retry) },
                    Modifier.testTag("resumen.failed")
                )
            }

            is ResumenState.Ready -> {
                if (layout.twoPane && maxWidth >= ResumenTwoPaneWidth) {
                    Row(
                        content,
                        horizontalArrangement = Arrangement.spacedBy(SelloTheme.spacing.lg)
                    ) {
                        ResumenPane(Modifier.weight(1f)) {
                            SummarySlips(state, onIncome, onAction)
                        }
                        ResumenPane(Modifier.weight(1f)) {
                            ChartSlips(state, onAction, onCategory, onExpense)
                        }
                    }
                } else {
                    ResumenPane(content) {
                        SummarySlips(state, onIncome, onAction)
                        ChartSlips(state, onAction, onCategory, onExpense)
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumenPane(modifier: Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.lg),
        content = content
    )
}

@Composable
private fun SummarySlips(
    state: ResumenState.Ready,
    onIncome: () -> Unit,
    onAction: (ResumenAction) -> Unit
) {
    if (state.stale) {
        ErrorSlip(
            stringResource(R.string.resumen_stale, state.asOf),
            { onAction(ResumenAction.Retry) },
            Modifier.testTag("resumen.stale")
        )
    }
    Slip(Modifier.fillMaxWidth().testTag("resumen.flow")) {
        ResumenHeading(stringResource(R.string.resumen_net_flow))
        MoneyText(state.netFlow, style = MoneyStyle.Hero)
        Text(stringResource(R.string.resumen_flow_explanation))
        LeaderLine(
            stringResource(R.string.resumen_income),
            onClick = onIncome,
            modifier = Modifier.testTag("resumen.income")
        ) { MoneyText(state.income) }
        LeaderLine(stringResource(R.string.resumen_spent)) { MoneyText(state.spent) }
        Text(stringResource(R.string.resumen_actual_through, state.asOf))
        Text(stringResource(R.string.resumen_actual_label))
        Stamp(
            text = stringResource(
                when (state.actualVerdict) {
                    PastVerdict.WithinLimit -> R.string.resumen_actual_within
                    PastVerdict.Over -> R.string.resumen_actual_over
                    null -> R.string.resumen_actual_no_limit
                }
            ),
            ink = when (state.actualVerdict) {
                PastVerdict.WithinLimit -> StampInk.Good
                PastVerdict.Over -> StampInk.Over
                null -> StampInk.Neutral
            },
            size = StampSize.Large
        )
        state.forecast?.let { estimate ->
            LeaderLine(stringResource(R.string.resumen_forecast)) { MoneyText(estimate) }
        }
        SelloButton(stringResource(R.string.resumen_open_income), onIncome, kind = ButtonKind.Text)
    }
    Slip(Modifier.fillMaxWidth().testTag("resumen.allocation")) {
        ResumenHeading(stringResource(R.string.resumen_unallocated))
        MoneyText(state.unallocated, style = MoneyStyle.Title)
        LeaderLine(stringResource(R.string.resumen_allocated)) { MoneyText(state.allocated) }
        Text(stringResource(R.string.resumen_allocation_explanation))
    }
}

@Composable
private fun ChartSlips(
    state: ResumenState.Ready,
    onAction: (ResumenAction) -> Unit,
    onCategory: (String) -> Unit,
    onExpense: () -> Unit
) {
    if (state.empty) {
        EmptySlip(
            stringResource(R.string.resumen_empty),
            stringResource(R.string.resumen_empty_body),
            Modifier.fillMaxWidth().testTag("resumen.empty"),
            stringResource(R.string.resumen_record_expense),
            onExpense
        )
        return
    }
    Slip(Modifier.fillMaxWidth().testTag("resumen.daily")) {
        val selected = state.daily.firstOrNull { it.chart.id == state.selectedDaily }
        ResumenHeading(stringResource(R.string.resumen_cumulative))
        if (selected == null) {
            MoneyText(state.spent, style = MoneyStyle.Title)
        } else {
            Text(selected.label)
            MoneyText(
                selected.amount,
                style = MoneyStyle.Title,
                modifier = Modifier.testTag("resumen.daily.selected")
            )
            selected.detail?.let { amount ->
                LeaderLine(stringResource(R.string.resumen_day_spent)) { MoneyText(amount) }
            }
        }
        ChartWidth(state.daily.size, "resumen.daily.chart") { modifier ->
            LineChart(
                state.daily.map { it.chart },
                stringResource(R.string.resumen_cumulative),
                modifier,
                state.selectedDaily,
                { onAction(ResumenAction.SelectDaily(it)) }
            )
        }
    }
    Slip(Modifier.fillMaxWidth().testTag("resumen.history")) {
        ResumenHeading(stringResource(R.string.resumen_monthly_spending))
        val selected = state.history.firstOrNull { it.chart.id == state.selectedHistory }
        selected?.let { point ->
            Text(point.label)
            MoneyText(
                point.amount,
                style = MoneyStyle.Title,
                modifier = Modifier.testTag("resumen.history.selected")
            )
            point.detail?.let { limit ->
                LeaderLine(stringResource(R.string.resumen_month_limit)) { MoneyText(limit) }
            }
        }
        ChartWidth(state.history.size, "resumen.history.chart") { modifier ->
            ColumnsChart(
                state.history.map { it.chart },
                stringResource(R.string.resumen_monthly_spending),
                modifier,
                state.selectedHistory,
                { onAction(ResumenAction.SelectHistory(it)) }
            )
        }
        if (state.insufficientHistory) Text(stringResource(R.string.resumen_insufficient_history))
    }
    if (state.categories.isNotEmpty()) {
        Slip(Modifier.fillMaxWidth().testTag("resumen.categories")) {
            ResumenHeading(stringResource(R.string.resumen_categories))
            ChartWidth(state.categories.size, "resumen.categories.chart") { modifier ->
                ColumnsChart(
                    state.categories.mapIndexed { index, point ->
                        point.chart.copy(axisLabel = (index + 1).toString())
                    },
                    stringResource(R.string.resumen_categories),
                    modifier,
                    onSelect = onCategory
                )
            }
            state.categories.forEach { category ->
                LeaderLine(
                    category.label,
                    onClick = { onCategory(category.chart.id) },
                    modifier = Modifier.testTag("resumen.category.${category.chart.id}")
                ) {
                    MoneyText(category.amount)
                }
            }
        }
    }
}

@Composable
private fun ResumenHeading(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.semantics {
            heading()
        }
    )
}

@Composable
private fun ChartWidth(count: Int, tag: String, chart: @Composable (Modifier) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth().testTag(tag)) {
        val width = maxOf(maxWidth, (count * 48).dp)
        Box(Modifier.horizontalScroll(rememberScrollState())) {
            chart(Modifier.width(width))
        }
    }
}

private val ResumenTwoPaneWidth = 600.dp
