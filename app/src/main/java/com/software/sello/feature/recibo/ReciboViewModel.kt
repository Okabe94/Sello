package com.software.sello.feature.recibo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.software.sello.domain.port.MonthlySnapshots
import com.software.sello.domain.port.SnapshotState
import com.software.sello.navigation.MonthSession
import com.software.sello.presentation.money.MoneyFormatter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * Shows the selected month from the shared snapshot. It only formats what the
 * snapshot says: no amount is added, compared or defaulted here.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ReciboViewModel(
    session: MonthSession,
    private val snapshots: MonthlySnapshots,
    private val money: MoneyFormatter
) : ViewModel() {
    private val attempt = MutableStateFlow(0)

    val state: StateFlow<ReciboState> =
        combine(session.selectedMonth, attempt) { month, _ -> month }
            .flatMapLatest(snapshots::observe)
            .map(::render)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(STOP_AFTER_MILLIS),
                ReciboState.Loading
            )

    fun onAction(action: ReciboAction) {
        when (action) {
            // A new attempt starts a new observation of the same month.
            ReciboAction.Retry -> attempt.update { it + 1 }
        }
    }

    private fun render(state: SnapshotState): ReciboState = when (state) {
        SnapshotState.Loading -> ReciboState.Loading

        is SnapshotState.Failed -> ReciboState.Failed

        is SnapshotState.Ready -> if (state.snapshot.categories.isEmpty()) {
            ReciboState.FirstRun
        } else {
            ReciboState.Spending(money.format(state.snapshot.spent))
        }
    }

    private companion object {
        /** Long enough to outlast a rotation without reading again. */
        const val STOP_AFTER_MILLIS = 5_000L
    }
}
