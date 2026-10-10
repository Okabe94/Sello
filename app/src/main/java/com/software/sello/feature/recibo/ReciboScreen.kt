package com.software.sello.feature.recibo

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
import com.software.sello.designsystem.component.EmptySlip
import com.software.sello.designsystem.component.ErrorSlip
import com.software.sello.designsystem.component.MoneyStyle
import com.software.sello.designsystem.component.MoneyText
import com.software.sello.designsystem.component.Slip
import com.software.sello.designsystem.component.SlipSkeleton
import com.software.sello.designsystem.component.TotalLine

/** Owns the view model and its lifecycle; [ReciboScreen] only draws. */
@Composable
fun ReciboRoot(viewModels: ViewModelProvider.Factory, contentPadding: PaddingValues) {
    val viewModel: ReciboViewModel = viewModel(factory = viewModels)
    val state by viewModel.state.collectAsStateWithLifecycle()
    ReciboScreen(state, viewModel::onAction, contentPadding)
}

@Composable
fun ReciboScreen(
    state: ReciboState,
    onAction: (ReciboAction) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier = Modifier
) {
    Column(
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
                modifier = Modifier.testTag(RECIBO_FIRST_RUN_TAG)
            )

            is ReciboState.Spending -> Slip(Modifier.testTag(RECIBO_SPENDING_TAG)) {
                TotalLine(stringResource(R.string.recibo_spent)) {
                    MoneyText(state.spent, style = MoneyStyle.Total)
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

const val RECIBO_LOADING_TAG = "recibo:loading"
const val RECIBO_FIRST_RUN_TAG = "recibo:first-run"
const val RECIBO_SPENDING_TAG = "recibo:spending"
const val RECIBO_FAILED_TAG = "recibo:failed"
