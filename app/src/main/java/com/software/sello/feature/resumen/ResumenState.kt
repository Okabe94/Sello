package com.software.sello.feature.resumen

import androidx.compose.runtime.Immutable
import com.software.sello.designsystem.component.ChartPoint
import com.software.sello.designsystem.component.MoneyTextValue

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
        val daily: List<ResumenPoint>,
        val history: List<ResumenPoint>,
        /** The selected month's limit, drawn once as a dashed line over the columns. */
        val monthLimit: ResumenLimit?,
        /** Ranked by what was spent, most first; their amounts add up to [spent]. */
        val categories: List<ResumenCategory>,
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
    val detail: MoneyTextValue? = null,
    /** The month in progress: drawn striped and said to be up to today, never as a whole month. */
    val current: Boolean = false
)

@Immutable
data class ResumenLimit(val value: Long, val amount: MoneyTextValue)

/**
 * A category in "¿Dónde se va?". [share] is how much of the bar to fill: geometry only,
 * never shown as a number or turned back into money.
 */
@Immutable
data class ResumenCategory(
    val id: String,
    val name: String,
    val iconKey: String,
    val amount: MoneyTextValue,
    val share: Float
)

sealed interface ResumenAction {
    data object Retry : ResumenAction

    data class SelectDaily(val id: String) : ResumenAction

    data class SelectHistory(val id: String) : ResumenAction
}
