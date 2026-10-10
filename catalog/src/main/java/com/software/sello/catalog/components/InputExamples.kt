package com.software.sello.catalog.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.software.sello.designsystem.component.AmountField
import com.software.sello.designsystem.component.AmountKeypad
import com.software.sello.designsystem.component.ButtonKind
import com.software.sello.designsystem.component.ChipFlow
import com.software.sello.designsystem.component.FieldMessage
import com.software.sello.designsystem.component.FieldMessageText
import com.software.sello.designsystem.component.FieldPill
import com.software.sello.designsystem.component.FormField
import com.software.sello.designsystem.component.IconChoice
import com.software.sello.designsystem.component.IconChoiceGrid
import com.software.sello.designsystem.component.SegmentedSwitch
import com.software.sello.designsystem.component.SelloButton
import com.software.sello.designsystem.component.SelloChip
import com.software.sello.designsystem.component.Slip
import com.software.sello.designsystem.component.SwitchRow
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.theme.SelloTheme
import kotlinx.coroutines.delay

@Composable
private fun Stack(content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.slipGap)) { content() }
}

@Composable
private fun Caption(text: String) {
    Text(text, style = MaterialTheme.typography.labelMedium, color = SelloTheme.colors.inkSoft)
}

private fun draftMessage(draft: DraftFixture, limitShown: Boolean): FieldMessage? = when {
    draft.rejected != null -> FieldMessage("Eso no es un monto. Escribe solo números, como 1.234.")
    limitShown -> FieldMessage("Máximo 12 dígitos", isError = false)
    else -> null
}

/** The amount display and keypad wired to a catalog-local draft. */
@Composable
private fun AmountEntry(
    draft: DraftFixture,
    onDraft: (DraftFixture) -> Unit,
    enabled: Boolean = true
) {
    val limitShown = draft.limitPulse > 0 && draft.digits.length >= DraftFixture.MAX_DIGITS - 2
    AmountField(
        amount = draft.amount,
        rejectedText = draft.rejected,
        message = draftMessage(draft, limitShown),
        limitPulse = draft.limitPulse,
        onPaste = { onDraft(draft.paste(it)) },
        modifier = Modifier.fillMaxWidth().testTag("amount:field")
    )
    AmountKeypad(
        onDigit = { onDraft(draft.type(it.toString())) },
        onTripleZero = { onDraft(draft.type("000")) },
        onBackspace = { onDraft(draft.backspace()) },
        onClearAll = { onDraft(DraftFixture()) },
        enabled = enabled,
        modifier = Modifier.testTag("amount:keypad")
    )
}

@Composable
fun AmountKeypadExample() {
    var draft by remember { mutableStateOf(DraftFixture()) }
    Stack {
        AmountEntry(draft, { draft = it })
        Caption("Sin portapapeles a mano: simula lo que alguien pegaría")
        Row(horizontalArrangement = Arrangement.spacedBy(SelloTheme.spacing.sm)) {
            for (sample in listOf("1e3", "-100", "12.34", "1.234")) {
                SelloButton(
                    text = sample,
                    onClick = { draft = draft.paste(sample) },
                    kind = ButtonKind.Text,
                    modifier = Modifier.testTag("amount:paste:$sample")
                )
            }
        }
        SelloButton("Reiniciar", { draft = DraftFixture() }, kind = ButtonKind.Outlined)
    }
}

private val CategoryFixtures = listOf(
    "Alimentación" to SelloIcon.Restaurant,
    "Transporte" to SelloIcon.DirectionsBus,
    "Café" to SelloIcon.LocalCafe,
    "Hogar" to SelloIcon.Home,
    "Una categoría de nombre largo" to SelloIcon.ReceiptLong
)

private val SourceFixtures =
    listOf("Salario", "Freelance", "Ingresos pasivos", "Transferencia", "Otro")

@Composable
fun ChipExample() {
    var category by rememberSaveable { mutableIntStateOf(-1) }
    var source by rememberSaveable { mutableIntStateOf(0) }
    Stack {
        Caption("Categorías: se acomodan en varias líneas")
        ChipFlow {
            CategoryFixtures.forEachIndexed { index, (name, icon) ->
                SelloChip(name, category == index, { category = index }, icon = icon)
            }
        }
        if (category < 0) FieldMessageText(FieldMessage("Elige una categoría"))
        Caption("Fuentes: una sola línea que se desliza")
        ChipFlow(singleLine = true) {
            SourceFixtures.forEachIndexed { index, name ->
                SelloChip(name, source == index, { source = index })
            }
        }
        Caption("Deshabilitado")
        ChipFlow {
            SelloChip("Archivada", selected = false, onClick = {}, enabled = false)
        }
        SelloButton("Quitar selección", { category = -1 }, kind = ButtonKind.Text)
    }
}

@Composable
fun FieldPillExample() {
    var presses by rememberSaveable { mutableIntStateOf(0) }
    Stack {
        Row(horizontalArrangement = Arrangement.spacedBy(SelloTheme.spacing.sm)) {
            FieldPill("Hoy, jue 22", SelloIcon.Event, { presses++ })
            FieldPill("Nota", SelloIcon.EditNote, { presses++ }, placeholder = true)
        }
        Caption("Pulsaciones: $presses")
    }
}

private const val NAME_LIMIT = 24
private const val NOTE_LIMIT = 60

private fun count(text: String) = text.trim().let { it.codePointCount(0, it.length) }

@Composable
fun FormFieldExample() {
    var name by rememberSaveable { mutableStateOf("Mercado de plaza") }
    var note by rememberSaveable { mutableStateOf("") }
    Slip(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.lg)) {
            FormField(
                label = "Nombre",
                value = name,
                onValueChange = { name = it },
                message = when {
                    name.isBlank() -> FieldMessage("Escribe un nombre")
                    count(name) > NAME_LIMIT -> FieldMessage("Máximo $NAME_LIMIT caracteres")
                    else -> null
                },
                counter = "${count(name)}/$NAME_LIMIT",
                modifier = Modifier.testTag("field:name")
            )
            FormField(
                label = "Nota (opcional)",
                value = note,
                onValueChange = { note = it },
                message = if (count(note) > NOTE_LIMIT) {
                    FieldMessage("Máximo $NOTE_LIMIT caracteres")
                } else {
                    FieldMessage("Solo tú la ves", isError = false)
                },
                counter = "${count(note)}/$NOTE_LIMIT",
                modifier = Modifier.testTag("field:note")
            )
            FormField("Deshabilitado", "No editable", {}, enabled = false)
        }
    }
}

@Composable
fun SwitchExample() {
    var repeats by rememberSaveable { mutableStateOf(false) }
    var mode by rememberSaveable { mutableIntStateOf(0) }
    Slip(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.md)) {
            SwitchRow("Avisarme al acercarme al límite", repeats, { repeats = it })
            SwitchRow(
                title = "Con icono y detalle",
                checked = !repeats,
                onCheckedChange = { repeats = !it },
                icon = SelloIcon.Event,
                subtitle = "El renglón completo es el control"
            )
            SwitchRow("Deshabilitado", checked = true, onCheckedChange = {}, enabled = false)
            SegmentedSwitch(listOf("Sistema", "Claro", "Oscuro"), mode, { mode = it })
        }
    }
}

/** One icon chosen, one tapped changes it, and a disabled copy that ignores taps. */
@Composable
fun IconChoiceExample() {
    var picked by rememberSaveable { mutableStateOf("home") }
    val choices = remember {
        listOf(
            IconChoice("restaurant", SelloIcon.Restaurant, "Comida"),
            IconChoice("home", SelloIcon.Home, "Hogar"),
            IconChoice("directions_bus", SelloIcon.DirectionsBus, "Transporte"),
            IconChoice("theaters", SelloIcon.Theaters, "Entretenimiento"),
            IconChoice("medical_services", SelloIcon.MedicalServices, "Salud"),
            IconChoice("checkroom", SelloIcon.Checkroom, "Ropa"),
            IconChoice("local_cafe", SelloIcon.LocalCafe, "Café"),
            IconChoice("school", SelloIcon.School, "Educación"),
            IconChoice("pets", SelloIcon.Pets, "Mascotas"),
            IconChoice("fitness_center", SelloIcon.FitnessCenter, "Gimnasio"),
            IconChoice("shopping_cart", SelloIcon.ShoppingCart, "Mercado"),
            IconChoice("flight", SelloIcon.Flight, "Viajes"),
            IconChoice("phone_iphone", SelloIcon.PhoneIphone, "Celular"),
            IconChoice("child_care", SelloIcon.ChildCare, "Hijos")
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.md)) {
        Slip(modifier = Modifier.fillMaxWidth(), pinked = false) {
            IconChoiceGrid(choices, picked, { picked = it }, Modifier.testTag("icons:grid"))
        }
        Text(
            "Elegido: $picked",
            style = MaterialTheme.typography.bodyLarge,
            color = SelloTheme.colors.ink,
            modifier = Modifier.testTag("icons:picked")
        )
        Slip(modifier = Modifier.fillMaxWidth(), pinked = false) {
            IconChoiceGrid(choices.take(4), "home", { picked = it }, enabled = false)
        }
    }
}

/** Everything an entry sheet needs, together, with nothing behind it. */
@Composable
fun ExpenseEntryExample() {
    var draft by remember { mutableStateOf(DraftFixture()) }
    var category by rememberSaveable { mutableIntStateOf(-1) }
    var note by rememberSaveable { mutableStateOf("") }
    var busy by rememberSaveable { mutableStateOf(false) }
    var result by rememberSaveable { mutableStateOf("") }
    val ready = draft.amount != null && category >= 0 && count(note) <= NOTE_LIMIT
    LaunchedEffect(busy) {
        if (busy) {
            delay(ENTRY_BUSY_MILLIS)
            result = "El catálogo no guarda nada: esto solo muestra el estado ocupado."
            busy = false
        }
    }
    Stack {
        AmountEntry(draft, { draft = it }, enabled = !busy)
        ChipFlow {
            CategoryFixtures.forEachIndexed { index, (name, icon) ->
                SelloChip(name, category == index, {
                    category = index
                }, icon = icon, enabled = !busy)
            }
        }
        FormField(
            label = "Nota (opcional)",
            value = note,
            onValueChange = { note = it },
            enabled = !busy,
            message = if (count(note) >
                NOTE_LIMIT
            ) {
                FieldMessage("Máximo $NOTE_LIMIT caracteres")
            } else {
                null
            },
            counter = "${count(note)}/$NOTE_LIMIT",
            modifier = Modifier.testTag("entry:note")
        )
        if (!ready && !busy) {
            Caption(
                when {
                    draft.amount == null -> "Escribe un monto"
                    category < 0 -> "Elige una categoría"
                    else -> "Acorta la nota"
                }
            )
        }
        SelloButton(
            text = "Anotar",
            onClick = { busy = true },
            enabled = ready,
            loading = busy,
            modifier = Modifier.fillMaxWidth().testTag("entry:save")
        )
        if (result.isNotEmpty()) Caption(result)
    }
}

private const val ENTRY_BUSY_MILLIS = 1_200L
