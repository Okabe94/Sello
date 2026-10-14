package com.software.sello.feature.income

import com.software.sello.designsystem.component.MoneyTextValue
import java.time.LocalDate
import java.time.YearMonth

/** Why the amount as typed or pasted cannot be used. The text is kept as it was. */
enum class IncomeAmountProblem { NotAnAmount, Zero, TooLarge }

enum class IncomeTextProblem { TooLong, NotAllowed }

/**
 * What still has to be filled in before the income can be recorded, most important
 * first. [SourceName] is for "Otro", which needs a name. [Changes] is only for a
 * recorded income being corrected: nothing differs yet from what is stored.
 */
enum class IncomeMissing { Amount, SourceName, Note, Changes }

/** Why recording did not finish. None of these means the income was saved. */
enum class IncomeFormError {
    /** Storage failed and nothing was written. */
    NotSaved,

    /** The date is after the current day. */
    FutureDate,

    /** The date cannot be stored. */
    InvalidDate,

    /** The data was replaced meanwhile (restore or reset). */
    DataChanged,

    /** It is not known yet whether it was saved; asking again will tell. */
    Unknown,

    /** The income being corrected no longer exists. */
    IncomeGone,

    /** The income being corrected was changed since this form read it. */
    IncomeChanged,

    /** Deleting failed and the income is still there. */
    NotDeleted
}

/**
 * A saved income, read back from storage after its receipt confirmed it. [details] is
 * null when the income is saved but could not be read back; it is still saved.
 * [sourceKey] is the stable key; the screen names it.
 */
data class IncomeReceiptView(val incomeId: String, val details: Details?) {
    data class Details(
        val amount: MoneyTextValue,
        val sourceKey: String,
        val sourceName: String?,
        val date: LocalDate,
        val note: String?
    )
}

/**
 * The income form. [amount] is present only for a valid amount; [amountText] is what
 * was pasted when it could not be used. [sourceKey] is one of [sourceKeys]; [otherName]
 * is what was typed for "Otro" and is kept, but not sent, while another source is chosen.
 */
data class IncomeFormState(
    val loading: Boolean = true,
    val loadFailed: Boolean = false,
    val amount: MoneyTextValue? = null,
    val amountText: String? = null,
    val amountProblem: IncomeAmountProblem? = null,
    /** Goes up each time a key is refused because twelve digits is the most there can be. */
    val limitPulse: Int = 0,
    val sourceKeys: List<String> = emptyList(),
    val sourceKey: String = "",
    val otherName: String = "",
    val otherNameProblem: IncomeTextProblem? = null,
    val date: LocalDate,
    val today: LocalDate,
    val datePickerOpen: Boolean = false,
    val note: String = "",
    val noteProblem: IncomeTextProblem? = null,
    val missing: IncomeMissing? = IncomeMissing.Amount,
    val busy: Boolean = false,
    val generationChanged: Boolean = false,
    val error: IncomeFormError? = null,
    val confirmDiscard: Boolean = false,
    val receipt: IncomeReceiptView? = null,
    /** The form is finished and should close. */
    val closed: Boolean = false,
    /** A recorded income is being corrected, not a new one written. */
    val editing: Boolean = false,
    val monthPreview: MoneyTextValue? = null,
    val previewLoading: Boolean = false,
    val previewUnavailable: Boolean = false,
    /** Months other than the current one whose income saving or deleting would change. */
    val pastMonths: List<YearMonth> = emptyList()
) {
    val canSubmit: Boolean
        get() = !loading && !loadFailed && !busy && !generationChanged && missing == null &&
            error !in Blocking

    /** While saving, or once nothing here can be sent as it is, the draft must not change. */
    val editable: Boolean get() = !busy && !generationChanged && error !in Blocking &&
        receipt == null

    val canDelete: Boolean get() = editing && !loading && !loadFailed && editable

    val needsOtherName: Boolean get() = sourceKey == OTHER

    companion object {
        /** The key of the source that carries a name of its own. */
        const val OTHER = "other"

        private val Blocking = setOf(
            IncomeFormError.Unknown,
            IncomeFormError.IncomeGone,
            IncomeFormError.IncomeChanged
        )
    }
}

sealed interface IncomeFormAction {
    data class Digit(val digit: Int) : IncomeFormAction

    data object TripleZero : IncomeFormAction

    data object Backspace : IncomeFormAction

    data object ClearAmount : IncomeFormAction

    /** Text from the clipboard, exactly as it was. */
    data class Paste(val text: String) : IncomeFormAction

    data class SourcePicked(val key: String) : IncomeFormAction

    data class OtherNameChanged(val text: String) : IncomeFormAction

    data object OpenDatePicker : IncomeFormAction

    data object CloseDatePicker : IncomeFormAction

    data class DatePicked(val date: LocalDate) : IncomeFormAction

    data class NoteChanged(val text: String) : IncomeFormAction

    data object Submit : IncomeFormAction

    /** Reload the form, or ask again about a save whose outcome is unknown. */
    data object Retry : IncomeFormAction

    data object Back : IncomeFormAction

    data object DiscardConfirmed : IncomeFormAction

    data object KeepEditing : IncomeFormAction

    /** Leave the receipt. */
    data object Done : IncomeFormAction

    /** Delete the recorded income being corrected. */
    data object Delete : IncomeFormAction
}
