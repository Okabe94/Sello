package com.software.sello.feature.recibo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.software.sello.R
import com.software.sello.designsystem.component.ActionLine
import com.software.sello.designsystem.component.ButtonKind
import com.software.sello.designsystem.component.CategoryCircle
import com.software.sello.designsystem.component.EmptySlip
import com.software.sello.designsystem.component.ErrorSlip
import com.software.sello.designsystem.component.LeaderLine
import com.software.sello.designsystem.component.LineAction
import com.software.sello.designsystem.component.MoneyStyle
import com.software.sello.designsystem.component.MoneyText
import com.software.sello.designsystem.component.SelloButton
import com.software.sello.designsystem.component.Slip
import com.software.sello.designsystem.component.SlipSkeleton
import com.software.sello.designsystem.theme.SelloTheme
import com.software.sello.presentation.category.CategoryIcons
import com.software.sello.presentation.date.DateLabels
import com.software.sello.presentation.month.MonthNames

/**
 * One category in the selected month: what is left of its limit, what was spent, and
 * the expenses themselves. [showName] is for when it sits beside Recibo, where the
 * title bar names the month and not the category.
 */
@Composable
fun CategoryDetailScreen(
    state: CategoryDetailState,
    months: MonthNames,
    dates: DateLabels,
    showName: Boolean,
    onAction: (CategoryDetailAction) -> Unit,
    onRecordHere: () -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    onEditExpense: (String) -> Unit = {}
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.md),
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .testTag(CATEGORY_DETAIL_TAG)
    ) {
        when (state) {
            CategoryDetailState.Closed -> Unit

            CategoryDetailState.Loading -> SlipSkeleton(
                Modifier.testTag(CATEGORY_DETAIL_LOADING_TAG)
            )

            CategoryDetailState.Failed -> ErrorSlip(
                message = stringResource(R.string.category_detail_failed),
                onRetry = { onAction(CategoryDetailAction.Retry) },
                modifier = Modifier.testTag(CATEGORY_DETAIL_FAILED_TAG)
            )

            CategoryDetailState.Gone -> EmptySlip(
                title = stringResource(R.string.category_detail_gone_title),
                body = stringResource(R.string.category_detail_gone_body),
                modifier = Modifier.testTag(CATEGORY_DETAIL_GONE_TAG)
            )

            is CategoryDetailState.Ready -> {
                if (state.stale) {
                    StaleSlip(
                        { onAction(CategoryDetailAction.Retry) },
                        Modifier.testTag(CATEGORY_DETAIL_STALE_TAG)
                    )
                }
                HeaderSlip(state.category, showName)
                Expenses(state, months, dates, onAction, onRecordHere, onEditExpense)
            }
        }
    }
}

@Composable
private fun HeaderSlip(category: ReciboCategory, showName: Boolean) {
    Slip(Modifier.testTag(CATEGORY_DETAIL_HEADER_TAG)) {
        if (showName) {
            Text(
                text = category.name,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(Modifier.height(SelloTheme.spacing.xs))
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SelloTheme.spacing.md)
        ) {
            CategoryCircle(
                icon = CategoryIcons.iconFor(category.iconKey),
                fraction = category.fraction,
                size = 56.dp,
                over = category.over
            )
            FigureHeader(
                headline = detailHeadline(category.figure.headline),
                figure = category.figure,
                style = MoneyStyle.Total,
                // Without a limit there is nothing to judge; the line below says why.
                showStamp = category.limit != null,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(SelloTheme.spacing.sm))
        LeaderLine(
            stringResource(R.string.category_detail_spent),
            Modifier.testTag(CATEGORY_DETAIL_SPENT_TAG)
        ) { MoneyText(category.spent) }
        if (category.limit != null) {
            LeaderLine(
                stringResource(R.string.category_detail_limit),
                Modifier.testTag(CATEGORY_DETAIL_LIMIT_TAG)
            ) { MoneyText(category.limit) }
        } else {
            LeaderLine(
                stringResource(R.string.category_detail_limit),
                stampText(category.figure.stamp),
                Modifier.testTag(CATEGORY_DETAIL_LIMIT_TAG)
            )
        }
        if (category.archived) {
            Text(
                text = stringResource(R.string.recibo_category_archived),
                style = MaterialTheme.typography.bodyMedium,
                color = SelloTheme.colors.inkSoft
            )
        }
    }
}

@Composable
private fun Expenses(
    state: CategoryDetailState.Ready,
    months: MonthNames,
    dates: DateLabels,
    onAction: (CategoryDetailAction) -> Unit,
    onRecordHere: () -> Unit,
    onEditExpense: (String) -> Unit
) {
    when (val list = state.expenses) {
        ExpenseListState.Loading -> SlipSkeleton(Modifier.testTag(CATEGORY_DETAIL_LIST_LOADING_TAG))

        ExpenseListState.Failed -> ErrorSlip(
            message = stringResource(R.string.category_detail_list_failed),
            onRetry = { onAction(CategoryDetailAction.Retry) },
            modifier = Modifier.testTag(CATEGORY_DETAIL_LIST_FAILED_TAG)
        )

        ExpenseListState.Empty -> EmptySlip(
            title = stringResource(
                R.string.category_detail_empty_title,
                months.inSentence(state.month)
            ),
            body = stringResource(R.string.category_detail_empty_body),
            modifier = Modifier.testTag(CATEGORY_DETAIL_EMPTY_TAG),
            actionLabel = stringResource(R.string.category_detail_record_here)
                .takeIf { state.canRecord },
            onAction = onRecordHere.takeIf { state.canRecord }
        )

        is ExpenseListState.Rows -> ExpenseList(state, list, dates, onAction, onEditExpense)
    }
}

@Composable
private fun ExpenseList(
    state: CategoryDetailState.Ready,
    list: ExpenseListState.Rows,
    dates: DateLabels,
    onAction: (CategoryDetailAction) -> Unit,
    onEditExpense: (String) -> Unit
) {
    val count = state.category.expenseCount
    val edit = stringResource(R.string.category_detail_expense_edit)
    val delete = stringResource(R.string.category_detail_expense_delete)
    Slip(Modifier.testTag(CATEGORY_DETAIL_LIST_TAG), pinked = false) {
        LeaderLine(
            stringResource(R.string.category_detail_expenses),
            pluralStringResource(R.plurals.category_detail_expense_count, count, count),
            Modifier.semantics { heading() }
        )
        // Always there, so how to correct an expense never has to be remembered.
        Text(
            text = stringResource(R.string.category_detail_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = SelloTheme.colors.inkSoft,
            modifier = Modifier.testTag(CATEGORY_DETAIL_HINT_TAG)
        )
        state.deleteProblem?.let { problem ->
            Text(
                text = stringResource(problem.text()),
                style = MaterialTheme.typography.bodyMedium,
                color = SelloTheme.colors.semantic.loss,
                modifier = Modifier
                    .testTag(CATEGORY_DETAIL_DELETE_PROBLEM_TAG)
                    .semantics { liveRegion = LiveRegionMode.Polite }
            )
        }
        list.rows.forEach { row ->
            // Each row is its expense, not its place in the list: a new expense at the
            // top is one row added, instead of every row below it drawn again.
            key(row.id) {
                val day = dates.day(row.date, state.today)
                val remove = LineAction(delete) { onAction(CategoryDetailAction.Delete(row.id)) }
                ActionLine(
                    label = row.note ?: stringResource(R.string.category_detail_no_note),
                    onClick = { onEditExpense(row.id) },
                    actions = listOf(LineAction(edit) { onEditExpense(row.id) }, remove),
                    modifier = Modifier.testTag(categoryExpenseTag(row.id)),
                    caption = if (row.notCountedYet) {
                        day + ". " + stringResource(R.string.category_detail_not_counted)
                    } else {
                        day
                    },
                    swipeAway = remove
                ) { MoneyText(row.amount) }
            }
        }
        if (list.more) {
            Spacer(Modifier.height(SelloTheme.spacing.sm))
            if (list.rows.size < count) {
                Text(
                    text = stringResource(R.string.category_detail_listed, list.rows.size, count),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SelloTheme.colors.inkSoft
                )
            }
            if (list.moreFailed) {
                Text(
                    text = stringResource(R.string.category_detail_more_failed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SelloTheme.colors.semantic.loss,
                    modifier = Modifier
                        .testTag(CATEGORY_DETAIL_MORE_FAILED_TAG)
                        .semantics { liveRegion = LiveRegionMode.Polite }
                )
            }
            SelloButton(
                text = stringResource(R.string.category_detail_more),
                onClick = { onAction(CategoryDetailAction.ShowMore) },
                modifier = Modifier.testTag(CATEGORY_DETAIL_MORE_TAG),
                kind = ButtonKind.Outlined,
                loading = list.loadingMore
            )
        }
    }
}

private fun DeleteProblem.text() = when (this) {
    DeleteProblem.Gone -> R.string.category_detail_delete_gone
    DeleteProblem.Changed -> R.string.category_detail_delete_changed
    DeleteProblem.DataChanged -> R.string.category_detail_delete_data_changed
    DeleteProblem.Failed -> R.string.category_detail_delete_failed
}

@Composable
private fun detailHeadline(headline: ReciboHeadline): String = stringResource(
    when (headline) {
        ReciboHeadline.Remaining -> R.string.category_detail_headline_remaining
        ReciboHeadline.Overspent -> R.string.category_detail_headline_overspent
        ReciboHeadline.LeftOver -> R.string.category_detail_headline_left_over
        ReciboHeadline.Spent -> R.string.category_detail_headline_spent
    }
)

fun categoryExpenseTag(id: String) = "detail:expense:$id"

const val CATEGORY_DETAIL_TAG = "detail"
const val CATEGORY_DETAIL_LOADING_TAG = "detail:loading"
const val CATEGORY_DETAIL_FAILED_TAG = "detail:failed"
const val CATEGORY_DETAIL_GONE_TAG = "detail:gone"
const val CATEGORY_DETAIL_STALE_TAG = "detail:stale"
const val CATEGORY_DETAIL_HEADER_TAG = "detail:header"
const val CATEGORY_DETAIL_SPENT_TAG = "detail:spent"
const val CATEGORY_DETAIL_LIMIT_TAG = "detail:limit"
const val CATEGORY_DETAIL_LIST_TAG = "detail:list"
const val CATEGORY_DETAIL_LIST_LOADING_TAG = "detail:list-loading"
const val CATEGORY_DETAIL_LIST_FAILED_TAG = "detail:list-failed"
const val CATEGORY_DETAIL_EMPTY_TAG = "detail:empty"
const val CATEGORY_DETAIL_MORE_TAG = "detail:more"
const val CATEGORY_DETAIL_HINT_TAG = "detail:hint"
const val CATEGORY_DETAIL_DELETE_PROBLEM_TAG = "detail:delete-problem"
const val CATEGORY_DETAIL_MORE_FAILED_TAG = "detail:more-failed"
