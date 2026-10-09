package com.software.sello.designsystem.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogWindowProvider
import com.software.sello.designsystem.R
import com.software.sello.designsystem.theme.SelloTheme

/**
 * A confirmation for something that cannot be undone. [message] says what is affected
 * and how much, with the consequence in bold. Back and a tap outside cancel. While
 * [busy] the action shows a spinner and nothing can be pressed or dismissed, so the
 * action is requested once.
 */
@Composable
fun ConfirmSlip(
    title: String,
    message: AnnotatedString,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = true,
    busy: Boolean = false,
    dismissLabel: String = stringResource(R.string.sello_cancel)
) {
    Dialog(onDismissRequest = { if (!busy) onDismiss() }) {
        val window = (LocalView.current.parent as? DialogWindowProvider)?.window
        SideEffect { window?.setDimAmount(SCRIM_ALPHA) }
        Slip(
            pinked = false,
            contentPadding = SlipDefaults.PlainPadding,
            modifier = Modifier.semantics { paneTitle = title }
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 22.sp),
                modifier = Modifier.semantics { heading() }
            )
            Spacer(Modifier.height(SelloTheme.spacing.sm))
            Text(text = message, style = MaterialTheme.typography.bodyLarge)
            TearLine(bleed = 20.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(SelloTheme.spacing.md)) {
                SelloButton(
                    text = dismissLabel,
                    onClick = onDismiss,
                    kind = ButtonKind.Outlined,
                    enabled = !busy,
                    modifier = Modifier.weight(1f)
                )
                SelloButton(
                    text = confirmLabel,
                    onClick = onConfirm,
                    kind = if (destructive) ButtonKind.Destructive else ButtonKind.Filled,
                    loading = busy,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private const val SCRIM_ALPHA = 0.55f

/** A slip with nothing to show yet: a title, one sentence and at most one action. */
@Composable
fun EmptySlip(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Slip(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.semantics { heading() }
        )
        Spacer(Modifier.height(SelloTheme.spacing.xs))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyLarge,
            color = SelloTheme.colors.inkSoft
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(SelloTheme.spacing.lg))
            SelloButton(text = actionLabel, onClick = onAction, kind = ButtonKind.Outlined)
        }
    }
}

/**
 * A slip whose content could not be read. [message] says what failed in plain words.
 * It never shows a figure: a failed read has no amount, not a zero.
 */
@Composable
fun ErrorSlip(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.sello_error_title),
    retryLabel: String = stringResource(R.string.sello_retry)
) {
    Slip(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.semantics { heading() }
            )
            Spacer(Modifier.height(SelloTheme.spacing.xs))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = SelloTheme.colors.inkSoft
            )
        }
        Spacer(Modifier.height(SelloTheme.spacing.lg))
        SelloButton(text = retryLabel, onClick = onRetry, kind = ButtonKind.Outlined)
    }
}

/**
 * The loading state of a slip: bars where figures will be. Callers show it only when
 * loading lasts long enough to notice. With reduced motion the bars do not pulse.
 */
@Composable
fun SlipSkeleton(modifier: Modifier = Modifier, lines: Int = 3) {
    val loading = stringResource(R.string.sello_loading)
    val alpha = if (SelloTheme.reducedMotion) {
        SKELETON_REST_ALPHA
    } else {
        val pulse by rememberInfiniteTransition(label = "skeleton").animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(SKELETON_PULSE_MILLIS), RepeatMode.Reverse),
            label = "skeleton"
        )
        pulse
    }
    Slip(
        modifier = modifier.fillMaxWidth().clearAndSetSemantics {
            contentDescription = loading
            progressBarRangeInfo = ProgressBarRangeInfo.Indeterminate
        }
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.md),
            modifier = Modifier.graphicsLayer { this.alpha = alpha }
        ) {
            repeat(lines.coerceAtLeast(1)) { index ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth(SkeletonWidths[index % SkeletonWidths.size])
                        .height(if (index == 0) 28.dp else 14.dp)
                        .background(SelloTheme.colors.paperDim, SelloTheme.shapes.bar)
                )
            }
        }
    }
}

private const val SKELETON_PULSE_MILLIS = 900
private const val SKELETON_REST_ALPHA = 0.7f
private val SkeletonWidths = listOf(0.45f, 0.8f, 0.6f)
