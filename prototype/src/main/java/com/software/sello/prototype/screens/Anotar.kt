package com.software.sello.prototype.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.software.sello.designsystem.component.ButtonKind
import com.software.sello.designsystem.component.ButtonTone
import com.software.sello.designsystem.component.CategoryCircle
import com.software.sello.designsystem.component.ChipFlow
import com.software.sello.designsystem.component.ConfirmSlip
import com.software.sello.designsystem.component.EntryAmountSlip
import com.software.sello.designsystem.component.FieldPill
import com.software.sello.designsystem.component.FormField
import com.software.sello.designsystem.component.LeaderLine
import com.software.sello.designsystem.component.MoneyStyle
import com.software.sello.designsystem.component.MoneyText
import com.software.sello.designsystem.component.SegmentedSwitch
import com.software.sello.designsystem.component.SelloButton
import com.software.sello.designsystem.component.SelloChip
import com.software.sello.designsystem.component.SelloScaffold
import com.software.sello.designsystem.component.Slip
import com.software.sello.designsystem.component.Stamp
import com.software.sello.designsystem.component.StampInk
import com.software.sello.designsystem.component.StampSize
import com.software.sello.designsystem.component.TearLine
import com.software.sello.designsystem.component.TotalLine
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.theme.SelloTheme
import com.software.sello.prototype.ActionIcon
import com.software.sello.prototype.AnotarRoute
import com.software.sello.prototype.AportarRoute
import com.software.sello.prototype.CloseButton
import com.software.sello.prototype.DiscardGuard
import com.software.sello.prototype.EntryControls
import com.software.sello.prototype.EntryLayout
import com.software.sello.prototype.IncomeReceiptRoute
import com.software.sello.prototype.LocalNav
import com.software.sello.prototype.LocalProto
import com.software.sello.prototype.ReceiptDesk
import com.software.sello.prototype.Sample
import com.software.sello.prototype.ScreenTag
import com.software.sello.prototype.SelloSheet
import com.software.sello.prototype.Soft
import com.software.sello.prototype.VariosRoute
import com.software.sello.prototype.cop
import com.software.sello.prototype.grouped
import com.software.sello.prototype.rememberDraft

private val sources = listOf("Salario", "Freelance", "Ingresos pasivos", "Transferencia", "Otro")

/** Screen 2. One sheet for an expense or an income; only the amount is required. */
@Composable
fun AnotarScreen(route: AnotarRoute) {
    val nav = LocalNav.current
    val proto = LocalProto.current
    val draft = rememberDraft(if (route.edit) 48_700 else null)
    var income by rememberSaveable { mutableStateOf(route.income) }
    var categoryId by rememberSaveable { mutableStateOf(route.categoryId ?: "alimentacion") }
    var source by rememberSaveable { mutableIntStateOf(1) }
    var note by rememberSaveable { mutableStateOf(if (route.edit) "Rappi, almuerzo" else "") }
    var repeats by rememberSaveable { mutableStateOf(route.recurring) }
    var repeatSheet by rememberSaveable { mutableStateOf(false) }
    var noteOpen by rememberSaveable { mutableStateOf(false) }
    var deleting by rememberSaveable { mutableStateOf(false) }
    val leave = DiscardGuard(
        dirty = draft.digits.isNotEmpty() && !route.edit,
        question = if (income) "¿Descartar este ingreso?" else "¿Descartar este gasto?"
    ) { nav.popBackStack() }
    val category = Sample.category(categoryId)
    val title = when {
        route.recurring && route.edit -> "Editar recurrente"
        route.edit -> if (income) "Editar ingreso" else "Editar gasto"
        else -> "Anotar"
    }
    ScreenTag(
        when {
            route.edit && route.recurring -> "2 · Anotar · editar recurrente"
            route.edit -> if (income) "2 · Anotar · editar ingreso" else "2 · Anotar · editar gasto"
            income -> "2 · Anotar · ingreso"
            else -> "2 · Anotar · gasto"
        }
    )
    SelloScaffold(
        title = title,
        navigationIcon = { CloseButton(leave) },
        actions = {
            if (route.edit) {
                ActionIcon(Icons.Rounded.Delete, "Borrar") { deleting = true }
            } else if (!income) {
                ActionIcon(Icons.AutoMirrored.Rounded.PlaylistAdd, "Anotar varios") {
                    nav.popBackStack()
                    nav.navigate(VariosRoute(categoryId = categoryId))
                }
            }
        }
    ) { padding ->
        EntryLayout(
            padding = padding,
            draft = {
                if (!route.edit) {
                    SegmentedSwitch(listOf("Gasto", "Ingreso"), if (income) 1 else 0, {
                        income =
                            it == 1
                    })
                }
                EntryAmountSlip(
                    label = if (income) "mié 14 oct, renglón nuevo · ${sources[source]}" else "jue 22 oct, renglón nuevo · ${category.name}",
                    amount = draft.digits.toLongOrNull()?.let { cop(it, signed = income) },
                    icon = if (income) null else category.icon,
                    tone = if (income) ButtonTone.Gain else ButtonTone.Brand
                )
                Slip {
                    if (income) {
                        LeaderLine("Octubre sumará") { MoneyText(cop(5_200_000 + draft.amount)) }
                        LeaderLine("Sin destino todavía") { MoneyText(cop(draft.amount - 358_750)) }
                    } else {
                        val categoryLeft = category.left - draft.amount
                        LeaderLine("Te quedarán") { MoneyText(cop(Sample.LEFT - draft.amount)) }
                        if (categoryLeft < 0) {
                            LeaderLine(
                                "Te pasas por ${grouped(-categoryLeft)} en ${category.name}",
                                "",
                                valueColor = SelloTheme.colors.semantic.loss
                            )
                        } else {
                            LeaderLine("En ${category.name}") { MoneyText(cop(categoryLeft)) }
                        }
                    }
                }
                ChipFlow {
                    if (income) {
                        sources.forEachIndexed { i, name ->
                            SelloChip(name, i == source, {
                                source =
                                    i
                            })
                        }
                    } else {
                        Sample.categories.forEach { c ->
                            SelloChip(c.name, c.id == categoryId, {
                                categoryId = c.id
                            }, icon = c.icon)
                        }
                    }
                }
                if (income && source == sources.lastIndex) {
                    FormField("Nombre de la fuente", "", {})
                }
                ChipFlow {
                    FieldPill(if (income) "mié 14 oct" else "Hoy, jue 22", SelloIcon.Event, {
                        proto.say("Aquí se abre el selector de fecha")
                    })
                    FieldPill(
                        note.ifBlank {
                            "Nota"
                        },
                        SelloIcon.EditNote,
                        { noteOpen = true },
                        placeholder = note.isBlank()
                    )
                    FieldPill(
                        if (repeats) "Se repite cada mes" else "No se repite",
                        SelloIcon.Event,
                        { repeatSheet = true },
                        placeholder = !repeats
                    )
                }
                if (noteOpen) {
                    FormField("Nota", note, { note = it.take(60) }, counter = "${note.length}/60")
                }
            },
            controls = {
                val missing = when {
                    draft.amount == 0L -> "Escribe el monto"
                    else -> null
                }
                val button = when {
                    route.edit -> "Guardar cambios"
                    income -> "Anotar ingreso"
                    else -> "Anotar gasto"
                }
                EntryControls(draft, button, enabled = missing == null, missing = missing, onSubmit = {
                    when {
                        route.edit -> {
                            nav.popBackStack()
                            proto.say("Cambios guardados")
                        }

                        income -> {
                            nav.popBackStack()
                            nav.navigate(IncomeReceiptRoute)
                        }

                        else -> {
                            nav.popBackStack()
                            proto.say("Gasto anotado", undo = true)
                        }
                    }
                })
            }
        )
    }
    if (repeatSheet) {
        RepeatSheet(
            onDone = {
                repeats = true
                repeatSheet = false
            },
            onDismiss = {
                repeats = false
                repeatSheet = false
            }
        )
    }
    if (deleting) {
        ConfirmSlip(
            title = if (income) "¿Borrar este ingreso?" else "¿Borrar este gasto?",
            message = AnnotatedString("Podrás deshacerlo durante unos segundos."),
            confirmLabel = "Borrar",
            onConfirm = {
                deleting = false
                nav.popBackStack()
                proto.say(if (income) "Ingreso borrado" else "Gasto borrado", undo = true)
            },
            onDismiss = { deleting = false }
        )
    }
}

/** "Se repite": frequency, the day it falls on and when it ends. */
@Composable
private fun RepeatSheet(onDone: () -> Unit, onDismiss: () -> Unit) {
    var every by rememberSaveable { mutableIntStateOf(2) }
    SelloSheet("Se repite", onDismiss, "2 · Hoja Se repite") {
        ChipFlow {
            listOf("Cada semana", "Cada 15 días", "Cada mes", "Otro").forEachIndexed { i, label ->
                SelloChip(label, i == every, { every = i })
            }
        }
        LeaderLine("El día") { Text("22 de cada mes", style = MaterialTheme.typography.bodyLarge) }
        LeaderLine("Termina") { Text("Nunca", style = MaterialTheme.typography.bodyLarge) }
        Soft("Próximo: dom 22 nov. Aparecerá solo en tu recibo.")
        SelloButton("Listo", onDone, Modifier.fillMaxWidth())
    }
}

private data class Line(
    val note: String,
    val categoryId: String?,
    val amount: Long,
    val day: String = ""
)

private val movementTypes = listOf("Aporte", "Valor actual", "Retiro", "Dividendo", "Interés")

/** Screen 3. Several expenses, or several investment movements, on one receipt. */
@Composable
fun VariosScreen(route: VariosRoute) {
    val nav = LocalNav.current
    val proto = LocalProto.current
    val lines = remember {
        mutableStateListOf<Line>().apply {
            if (route.movements) {
                add(Line("Aporte", null, 300_000, "sáb 3 oct"))
                add(Line("Dividendo", null, 24_100, "mar 29 sep"))
                add(Line("Valor actual", null, 0, "hoy, jue 22"))
            } else {
                add(Line("Uber", "transporte", 18_400))
                add(Line("Farmacia Pasteur", null, 75_100))
                add(Line("Carulla", route.categoryId ?: "alimentacion", 0))
            }
        }
    }
    var current by rememberSaveable { mutableIntStateOf(2) }
    val draft = rememberDraft(null)
    // The keypad types into the highlighted line.
    val shown = lines.mapIndexed { i, line ->
        if (i == current &&
            draft.digits.isNotEmpty()
        ) {
            line.copy(amount = draft.amount)
        } else {
            line
        }
    }
    val valid = shown.count { it.amount > 0 }
    val missingCategory = !route.movements && shown.any { it.amount > 0 && it.categoryId == null }
    val leave =
        DiscardGuard(dirty = true, question = "¿Descartar ${lines.size} renglones?") {
            nav.popBackStack()
        }
    val noun = if (route.movements) "movimientos" else "gastos"
    ScreenTag(if (route.movements) "3 · Varios movimientos" else "3 · Anotar varios")
    SelloScaffold(
        title = if (route.movements) "Varios movimientos" else "Anotar varios",
        subtitle = if (route.movements) "ETF S&P 500" else "jue 22 oct",
        navigationIcon = { CloseButton(leave) },
        actions = {
            if (!route.movements) {
                FieldPill("Hoy", SelloIcon.Event, {
                    proto.say("Una fecha para todo el recibo")
                })
            }
        }
    ) { padding ->
        EntryLayout(
            padding = padding,
            draft = {
                Slip {
                    Text(
                        "${lines.size} renglones",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Soft(
                        when {
                            route.movements -> "cada uno con su fecha"
                            missingCategory -> "a uno le falta categoría"
                            else -> "desliza un renglón para borrarlo"
                        },
                        color = if (missingCategory) SelloTheme.colors.semantic.loss else SelloTheme.colors.inkSoft
                    )
                    shown.forEachIndexed { i, line ->
                        VariosLine(
                            line = line,
                            movements = route.movements,
                            selected = i == current,
                            onClick = {
                                if (draft.digits.isNotEmpty()) lines[current] = shown[current]
                                current = i
                                draft.digits = ""
                            },
                            onDelete = if (lines.size > 1) {
                                {
                                    lines.removeAt(i)
                                    current = current.coerceAtMost(lines.lastIndex)
                                    draft.digits = ""
                                    proto.say("Renglón borrado", undo = true)
                                }
                            } else {
                                null
                            }
                        )
                    }
                    SelloButton(
                        "Otro renglón",
                        {
                            if (draft.digits.isNotEmpty()) lines[current] = shown[current]
                            lines.add(
                                Line("Sin nota", lines.lastOrNull()?.categoryId, 0, "hoy, jue 22")
                            )
                            current = lines.lastIndex
                            draft.digits = ""
                        },
                        kind = ButtonKind.Text,
                        icon = SelloIcon.Add,
                        enabled = lines.size < 30
                    )
                    if (!route.movements) {
                        TotalLine("Total") {
                            MoneyText(cop(shown.sumOf { it.amount }), style = MoneyStyle.Title)
                        }
                    }
                }
                ChipFlow(singleLine = true) {
                    if (route.movements) {
                        movementTypes.forEach { type ->
                            SelloChip(type, shown.getOrNull(current)?.note == type, {
                                lines[current] = shown[current].copy(note = type)
                            })
                        }
                    } else {
                        Sample.categories.forEach { c ->
                            SelloChip(c.name, shown.getOrNull(current)?.categoryId == c.id, {
                                lines[current] = shown[current].copy(categoryId = c.id)
                            }, icon = c.icon)
                        }
                    }
                }
                FieldPill(shown.getOrNull(current)?.note ?: "Nota", SelloIcon.EditNote, {})
            },
            controls = {
                val missing = when {
                    valid == 0 -> "Escribe al menos un monto"
                    missingCategory -> "A un renglón le falta categoría"
                    else -> null
                }
                val button = if (valid == 1) {
                    if (route.movements) "Anotar movimiento" else "Anotar gasto"
                } else {
                    "Anotar $valid $noun"
                }
                EntryControls(draft, button, enabled = missing == null, missing = missing, onSubmit = {
                    nav.popBackStack()
                    proto.say("$valid $noun anotados", undo = true)
                })
            }
        )
    }
}

@Composable
private fun VariosLine(
    line: Line,
    movements: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onDelete: (() -> Unit)?
) {
    val colors = SelloTheme.colors
    val category = line.categoryId?.let { Sample.category(it) }
    val problem = !movements && line.amount > 0 && category == null
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (selected) colors.brandSoft else colors.paper,
                MaterialTheme.shapes.small
            )
            .then(
                if (problem) {
                    Modifier.border(
                        2.dp,
                        colors.semantic.loss,
                        MaterialTheme.shapes.small
                    )
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        if (category != null) {
            CategoryCircle(category.icon, category.fraction, size = 32.dp, animate = false)
        }
        Column(Modifier.weight(1f)) {
            Text(line.note, style = MaterialTheme.typography.bodyLarge)
            Soft(
                when {
                    movements -> line.day
                    category != null -> category.name
                    else -> "Elige una categoría"
                },
                color = if (problem) colors.semantic.loss else colors.inkSoft
            )
        }
        MoneyText(cop(line.amount, signed = movements))
        if (selected &&
            onDelete != null
        ) {
            ActionIcon(Icons.Rounded.Delete, "Borrar renglón", onDelete)
        }
    }
}

/** Screen 8's reward: the income receipt, with the free money offered to a goal. */
@Composable
fun IncomeReceiptScreen() {
    val nav = LocalNav.current
    ScreenTag("8 · Comprobante de ingreso")
    SelloScaffold(title = "Ingreso anotado", navigationIcon = {
        CloseButton { nav.popBackStack() }
    }) { padding ->
        ReceiptDesk(padding) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Comprobante de ingreso", style = MaterialTheme.typography.titleMedium)
                    Soft("Freelance · mié 14 oct 2026")
                }
                Stamp("Recibido", StampInk.Good, size = StampSize.Large, land = true)
            }
            MoneyText(
                cop(850_000, signed = true),
                style = MoneyStyle.Total,
                color = SelloTheme.colors.semantic.gain
            )
            TearLine()
            LeaderLine("Octubre lleva") { MoneyText(cop(6_050_000)) }
            LeaderLine("Ya tiene destino") { MoneyText(cop(5_558_750)) }
            LeaderLine("Sin destino") { MoneyText(cop(491_250)) }
            TearLine()
            SelloButton(
                "Llevar 491.250 a una meta",
                {
                    nav.popBackStack()
                    nav.navigate(AportarRoute("emergencia", 491_250))
                },
                Modifier.fillMaxWidth()
            )
            SelloButton("Listo", {
                nav.popBackStack()
            }, Modifier.fillMaxWidth(), kind = ButtonKind.Text)
        }
    }
}

/** Screen 12. The add sheet for one investment. */
@Composable
fun MovementScreen(id: String) {
    val nav = LocalNav.current
    val proto = LocalProto.current
    val investment = Sample.investment(id)
    val draft = rememberDraft(300_000)
    var type by rememberSaveable { mutableIntStateOf(0) }
    val leave = DiscardGuard(dirty = false, question = "") { nav.popBackStack() }
    ScreenTag("12 · Movimiento")
    SelloScaffold(
        title = "Movimiento",
        subtitle = investment.name,
        navigationIcon = { CloseButton(leave) },
        actions = {
            ActionIcon(Icons.AutoMirrored.Rounded.PlaylistAdd, "Varios") {
                nav.popBackStack()
                nav.navigate(VariosRoute(movements = true))
            }
        }
    ) { padding ->
        EntryLayout(
            padding = padding,
            draft = {
                EntryAmountSlip(
                    label = "jue 22 oct, renglón nuevo · ${movementTypes[type]}",
                    amount = draft.digits.toLongOrNull()?.let { cop(it) }
                )
                Slip {
                    when (type) {
                        0 -> {
                            LeaderLine("Habrás aportado") {
                                MoneyText(cop(8_200_000 + draft.amount))
                            }
                            Soft("USD 74,07 a $ 4.050 · 24 aportes")
                        }

                        1 -> {
                            LeaderLine("Ganancia") {
                                MoneyText(cop(draft.amount - 8_200_000, signed = true))
                            }
                            Soft("Rendimiento nuevo")
                        }

                        2 -> LeaderLine("Quedará en") {
                            MoneyText(cop(investment.value - draft.amount))
                        }

                        else -> Soft("Se lista y se suma aparte; no cambia el valor.")
                    }
                }
                ChipFlow {
                    (movementTypes + "Nota").forEachIndexed { i, label ->
                        SelloChip(label, i == type, {
                            type =
                                i
                        })
                    }
                }
                ChipFlow {
                    FieldPill("Hoy, jue 22", SelloIcon.Event, {})
                    FieldPill("Nota", SelloIcon.EditNote, {}, placeholder = true)
                }
            },
            controls = {
                EntryControls(draft, "Anotar movimiento", enabled = draft.amount > 0, missing = null, onSubmit = {
                    nav.popBackStack()
                    proto.say(if (type == 0) "Aportado" else "Movimiento anotado", undo = true)
                })
            }
        )
    }
}
