package com.software.sello.feature.expense

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.software.sello.R
import com.software.sello.designsystem.component.AmountField
import com.software.sello.designsystem.component.AmountKeypad
import com.software.sello.designsystem.component.ChipFlow
import com.software.sello.designsystem.component.ConfirmSlip
import com.software.sello.designsystem.component.ErrorSlip
import com.software.sello.designsystem.component.FieldMessage
import com.software.sello.designsystem.component.FieldMessageText
import com.software.sello.designsystem.component.FieldPill
import com.software.sello.designsystem.component.FormField
import com.software.sello.designsystem.component.LeaderLine
import com.software.sello.designsystem.component.MoneyStyle
import com.software.sello.designsystem.component.MoneyText
import com.software.sello.designsystem.component.SelloButton
import com.software.sello.designsystem.component.SelloChip
import com.software.sello.designsystem.component.SelloScaffold
import com.software.sello.designsystem.component.Slip
import com.software.sello.designsystem.component.SlipSkeleton
import com.software.sello.designsystem.component.Stamp
import com.software.sello.designsystem.component.StampInk
import com.software.sello.designsystem.component.StampSize
import com.software.sello.designsystem.component.TearLine
import com.software.sello.designsystem.component.TotalLine
import com.software.sello.designsystem.component.selloWindowLayout
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.icon.painter
import com.software.sello.designsystem.theme.SelloTheme
import com.software.sello.domain.model.Note
import com.software.sello.presentation.category.CategoryIcons
import com.software.sello.presentation.date.DateLabels
import com.software.sello.presentation.date.dateLabels
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** Owns the view model, Back, and the one-time feedback when a receipt appears. */
@Composable
fun ExpenseEntryRoot(viewModels: ViewModelProvider.Factory, onClosed: () -> Unit) {
    val viewModel: ExpenseEntryViewModel = viewModel(factory = viewModels)
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.closed) { if (state.closed) onClosed() }

    // Feedback for a save that already happened. It runs once per receipt and nothing
    // depends on it: if the device cannot vibrate, the expense is saved all the same.
    val haptics = LocalHapticFeedback.current
    var acknowledged by rememberSaveable { mutableStateOf<String?>(null) }
    val receipt = state.receipt?.expenseId
    LaunchedEffect(receipt) {
        if (receipt != null && acknowledged != receipt) {
            acknowledged = receipt
            runCatching { haptics.performHapticFeedback(HapticFeedbackType.LongPress) }
        }
    }
    BackHandler { viewModel.onAction(ExpenseEntryAction.Back) }
    ExpenseEntryScreen(state, dateLabels(), viewModel::onAction)
}

@Composable
fun ExpenseEntryScreen(
    state: ExpenseEntryState,
    dates: DateLabels,
    onAction: (ExpenseEntryAction) -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        // Sideways or wide: the slip at the left, the keypad always in view at the right.
        val form = state.receipt == null && !state.loading && !state.loadFailed
        val split = form && selloWindowLayout(maxWidth, maxHeight).twoPane
        SelloScaffold(
            title = stringResource(R.string.expense_entry_title),
            navigationIcon = {
                IconButton(onClick = { onAction(ExpenseEntryAction.Back) }) {
                    Icon(
                        SelloIcon.ArrowBack.painter(),
                        stringResource(R.string.expense_entry_close)
                    )
                }
            },
            secondaryPane = if (split) {
                { padding -> Pane(padding) { Controls(state, onAction) } }
            } else {
                null
            }
        ) { padding ->
            when {
                state.receipt != null -> Pane(padding) {
                    Receipt(state.receipt, dates, state.today, onAction)
                }

                state.loading -> Pane(padding) {
                    SlipSkeleton(Modifier.testTag(ENTRY_LOADING_TAG))
                }

                state.loadFailed -> Pane(padding) {
                    ErrorSlip(
                        message = stringResource(R.string.expense_entry_load_failed),
                        onRetry = { onAction(ExpenseEntryAction.Retry) },
                        modifier = Modifier.testTag(ENTRY_LOAD_FAILED_TAG)
                    )
                }

                split -> Pane(padding) { Draft(state, dates, onAction) }

                else -> Stacked(state, dates, onAction, padding, maxHeight * PINNED_SHARE)
            }
        }
    }
    if (state.confirmDiscard) {
        ConfirmSlip(
            title = stringResource(R.string.expense_entry_discard_title),
            message = AnnotatedString(stringResource(R.string.expense_entry_discard_message)),
            confirmLabel = stringResource(R.string.expense_entry_discard_confirm),
            onConfirm = { onAction(ExpenseEntryAction.DiscardConfirmed) },
            onDismiss = { onAction(ExpenseEntryAction.KeepEditing) },
            destructive = false,
            dismissLabel = stringResource(R.string.expense_entry_discard_keep)
        )
    }
    if (state.datePickerOpen) DayPicker(state.date, state.today, onAction)
}

@Composable
private fun Pane(padding: PaddingValues, content: @Composable () -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.md),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding)
            .imePadding()
    ) { content() }
}

/**
 * One column: the slip, category, date and note scroll at the top, and the keypad
 * with the button stay in view at the bottom, so the button is never scrolled away.
 * While the system keyboard is up for the note, the keypad steps aside for it.
 */
@Composable
private fun Stacked(
    state: ExpenseEntryState,
    dates: DateLabels,
    onAction: (ExpenseEntryAction) -> Unit,
    padding: PaddingValues,
    pinnedMax: Dp
) {
    val keyboardOpen = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    Column(modifier = Modifier.fillMaxSize().padding(padding).imePadding()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.md),
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())
        ) { Draft(state, dates, onAction) }
        // Scrolls on its own only when a very large font makes it taller than its share.
        Column(
            verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.sm),
            modifier = Modifier
                .heightIn(max = pinnedMax)
                .verticalScroll(rememberScrollState())
                .padding(top = SelloTheme.spacing.sm)
                .testTag(ENTRY_PINNED_TAG)
        ) { Controls(state, onAction, keypad = !keyboardOpen) }
    }
}

/** The slip being written, the category choice, the date and the note. */
@Composable
private fun Draft(
    state: ExpenseEntryState,
    dates: DateLabels,
    onAction: (ExpenseEntryAction) -> Unit
) {
    val day = dates.day(state.date, state.today)
    Slip(modifier = Modifier.fillMaxWidth().testTag(ENTRY_SLIP_TAG)) {
        Text(
            text = stringResource(R.string.expense_entry_new_line, day),
            style = MaterialTheme.typography.labelMedium,
            color = SelloTheme.colors.inkSoft
        )
        Text(
            text = state.category?.name ?: stringResource(R.string.expense_entry_no_category),
            style = MaterialTheme.typography.titleMedium,
            color = if (state.category == null) SelloTheme.colors.inkSoft else SelloTheme.colors.ink
        )
        AmountField(
            amount = state.amount,
            modifier = Modifier.testTag(ENTRY_AMOUNT_TAG),
            rejectedText = state.amountText,
            message = state.amountProblem?.let { FieldMessage(stringResource(it.text())) },
            limitPulse = state.limitPulse,
            onPaste = if (state.editable) {
                { onAction(ExpenseEntryAction.Paste(it)) }
            } else {
                null
            }
        )
        state.preview?.let { preview ->
            TearLine()
            Text(
                text = stringResource(R.string.expense_entry_preview),
                style = MaterialTheme.typography.labelMedium,
                color = SelloTheme.colors.inkSoft,
                modifier = Modifier.testTag(ENTRY_PREVIEW_TAG)
            )
            PreviewRow(preview.month, monthLabel(preview.month.kind))
            preview.category?.let { line ->
                PreviewRow(line, categoryLabel(line.kind, state.category?.name.orEmpty()))
            }
        }
    }
    Text(
        text = stringResource(R.string.expense_entry_categories),
        style = MaterialTheme.typography.labelLarge,
        color = SelloTheme.colors.inkSoft,
        modifier = Modifier.semantics { heading() }
    )
    ChipFlow(modifier = Modifier.testTag(ENTRY_CATEGORIES_TAG)) {
        state.categories.forEach { category ->
            SelloChip(
                label = category.name,
                selected = category.id == state.categoryId,
                onClick = { onAction(ExpenseEntryAction.CategoryPicked(category.id)) },
                icon = CategoryIcons.iconFor(category.iconKey),
                enabled = state.editable
            )
        }
    }
    val dateDescription = stringResource(R.string.expense_entry_date, day)
    FieldPill(
        text = day,
        icon = SelloIcon.Event,
        onClick = { onAction(ExpenseEntryAction.OpenDatePicker) },
        modifier = Modifier
            .testTag(ENTRY_DATE_TAG)
            .semantics { contentDescription = dateDescription }
    )
    FormField(
        label = stringResource(R.string.expense_entry_note),
        value = state.note,
        onValueChange = { onAction(ExpenseEntryAction.NoteChanged(it)) },
        modifier = Modifier.testTag(ENTRY_NOTE_TAG),
        message = state.noteProblem?.let { FieldMessage(stringResource(it.text())) },
        counter = stringResource(
            R.string.expense_entry_note_counter,
            state.note.trim().codePointCount(0, state.note.trim().length),
            Note.MAX_CODE_POINTS
        ),
        enabled = state.editable,
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done
        )
    )
}

/** The keypad, what is missing or went wrong, and the one button. */
@Composable
private fun Controls(
    state: ExpenseEntryState,
    onAction: (ExpenseEntryAction) -> Unit,
    keypad: Boolean = true
) {
    if (keypad) {
        AmountKeypad(
            onDigit = { onAction(ExpenseEntryAction.Digit(it)) },
            onTripleZero = { onAction(ExpenseEntryAction.TripleZero) },
            onBackspace = { onAction(ExpenseEntryAction.Backspace) },
            onClearAll = { onAction(ExpenseEntryAction.ClearAmount) },
            modifier = Modifier.testTag(ENTRY_KEYPAD_TAG),
            enabled = state.editable
        )
    }
    state.error?.let { error ->
        FieldMessageText(
            FieldMessage(stringResource(error.text())),
            Modifier.testTag(ENTRY_ERROR_TAG)
        )
    }
    if (state.error == EntryError.Unknown) {
        SelloButton(
            text = stringResource(R.string.expense_entry_check_again),
            onClick = { onAction(ExpenseEntryAction.Retry) },
            modifier = Modifier.fillMaxWidth().testTag(ENTRY_SUBMIT_TAG),
            loading = state.busy
        )
        return
    }
    if (state.missing != null && !state.busy) {
        FieldMessageText(
            FieldMessage(stringResource(state.missing.text()), isError = false),
            Modifier.testTag(ENTRY_MISSING_TAG)
        )
    }
    // Read aloud as the whole sentence, so it is clear what will be recorded and where.
    val sentence = if (state.amount != null && state.category != null) {
        stringResource(
            R.string.expense_entry_submit_description,
            state.amount.spoken,
            state.category!!.name
        )
    } else {
        stringResource(R.string.expense_entry_submit)
    }
    SelloButton(
        text = stringResource(R.string.expense_entry_submit),
        onClick = { onAction(ExpenseEntryAction.Submit) },
        modifier = Modifier
            .fillMaxWidth()
            .testTag(ENTRY_SUBMIT_TAG)
            .semantics { contentDescription = sentence },
        enabled = state.canSubmit,
        loading = state.busy
    )
}

@Composable
private fun PreviewRow(line: PreviewLine, label: String) {
    LeaderLine(label) {
        MoneyText(
            line.amount,
            color = if (line.kind == PreviewLine.Kind.Over) {
                SelloTheme.colors.semantic.loss
            } else {
                SelloTheme.colors.ink
            }
        )
    }
}

@Composable
private fun monthLabel(kind: PreviewLine.Kind) = stringResource(
    when (kind) {
        PreviewLine.Kind.Remaining -> R.string.expense_entry_preview_month_remaining
        PreviewLine.Kind.Over -> R.string.expense_entry_preview_month_over
        PreviewLine.Kind.Spent -> R.string.expense_entry_preview_month_spent
    }
)

@Composable
private fun categoryLabel(kind: PreviewLine.Kind, name: String) = stringResource(
    when (kind) {
        PreviewLine.Kind.Remaining -> R.string.expense_entry_preview_category_remaining
        PreviewLine.Kind.Over -> R.string.expense_entry_preview_category_over
        PreviewLine.Kind.Spent -> R.string.expense_entry_preview_category_spent
    },
    name
)

/** Shown only for an expense a receipt confirmed. The stamp decorates; it proves nothing. */
@Composable
private fun Receipt(
    receipt: ExpenseReceiptView,
    dates: DateLabels,
    today: LocalDate,
    onAction: (ExpenseEntryAction) -> Unit
) {
    Slip(modifier = Modifier.fillMaxWidth().testTag(ENTRY_RECEIPT_TAG)) {
        Text(
            text = stringResource(R.string.expense_receipt_title),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() }
        )
        Stamp(
            text = stringResource(R.string.expense_receipt_stamp),
            ink = StampInk.Done,
            modifier = Modifier.padding(vertical = SelloTheme.spacing.md),
            size = StampSize.Large,
            land = true
        )
        val details = receipt.details
        if (details == null) {
            Text(
                text = stringResource(R.string.expense_receipt_unreadable),
                style = MaterialTheme.typography.bodyMedium,
                color = SelloTheme.colors.inkSoft
            )
        } else {
            LeaderLine(
                stringResource(R.string.expense_receipt_category),
                details.categoryName,
                icon = CategoryIcons.iconFor(details.iconKey)
            )
            LeaderLine(
                stringResource(R.string.expense_receipt_date),
                dates.day(details.date, today)
            )
            details.note?.let { LeaderLine(stringResource(R.string.expense_receipt_note), it) }
            TotalLine(stringResource(R.string.expense_receipt_total)) {
                MoneyText(details.amount, style = MoneyStyle.Total)
            }
        }
    }
    SelloButton(
        text = stringResource(R.string.expense_receipt_done),
        onClick = { onAction(ExpenseEntryAction.Done) },
        modifier = Modifier.fillMaxWidth().testTag(ENTRY_DONE_TAG)
    )
}

/** Days after [today] cannot be chosen. The picker works in whole days, with no time or zone. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DayPicker(date: LocalDate, today: LocalDate, onAction: (ExpenseEntryAction) -> Unit) {
    val picker = rememberDatePickerState(
        initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = !day(utcTimeMillis).isAfter(today)

            override fun isSelectableYear(year: Int) = year <= today.year
        }
    )
    DatePickerDialog(
        onDismissRequest = { onAction(ExpenseEntryAction.CloseDatePicker) },
        confirmButton = {
            SelloButton(
                text = stringResource(R.string.expense_entry_date_confirm),
                onClick = {
                    val picked = picker.selectedDateMillis?.let(::day)
                    if (picked == null) {
                        onAction(ExpenseEntryAction.CloseDatePicker)
                    } else {
                        onAction(ExpenseEntryAction.DatePicked(picked))
                    }
                },
                modifier = Modifier.testTag(ENTRY_DATE_CONFIRM_TAG)
            )
        }
    ) {
        DatePicker(state = picker)
    }
}

private fun day(utcMillis: Long): LocalDate =
    Instant.ofEpochMilli(utcMillis).atZone(ZoneOffset.UTC).toLocalDate()

private fun AmountProblem.text() = when (this) {
    AmountProblem.NotAnAmount -> R.string.expense_entry_amount_not_amount
    AmountProblem.Zero -> R.string.expense_entry_amount_zero
    AmountProblem.TooLarge -> R.string.expense_entry_amount_too_large
}

private fun NoteProblem.text() = when (this) {
    NoteProblem.TooLong -> R.string.expense_entry_note_too_long
    NoteProblem.NotAllowed -> R.string.expense_entry_note_not_allowed
}

private fun Missing.text() = when (this) {
    Missing.Amount -> R.string.expense_entry_missing_amount
    Missing.Category -> R.string.expense_entry_missing_category
    Missing.Note -> R.string.expense_entry_missing_note
}

private fun EntryError.text() = when (this) {
    EntryError.NotSaved -> R.string.expense_entry_not_saved
    EntryError.CategoryGone -> R.string.expense_entry_category_gone
    EntryError.FutureDate -> R.string.expense_entry_future_date
    EntryError.DataChanged -> R.string.expense_entry_data_changed
    EntryError.Unknown -> R.string.expense_entry_unknown
}

const val ENTRY_LOADING_TAG = "expense-entry:loading"
const val ENTRY_LOAD_FAILED_TAG = "expense-entry:load-failed"
const val ENTRY_SLIP_TAG = "expense-entry:slip"
const val ENTRY_AMOUNT_TAG = "expense-entry:amount"
const val ENTRY_PREVIEW_TAG = "expense-entry:preview"
const val ENTRY_CATEGORIES_TAG = "expense-entry:categories"
const val ENTRY_DATE_TAG = "expense-entry:date"
const val ENTRY_DATE_CONFIRM_TAG = "expense-entry:date-confirm"
const val ENTRY_NOTE_TAG = "expense-entry:note"
const val ENTRY_KEYPAD_TAG = "expense-entry:keypad"
const val ENTRY_PINNED_TAG = "expense-entry:pinned"

/** The most of the window the keypad and button may take. */
private const val PINNED_SHARE = 0.6f
const val ENTRY_MISSING_TAG = "expense-entry:missing"
const val ENTRY_ERROR_TAG = "expense-entry:error"
const val ENTRY_SUBMIT_TAG = "expense-entry:submit"
const val ENTRY_RECEIPT_TAG = "expense-entry:receipt"
const val ENTRY_DONE_TAG = "expense-entry:done"
