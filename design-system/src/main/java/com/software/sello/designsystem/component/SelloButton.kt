package com.software.sello.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.icon.painter
import com.software.sello.designsystem.theme.SelloTheme

/** One filled button per screen; Destructive only inside a confirmation. */
enum class ButtonKind { Filled, Outlined, Text, Destructive }

/**
 * Sello's button. While [loading] it keeps its label and colours, shows a spinner in
 * place of the icon and ignores presses, so a save cannot be submitted twice.
 */
@Composable
fun SelloButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    kind: ButtonKind = ButtonKind.Filled,
    icon: SelloIcon? = null,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    val colors = SelloTheme.colors
    val (container, content) = when (kind) {
        ButtonKind.Filled -> colors.brand to colors.onBrand
        ButtonKind.Destructive -> colors.semantic.loss to colors.semantic.onLoss
        ButtonKind.Outlined, ButtonKind.Text -> Color.Transparent to colors.brand
    }
    // A loading button stays at full strength; a disabled one fades to 38 %.
    val fade = if (loading) 1f else DISABLED_ALPHA
    val isFilled = kind == ButtonKind.Filled || kind == ButtonKind.Destructive
    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        shape = SelloTheme.shapes.button,
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = content,
            disabledContainerColor = if (isFilled) container.copy(alpha = fade) else container,
            disabledContentColor = content.copy(alpha = fade)
        ),
        border = if (kind == ButtonKind.Outlined) {
            BorderStroke(1.5.dp, colors.brand.copy(alpha = if (enabled || loading) 1f else fade))
        } else {
            null
        },
        modifier = modifier.heightIn(min = if (kind == ButtonKind.Text) 46.dp else 56.dp)
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = LocalContentColor.current,
                strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
        } else if (icon != null) {
            Icon(icon.painter(), contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = if (kind == ButtonKind.Text) {
                MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp)
            } else {
                MaterialTheme.typography.labelLarge
            },
            textAlign = TextAlign.Center
        )
    }
}

private const val DISABLED_ALPHA = 0.38f
