package com.software.sello.feature.resumen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.software.sello.designsystem.component.MoneyTextValue
import com.software.sello.domain.port.IncomeMonthState
import com.software.sello.domain.port.IncomeReads
import com.software.sello.navigation.MonthSession
import com.software.sello.presentation.money.MoneyFormatter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

sealed interface ResumenState {
    data object Loading : ResumenState

    data object Failed : ResumenState

    data class Ready(val income: MoneyTextValue) : ResumenState
}

sealed interface ResumenAction {
    data object Retry : ResumenAction
}

@OptIn(ExperimentalCoroutinesApi::class)
class ResumenViewModel(reads: IncomeReads, session: MonthSession, money: MoneyFormatter) :
    ViewModel() {
    private val attempts = MutableStateFlow(0)
    val state = combine(session.selectedMonth, attempts) { month, _ -> month }
        .flatMapLatest(reads::observeMonth)
        .map { state ->
            when (state) {
                IncomeMonthState.Loading -> ResumenState.Loading
                is IncomeMonthState.Failed -> ResumenState.Failed
                is IncomeMonthState.Ready -> ResumenState.Ready(money.format(state.month.total))
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, ResumenState.Loading)

    fun onAction(action: ResumenAction) {
        when (action) {
            ResumenAction.Retry -> attempts.update { it + 1 }
        }
    }
}
