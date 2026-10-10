package com.software.sello.feature.expense

import com.software.sello.designsystem.component.MoneyTextValue
import java.time.LocalDate

/** Why the amount as typed or pasted cannot be used. The text is kept as it was. */
enum class AmountProblem { NotAnAmount, Zero, TooLarge }

enum class NoteProblem { TooLong, NotAllowed }

/** What still has to be filled in before the expense can be recorded, most important first. */
enum class Missing { Amount, Category, Note }

/** Why recording did not finish. None of these means the expense was saved. */
enum class EntryError {
    /** Storage failed and nothing was written. */
    NotSaved,

    /** The chosen category no longer accepts expenses; another must be chosen. */
    CategoryGone,

    /** The date is after the current day. */
    FutureDate,

    /** The data was replaced meanwhile (restore or reset). */
    DataChanged,

    /** It is not known yet whether it was saved; asking again will tell. */
    Unknown
}

data class EntryCategory(val id: String, val name: String, val iconKey: String)

/** One line of the preview. [over] means the figure is how far past the limit it would go. */
data class PreviewLine(val kind: Kind, val amount: MoneyTextValue) {
    enum class Kind { Remaining, Over, Spent }
}

/**
 * What the month and the category would look like after saving. It is an estimate
 * from the draft, shown as such; it is never what was saved.
 */
data class EntryPreview(val month: PreviewLine, val category: PreviewLine?)

/**
 * A saved expense, read back from storage after its receipt confirmed it. [details]
 * is null when the expense is saved but could not be read back; it is still saved.
 */
data class ExpenseReceiptView(val expenseId: String, val details: Details?) {
    data class Details(
        val amount: MoneyTextValue,
        val categoryName: String,
        val iconKey: String,
        val date: LocalDate,
        val note: String?
    )
}

/**
 * The entry form. [amount] is present only for a valid amount; [amountText] is what
 * was pasted when it could not be used. Nothing typed is ever changed into something else.
 */
data class ExpenseEntryState(
    val loading: Boolean = true,
    val loadFailed: Boolean = false,
    val amount: MoneyTextValue? = null,
    val amountText: String? = null,
    val amountProblem: AmountProblem? = null,
    /** Goes up each time a key is refused because twelve digits is the most there can be. */
    val limitPulse: Int = 0,
    val categories: List<EntryCategory> = emptyList(),
    val categoryId: String? = null,
    val date: LocalDate,
    val today: LocalDate,
    val datePickerOpen: Boolean = false,
    val note: String = "",
    val noteProblem: NoteProblem? = null,
    val preview: EntryPreview? = null,
    val missing: Missing? = Missing.Amount,
    val busy: Boolean = false,
    val error: EntryError? = null,
    val confirmDiscard: Boolean = false,
    val receipt: ExpenseReceiptView? = null,
    /** The form is finished and should close. */
    val closed: Boolean = false
) {
    val canSubmit: Boolean
        get() = !loading && !loadFailed && !busy && missing == null && error != EntryError.Unknown

    /** While saving, or while a save's outcome is unknown, the draft must not change. */
    val editable: Boolean get() = !busy && error != EntryError.Unknown && receipt == null

    val category: EntryCategory? get() = categories.firstOrNull { it.id == categoryId }
}

sealed interface ExpenseEntryAction {
    data class Digit(val digit: Int) : ExpenseEntryAction

    data object TripleZero : ExpenseEntryAction

    data object Backspace : ExpenseEntryAction

    data object ClearAmount : ExpenseEntryAction

    /** Text from the clipboard, exactly as it was. */
    data class Paste(val text: String) : ExpenseEntryAction

    data class CategoryPicked(val id: String) : ExpenseEntryAction

    data object OpenDatePicker : ExpenseEntryAction

    data object CloseDatePicker : ExpenseEntryAction

    data class DatePicked(val date: LocalDate) : ExpenseEntryAction

    data class NoteChanged(val text: String) : ExpenseEntryAction

    data object Submit : ExpenseEntryAction

    /** Reload the form, or ask again about a save whose outcome is unknown. */
    data object Retry : ExpenseEntryAction

    data object Back : ExpenseEntryAction

    data object DiscardConfirmed : ExpenseEntryAction

    data object KeepEditing : ExpenseEntryAction

    /** Leave the receipt. */
    data object Done : ExpenseEntryAction
}
