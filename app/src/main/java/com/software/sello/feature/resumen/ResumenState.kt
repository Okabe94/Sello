package com.software.sello.feature.resumen

import androidx.compose.runtime.Immutable
import com.software.sello.designsystem.component.ChartPoint
import com.software.sello.designsystem.component.MoneyTextValue
import com.software.sello.domain.model.PastVerdict

sealed interface ResumenState {
    data object Loading : ResumenState

    data object Failed : ResumenState

    @Immutable
    data class Ready(
        val month: String,
        val income: MoneyTextValue,
        val spent: MoneyTextValue,
        val netFlow: MoneyTextValue,
        val allocated: MoneyTextValue,
        val unallocated: MoneyTextValue,
        val actualVerdict: PastVerdict?,
        val forecast: MoneyTextValue?,
        val daily: List<ResumenPoint>,
        val history: List<ResumenPoint>,
        val categories: List<ResumenPoint>,
        val empty: Boolean,
        val insufficientHistory: Boolean,
        val stale: Boolean,
        val asOf: String,
        val selectedDaily: String? = null,
        val selectedHistory: String? = null
    ) : ResumenState
}

@Immutable
data class ResumenPoint(
    val chart: ChartPoint,
    val label: String,
    val amount: MoneyTextValue,
    val detail: MoneyTextValue? = null
)

sealed interface ResumenAction {
    data object Retry : ResumenAction

    data class SelectDaily(val id: String) : ResumenAction

    data class SelectHistory(val id: String) : ResumenAction
}
