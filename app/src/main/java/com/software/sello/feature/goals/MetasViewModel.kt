package com.software.sello.feature.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.software.sello.domain.port.FinancialClock
import com.software.sello.domain.port.GoalReads
import com.software.sello.presentation.money.MoneyFormatter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * Metas from the goal board as it changes. The band's month is the financial day's,
 * never the month chosen on Recibo: goals are not tied to a month.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MetasViewModel(reads: GoalReads, clock: FinancialClock, money: MoneyFormatter) : ViewModel() {
    private val render = GoalsRenderModel(money)
    private val attempts = MutableStateFlow(0)

    val state: StateFlow<MetasState> = attempts.flatMapLatest { reads.observeBoard() }
        .combine(clock.today) { board, today -> render.metas(board, today.date) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(STOP_AFTER_MILLIS),
            MetasState.Loading
        )

    fun onAction(action: MetasAction) {
        when (action) {
            // A new attempt starts a new observation.
            MetasAction.Retry -> attempts.update { it + 1 }

            // Handled by the root, which owns navigation.
            is MetasAction.Open -> Unit
        }
    }

    private companion object {
        /** Long enough to outlast a rotation without reading again. */
        const val STOP_AFTER_MILLIS = 5_000L
    }
}
