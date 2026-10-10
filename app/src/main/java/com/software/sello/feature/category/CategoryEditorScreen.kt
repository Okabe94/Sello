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
import androidx.compose.ui.res.stringResource
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
import com.software.sello.designsystem.component.ChipFlow
import com.software.sello.designsystem.component.ConfirmSlip
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
            EditorResult.Discarded -> onFinished(null)
            null -> Unit
        }
    }
    // Back asks before throwing a draft away, and does nothing while saving.
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
        title = stringResource(R.string.category_editor_title),
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = { onAction(CategoryEditorAction.Back) }) {
                Icon(SelloIcon.ArrowBack.painter(), stringResource(R.string.category_editor_back))
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
            SwitchRow(
                title = stringResource(R.string.category_editor_unlimited),
                checked = state.unlimited,
                onCheckedChange = { onAction(CategoryEditorAction.UnlimitedChanged(it)) },
                modifier = Modifier.testTag(EDITOR_UNLIMITED_TAG),
                subtitle = stringResource(R.string.category_editor_unlimited_hint),
                enabled = state.editable
            )
            FormField(
                label = stringResource(R.string.category_editor_limit_amount),
                value = state.limitText,
                onValueChange = { onAction(CategoryEditorAction.LimitChanged(it)) },
                modifier = Modifier.testTag(EDITOR_LIMIT_TAG),
                message = state.limitError?.let { FieldMessage(stringResource(it.text())) },
                enabled = state.editable && !state.unlimited,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                )
            )
            ChipFlow {
                state.suggestions.forEach { suggestion ->
                    SelloChip(
                        label = suggestion.label,
                        selected = !state.unlimited &&
                            state.limitText == suggestion.pesos.toString(),
                        onClick = {
                            onAction(CategoryEditorAction.SuggestionPicked(suggestion.pesos))
                        },
                        enabled = state.editable
                    )
                }
            }
            Text(
                text = stringResource(R.string.category_editor_limit_scope),
                style = MaterialTheme.typography.bodySmall,
                color = SelloTheme.colors.inkSoft
            )
        }
    }
    state.saveError?.let { error ->
        FieldMessageText(
            FieldMessage(stringResource(error.text())),
            Modifier.testTag(EDITOR_SAVE_ERROR_TAG)
        )
    }
    if (state.saveError == SaveError.Unknown) {
        SelloButton(
            text = stringResource(R.string.category_editor_check_again),
            onClick = { onAction(CategoryEditorAction.Retry) },
            modifier = Modifier.fillMaxWidth().testTag(EDITOR_SUBMIT_TAG),
            loading = state.busy
        )
    } else {
        SelloButton(
            text = stringResource(R.string.category_editor_create),
            onClick = { onAction(CategoryEditorAction.Submit) },
            modifier = Modifier.fillMaxWidth().testTag(EDITOR_SUBMIT_TAG),
            enabled = state.canSubmit,
            loading = state.busy
        )
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

private fun SaveError.text() = when (this) {
    SaveError.NotSaved -> R.string.category_editor_not_saved
    SaveError.DataChanged -> R.string.category_editor_data_changed
    SaveError.Unknown -> R.string.category_editor_unknown
}

const val EDITOR_LOADING_TAG = "category-editor:loading"
const val EDITOR_LOAD_FAILED_TAG = "category-editor:load-failed"
const val EDITOR_PREREQUISITE_TAG = "category-editor:prerequisite"
const val EDITOR_NAME_TAG = "category-editor:name"
const val EDITOR_UNLIMITED_TAG = "category-editor:unlimited"
const val EDITOR_LIMIT_TAG = "category-editor:limit"
const val EDITOR_SAVE_ERROR_TAG = "category-editor:save-error"
const val EDITOR_SUBMIT_TAG = "category-editor:submit"
