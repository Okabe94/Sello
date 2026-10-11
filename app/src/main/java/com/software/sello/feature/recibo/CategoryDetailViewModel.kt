package com.software.sello.feature.recibo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.software.sello.domain.model.CategoryId
import com.software.sello.domain.model.Expense
import com.software.sello.domain.model.MonthlySnapshot
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.port.ExpenseCursor
import com.software.sello.domain.port.ExpenseReads
import com.software.sello.domain.port.MonthlySnapshots
import com.software.sello.domain.port.SnapshotState
import com.software.sello.navigation.MonthSession
import com.software.sello.presentation.money.MoneyFormatter
import java.time.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * One category in the selected month. Its figures come from the same shared snapshot
 * as Recibo; its expenses are listed a page at a time, newest first. Every new
 * snapshot lists them again, so a saved expense appears without anything here adding
 * to or subtracting from what is on screen. A page is only ever a list: no figure is
 * worked out from the rows.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CategoryDetailViewModel(
    session: MonthSession,
    private val snapshots: MonthlySnapshots,
    private val expenses: ExpenseReads,
    private val money: MoneyFormatter
) : ViewModel() {
    private val render = ReciboRenderModel(money)
    private val category = MutableStateFlow<String?>(null)
    private val attempt = MutableStateFlow(0)

    /** How many pages are wanted of the list being shown. */
    private val pagesWanted = MutableStateFlow(1)

    private data class Shown(val month: YearMonth, val categoryId: String?)

    /** The list last shown and what it was a list of, kept so a refresh does not blank it. */
    private var kept: Pair<Shown, ExpenseListState>? = null

    val state: StateFlow<CategoryDetailState> =
        combine(session.selectedMonth, category, attempt) { month, id, _ -> Shown(month, id) }
            .flatMapLatest { shown ->
                // Another month or category starts from its first page.
                if (kept?.first != shown) pagesWanted.value = 1
                val id = shown.categoryId?.let { CategoryId.of(it) as? Outcome.Success }?.value
                when {
                    shown.categoryId == null -> flowOf(CategoryDetailState.Closed)
                    id == null -> flowOf(CategoryDetailState.Gone)
                    else -> snapshots.observe(shown.month).flatMapLatest { detail(it, shown, id) }
                }
            }
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(STOP_AFTER_MILLIS),
                CategoryDetailState.Loading
            )

    fun onAction(action: CategoryDetailAction) {
        when (action) {
            is CategoryDetailAction.Show -> category.value = action.categoryId

            // A new attempt starts a new observation of the same month and category.
            CategoryDetailAction.Retry -> attempt.update { it + 1 }

            CategoryDetailAction.ShowMore -> {
                val list = (state.value as? CategoryDetailState.Ready)?.expenses
                // One page per request: a second tap while one is being read asks nothing.
                if (list is ExpenseListState.Rows && list.more && !list.loadingMore) {
                    pagesWanted.update { it + 1 }
                }
            }
        }
    }

    private fun detail(
        state: SnapshotState,
        shown: Shown,
        id: CategoryId
    ): Flow<CategoryDetailState> {
        val (snapshot, stale) = when (state) {
            SnapshotState.Loading -> return flowOf(CategoryDetailState.Loading)
            is SnapshotState.Ready -> state.snapshot to false
            is SnapshotState.Failed -> (state.lastGood ?: return flowOf(Failed)) to true
        }
        val line = render.categories(snapshot).firstOrNull { it.id == shown.categoryId }
            ?: return flowOf(CategoryDetailState.Gone)
        val ready = CategoryDetailState.Ready(
            category = line,
            month = snapshot.month,
            today = snapshot.asOf,
            stale = stale,
            canRecord = !line.archived && snapshot.month == YearMonth.from(snapshot.asOf),
            expenses = kept?.takeIf { it.first == shown }?.second ?: ExpenseListState.Loading
        )
        return flow {
            emit(ready)
            list(id, snapshot).collect { list ->
                // Only a list that was read is worth showing again while refreshing.
                if (list !is ExpenseListState.Failed) kept = shown to list
                emit(ready.copy(expenses = list))
            }
        }
    }

    /** The first pages, then one more each time another is asked for. */
    private fun list(id: CategoryId, snapshot: MonthlySnapshot): Flow<ExpenseListState> = flow {
        val rows = ArrayList<ExpenseRow>()
        var pages = 0
        var next: ExpenseCursor? = null
        pagesWanted.collect { wanted ->
            while (pages < wanted && (pages == 0 || next != null)) {
                if (pages > 0) emit(ExpenseListState.Rows(rows.toList(), true, loadingMore = true))
                when (val page = expenses.inCategory(id, snapshot.month, next, PAGE_SIZE)) {
                    is Outcome.Failure -> {
                        emit(
                            if (pages == 0) {
                                ExpenseListState.Failed
                            } else {
                                ExpenseListState.Rows(rows.toList(), true, moreFailed = true)
                            }
                        )
                        // Asking again asks for this same page, not for one after it.
                        pagesWanted.value = maxOf(pages, 1)
                        return@collect
                    }

                    is Outcome.Success -> {
                        rows += page.value.expenses.map { row(it, snapshot) }
                        next = page.value.next
                        pages++
                        emit(
                            if (rows.isEmpty()) {
                                ExpenseListState.Empty
                            } else {
                                ExpenseListState.Rows(rows.toList(), more = next != null)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun row(expense: Expense, snapshot: MonthlySnapshot) = ExpenseRow(
        id = expense.id.value,
        date = expense.date,
        note = expense.note?.value,
        amount = money.format(expense.amount.money),
        notCountedYet = expense.date > snapshot.asOf
    )

    private companion object {
        val Failed = CategoryDetailState.Failed

        /** Long enough to outlast a rotation without reading again. */
        const val STOP_AFTER_MILLIS = 5_000L
        const val PAGE_SIZE = 20
    }
}
