package com.software.sello.feature.income

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.software.sello.R
import com.software.sello.designsystem.component.AmountField
import com.software.sello.designsystem.component.AmountKeypad
import com.software.sello.designsystem.component.ButtonKind
import com.software.sello.designsystem.component.ChipFlow
import com.software.sello.designsystem.component.ConfirmSlip
import com.software.sello.designsystem.component.ErrorSlip
import com.software.sello.designsystem.component.FieldMessage
import com.software.sello.designsystem.component.FormField
import com.software.sello.designsystem.component.MoneyStyle
import com.software.sello.designsystem.component.MoneyText
import com.software.sello.designsystem.component.SegmentedSwitch
import com.software.sello.designsystem.component.SelloButton
import com.software.sello.designsystem.component.SelloChip
import com.software.sello.designsystem.component.SelloScaffold
import com.software.sello.designsystem.component.Slip
import com.software.sello.designsystem.component.SlipSkeleton
import com.software.sello.designsystem.component.Stamp
import com.software.sello.designsystem.component.StampInk
import com.software.sello.designsystem.component.StampSize
import com.software.sello.designsystem.component.selloWindowLayout
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.icon.painter
import com.software.sello.designsystem.theme.SelloTheme
import com.software.sello.presentation.date.dateLabels
import com.software.sello.presentation.income.incomeSourceLabel
import com.software.sello.presentation.month.monthNames
import java.time.Instant
import java.time.ZoneOffset

@Composable
fun IncomeFormRoot(
    viewModels: ViewModelProvider.Factory,
    onClosed: () -> Unit,
    onExpense: (() -> Unit)? = null
) {
    val model: IncomeFormViewModel = viewModel(factory = viewModels)
    val state by model.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.closed) { if (state.closed) onClosed() }
    val haptics = LocalHapticFeedback.current
    var acknowledged by rememberSaveable { mutableStateOf<String?>(null) }
    val receipt = state.receipt?.incomeId
    LaunchedEffect(receipt) {
        if (receipt != null && acknowledged != receipt) {
            acknowledged = receipt
            runCatching { haptics.performHapticFeedback(HapticFeedbackType.LongPress) }
        }
    }
    BackHandler { model.onAction(IncomeFormAction.Back) }
    IncomeFormScreen(state, model::onAction, onExpense = onExpense)
}

@Composable
fun IncomeFormScreen(
    state: IncomeFormState,
    onAction: (IncomeFormAction) -> Unit,
    modifier: Modifier = Modifier,
    onExpense: (() -> Unit)? = null
) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val split = state.receipt == null && !state.loading && !state.loadFailed &&
            selloWindowLayout(maxWidth, maxHeight).twoPane
        SelloScaffold(
            title = stringResource(
                if (state.editing) R.string.income_edit_title else R.string.income_entry_title
            ),
            navigationIcon = {
                IconButton(onClick = { onAction(IncomeFormAction.Back) }) {
                    Icon(
                        SelloIcon.ArrowBack.painter(),
                        stringResource(R.string.expense_entry_close)
                    )
                }
            },
            actions = {
                if (state.editing) {
                    SelloButton(
                        stringResource(R.string.expense_edit_delete),
                        { onAction(IncomeFormAction.Delete) },
                        kind = ButtonKind.Text,
                        enabled = state.canDelete,
                        modifier = Modifier.testTag("income.delete")
                    )
                }
            },
            secondaryPane = if (split) {
                { padding -> IncomePane(padding) { IncomeControls(state, onAction) } }
            } else {
                null
            }
        ) { padding ->
            IncomePane(padding) {
                when {
                    state.receipt != null -> {
                        Slip(Modifier.testTag("income.receipt")) {
                            Stamp(
                                stringResource(R.string.income_received),
                                ink = StampInk.Good,
                                size = StampSize.Large,
                                land = true
                            )
                            val details = state.receipt.details
                            if (details == null) {
                                Text(stringResource(R.string.income_receipt_unreadable))
                            } else {
                                MoneyText(details.amount, style = MoneyStyle.Total)
                                Text(incomeSourceLabel(details.sourceKey, details.sourceName))
                                Text(dateLabels().day(details.date, state.today))
                                details.note?.let { Text(it) }
                            }
                        }
                        SelloButton(stringResource(R.string.expense_receipt_done), {
                            onAction(IncomeFormAction.Done)
                        })
                    }

                    state.loading -> SlipSkeleton(Modifier.testTag("income.loading"))

                    state.loadFailed -> ErrorSlip(
                        stringResource(R.string.income_load_failed),
                        { onAction(IncomeFormAction.Retry) },
                        modifier = Modifier.testTag("income.loadFailed")
                    )

                    else -> {
                        if (!state.editing && onExpense != null) {
                            SegmentedSwitch(
                                listOf(
                                    stringResource(R.string.entry_expense),
                                    stringResource(R.string.entry_income)
                                ),
                                1,
                                { if (it == 0 && state.editable) onExpense() },
                                Modifier.testTag("entry.kind")
                            )
                        }
                        IncomeDraft(state, onAction)
                        if (!split) IncomeControls(state, onAction)
                    }
                }
            }
        }
    }
    if (state.confirmDiscard) {
        ConfirmSlip(
            title = stringResource(R.string.income_discard_title),
            message = AnnotatedString(stringResource(R.string.expense_entry_discard_message)),
            confirmLabel = stringResource(R.string.expense_entry_discard_confirm),
            onConfirm = { onAction(IncomeFormAction.DiscardConfirmed) },
            onDismiss = { onAction(IncomeFormAction.KeepEditing) }
        )
    }
    if (state.datePickerOpen) IncomeDatePicker(state, onAction)
}

@Composable
private fun IncomePane(padding: PaddingValues, content: @Composable () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(padding).imePadding()
            .verticalScroll(rememberScrollState()).padding(SelloTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.lg)
    ) { content() }
}

@Composable
private fun IncomeDraft(state: IncomeFormState, onAction: (IncomeFormAction) -> Unit) {
    Slip {
        AmountField(
            amount = state.amount,
            rejectedText = state.amountText,
            message = state.amountProblem?.let {
                FieldMessage(stringResource(R.string.income_amount_invalid))
            },
            limitPulse = state.limitPulse,
            onPaste = if (state.editable) {
                { onAction(IncomeFormAction.Paste(it)) }
            } else {
                null
            },
            modifier = Modifier.testTag("income.amount")
        )
    }
    ChipFlow {
        state.sourceKeys.forEach { key ->
            SelloChip(
                incomeSourceLabel(key),
                state.sourceKey == key,
                { onAction(IncomeFormAction.SourcePicked(key)) },
                enabled = state.editable,
                modifier = Modifier.testTag("income.source.$key")
            )
        }
    }
    if (state.needsOtherName) {
        FormField(
            stringResource(R.string.income_source_name),
            state.otherName,
            { onAction(IncomeFormAction.OtherNameChanged(it)) },
            enabled = state.editable,
            message = state.otherNameProblem?.let {
                FieldMessage(stringResource(R.string.income_source_invalid))
            },
            modifier = Modifier.testTag("income.other")
        )
    }
    SelloButton(
        dateLabels().day(state.date, state.today),
        { onAction(IncomeFormAction.OpenDatePicker) },
        kind = ButtonKind.Text,
        enabled = state.editable,
        modifier = Modifier.testTag("income.date")
    )
    Text(
        stringResource(R.string.income_history_guidance),
        style = MaterialTheme.typography.bodySmall
    )
    if (state.pastMonths.isNotEmpty()) {
        val months = monthNames()
        Text(
            stringResource(
                R.string.income_past_months,
                state.pastMonths.joinToString {
                    months.title(it)
                }
            ),
            modifier = Modifier.testTag("income.pastMonths")
        )
    }
    FormField(
        stringResource(R.string.expense_entry_note),
        state.note,
        { onAction(IncomeFormAction.NoteChanged(it)) },
        enabled = state.editable,
        message = state.noteProblem?.let {
            FieldMessage(stringResource(R.string.income_note_invalid))
        },
        modifier = Modifier.testTag("income.note")
    )
}

@Composable
private fun IncomeControls(state: IncomeFormState, onAction: (IncomeFormAction) -> Unit) {
    state.error?.let { error ->
        Text(
            stringResource(
                when (error) {
                    IncomeFormError.Unknown -> R.string.expense_entry_unknown
                    IncomeFormError.IncomeGone -> R.string.income_gone
                    IncomeFormError.IncomeChanged -> R.string.income_changed
                    IncomeFormError.DataChanged -> R.string.income_data_changed
                    IncomeFormError.FutureDate -> R.string.expense_entry_future_date
                    IncomeFormError.InvalidDate -> R.string.expense_entry_invalid_date
                    IncomeFormError.NotDeleted -> R.string.income_not_deleted
                    else -> R.string.income_not_saved
                }
            ),
            modifier = Modifier.testTag("income.error")
        )
    }
    AmountKeypad(
        { onAction(IncomeFormAction.Digit(it)) },
        { onAction(IncomeFormAction.TripleZero) },
        { onAction(IncomeFormAction.Backspace) },
        { onAction(IncomeFormAction.ClearAmount) },
        enabled = state.editable,
        modifier = Modifier.testTag("income.keypad")
    )
    SelloButton(
        text = stringResource(
            when {
                state.error == IncomeFormError.Unknown -> R.string.expense_entry_check_again
                state.editing -> R.string.expense_edit_submit
                else -> R.string.income_save
            }
        ),
        onClick = {
            onAction(
                if (state.error ==
                    IncomeFormError.Unknown
                ) {
                    IncomeFormAction.Retry
                } else {
                    IncomeFormAction.Submit
                }
            )
        },
        enabled = state.canSubmit || state.error == IncomeFormError.Unknown,
        loading = state.busy,
        modifier = Modifier.fillMaxWidth().testTag("income.submit")
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun IncomeDatePicker(state: IncomeFormState, onAction: (IncomeFormAction) -> Unit) {
    val picker = rememberDatePickerState(
        initialSelectedDateMillis = state.date.atStartOfDay(
            ZoneOffset.UTC
        ).toInstant().toEpochMilli(),
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = !Instant.ofEpochMilli(
                utcTimeMillis
            ).atZone(ZoneOffset.UTC).toLocalDate().isAfter(state.today)

            override fun isSelectableYear(year: Int) = year <= state.today.year
        }
    )
    DatePickerDialog(
        onDismissRequest = { onAction(IncomeFormAction.CloseDatePicker) },
        confirmButton = {
            SelloButton(stringResource(R.string.expense_entry_date_confirm), {
                picker.selectedDateMillis?.let {
                    onAction(
                        IncomeFormAction.DatePicked(
                            Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        )
                    )
                } ?: onAction(IncomeFormAction.CloseDatePicker)
            })
        }
    ) { DatePicker(picker) }
}
