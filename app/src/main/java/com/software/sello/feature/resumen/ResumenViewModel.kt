package com.software.sello.feature.resumen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.software.sello.domain.port.ResumenSnapshots
import com.software.sello.navigation.MonthSession
import com.software.sello.presentation.money.MoneyFormatter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ResumenViewModel(
    private val saved: SavedStateHandle,
    reads: ResumenSnapshots,
    session: MonthSession,
    money: MoneyFormatter
) : ViewModel() {
    private val attempts = MutableStateFlow(0)
    private val render = ResumenRenderModel(money)
    private var dailyExpiry: Job? = null
    private var historyExpiry: Job? = null
    private val snapshot = combine(session.selectedMonth, attempts) { month, _ -> month }
        .flatMapLatest(reads::observe).map(render::render)

    val state = combine(
        snapshot,
        saved.getStateFlow<String?>(DAILY, null),
        saved.getStateFlow<String?>(HISTORY, null)
    ) { state, daily, history ->
        if (state is ResumenState.Ready) {
            state.copy(
                selectedDaily = selection(daily, state.month, state.daily),
                selectedHistory = selection(history, state.month, state.history)
            )
        } else {
            state
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000, 0), ResumenState.Loading)

    init {
        if (saved.get<String>(DAILY) != null) dailyExpiry = expire(DAILY)
        if (saved.get<String>(HISTORY) != null) historyExpiry = expire(HISTORY)
    }

    fun onAction(action: ResumenAction) {
        when (action) {
            ResumenAction.Retry -> attempts.update { it + 1 }
            is ResumenAction.SelectDaily -> select(DAILY, action.id)
            is ResumenAction.SelectHistory -> select(HISTORY, action.id)
        }
    }

    private fun select(key: String, id: String) {
        val current = state.value as? ResumenState.Ready ?: return
        val points = if (key == DAILY) current.daily else current.history
        if (points.none { it.chart.id == id }) return
        saved[key] = "${current.month}|$id"
        if (key == DAILY) {
            dailyExpiry?.cancel()
            dailyExpiry = expire(key)
        } else {
            historyExpiry?.cancel()
            historyExpiry = expire(key)
        }
    }

    private fun expire(key: String) = viewModelScope.launch {
        delay(3_000)
        saved[key] = null
    }

    private fun selection(value: String?, month: String, points: List<ResumenPoint>): String? =
        value?.takeIf { it.startsWith("$month|") }?.substringAfter('|')
            ?.takeIf { id -> points.any { it.chart.id == id } }

    private companion object {
        const val DAILY = "resumen.daily"
        const val HISTORY = "resumen.history"
    }
}
