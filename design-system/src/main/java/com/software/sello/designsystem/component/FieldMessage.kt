package com.software.sello.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.icon.painter
import com.software.sello.designsystem.theme.SelloTheme

/**
 * What a control says about its own value: an error the user must fix, or a neutral
 * hint. Features decide the words from their typed validation result; every Sello
 * control shows them the same way. A control never corrects the value instead.
 */
@Immutable
data class FieldMessage(val text: String, val isError: Boolean = true)

/** The shared look of a [FieldMessage]. Announced when it appears. */
@Composable
fun FieldMessageText(message: FieldMessage, modifier: Modifier = Modifier) {
    val color = if (message.isError) SelloTheme.colors.semantic.loss else SelloTheme.colors.inkSoft
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.semantics(mergeDescendants = true) {
            liveRegion = LiveRegionMode.Polite
        }
    ) {
        if (message.isError) {
            Icon(
                painter = SelloIcon.Error.painter(),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }
        Text(message.text, style = MaterialTheme.typography.labelMedium, color = color)
    }
}
