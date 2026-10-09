package com.software.sello.catalog.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import com.software.sello.designsystem.component.ButtonKind
import com.software.sello.designsystem.component.ConfirmSlip
import com.software.sello.designsystem.component.EmptySlip
import com.software.sello.designsystem.component.ErrorSlip
import com.software.sello.designsystem.component.SelloButton
import com.software.sello.designsystem.component.SlipSkeleton
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.theme.SelloTheme
import kotlinx.coroutines.delay

@Composable
private fun Stack(content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.slipGap)) { content() }
}

@Composable
private fun Result(text: String, tag: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodyLarge,
        color = SelloTheme.colors.ink,
        modifier = Modifier.testTag(tag)
    )
}

private enum class ConfirmStage { Closed, Asking, Busy }

@Composable
fun ConfirmSlipExample() {
    var stage by rememberSaveable { mutableStateOf(ConfirmStage.Closed) }
    var outcome by rememberSaveable { mutableStateOf("Sin respuesta") }
    Stack {
        SelloButton(
            text = "Borrar categoría",
            onClick = { stage = ConfirmStage.Asking },
            kind = ButtonKind.Outlined,
            modifier = Modifier.testTag("confirm:open")
        )
        Result(outcome, "confirm:outcome")
    }
    if (stage != ConfirmStage.Closed) {
        // Stands in for a real command: busy for a moment, then a confirmed result.
        LaunchedEffect(stage) {
            if (stage == ConfirmStage.Busy) {
                delay(CONFIRM_BUSY_MILLIS)
                outcome = "Confirmado"
                stage = ConfirmStage.Closed
            }
        }
        ConfirmSlip(
            title = "¿Borrar Café?",
            message = buildAnnotatedString {
                append("Se borran la categoría y ")
                withStyle(SpanStyle(fontWeight = FontWeight.W700)) { append("sus 12 gastos") }
                append(". No se puede deshacer.")
            },
            confirmLabel = "Borrar",
            onConfirm = { stage = ConfirmStage.Busy },
            onDismiss = {
                outcome = "Cancelado"
                stage = ConfirmStage.Closed
            },
            busy = stage == ConfirmStage.Busy
        )
    }
}

private const val CONFIRM_BUSY_MILLIS = 1_200L

@Composable
fun EmptySlipExample() {
    var actions by rememberSaveable { mutableIntStateOf(0) }
    Stack {
        EmptySlip(
            title = "Aún no hay gastos",
            body = "Anota el primero y aquí verás cuánto te queda.",
            actionLabel = "Anotar un gasto",
            onAction = { actions++ }
        )
        Result("Acciones: $actions", "empty:actions")
        EmptySlip(title = "Sin movimientos este mes", body = "Nada que mostrar todavía.")
    }
}

@Composable
fun ErrorSlipExample() {
    var retries by rememberSaveable { mutableIntStateOf(0) }
    Stack {
        ErrorSlip(
            message = "No pudimos leer tus gastos de octubre. Tus datos siguen guardados.",
            onRetry = { retries++ }
        )
        Result("Reintentos: $retries", "error:retries")
    }
}

@Composable
fun SlipSkeletonExample() {
    Stack {
        SlipSkeleton()
        SlipSkeleton(lines = 5)
    }
}

@Composable
fun ButtonExample() {
    var presses by rememberSaveable { mutableIntStateOf(0) }
    Stack {
        ButtonKind.entries.forEach { kind ->
            SelloButton(
                text = kind.name,
                onClick = { presses++ },
                kind = kind,
                modifier = Modifier.fillMaxWidth().testTag("button:${kind.name}")
            )
        }
        SelloButton("Con icono", { presses++ }, Modifier.fillMaxWidth(), icon = SelloIcon.Add)
        SelloButton(
            text = "Guardando",
            onClick = { presses++ },
            loading = true,
            modifier = Modifier.fillMaxWidth().testTag("button:loading")
        )
        SelloButton(
            text = "Escribe un monto",
            onClick = { presses++ },
            enabled = false,
            modifier = Modifier.fillMaxWidth().testTag("button:disabled")
        )
        Result("Pulsaciones: $presses", "button:presses")
    }
}
