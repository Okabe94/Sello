package com.software.sello.feature.expense

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.software.sello.domain.model.CategoryId
import com.software.sello.domain.model.ExpenseId
import com.software.sello.domain.model.MonthlySnapshot
import com.software.sello.domain.model.Note
import com.software.sello.domain.model.OperationId
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.TextError
import com.software.sello.domain.model.TransactionAmountError
import com.software.sello.domain.policy.AmountDraft
import com.software.sello.domain.policy.AmountDraftError
import com.software.sello.domain.policy.EffectiveDates
import com.software.sello.domain.policy.ExpensePreviewPolicy
import com.software.sello.domain.policy.PreviewFigure
import com.software.sello.domain.port.CreateExpense
import com.software.sello.domain.port.ExpenseCommandOutcome
import com.software.sello.domain.port.ExpenseCommands
import com.software.sello.domain.port.ExpenseReads
import com.software.sello.domain.port.ExpenseRejection
import com.software.sello.domain.port.FinancialClock
import com.software.sello.domain.port.MonthlySnapshots
import com.software.sello.domain.port.RecordIdSource
import com.software.sello.domain.port.SnapshotState
import com.software.sello.presentation.money.MoneyFormatter
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Records one expense through the real command. The draft, the identifier of a save
 * in progress and the saved expense's identifier are kept in [saved], so rotation or
 * the process being killed neither loses the draft nor records the expense twice.
 * The receipt is shown only for an expense a receipt confirmed, read back from storage.
 *
 * The preview comes from the shared snapshot and policy. Nothing is added or
 * compared here.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ExpenseEntryViewModel(
    private val saved: SavedStateHandle,
    private val commands: ExpenseCommands,
    private val reads: ExpenseReads,
    snapshots: MonthlySnapshots,
    private val clock: FinancialClock,
    private val ids: RecordIdSource,
    private val money: MoneyFormatter
) : ViewModel() {
    private data class Draft(
        val amount: AmountDraft,
        val categoryId: String?,
        val date: LocalDate,
        val note: String
    )

    /** Everything that is not the draft itself. */
    private data class Progress(
        val limitPulse: Int = 0,
        val datePickerOpen: Boolean = false,
        val noteProblem: NoteProblem? = null,
        val busy: Boolean = false,
        val error: EntryError? = null,
        val confirmDiscard: Boolean = false,
        val receipt: ExpenseReceiptView? = null,
        val closed: Boolean = false,
        val attempt: Int = 0
    )

    private val draft = MutableStateFlow(
        Draft(
            amount = saved.get<String>(KEY_AMOUNT)?.let(AmountDraft::paste) ?: prefilledAmount(),
            categoryId = saved[KEY_CATEGORY],
            date = savedDate() ?: clock.today.value.date,
            note = saved[KEY_NOTE] ?: ""
        )
    )
    private val progress = MutableStateFlow(Progress())

    init {
        // "Today" is fixed when the form opens, so a draft restored tomorrow is still
        // dated the day it was started unless the person changes it.
        if (saved.get<String>(KEY_DATE) == null) saved[KEY_DATE] = draft.value.date.toString()
    }

    /** The month the expense would be dated in, so the preview is about the right month. */
    private val month: StateFlow<SnapshotState> =
        combine(
            draft.map { YearMonth.from(it.date) }.distinctUntilChanged(),
            progress.map { it.attempt }.distinctUntilChanged()
        ) { month, _ -> month }
            .flatMapLatest(snapshots::observe)
            .stateIn(viewModelScope, SharingStarted.Eagerly, SnapshotState.Loading)

    val state: StateFlow<ExpenseEntryState> =
        combine(draft, progress, month, clock.today) { draft, progress, month, today ->
            render(draft, progress, month, today.date)
        }.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            render(draft.value, progress.value, month.value, clock.today.value.date)
        )

    init {
        viewModelScope.launch {
            month.collect { state ->
                if (state is SnapshotState.Ready) chooseCategoryOnce(state.snapshot)
            }
        }
        // A saved expense or a save in flight from before the process stopped.
        viewModelScope.launch {
            val savedExpense = saved.get<String>(KEY_SAVED_EXPENSE)
            when {
                savedExpense != null -> showReceipt(savedExpense)
                saved.get<String>(KEY_OPERATION) != null -> recover()
            }
        }
    }

    private fun prefilledAmount(): AmountDraft {
        val text = saved.get<String>(KEY_PREFILL_AMOUNT) ?: return AmountDraft.Empty
        // A prefilled amount that is not valid is dropped, not shown as an error the
        // person did not make.
        return AmountDraft.paste(text).takeIf { it is AmountDraft.Digits } ?: AmountDraft.Empty
    }

    private fun savedDate(): LocalDate? =
        (saved.get<String>(KEY_DATE)?.let(EffectiveDates::parseDate) as? Outcome.Success)?.value

    private fun eligible(snapshot: MonthlySnapshot) =
        snapshot.categories.filter { !it.category.archived }.map { it.category }

    /**
     * The first time the categories are known and none is chosen: the one the caller
     * asked for, else the one used last, else the only one there is. One that cannot
     * take an expense is never chosen.
     */
    private suspend fun chooseCategoryOnce(snapshot: MonthlySnapshot) {
        if (saved.get<Boolean>(KEY_CATEGORY_DEFAULTED) == true) return
        saved[KEY_CATEGORY_DEFAULTED] = true
        if (draft.value.categoryId != null) return
        val ids = eligible(snapshot).map { it.id.value }
        val asked = saved.get<String>(KEY_PREFILL_CATEGORY)
        val lastUsed = (reads.lastUsedCategory() as? Outcome.Success)?.value?.value
        val choice = when {
            asked != null -> asked.takeIf { it in ids }
            lastUsed in ids -> lastUsed
            else -> ids.singleOrNull()
        }
        if (choice != null && draft.value.categoryId == null) setCategory(choice)
    }

    private fun setCategory(id: String?) {
        saved[KEY_CATEGORY] = id
        draft.update { it.copy(categoryId = id) }
    }

    private fun setAmount(amount: AmountDraft) {
        saved[KEY_AMOUNT] = when (amount) {
            AmountDraft.Empty -> null
            is AmountDraft.Digits -> amount.digits
            is AmountDraft.Rejected -> amount.original
        }
        draft.update { it.copy(amount = amount) }
    }

    fun onAction(action: ExpenseEntryAction) {
        val now = state.value
        when (action) {
            is ExpenseEntryAction.Digit -> if (now.editable) {
                key(
                    draft.value.amount.digit(action.digit)
                )
            }

            ExpenseEntryAction.TripleZero -> if (now.editable) key(draft.value.amount.tripleZero())

            ExpenseEntryAction.Backspace ->
                if (now.editable) setAmount(draft.value.amount.backspace())

            ExpenseEntryAction.ClearAmount -> if (now.editable) setAmount(AmountDraft.Empty)

            is ExpenseEntryAction.Paste -> if (now.editable) {
                setAmount(
                    AmountDraft.paste(action.text)
                )
            }

            is ExpenseEntryAction.CategoryPicked ->
                if (now.editable && now.categories.any { it.id == action.id }) {
                    setCategory(action.id)
                    progress.update { it.copy(error = null) }
                }

            ExpenseEntryAction.OpenDatePicker ->
                if (now.editable) progress.update { it.copy(datePickerOpen = true) }

            ExpenseEntryAction.CloseDatePicker -> progress.update {
                it.copy(datePickerOpen = false)
            }

            is ExpenseEntryAction.DatePicked -> if (now.editable) {
                // A day after today cannot be picked; the picker closes either way.
                if (!action.date.isAfter(now.today)) {
                    saved[KEY_DATE] = action.date.toString()
                    draft.update { it.copy(date = action.date) }
                }
                progress.update { it.copy(datePickerOpen = false, error = null) }
            }

            is ExpenseEntryAction.NoteChanged -> if (now.editable) {
                saved[KEY_NOTE] = action.text
                draft.update { it.copy(note = action.text) }
            }

            ExpenseEntryAction.Submit -> if (now.canSubmit && now.editable) submit()

            ExpenseEntryAction.Retry -> when {
                now.busy -> Unit
                saved.get<String>(KEY_OPERATION) != null -> viewModelScope.launch { recover() }
                else -> progress.update { it.copy(attempt = it.attempt + 1) }
            }

            ExpenseEntryAction.Back -> when {
                now.busy -> Unit
                now.receipt != null -> progress.update { it.copy(closed = true) }
                isTyped() -> progress.update { it.copy(confirmDiscard = true) }
                else -> progress.update { it.copy(closed = true) }
            }

            ExpenseEntryAction.DiscardConfirmed ->
                progress.update { it.copy(confirmDiscard = false, closed = true) }

            ExpenseEntryAction.KeepEditing -> progress.update { it.copy(confirmDiscard = false) }

            ExpenseEntryAction.Done -> if (now.receipt !=
                null
            ) {
                progress.update { it.copy(closed = true) }
            }
        }
    }

    private fun key(change: AmountDraft.Change) {
        setAmount(change.draft)
        if (change.limitReached) progress.update { it.copy(limitPulse = it.limitPulse + 1) }
    }

    private fun isTyped() = draft.value.amount != AmountDraft.Empty || draft.value.note.isNotBlank()

    private fun submit() {
        val draft = draft.value
        val snapshot = (month.value as? SnapshotState.Ready)?.snapshot ?: return
        val amount = (draft.amount.toTransactionAmount() as? Outcome.Success)?.value ?: return
        // The category as the form shows it chosen: one that is offered, or none.
        val chosen = state.value.categoryId
        val category = (chosen?.let(CategoryId::of) as? Outcome.Success)?.value ?: return
        val note = (Note.of(draft.note) as? Outcome.Success ?: return).value
        // The identifier is saved before the command leaves, so whatever happens next
        // this attempt can be recognised and never recorded twice.
        val operation = (OperationId.of(ids.next()) as Outcome.Success).value
        saved[KEY_OPERATION] = operation.value
        progress.update { it.copy(busy = true, error = null) }
        val command =
            CreateExpense(operation, snapshot.generation, category, amount, draft.date, note)
        viewModelScope.launch { settle(commands.create(command)) }
    }

    private suspend fun settle(outcome: ExpenseCommandOutcome) {
        when (outcome) {
            is ExpenseCommandOutcome.Committed -> saved(outcome.expense.expenseId)

            is ExpenseCommandOutcome.OutcomeUnknown -> recover()

            is ExpenseCommandOutcome.Rejected -> {
                // Certain: nothing was written, so this attempt is over. The draft stays.
                saved[KEY_OPERATION] = null
                val error = when (outcome.reason) {
                    ExpenseRejection.CategoryMissing, ExpenseRejection.CategoryArchived -> {
                        setCategory(null)
                        EntryError.CategoryGone
                    }

                    is ExpenseRejection.FutureDate -> EntryError.FutureDate

                    is ExpenseRejection.StaleGeneration -> EntryError.DataChanged

                    else -> EntryError.NotSaved
                }
                progress.update { it.copy(busy = false, error = error) }
            }
        }
    }

    /** Asks storage how the saved operation ended. Only a receipt counts as saved. */
    private suspend fun recover() {
        val operation = (saved.get<String>(KEY_OPERATION)?.let(OperationId::of) as? Outcome.Success)
            ?.value
        if (operation == null) {
            saved[KEY_OPERATION] = null
            return progress.update { it.copy(busy = false) }
        }
        progress.update { it.copy(busy = true, error = null) }
        when (val found = commands.find(operation)) {
            is Outcome.Failure -> progress.update {
                it.copy(busy = false, error = EntryError.Unknown)
            }

            is Outcome.Success -> {
                val committed = found.value
                if (committed != null) {
                    saved(committed.expenseId)
                } else {
                    saved[KEY_OPERATION] = null
                    progress.update { it.copy(busy = false, error = null) }
                }
            }
        }
    }

    private suspend fun saved(expenseId: ExpenseId) {
        saved[KEY_SAVED_EXPENSE] = expenseId.value
        saved[KEY_OPERATION] = null
        showReceipt(expenseId.value)
    }

    /** Reads the stored expense back; what is shown is what storage holds, not the draft. */
    private suspend fun showReceipt(expenseId: String) {
        val id = (ExpenseId.of(expenseId) as? Outcome.Success)?.value
        val expense = (id?.let { reads.byId(it) } as? Outcome.Success)?.value
        // The category's name comes from the same read the form uses; wait for it once.
        val read = month.first { it !is SnapshotState.Loading }
        val details = expense?.let {
            val category = (read as? SnapshotState.Ready)?.snapshot?.categories
                ?.firstOrNull { line -> line.category.id == it.categoryId }?.category
            ExpenseReceiptView.Details(
                amount = money.format(it.amount.money),
                categoryName = category?.name?.value.orEmpty(),
                iconKey = category?.icon?.value.orEmpty(),
                date = it.date,
                note = it.note?.value
            )
        }
        progress.update {
            it.copy(busy = false, error = null, receipt = ExpenseReceiptView(expenseId, details))
        }
    }

    private fun render(
        draft: Draft,
        progress: Progress,
        month: SnapshotState,
        today: LocalDate
    ): ExpenseEntryState {
        val snapshot = (month as? SnapshotState.Ready)?.snapshot
        val categories = snapshot?.let(::eligible).orEmpty()
            .map { EntryCategory(it.id.value, it.name.value, it.icon.value) }
        val amount = (draft.amount.toTransactionAmount() as? Outcome.Success)?.value
        val noteProblem = when (val note = Note.of(draft.note)) {
            is Outcome.Success -> null

            is Outcome.Failure -> when (note.error) {
                is TextError.TooLong -> NoteProblem.TooLong
                else -> NoteProblem.NotAllowed
            }
        }
        val chosen = draft.categoryId?.takeIf { id -> categories.any { it.id == id } }
        val preview = if (snapshot != null && amount != null) {
            val category = (chosen?.let(CategoryId::of) as? Outcome.Success)?.value
            (ExpensePreviewPolicy.preview(snapshot, amount, category) as? Outcome.Success)?.value
        } else {
            null
        }
        return ExpenseEntryState(
            loading = month is SnapshotState.Loading && progress.receipt == null,
            loadFailed = month is SnapshotState.Failed && progress.receipt == null,
            amount = amount?.let { money.format(it.money) },
            amountText = (draft.amount as? AmountDraft.Rejected)?.original,
            amountProblem = (draft.amount as? AmountDraft.Rejected)?.let { problem(it.error) },
            limitPulse = progress.limitPulse,
            categories = categories,
            categoryId = chosen,
            date = draft.date,
            today = today,
            datePickerOpen = progress.datePickerOpen,
            note = draft.note,
            noteProblem = noteProblem,
            preview = preview?.let { EntryPreview(line(it.month), it.category?.let(::line)) },
            missing = when {
                amount == null -> Missing.Amount
                chosen == null -> Missing.Category
                noteProblem != null -> Missing.Note
                else -> null
            },
            busy = progress.busy,
            error = progress.error,
            confirmDiscard = progress.confirmDiscard,
            receipt = progress.receipt,
            closed = progress.closed
        )
    }

    private fun problem(error: AmountDraftError) = when (error) {
        is AmountDraftError.Range -> when (error.error) {
            TransactionAmountError.NotPositive -> AmountProblem.Zero
            TransactionAmountError.AboveMaximum -> AmountProblem.TooLarge
            is TransactionAmountError.UnsupportedCurrency -> AmountProblem.NotAnAmount
        }

        else -> AmountProblem.NotAnAmount
    }

    private fun line(figure: PreviewFigure) = when (figure) {
        is PreviewFigure.Spent -> PreviewLine(PreviewLine.Kind.Spent, money.format(figure.total))

        is PreviewFigure.Over -> PreviewLine(PreviewLine.Kind.Over, money.format(figure.by))

        is PreviewFigure.Remaining ->
            PreviewLine(PreviewLine.Kind.Remaining, money.format(figure.amount))
    }

    companion object {
        /** Route arguments: untrusted hints for the draft, checked like any other input. */
        const val KEY_PREFILL_CATEGORY = "categoryId"
        const val KEY_PREFILL_AMOUNT = "amount"

        private const val KEY_AMOUNT = "expense.amount"
        private const val KEY_CATEGORY = "expense.category"
        private const val KEY_CATEGORY_DEFAULTED = "expense.categoryDefaulted"
        private const val KEY_DATE = "expense.date"
        private const val KEY_NOTE = "expense.note"
        private const val KEY_OPERATION = "expense.operation"
        private const val KEY_SAVED_EXPENSE = "expense.saved"
    }
}
