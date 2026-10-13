package com.software.sello.feature.income

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.software.sello.designsystem.component.MoneyTextValue
import com.software.sello.domain.model.IncomeSource
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.port.FinancialClock
import com.software.sello.domain.port.IncomeCursor
import com.software.sello.domain.port.IncomeMonth
import com.software.sello.domain.port.IncomeMonthState
import com.software.sello.domain.port.IncomeReads
import com.software.sello.navigation.MonthSession
import com.software.sello.presentation.money.MoneyFormatter
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class IncomeLine(
    val id: String,
    val amount: MoneyTextValue,
    val sourceKey: String,
    val sourceName: String?,
    val date: LocalDate,
    val note: String?
)

sealed interface IncomeHistoryState {
    data object Loading : IncomeHistoryState

    data object Failed : IncomeHistoryState

    data class Ready(
        val month: YearMonth,
        val total: MoneyTextValue,
        val lines: List<IncomeLine>,
        val more: Boolean,
        val busy: Boolean = false,
        val pageFailed: Boolean = false
    ) : IncomeHistoryState
}

sealed interface IncomeHistoryAction {
    data object Retry : IncomeHistoryAction

    data object More : IncomeHistoryAction
}

@OptIn(ExperimentalCoroutinesApi::class)
class IncomeHistoryViewModel(
    private val reads: IncomeReads,
    private val session: MonthSession,
    private val money: MoneyFormatter,
    clock: FinancialClock
) : ViewModel() {
    val today = clock.today.map {
        it.date
    }.stateIn(viewModelScope, SharingStarted.Eagerly, clock.today.value.date)
    private val shown = MutableStateFlow<IncomeHistoryState>(IncomeHistoryState.Loading)
    val state: StateFlow<IncomeHistoryState> = shown.asStateFlow()
    private val attempts = MutableStateFlow(0)
    private var cursor: IncomeCursor? = null
    private var snapshot: IncomeMonth? = null
    private var epoch = 0

    init {
        viewModelScope.launch {
            combine(session.selectedMonth, attempts) { month, _ -> month }
                .flatMapLatest(reads::observeMonth).collectLatest { state ->
                    val epoch = ++epoch
                    cursor = null
                    snapshot = null
                    shown.value = IncomeHistoryState.Loading
                    when (state) {
                        IncomeMonthState.Loading -> Unit

                        is IncomeMonthState.Failed -> shown.value = IncomeHistoryState.Failed

                        is IncomeMonthState.Ready -> {
                            snapshot = state.month
                            page(state.month, first = true, epoch)
                        }
                    }
                }
        }
    }

    fun onAction(action: IncomeHistoryAction) {
        when (action) {
            IncomeHistoryAction.Retry -> attempts.update { it + 1 }

            IncomeHistoryAction.More -> {
                val current = shown.value as? IncomeHistoryState.Ready ?: return
                val snapshot = snapshot ?: return
                if (current.busy || !current.more) return
                shown.value = current.copy(busy = true, pageFailed = false)
                val epoch = epoch
                viewModelScope.launch { page(snapshot, first = false, epoch) }
            }
        }
    }

    private suspend fun page(expected: IncomeMonth, first: Boolean, epoch: Int) {
        val page = reads.inMonth(expected.month, if (first) null else cursor, PAGE_SIZE)
        val checked = reads.month(expected.month)
        if (this.epoch != epoch || snapshot != expected ||
            session.selectedMonthNow != expected.month
        ) {
            return
        }
        if (checked is Outcome.Failure || page is Outcome.Failure) {
            val previous = shown.value as? IncomeHistoryState.Ready
            shown.value = if (first || previous == null) {
                IncomeHistoryState.Failed
            } else {
                previous.copy(busy = false, pageFailed = true)
            }
            return
        }
        val latest = (checked as Outcome.Success).value
        if (latest.generation != expected.generation || latest.revision != expected.revision) {
            attempts.update { it + 1 }
            return
        }
        val result = (page as Outcome.Success).value
        val previous = if (first) {
            emptyList()
        } else {
            (shown.value as? IncomeHistoryState.Ready)?.lines.orEmpty()
        }
        cursor = result.next
        shown.value = IncomeHistoryState.Ready(
            expected.month,
            money.format(expected.total),
            previous + result.incomes.map {
                IncomeLine(
                    it.id.value,
                    money.format(it.amount.money),
                    it.source.key,
                    (it.source as? IncomeSource.Other)?.name?.value,
                    it.date,
                    it.note?.value
                )
            },
            more = result.next != null
        )
    }

    private companion object {
        const val PAGE_SIZE = 50
    }
}
