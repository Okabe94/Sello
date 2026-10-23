package com.software.sello.feature.category

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.software.sello.R
import com.software.sello.designsystem.component.ButtonKind
import com.software.sello.designsystem.component.ChipFlow
import com.software.sello.designsystem.component.ConfirmSlip
import com.software.sello.designsystem.component.DigitGrouping
import com.software.sello.designsystem.component.ErrorSlip
import com.software.sello.designsystem.component.FieldMessage
import com.software.sello.designsystem.component.FieldMessageText
import com.software.sello.designsystem.component.FormField
import com.software.sello.designsystem.component.IconChoice
import com.software.sello.designsystem.component.IconChoiceGrid
import com.software.sello.designsystem.component.SelloButton
import com.software.sello.designsystem.component.SelloChip
import com.software.sello.designsystem.component.SelloScaffold
import com.software.sello.designsystem.component.Slip
import com.software.sello.designsystem.component.SlipSkeleton
import com.software.sello.designsystem.component.SwitchRow
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.icon.painter
import com.software.sello.designsystem.theme.SelloTheme
import com.software.sello.domain.model.CategoryName
import com.software.sello.presentation.category.CategoryIcons
import com.software.sello.presentation.month.monthNames

/**
 * Owns the view model and what happens when the editor ends: [onFinished] is called
 * once, with the new category's identifier or null when nothing was created.
 */
@Composable
fun CategoryEditorRoot(viewModels: ViewModelProvider.Factory, onFinished: (String?) -> Unit) {
    val viewModel: CategoryEditorViewModel = viewModel(factory = viewModels)
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.result) {
        when (val result = state.result) {
            is EditorResult.Created -> onFinished(result.categoryId)

            EditorResult.Discarded,
            EditorResult.Updated,
            EditorResult.Archived,
            EditorResult.Unarchived -> onFinished(null)

            null -> Unit
        }
    }
    // Back asks before throwing a draft away, and does nothing while saving.
    BackHandler { viewModel.onAction(CategoryEditorAction.Back) }
    CategoryEditorScreen(state, viewModel::onAction)
}

/** The same form on an existing category. It ends when its changes are confirmed or left. */
@Composable
fun CategoryManagementRoot(viewModels: ViewModelProvider.Factory, onFinished: () -> Unit) {
    val viewModel: CategoryManagementViewModel = viewModel(factory = viewModels)
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.result) { if (state.result != null) onFinished() }
    BackHandler { viewModel.onAction(CategoryEditorAction.Back) }
    CategoryEditorScreen(state, viewModel::onAction)
}

@Composable
fun CategoryEditorScreen(
    state: CategoryEditorState,
    onAction: (CategoryEditorAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val scroll = rememberScrollState()
    // The name is at the top and the button at the bottom: an error on the name must
    // not appear where the person cannot see it.
    LaunchedEffect(state.nameError) {
        if (state.nameError != null) scroll.scrollTo(0)
    }
    SelloScaffold(
        title = stringResource(
            if (state.editing) R.string.category_edit_title else R.string.category_editor_title
        ),
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = { onAction(CategoryEditorAction.Back) }) {
                Icon(SelloIcon.ArrowBack.painter(), stringResource(R.string.category_editor_back))
            }
        },
        actions = {
            // Only an existing category that has been read can be archived. It asks first.
            if (state.editing && !state.loading && !state.loadFailed) {
                val description = stringResource(
                    if (state.archived) {
                        R.string.category_edit_unarchive_description
                    } else {
                        R.string.category_edit_archive_description
                    }
                )
                SelloButton(
                    text = stringResource(
                        if (state.archived) {
                            R.string.category_edit_unarchive
                        } else {
                            R.string.category_edit_archive
                        }
                    ),
                    onClick = { onAction(CategoryEditorAction.Archive) },
                    kind = ButtonKind.Text,
                    enabled = state.canArchive,
                    modifier = Modifier
                        .testTag(EDITOR_ARCHIVE_TAG)
                        .semantics { contentDescription = description }
                )
            }
        }
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.md),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(padding)
                .imePadding()
        ) {
            when {
                state.loading -> SlipSkeleton(Modifier.testTag(EDITOR_LOADING_TAG))

                state.loadFailed -> ErrorSlip(
                    message = stringResource(R.string.category_editor_load_failed),
                    onRetry = { onAction(CategoryEditorAction.Retry) },
                    modifier = Modifier.testTag(EDITOR_LOAD_FAILED_TAG)
                )

                else -> Form(state, onAction)
            }
        }
    }
    if (state.confirmDiscard) {
        ConfirmSlip(
            title = stringResource(R.string.category_editor_discard_title),
            message = AnnotatedString(stringResource(R.string.category_editor_discard_message)),
            confirmLabel = stringResource(R.string.category_editor_discard_confirm),
            onConfirm = { onAction(CategoryEditorAction.DiscardConfirmed) },
            onDismiss = { onAction(CategoryEditorAction.KeepEditing) },
            dismissLabel = stringResource(R.string.category_editor_discard_keep)
        )
    }
    state.archiveQuestion?.let { question -> ArchiveConfirmation(state.name, question, onAction) }
}

/**
 * Says what archiving or unarchiving does to new expenses, to the budget and to what
 * is already recorded, before it is done. Nothing is erased either way, so it is not
 * drawn as a destructive choice.
 */
@Composable
private fun ArchiveConfirmation(
    name: String,
    question: ArchiveQuestion,
    onAction: (CategoryEditorAction) -> Unit
) {
    val months = monthNames()
    val thisMonth = months.inSentence(question.currentMonth)
    val sentences = buildList {
        if (question.unarchive) {
            add(
                stringResource(
                    if (question.resumesBudget) {
                        R.string.category_unarchive_resumes
                    } else {
                        R.string.category_unarchive_same
                    },
                    thisMonth
                )
            )
        } else {
            add(
                stringResource(
                    R.string.category_archive_effect,
                    thisMonth.replaceFirstChar { it.titlecase() },
                    months.inSentence(question.currentMonth.plusMonths(1))
                )
            )
            val count = question.expensesThisMonth
            add(
                if (count == null) {
                    stringResource(R.string.category_archive_keeps)
                } else {
                    pluralStringResource(
                        R.plurals.category_archive_keeps_count,
                        count,
                        count,
                        thisMonth
                    )
                }
            )
        }
        if (question.discardsChanges) add(stringResource(R.string.category_archive_discards))
    }
    ConfirmSlip(
        title = stringResource(
            if (question.unarchive) {
                R.string.category_unarchive_title
            } else {
                R.string.category_archive_title
            },
            name
        ),
        message = AnnotatedString(sentences.joinToString(" ")),
        confirmLabel = stringResource(
            if (question.unarchive) {
                R.string.category_edit_unarchive
            } else {
                R.string.category_edit_archive
            }
        ),
        onConfirm = { onAction(CategoryEditorAction.ArchiveConfirmed) },
        onDismiss = { onAction(CategoryEditorAction.ArchiveDismissed) },
        destructive = false
    )
}

@Composable
private fun Form(state: CategoryEditorState, onAction: (CategoryEditorAction) -> Unit) {
    if (state.forEntry) {
        FieldMessageText(
            FieldMessage(
                stringResource(R.string.category_editor_needed_for_entry),
                isError = false
            ),
            Modifier.testTag(EDITOR_PREREQUISITE_TAG)
        )
    }
    if (state.archived) {
        FieldMessageText(
            FieldMessage(stringResource(R.string.category_edit_archived), isError = false),
            Modifier.testTag(EDITOR_ARCHIVED_TAG)
        )
    }
    Slip(modifier = Modifier.fillMaxWidth(), pinked = false) {
        Column(verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.md)) {
            FormField(
                label = stringResource(R.string.category_editor_name),
                value = state.name,
                onValueChange = { onAction(CategoryEditorAction.NameChanged(it)) },
                modifier = Modifier.testTag(EDITOR_NAME_TAG),
                message = state.nameError?.let { FieldMessage(stringResource(it.text())) },
                counter = stringResource(
                    R.string.category_editor_name_counter,
                    state.name.trim().codePointCount(0, state.name.trim().length),
                    CategoryName.MAX_CODE_POINTS
                ),
                enabled = state.editable,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                )
            )
            SectionLabel(stringResource(R.string.category_editor_icon))
            IconChoiceGrid(
                choices = state.iconKeys.map { key ->
                    val option = CategoryIcons.options.first { it.key == key }
                    IconChoice(key, option.icon, stringResource(option.label))
                },
                selectedId = state.iconKey,
                onPick = { onAction(CategoryEditorAction.IconPicked(it)) },
                enabled = state.editable
            )
        }
    }
    Slip(modifier = Modifier.fillMaxWidth(), pinked = false) {
        Column(verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.md)) {
            SectionLabel(stringResource(R.string.category_editor_limit))
            if (state.limitLocked) {
                Text(
                    text = stringResource(R.string.category_edit_limit_locked),
                    style = MaterialTheme.typography.bodyMedium,
                    color = SelloTheme.colors.inkSoft
                )
            } else {
                LimitFields(state, onAction)
                Text(
                    text = limitScope(state),
                    style = MaterialTheme.typography.bodySmall,
                    color = SelloTheme.colors.inkSoft,
                    modifier = Modifier.testTag(EDITOR_SCOPE_TAG)
                )
            }
        }
    }
    if (state.savedParts.isNotEmpty() && state.saveError != null) {
        val saved = state.savedParts.map { stringResource(it.text()) }.joinToString(", ")
        FieldMessageText(
            FieldMessage(stringResource(R.string.category_edit_saved_parts, saved)),
            Modifier.testTag(EDITOR_SAVED_PARTS_TAG)
        )
    } else {
        state.saveError?.let { error ->
            FieldMessageText(
                FieldMessage(stringResource(error.text(state.editing))),
                Modifier.testTag(EDITOR_SAVE_ERROR_TAG)
            )
        }
    }
    if (state.editing && !state.changed && state.saveError == null && !state.busy) {
        FieldMessageText(
            FieldMessage(stringResource(R.string.category_edit_no_changes), isError = false),
            Modifier.testTag(EDITOR_NO_CHANGES_TAG)
        )
    }
    when (state.saveError) {
        SaveError.Unknown -> SelloButton(
            text = stringResource(R.string.category_editor_check_again),
            onClick = { onAction(CategoryEditorAction.Retry) },
            modifier = Modifier.fillMaxWidth().testTag(EDITOR_SUBMIT_TAG),
            loading = state.busy
        )

        // The person decides: what the category is now, or what they typed over it.
        SaveError.Conflict -> {
            SelloButton(
                text = stringResource(R.string.category_edit_conflict_reload),
                onClick = { onAction(CategoryEditorAction.ReloadDiscardingDraft) },
                modifier = Modifier.fillMaxWidth().testTag(EDITOR_CONFLICT_RELOAD_TAG)
            )
            SelloButton(
                text = stringResource(R.string.category_edit_conflict_keep),
                onClick = { onAction(CategoryEditorAction.ReloadKeepingDraft) },
                modifier = Modifier.fillMaxWidth().testTag(EDITOR_CONFLICT_KEEP_TAG),
                kind = ButtonKind.Outlined
            )
        }

        else -> SelloButton(
            text = stringResource(
                if (state.editing) {
                    R.string.category_edit_submit
                } else {
                    R.string.category_editor_create
                }
            ),
            onClick = { onAction(CategoryEditorAction.Submit) },
            modifier = Modifier.fillMaxWidth().testTag(EDITOR_SUBMIT_TAG),
            enabled = state.canSubmit,
            loading = state.busy
        )
    }
}

/** Which months a limit typed here is for. A new category's applies from this month. */
@Composable
private fun limitScope(state: CategoryEditorState): String {
    val month = state.targetMonth ?: return stringResource(R.string.category_editor_limit_scope)
    return stringResource(
        if (state.historical) {
            R.string.category_edit_scope_past
        } else {
            R.string.category_edit_scope_current
        },
        monthNames().title(month)
    )
}

@Composable
private fun LimitFields(state: CategoryEditorState, onAction: (CategoryEditorAction) -> Unit) {
    SwitchRow(
        title = stringResource(R.string.category_editor_unlimited),
        checked = state.unlimited,
        onCheckedChange = { onAction(CategoryEditorAction.UnlimitedChanged(it)) },
        modifier = Modifier.testTag(EDITOR_UNLIMITED_TAG),
        subtitle = stringResource(R.string.category_editor_unlimited_hint),
        enabled = state.editable
    )
    // With no limit chosen there is no amount to type or to pick, so neither is
    // shown. What was typed is kept by the form and comes back with the switch.
    if (!state.unlimited) {
        FormField(
            label = stringResource(R.string.category_editor_limit_amount),
            value = state.limitText,
            onValueChange = { onAction(CategoryEditorAction.LimitChanged(it)) },
            modifier = Modifier.testTag(EDITOR_LIMIT_TAG),
            message = state.limitError?.let { FieldMessage(stringResource(it.text())) },
            enabled = state.editable,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            // Drawn 500.000 so its size can be read; limitText stays as typed.
            visualTransformation = DigitGrouping()
        )
        ChipFlow {
            state.suggestions.forEach { suggestion ->
                SelloChip(
                    label = suggestion.label,
                    selected = state.limitText == suggestion.pesos.toString(),
                    onClick = { onAction(CategoryEditorAction.SuggestionPicked(suggestion.pesos)) },
                    modifier = Modifier.semantics { contentDescription = suggestion.spoken },
                    enabled = state.editable
                )
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = SelloTheme.colors.inkSoft,
        modifier = Modifier.semantics { heading() }
    )
}

private fun NameError.text() = when (this) {
    NameError.Required -> R.string.category_editor_name_required
    NameError.TooLong -> R.string.category_editor_name_too_long
    NameError.NotAllowed -> R.string.category_editor_name_not_allowed
    NameError.Taken -> R.string.category_editor_name_taken
}

private fun LimitError.text() = when (this) {
    LimitError.Required -> R.string.category_editor_limit_required
    LimitError.NotAnAmount -> R.string.category_editor_limit_invalid
}

private fun EditPart.text() = when (this) {
    EditPart.Name -> R.string.category_edit_part_name
    EditPart.Icon -> R.string.category_edit_part_icon
    EditPart.Limit -> R.string.category_edit_part_limit
}

/** A change that was refused left the category as it was; a new one was not created. */
private fun SaveError.text(editing: Boolean) = when (this) {
    SaveError.NotSaved ->
        if (editing) R.string.category_edit_not_saved else R.string.category_editor_not_saved

    SaveError.DataChanged ->
        if (editing) R.string.category_edit_data_changed else R.string.category_editor_data_changed

    SaveError.Unknown -> R.string.category_editor_unknown

    SaveError.Conflict -> R.string.category_edit_conflict

    SaveError.CategoryGone -> R.string.category_edit_gone
}

const val EDITOR_LOADING_TAG = "category-editor:loading"
const val EDITOR_LOAD_FAILED_TAG = "category-editor:load-failed"
const val EDITOR_PREREQUISITE_TAG = "category-editor:prerequisite"
const val EDITOR_NAME_TAG = "category-editor:name"
const val EDITOR_UNLIMITED_TAG = "category-editor:unlimited"
const val EDITOR_LIMIT_TAG = "category-editor:limit"
const val EDITOR_SAVE_ERROR_TAG = "category-editor:save-error"
const val EDITOR_SUBMIT_TAG = "category-editor:submit"
const val EDITOR_ARCHIVE_TAG = "category-editor:archive"
const val EDITOR_ARCHIVED_TAG = "category-editor:archived"
const val EDITOR_SCOPE_TAG = "category-editor:scope"
const val EDITOR_SAVED_PARTS_TAG = "category-editor:saved-parts"
const val EDITOR_NO_CHANGES_TAG = "category-editor:no-changes"
const val EDITOR_CONFLICT_RELOAD_TAG = "category-editor:conflict-reload"
const val EDITOR_CONFLICT_KEEP_TAG = "category-editor:conflict-keep"
