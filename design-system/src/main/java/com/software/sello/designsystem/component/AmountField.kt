package com.software.sello.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.IntOffset
import com.software.sello.designsystem.R
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.icon.painter
import com.software.sello.designsystem.theme.SelloMotion
import com.software.sello.designsystem.theme.SelloTheme
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

/**
 * The amount being entered, above the keypad. It shows one of three things and never
 * blends them:
 * - [amount]: the typed figure;
 * - nothing typed (`amount == null`): a faint placeholder announced as [emptyDescription],
 *   which is a draft, not zero;
 * - [rejectedText]: pasted text that was refused, drawn exactly as received.
 *
 * [message] explains an error or a hint such as the digit limit. Each change of
 * [limitPulse] nudges the figure to show a press was ignored. With [onPaste] a paste
 * button hands the clipboard text to the caller untouched.
 */
@Composable
fun AmountField(
    amount: MoneyTextValue?,
    modifier: Modifier = Modifier,
    rejectedText: String? = null,
    message: FieldMessage? = null,
    limitPulse: Int = 0,
    emptyDescription: String = stringResource(R.string.sello_amount_empty),
    onPaste: ((String) -> Unit)? = null,
    pasteLabel: String = stringResource(R.string.sello_paste)
) {
    val reduced = SelloTheme.reducedMotion
    val nudge = remember { Animatable(0f) }
    LaunchedEffect(limitPulse) {
        if (limitPulse > 0 && !reduced) {
            nudge.snapTo(0f)
            nudge.animateTo(1f, SelloMotion.feed())
            nudge.animateTo(0f, SelloMotion.feed())
        }
    }
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.weight(1f).offset {
                    IntOffset((nudge.value * SelloMotion.FeedOffset.toPx()).roundToInt(), 0)
                }
            ) {
                when {
                    rejectedText != null -> Text(
                        text = rejectedText,
                        style = MaterialTheme.typography.headlineSmall,
                        color = SelloTheme.colors.semantic.loss
                    )

                    amount != null -> MoneyText(amount, style = MoneyStyle.Total)

                    else -> Text(
                        text = "0",
                        style = MaterialTheme.typography.displayMedium,
                        color = SelloTheme.colors.outline,
                        modifier = Modifier.clearAndSetSemantics {
                            contentDescription = emptyDescription
                        }
                    )
                }
            }
            if (onPaste != null) {
                val clipboard = LocalClipboard.current
                val scope = rememberCoroutineScope()
                IconButton(
                    onClick = {
                        scope.launch {
                            val clip = clipboard.getClipEntry()?.clipData
                            val text = clip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.text
                            if (text != null) onPaste(text.toString())
                        }
                    }
                ) {
                    Icon(
                        painter = SelloIcon.ContentPaste.painter(),
                        contentDescription = pasteLabel,
                        tint = SelloTheme.colors.brand
                    )
                }
            }
        }
        if (message != null) FieldMessageText(message)
    }
}
