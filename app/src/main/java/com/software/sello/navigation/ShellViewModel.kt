package com.software.sello.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.software.sello.domain.model.CategoryId
import com.software.sello.domain.model.Money
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.TransactionAmount
import java.time.YearMonth
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * What the frame around every screen shows. [pickerYear] is the year the month picker
 * is displaying, which is not necessarily the selected month's. [pendingEntry] is an
 * entry request waiting for the entry form to take it.
 */
data class ShellState(
    val currentMonth: YearMonth,
    val selectedMonth: YearMonth,
    val monthPickerOpen: Boolean,
    val pickerYear: Int,
    val pendingEntry: EntryRequest?
)

sealed interface ShellAction {
    data object OpenMonthPicker : ShellAction

    data object CloseMonthPicker : ShellAction

    data class ShowPickerYear(val year: Int) : ShellAction

    data class PickMonth(val month: YearMonth) : ShellAction

    data object EnteredBackground : ShellAction

    data object ReturnedToForeground : ShellAction

    /** A link that opened the app. Anything that is not an entry link is ignored. */
    data class OpenLink(val link: String?) : ShellAction

    /** The entry form has taken the pending request. */
    data object EntryTaken : ShellAction
}

/**
 * Owns what must survive rotation and the process being killed for the frame: the
 * month selection (through [MonthSession]), the month picker, and a pending entry
 * request. Only identifiers and plain values are saved, never a record. It has no
 * access to any command: nothing here can create money.
 */
class ShellViewModel(private val saved: SavedStateHandle, private val session: MonthSession) :
    ViewModel() {
    init {
        // Present only when this process is continuing an earlier one.
        if (saved.contains(KEY_SESSION)) {
            session.restore(SavedMonthSession(saved[KEY_MONTH], saved[KEY_BACKGROUNDED]))
        }
        saved[KEY_SESSION] = true
    }

    val state: StateFlow<ShellState> = combine(
        session.currentMonth,
        session.selectedMonth,
        saved.getStateFlow(KEY_PICKER, false),
        saved.getStateFlow<Int?>(KEY_PICKER_YEAR, null),
        saved.getStateFlow(KEY_ENTRY, NO_ENTRY)
    ) { current, selected, pickerOpen, pickerYear, _ ->
        ShellState(current, selected, pickerOpen, pickerYear ?: selected.year, pendingEntry())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, now())

    private fun now() = ShellState(
        currentMonth = session.currentMonthNow,
        selectedMonth = session.selectedMonthNow,
        monthPickerOpen = saved[KEY_PICKER] ?: false,
        pickerYear = saved[KEY_PICKER_YEAR] ?: session.selectedMonthNow.year,
        pendingEntry = pendingEntry()
    )

    fun onAction(action: ShellAction) {
        when (action) {
            ShellAction.OpenMonthPicker -> {
                saved[KEY_PICKER_YEAR] = session.selectedMonthNow.year
                saved[KEY_PICKER] = true
            }

            ShellAction.CloseMonthPicker -> saved[KEY_PICKER] = false

            is ShellAction.ShowPickerYear -> {
                val year = action.year.coerceIn(EARLIEST_YEAR, session.currentMonthNow.year)
                saved[KEY_PICKER_YEAR] = year
            }

            is ShellAction.PickMonth -> if (session.select(action.month)) {
                saved[KEY_PICKER] = false
            }

            ShellAction.EnteredBackground -> session.enteredBackground()

            ShellAction.ReturnedToForeground -> session.returnedToForeground()

            is ShellAction.OpenLink -> EntryLinks.parse(action.link)?.let(::hold)

            ShellAction.EntryTaken -> {
                saved[KEY_ENTRY_CATEGORY] = null
                saved[KEY_ENTRY_AMOUNT] = null
                saved[KEY_ENTRY] = NO_ENTRY
            }
        }
        val session = session.save()
        saved[KEY_MONTH] = session.pinnedMonth
        saved[KEY_BACKGROUNDED] = session.backgroundedAtMillis
    }

    private fun hold(request: EntryRequest) {
        saved[KEY_ENTRY_CATEGORY] = request.categoryId?.value
        saved[KEY_ENTRY_AMOUNT] = request.amount?.money?.minorUnits
        // Changed last, and to a new number each time, so observers see every request
        // and always a complete one.
        saved[KEY_ENTRY] = (saved.get<Int>(KEY_ENTRY) ?: NO_ENTRY) + 1
    }

    /** Rebuilt from saved values through the same rules, so a tampered state cannot pass. */
    private fun pendingEntry(): EntryRequest? {
        if ((saved.get<Int>(KEY_ENTRY) ?: NO_ENTRY) == NO_ENTRY) return null
        val category = saved.get<String>(KEY_ENTRY_CATEGORY)?.let(CategoryId::of)
        val amount = saved.get<Long>(KEY_ENTRY_AMOUNT)?.let { TransactionAmount.of(Money.cop(it)) }
        return EntryRequest(
            (category as? Outcome.Success)?.value,
            (amount as? Outcome.Success)?.value
        )
    }

    companion object {
        /** The month picker does not go back further than this. */
        const val EARLIEST_YEAR = 2000

        private const val NO_ENTRY = 0
        private const val KEY_SESSION = "shell.session"
        private const val KEY_MONTH = "shell.month"
        private const val KEY_BACKGROUNDED = "shell.backgroundedAt"
        private const val KEY_PICKER = "shell.picker"
        private const val KEY_PICKER_YEAR = "shell.pickerYear"
        private const val KEY_ENTRY = "shell.entry"
        private const val KEY_ENTRY_CATEGORY = "shell.entry.category"
        private const val KEY_ENTRY_AMOUNT = "shell.entry.amount"
    }
}
