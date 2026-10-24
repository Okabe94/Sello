package com.software.sello.feature.resumen

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.software.sello.domain.model.CategoryId
import com.software.sello.domain.model.DismissedSuggestion
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.port.PreferenceRead
import com.software.sello.domain.port.ResumenSnapshots
import com.software.sello.domain.port.SuggestionPreferences
import com.software.sello.navigation.MonthSession
import com.software.sello.presentation.money.MoneyFormatter
import java.time.YearMonth
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
    money: MoneyFormatter,
    private val suggestions: SuggestionPreferences
) : ViewModel() {
    private val attempts = MutableStateFlow(0)
    private val render = ResumenRenderModel(money)
    private var weekdayExpiry: Job? = null
    private var historyExpiry: Job? = null
    private val snapshot = combine(session.selectedMonth, attempts) { month, _ -> month }
        .flatMapLatest(reads::observe).combine(session.currentMonth, render::render)

    /** What was dismissed, as last read. A failed read hides nothing: it is only advice. */
    private val dismissed = suggestions.observeDismissedSuggestion().map { read ->
        when (read) {
            is PreferenceRead.Ready -> read.value
            is PreferenceRead.Failed -> read.lastGood
        }
    }

    val state = combine(
        snapshot,
        saved.getStateFlow<String?>(HISTORY, null),
        saved.getStateFlow<String?>(WEEKDAY, null),
        dismissed
    ) { state, history, weekday, dismissed ->
        if (state is ResumenState.Ready) {
            state.copy(
                selectedHistory = selection(history, state.month, state.history),
                selectedWeekday = selection(
                    weekday,
                    state.month,
                    state.weekdays?.points.orEmpty()
                ),
                suggestion = state.suggestion?.takeUnless {
                    dismissed?.month?.toString() == state.month &&
                        dismissed.categoryId.value == it.categoryId
                }
            )
        } else {
            state
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000, 0), ResumenState.Loading)

    init {
        if (saved.get<String>(HISTORY) != null) historyExpiry = expire(HISTORY)
        if (saved.get<String>(WEEKDAY) != null) weekdayExpiry = expire(WEEKDAY)
    }

    fun onAction(action: ResumenAction) {
        when (action) {
            ResumenAction.Retry -> attempts.update { it + 1 }
            is ResumenAction.SelectHistory -> select(HISTORY, action.id)
            is ResumenAction.SelectWeekday -> select(WEEKDAY, action.id)
            ResumenAction.DismissSuggestion -> dismiss()
        }
    }

    /** Remembers it for this month only: if it still applies next month, it comes back. */
    private fun dismiss() {
        val current = state.value as? ResumenState.Ready ?: return
        val suggestion = current.suggestion ?: return
        val month = runCatching { YearMonth.parse(current.month) }.getOrNull() ?: return
        val id = (CategoryId.of(suggestion.categoryId) as? Outcome.Success)?.value ?: return
        viewModelScope.launch { suggestions.dismissSuggestion(DismissedSuggestion(month, id)) }
    }

    private fun select(key: String, id: String) {
        val current = state.value as? ResumenState.Ready ?: return
        val points = if (key == WEEKDAY) current.weekdays?.points.orEmpty() else current.history
        if (points.none { it.chart.id == id }) return
        saved[key] = "${current.month}|$id"
        if (key == WEEKDAY) {
            weekdayExpiry?.cancel()
            weekdayExpiry = expire(key)
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
        const val HISTORY = "resumen.history"
        const val WEEKDAY = "resumen.weekday"
    }
}
