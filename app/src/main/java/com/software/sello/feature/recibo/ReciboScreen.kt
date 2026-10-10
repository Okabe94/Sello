package com.software.sello.feature.recibo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.software.sello.designsystem.component.TotalLine
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.theme.SelloTheme
import com.software.sello.presentation.category.CategoryIcons

/** Owns the view model and its lifecycle; [ReciboScreen] only draws. */
@Composable
fun ReciboRoot(
    viewModels: ViewModelProvider.Factory,
    contentPadding: PaddingValues,
    onCreateCategory: () -> Unit
) {
    val viewModel: ReciboViewModel = viewModel(factory = viewModels)
    val state by viewModel.state.collectAsStateWithLifecycle()
    ReciboScreen(
        state = state,
        onAction = { action ->
            if (action == ReciboAction.CreateCategory) onCreateCategory()
            viewModel.onAction(action)
        },
        contentPadding = contentPadding
    )
}

@Composable
fun ReciboScreen(
    state: ReciboState,
    onAction: (ReciboAction) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.md),
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
    ) {
        when (state) {
            ReciboState.Loading -> SlipSkeleton(Modifier.testTag(RECIBO_LOADING_TAG))

            ReciboState.FirstRun -> EmptySlip(
                title = stringResource(R.string.recibo_first_run_title),
                body = stringResource(R.string.recibo_first_run_body),
                modifier = Modifier.testTag(RECIBO_FIRST_RUN_TAG),
                actionLabel = stringResource(R.string.recibo_create_categories),
                onAction = { onAction(ReciboAction.CreateCategory) }
            )

            is ReciboState.Spending -> {
                Slip(Modifier.testTag(RECIBO_SPENDING_TAG)) {
                    TotalLine(stringResource(R.string.recibo_spent)) {
                        MoneyText(state.spent, style = MoneyStyle.Total)
                    }
                }
                Slip(Modifier.testTag(RECIBO_CATEGORIES_TAG), pinked = false) {
                    state.categories.forEach { category -> CategoryLine(category) }
                    SelloButton(
                        text = stringResource(R.string.recibo_add_category),
                        onClick = { onAction(ReciboAction.CreateCategory) },
                        modifier = Modifier.testTag(RECIBO_ADD_CATEGORY_TAG),
                        kind = ButtonKind.Text,
                        icon = SelloIcon.Add
                    )
                }
            }

            ReciboState.Failed -> ErrorSlip(
                message = stringResource(R.string.recibo_failed),
                onRetry = { onAction(ReciboAction.Retry) },
                modifier = Modifier.testTag(RECIBO_FAILED_TAG)
            )
        }
    }
}

/** A category with its limit that month. The whole line is read as one sentence. */
@Composable
private fun CategoryLine(category: ReciboCategory) {
    val caption = if (category.archived) stringResource(R.string.recibo_category_archived) else null
    val icon = CategoryIcons.iconFor(category.iconKey)
    when (val limit = category.limit) {
        is ReciboLimit.Amount -> LeaderLine(category.name, icon = icon, caption = caption) {
            MoneyText(limit.value)
        }

        ReciboLimit.Unlimited -> LeaderLine(
            category.name,
            stringResource(R.string.recibo_limit_unlimited),
            icon = icon,
            caption = caption
        )

        ReciboLimit.Paused -> LeaderLine(
            category.name,
            stringResource(R.string.recibo_limit_paused),
            icon = icon,
            caption = caption
        )

        ReciboLimit.NotSet -> LeaderLine(
            category.name,
            stringResource(R.string.recibo_limit_not_set),
            icon = icon,
            caption = caption
        )
    }
}

const val RECIBO_CATEGORIES_TAG = "recibo:categories"
const val RECIBO_ADD_CATEGORY_TAG = "recibo:add-category"
const val RECIBO_LOADING_TAG = "recibo:loading"
const val RECIBO_FIRST_RUN_TAG = "recibo:first-run"
const val RECIBO_SPENDING_TAG = "recibo:spending"
const val RECIBO_FAILED_TAG = "recibo:failed"
