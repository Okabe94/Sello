package com.software.sello.feature.resumen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.software.sello.R
import com.software.sello.designsystem.component.ErrorSlip
import com.software.sello.designsystem.component.MoneyText
import com.software.sello.designsystem.component.SelloButton
import com.software.sello.designsystem.component.Slip
import com.software.sello.designsystem.component.SlipSkeleton
import com.software.sello.designsystem.theme.SelloTheme

@Composable
fun ResumenScreen(
    state: ResumenState,
    onAction: (ResumenAction) -> Unit,
    onIncome: () -> Unit,
    padding: PaddingValues
) {
    Column(
        Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
            .padding(SelloTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.lg)
    ) {
        when (state) {
            ResumenState.Loading -> SlipSkeleton(Modifier.testTag("resumen.loading"))

            ResumenState.Failed -> ErrorSlip(
                stringResource(R.string.income_load_failed),
                { onAction(ResumenAction.Retry) },
                Modifier.testTag("resumen.failed")
            )

            is ResumenState.Ready -> Slip(Modifier.testTag("resumen.income")) {
                Text(stringResource(R.string.resumen_income))
                MoneyText(state.income)
                SelloButton(stringResource(R.string.resumen_open_income), onIncome)
            }
        }
    }
}
