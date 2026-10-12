package com.software.sello.feature.recibo

import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.software.sello.R
import com.software.sello.designsystem.component.ButtonKind
import com.software.sello.designsystem.component.CategoryCell
import com.software.sello.designsystem.component.CategoryGrid
import com.software.sello.designsystem.component.EmptySlip
import com.software.sello.designsystem.component.ErrorSlip
import com.software.sello.designsystem.component.LeaderLine
import com.software.sello.designsystem.component.LineAction
import com.software.sello.designsystem.component.MoneyStyle
import com.software.sello.designsystem.component.MoneyText
import com.software.sello.designsystem.component.SelloButton
import com.software.sello.designsystem.component.Slip
import com.software.sello.designsystem.component.SlipSkeleton
import com.software.sello.designsystem.component.Stamp
import com.software.sello.designsystem.component.StampInk
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.theme.SelloTheme
import com.software.sello.presentation.category.CategoryIcons

/**
 * Recibo: what is left this month and where it is going. [selectedCategoryId] marks
 * the category whose detail is shown beside it, when there is room for both.
 */
@Composable
fun ReciboScreen(
    state: ReciboState,
    onAction: (ReciboAction) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier,
    selectedCategoryId: String? = null
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.md),
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
    ) {
        when (state) {
            ReciboState.Loading -> Column(
                verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.md),
                modifier = Modifier.testTag(RECIBO_LOADING_TAG)
            ) {
                SlipSkeleton()
                SlipSkeleton()
            }

            ReciboState.FirstRun -> EmptySlip(
                title = stringResource(R.string.recibo_first_run_title),
                body = stringResource(R.string.recibo_first_run_body),
                modifier = Modifier.testTag(RECIBO_FIRST_RUN_TAG),
                actionLabel = stringResource(R.string.recibo_create_categories),
                onAction = { onAction(ReciboAction.CreateCategory) }
            )

            is ReciboState.Month -> {
                if (state.stale) {
                    StaleSlip({ onAction(ReciboAction.Retry) }, Modifier.testTag(RECIBO_STALE_TAG))
                }
                SummarySlip(state.summary)
                CategoriesSlip(state.categories, selectedCategoryId, onAction)
            }

            ReciboState.Failed -> ErrorSlip(
                message = stringResource(R.string.recibo_failed),
                onRetry = { onAction(ReciboAction.Retry) },
                modifier = Modifier.testTag(RECIBO_FAILED_TAG)
            )
        }
    }
}

/** Says that what follows is older data, and offers to read again. */
@Composable
internal fun StaleSlip(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Slip(modifier = modifier.fillMaxWidth(), pinked = false) {
        Text(
            text = stringResource(R.string.recibo_stale),
            style = MaterialTheme.typography.bodyLarge,
            color = SelloTheme.colors.semantic.loss,
            modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
        )
        SelloButton(
            text = stringResource(R.string.recibo_stale_retry),
            onClick = onRetry,
            kind = ButtonKind.Text
        )
    }
}

@Composable
private fun SummarySlip(summary: ReciboSummary) {
    Slip(Modifier.testTag(RECIBO_SUMMARY_TAG)) {
        FigureHeader(
            headline = reciboHeadline(summary.figure.headline),
            figure = summary.figure,
            style = MoneyStyle.Hero,
            showStamp = true
        )
        if (summary.days != null && summary.perDay != null) {
            Spacer(Modifier.height(SelloTheme.spacing.xs))
            LeaderLine(
                label = stringResource(R.string.recibo_per_day),
                caption = pluralStringResource(R.plurals.recibo_days, summary.days, summary.days),
                modifier = Modifier.testTag(RECIBO_PER_DAY_TAG)
            ) {
                MoneyText(
                    summary.perDay,
                    color = if (summary.perDayExhausted) {
                        SelloTheme.colors.semantic.loss
                    } else {
                        SelloTheme.colors.ink
                    }
                )
            }
        }
    }
}

/**
 * A sentence, its amount and its verdict, read as one: "Te quedan 937.200 pesos. Al
 * día." The verdict is a word on the screen and in the description, not a colour alone.
 */
@Composable
internal fun FigureHeader(
    headline: String,
    figure: BudgetFigure,
    style: MoneyStyle,
    showStamp: Boolean,
    modifier: Modifier = Modifier
) {
    val stamp = stampText(figure.stamp)
    val spoken =
        stringResource(R.string.recibo_summary_description, headline, figure.amount.spoken, stamp)
    Column(
        modifier = modifier.fillMaxWidth().clearAndSetSemantics {
            contentDescription = spoken
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SelloTheme.spacing.sm)
        ) {
            Text(
                text = headline,
                style = MaterialTheme.typography.bodyLarge,
                color = SelloTheme.colors.inkSoft,
                modifier = Modifier.weight(1f)
            )
            if (showStamp) {
                // Room for the corners of a rotated stamp.
                Stamp(stamp, stampInk(figure.stamp), Modifier.padding(vertical = 6.dp))
            }
        }
        MoneyText(
            figure.amount,
            style = style,
            color = if (figure.headline == ReciboHeadline.Overspent) {
                SelloTheme.colors.semantic.loss
            } else {
                SelloTheme.colors.ink
            }
        )
    }
}

@Composable
private fun CategoriesSlip(
    categories: List<ReciboCategory>,
    selectedCategoryId: String?,
    onAction: (ReciboAction) -> Unit
) {
    Slip(Modifier.testTag(RECIBO_CATEGORIES_TAG), pinked = false) {
        Text(
            text = stringResource(R.string.recibo_categories_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.semantics { heading() }
        )
        Text(
            text = stringResource(R.string.recibo_categories_caption),
            style = MaterialTheme.typography.bodyMedium,
            color = SelloTheme.colors.inkSoft
        )
        Spacer(Modifier.height(SelloTheme.spacing.sm))
        val editLabel = stringResource(R.string.recibo_category_edit)
        CategoryGrid(categories) { index, category ->
            val isSelected = category.id == selectedCategoryId
            CategoryCell(
                name = category.name,
                icon = CategoryIcons.iconFor(category.iconKey),
                amount = category.cellAmount,
                fraction = category.fraction,
                description = categoryDescription(category),
                onClick = { onAction(ReciboAction.OpenCategory(category.id)) },
                actions = listOf(
                    LineAction(editLabel) { onAction(ReciboAction.EditCategory(category.id)) }
                ),
                modifier = Modifier
                    .testTag(reciboCategoryTag(category.id))
                    .fillMaxWidth()
                    .background(
                        if (isSelected) SelloTheme.colors.brandSoft else SelloTheme.colors.paper,
                        MaterialTheme.shapes.small
                    )
                    .semantics { selected = isSelected },
                over = category.over,
                amountPrefix = if (category.limit == null) {
                    stringResource(R.string.recibo_cell_spent_prefix)
                } else {
                    null
                },
                staggerIndex = index
            )
        }
        Spacer(Modifier.height(SelloTheme.spacing.sm))
        SelloButton(
            text = stringResource(R.string.recibo_add_category),
            onClick = { onAction(ReciboAction.CreateCategory) },
            modifier = Modifier.testTag(RECIBO_ADD_CATEGORY_TAG),
            kind = ButtonKind.Text,
            icon = SelloIcon.Add
        )
    }
}

/** The whole cell in words, with both exact amounts and the verdict. */
@Composable
internal fun categoryDescription(category: ReciboCategory): String {
    val headline = reciboHeadline(category.figure.headline)
    val stamp = stampText(category.figure.stamp)
    val amount = category.figure.amount.spoken
    val sentence = if (category.limit != null) {
        stringResource(
            R.string.recibo_cell_limited,
            category.name,
            headline,
            amount,
            category.limit.spoken,
            stamp
        )
    } else {
        stringResource(R.string.recibo_cell_open, category.name, headline, amount, stamp)
    }
    return if (category.archived) {
        stringResource(R.string.recibo_cell_archived, sentence)
    } else {
        sentence
    }
}

@Composable
internal fun reciboHeadline(headline: ReciboHeadline): String = stringResource(
    when (headline) {
        ReciboHeadline.Remaining -> R.string.recibo_headline_remaining
        ReciboHeadline.Overspent -> R.string.recibo_headline_overspent
        ReciboHeadline.LeftOver -> R.string.recibo_headline_left_over
        ReciboHeadline.Spent -> R.string.recibo_headline_spent
    }
)

@Composable
internal fun stampText(stamp: ReciboStamp): String = stringResource(
    when (stamp) {
        ReciboStamp.OnTrack -> R.string.recibo_stamp_on_track
        ReciboStamp.Tight -> R.string.recibo_stamp_tight
        ReciboStamp.Over -> R.string.recibo_stamp_over
        ReciboStamp.WithinLimit -> R.string.recibo_stamp_within_limit
        ReciboStamp.OverLimit -> R.string.recibo_stamp_over_limit
        ReciboStamp.NoLimit -> R.string.recibo_stamp_no_limit
        ReciboStamp.Paused -> R.string.recibo_stamp_paused
        ReciboStamp.NotSet -> R.string.recibo_stamp_not_set
    }
)

internal fun stampInk(stamp: ReciboStamp): StampInk = when (stamp) {
    ReciboStamp.OnTrack, ReciboStamp.WithinLimit -> StampInk.Good

    ReciboStamp.Over, ReciboStamp.OverLimit -> StampInk.Over

    ReciboStamp.Tight, ReciboStamp.NoLimit, ReciboStamp.Paused, ReciboStamp.NotSet ->
        StampInk.Neutral
}

fun reciboCategoryTag(id: String) = "recibo:category:$id"

const val RECIBO_SUMMARY_TAG = "recibo:summary"
const val RECIBO_PER_DAY_TAG = "recibo:per-day"
const val RECIBO_CATEGORIES_TAG = "recibo:categories"
const val RECIBO_ADD_CATEGORY_TAG = "recibo:add-category"
const val RECIBO_LOADING_TAG = "recibo:loading"
const val RECIBO_FIRST_RUN_TAG = "recibo:first-run"
const val RECIBO_STALE_TAG = "recibo:stale"
const val RECIBO_FAILED_TAG = "recibo:failed"
