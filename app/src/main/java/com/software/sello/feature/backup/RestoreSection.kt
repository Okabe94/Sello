package com.software.sello.feature.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.software.sello.R
import com.software.sello.designsystem.component.Bullet
import com.software.sello.designsystem.component.BulletList
import com.software.sello.designsystem.component.ButtonKind
import com.software.sello.designsystem.component.ButtonTone
import com.software.sello.designsystem.component.ErrorSlip
import com.software.sello.designsystem.component.SelloButton
import com.software.sello.designsystem.component.Slip
import com.software.sello.designsystem.component.Stamp
import com.software.sello.designsystem.component.StampInk
import com.software.sello.designsystem.theme.SelloTheme
import com.software.sello.domain.model.CategoryOrder
import com.software.sello.domain.model.CategoryOrderDirection
import com.software.sello.domain.model.InkPreference
import com.software.sello.domain.model.ThemePreference
import com.software.sello.domain.port.BackupSummary
import com.software.sello.domain.port.PendingPreferences
import com.software.sello.domain.port.RestoreRejection
import com.software.sello.domain.port.StagedRestore
import com.software.sello.presentation.date.dateLabels

/**
 * Restoring from a file: choose, check, preview, confirm, then the outcome. Nothing is
 * replaced before the preview is confirmed; a failure says the data did not change.
 */
@Composable
fun RestoreSection(state: RestoreState, onAction: (RestoreAction) -> Unit) {
    when (val step = state.step) {
        RestoreStep.Idle, RestoreStep.Checking -> Slip(
            Modifier.fillMaxWidth().testTag(RESTORE_TAG),
            pinked = false
        ) {
            Heading(stringResource(R.string.restore_title))
            Body(stringResource(R.string.restore_body))
            SelloButton(
                text = stringResource(
                    if (step ==
                        RestoreStep.Checking
                    ) {
                        R.string.restore_checking
                    } else {
                        R.string.restore_choose
                    }
                ),
                onClick = { onAction(RestoreAction.Choose) },
                kind = ButtonKind.Outlined,
                loading = step == RestoreStep.Checking,
                enabled = !state.busy,
                modifier = Modifier.fillMaxWidth().padding(top = SelloTheme.spacing.md)
                    .testTag(RESTORE_CHOOSE_TAG)
            )
        }

        is RestoreStep.Rejected -> ErrorSlip(
            title = stringResource(R.string.restore_rejected_title),
            message = stringResource(step.reason.message()),
            retryLabel = stringResource(R.string.restore_choose_other),
            onRetry = { onAction(RestoreAction.Choose) },
            modifier = Modifier.testTag(RESTORE_REJECTED_TAG)
        )

        RestoreStep.Failed -> ErrorSlip(
            title = stringResource(R.string.restore_failed_title),
            message = stringResource(R.string.restore_failed_body),
            retryLabel = stringResource(R.string.restore_failed_action),
            onRetry = { onAction(RestoreAction.Dismiss) },
            modifier = Modifier.testTag(RESTORE_FAILED_TAG)
        )

        is RestoreStep.Previewing -> Preview(state, step.staged, step.refreshed, false, onAction)

        is RestoreStep.Replacing -> Preview(state, step.staged, false, true, onAction)

        is RestoreStep.Unknown -> ErrorSlip(
            title = stringResource(R.string.restore_unknown_title),
            message = stringResource(R.string.restore_unknown_body),
            retryLabel = stringResource(R.string.restore_unknown_action),
            onRetry = { onAction(RestoreAction.Confirm) },
            modifier = Modifier.testTag(RESTORE_UNKNOWN_TAG)
        )

        is RestoreStep.Restored -> if (step.outcome.preferences.done) {
            Restored(step.outcome.summary, onAction)
        } else {
            Pending(step.outcome.preferences, step.applying, onAction)
        }
    }
}

@Composable
private fun Preview(
    state: RestoreState,
    staged: StagedRestore,
    refreshed: Boolean,
    replacing: Boolean,
    onAction: (RestoreAction) -> Unit
) {
    Slip(
        Modifier.fillMaxWidth().testTag(RESTORE_PREVIEW_TAG),
        pinked = false
    ) {
        Heading(stringResource(R.string.restore_preview_title))
        state.zone?.let { zone ->
            Body(
                stringResource(
                    R.string.restore_preview_date,
                    dateLabels().moment(
                        staged.backupAt,
                        zone,
                        stringResource(R.string.backup_moment)
                    )
                )
            )
        }
        if (refreshed) {
            Text(
                text = stringResource(R.string.restore_preview_refreshed),
                style = MaterialTheme.typography.bodyMedium,
                color = SelloTheme.colors.semantic.loss,
                modifier = Modifier.padding(top = SelloTheme.spacing.sm)
                    .semantics { liveRegion = LiveRegionMode.Polite }
            )
        }
        Counts(staged.current, staged.replacement)
        Body(stringResource(R.string.restore_preview_warning))
        SelloButton(
            text = stringResource(
                if (replacing) R.string.restore_replacing else R.string.restore_confirm
            ),
            onClick = { onAction(RestoreAction.Confirm) },
            tone = ButtonTone.Loss,
            loading = replacing,
            enabled = !replacing,
            modifier = Modifier.fillMaxWidth().padding(top = SelloTheme.spacing.md)
                .testTag(RESTORE_CONFIRM_TAG)
        )
        SelloButton(
            text = stringResource(R.string.restore_cancel),
            onClick = { onAction(RestoreAction.Cancel) },
            kind = ButtonKind.Outlined,
            enabled = !replacing,
            modifier = Modifier.fillMaxWidth().padding(top = SelloTheme.spacing.sm)
                .testTag(RESTORE_CANCEL_TAG)
        )
    }
}

/** Now and backup, side by side: categories, expenses, incomes. */
@Composable
private fun Counts(current: BackupSummary, replacement: BackupSummary) {
    val now = stringResource(R.string.restore_preview_now)
    val backup = stringResource(R.string.restore_preview_backup)
    Row(Modifier.fillMaxWidth().padding(top = SelloTheme.spacing.md)) {
        Spacer(Modifier.weight(1f))
        Cell(now, header = true)
        Cell(backup, header = true)
    }
    listOf(
        Triple(R.string.backup_categories, current.categories, replacement.categories),
        Triple(R.string.backup_expenses, current.expenses, replacement.expenses),
        Triple(R.string.backup_incomes, current.incomes, replacement.incomes)
    ).forEach { (label, before, after) ->
        val name = stringResource(label)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                .semantics(mergeDescendants = true) {}
        ) {
            Text(name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Cell(before.toString(), spoken = "$now $before")
            Cell(after.toString(), spoken = "$backup $after")
        }
    }
}

@Composable
private fun Cell(text: String, header: Boolean = false, spoken: String? = null) {
    Text(
        text = text,
        style = with(MaterialTheme.typography) { if (header) labelMedium else bodyLarge },
        fontWeight = if (header) null else FontWeight.W700,
        color = if (header) SelloTheme.colors.inkSoft else SelloTheme.colors.ink,
        textAlign = TextAlign.End,
        modifier = Modifier.width(88.dp).semantics {
            if (spoken != null) contentDescription = spoken
        }
    )
}

@Composable
private fun Restored(summary: BackupSummary, onAction: (RestoreAction) -> Unit) {
    Slip(Modifier.fillMaxWidth().testTag(RESTORE_DONE_TAG), pinked = false) {
        FlowRow(
            horizontalArrangement = Arrangement.SpaceBetween,
            itemVerticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Heading(stringResource(R.string.restore_done_title))
            Stamp(
                text = stringResource(R.string.restore_done_stamp),
                ink = StampInk.Good,
                land = true,
                modifier = Modifier.padding(vertical = 6.dp)
                    .semantics { liveRegion = LiveRegionMode.Polite }
            )
        }
        Body(
            stringResource(
                R.string.restore_done_body,
                pluralStringResource(
                    R.plurals.restore_categories,
                    summary.categories,
                    summary.categories
                ),
                pluralStringResource(
                    R.plurals.restore_expenses,
                    summary.expenses,
                    summary.expenses
                ),
                pluralStringResource(R.plurals.restore_incomes, summary.incomes, summary.incomes)
            )
        )
        GoToRecibo(onAction)
    }
}

/** D09's approved wording: the history is restored; only these preferences are not. */
@Composable
private fun Pending(
    preferences: PendingPreferences,
    applying: Boolean,
    onAction: (RestoreAction) -> Unit
) {
    Slip(Modifier.fillMaxWidth().testTag(RESTORE_PENDING_TAG), pinked = false) {
        Heading(stringResource(R.string.restore_pending_title))
        Body(stringResource(R.string.restore_pending_body))
        BulletList(
            listOfNotNull(
                preferences.appearance?.let {
                    Bullet(
                        null,
                        stringResource(
                            R.string.restore_pending_appearance,
                            stringResource(it.theme.label()),
                            stringResource(it.ink.label())
                        )
                    )
                },
                preferences.ordering?.let {
                    Bullet(
                        null,
                        stringResource(
                            R.string.restore_pending_ordering,
                            stringResource(it.order.label()),
                            stringResource(it.direction.label())
                        )
                    )
                }
            ),
            Modifier.padding(top = SelloTheme.spacing.sm)
        )
        SelloButton(
            text = stringResource(
                when {
                    preferences.appearance != null && preferences.ordering != null ->
                        R.string.restore_apply_both

                    preferences.appearance != null -> R.string.restore_apply_appearance

                    else -> R.string.restore_apply_ordering
                }
            ),
            onClick = { onAction(RestoreAction.ApplyPreferences) },
            loading = applying,
            enabled = !applying,
            modifier = Modifier.fillMaxWidth().padding(top = SelloTheme.spacing.md)
                .testTag(RESTORE_APPLY_TAG)
        )
        Text(
            text = stringResource(R.string.restore_pending_note),
            style = MaterialTheme.typography.bodySmall,
            color = SelloTheme.colors.inkSoft,
            modifier = Modifier.padding(top = SelloTheme.spacing.sm)
        )
        GoToRecibo(onAction, kind = ButtonKind.Text)
    }
}

@Composable
private fun GoToRecibo(onAction: (RestoreAction) -> Unit, kind: ButtonKind = ButtonKind.Filled) {
    SelloButton(
        text = stringResource(R.string.restore_go_to_recibo),
        onClick = { onAction(RestoreAction.Leave) },
        kind = kind,
        modifier = Modifier.fillMaxWidth().padding(top = SelloTheme.spacing.md)
            .testTag(RESTORE_LEAVE_TAG)
    )
}

@Composable
private fun Heading(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.semantics { heading() }
    )
}

@Composable
private fun Body(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = SelloTheme.colors.inkSoft,
        modifier = Modifier.padding(top = SelloTheme.spacing.xs)
    )
}

private fun RestoreRejection.message() = when (this) {
    RestoreRejection.Unreadable -> R.string.restore_rejected_unreadable
    RestoreRejection.TooLarge -> R.string.restore_rejected_too_large
    RestoreRejection.NotABackup -> R.string.restore_rejected_not_backup
    RestoreRejection.NewerVersion -> R.string.restore_rejected_newer
    RestoreRejection.Damaged -> R.string.restore_rejected_damaged
    RestoreRejection.InvalidContent -> R.string.restore_rejected_content
}

private fun ThemePreference.label() = when (this) {
    ThemePreference.System -> R.string.settings_theme_system
    ThemePreference.Light -> R.string.settings_theme_light
    ThemePreference.Dark -> R.string.settings_theme_dark
}

private fun InkPreference.label() = when (this) {
    InkPreference.Cobalto -> R.string.settings_ink_cobalto
    InkPreference.Violeta -> R.string.settings_ink_violeta
}

private fun CategoryOrder.label() = when (this) {
    CategoryOrder.MostUsed -> R.string.settings_order_most_used
    CategoryOrder.Alphabetical -> R.string.settings_order_alphabetical
    CategoryOrder.AmountSpent -> R.string.settings_order_amount_spent
    CategoryOrder.Limit -> R.string.settings_order_limit
    CategoryOrder.LatestExpenseAdded -> R.string.settings_order_latest_expense_added
}

private fun CategoryOrderDirection.label() = when (this) {
    CategoryOrderDirection.Descending -> R.string.restore_pending_descending
    CategoryOrderDirection.Ascending -> R.string.restore_pending_ascending
}

const val RESTORE_TAG = "restore"
const val RESTORE_CHOOSE_TAG = "restore:choose"
const val RESTORE_REJECTED_TAG = "restore:rejected"
const val RESTORE_FAILED_TAG = "restore:failed"
const val RESTORE_PREVIEW_TAG = "restore:preview"
const val RESTORE_CONFIRM_TAG = "restore:confirm"
const val RESTORE_CANCEL_TAG = "restore:cancel"
const val RESTORE_UNKNOWN_TAG = "restore:unknown"
const val RESTORE_DONE_TAG = "restore:done"
const val RESTORE_PENDING_TAG = "restore:pending"
const val RESTORE_APPLY_TAG = "restore:apply"
const val RESTORE_LEAVE_TAG = "restore:leave"
