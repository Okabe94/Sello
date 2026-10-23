package com.software.sello.feature.resumen

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.software.sello.R
import com.software.sello.designsystem.component.ButtonKind
import com.software.sello.designsystem.component.ChartReference
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
import com.software.sello.designsystem.component.SpendingRankRow
import com.software.sello.designsystem.component.TotalLine
import com.software.sello.designsystem.component.selloWindowLayout
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.icon.painter
import com.software.sello.designsystem.theme.SelloTheme
import com.software.sello.presentation.category.CategoryIcons

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

/**
 * Income first, as receipt lines: what came in, what is left after spending and what no
 * category budget takes, then the way to income history. Nothing is projected.
 */
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
        ResumenHeading(stringResource(R.string.resumen_income_title))
        LeaderLine(
            stringResource(R.string.resumen_income_recorded),
            onClick = onIncome,
            modifier = Modifier.testTag("resumen.income")
        ) { MoneyText(state.income) }
        LeaderLine(stringResource(R.string.resumen_net_flow)) { MoneyText(state.netFlow) }
        LeaderLine(
            stringResource(R.string.resumen_unallocated_line),
            modifier = Modifier.testTag("resumen.unallocated")
        ) { MoneyText(state.unallocated) }
        Text(
            stringResource(R.string.resumen_actual_through, state.asOf),
            style = MaterialTheme.typography.bodySmall,
            color = SelloTheme.colors.inkSoft
        )
        ResumenLink(stringResource(R.string.resumen_open_income_destination), onIncome)
    }
}

/** A brand text link with a chevron, at least 48dp tall. */
@Composable
private fun ResumenLink(label: String, onClick: () -> Unit) {
    val ink = SelloTheme.colors.brand
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .heightIn(min = SelloTheme.spacing.minTouchTarget)
            .clickable(role = Role.Button, onClick = onClick)
            .testTag("resumen.openIncome")
    ) {
        Text(label, fontWeight = FontWeight.W800, color = ink)
        Icon(SelloIcon.ChevronRight.painter(), null, Modifier.size(20.dp), tint = ink)
    }
}

/**
 * Month by month, where the money goes, and how this month built up. The month in
 * progress is striped, marked and said to be up to today, never drawn as a whole month.
 */
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
    Slip(Modifier.fillMaxWidth().testTag("resumen.history")) {
        ResumenHeading(stringResource(R.string.resumen_month_by_month))
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
        val inProgress = stringResource(R.string.resumen_month_in_progress)
        val highest = state.history.maxOfOrNull { it.chart.value }
        val limitLabel = state.monthLimit?.let {
            stringResource(R.string.resumen_limit_line, it.amount.digits)
        }
        val limitSpoken = state.monthLimit?.let {
            stringResource(R.string.resumen_limit_line, it.amount.spoken)
        }
        ChartWidth(state.history.size, "resumen.history.chart") { modifier ->
            ColumnsChart(
                state.history.map { point ->
                    point.chart.copy(
                        axisLabel = point.chart.axisLabel + if (point.current) "*" else "",
                        spoken = if (point.current) {
                            point.chart.spoken + ", " + inProgress
                        } else {
                            point.chart.spoken
                        },
                        // Values are printed only on the highest and the current columns.
                        valueLabel = point.amount.digits.takeIf {
                            point.current || point.chart.value == highest
                        }
                    )
                },
                stringResource(R.string.resumen_month_by_month),
                modifier,
                state.selectedHistory,
                { onAction(ResumenAction.SelectHistory(it)) },
                reference = state.monthLimit?.let {
                    ChartReference(it.value, limitLabel.orEmpty(), limitSpoken.orEmpty())
                },
                stripedCurrent = true
            )
        }
        if (state.history.any { it.current }) {
            Text(
                stringResource(R.string.resumen_in_progress_note),
                style = MaterialTheme.typography.bodySmall,
                color = SelloTheme.colors.inkSoft
            )
        }
        if (state.insufficientHistory) Text(stringResource(R.string.resumen_insufficient_history))
    }
    if (state.categories.isNotEmpty()) {
        Slip(Modifier.fillMaxWidth().testTag("resumen.categories")) {
            ResumenHeading(stringResource(R.string.resumen_where_it_goes))
            state.categories.forEach { category ->
                SpendingRankRow(
                    name = category.name,
                    icon = CategoryIcons.iconFor(category.iconKey),
                    amount = category.amount,
                    share = category.share,
                    onClick = { onCategory(category.id) },
                    modifier = Modifier.testTag("resumen.category.${category.id}")
                )
            }
            TotalLine(stringResource(R.string.income_list_total)) {
                MoneyText(state.spent, style = MoneyStyle.Title)
            }
        }
    }
    // How this month built up, day by day (kept until the v2 comparison replaces it).
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
