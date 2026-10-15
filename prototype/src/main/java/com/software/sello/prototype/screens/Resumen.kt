package com.software.sello.prototype.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.software.sello.designsystem.component.ActionLine
import com.software.sello.designsystem.component.ButtonKind
import com.software.sello.designsystem.component.CategoryCircle
import com.software.sello.designsystem.component.ChartPeriod
import com.software.sello.designsystem.component.ChartPoint
import com.software.sello.designsystem.component.ChartReference
import com.software.sello.designsystem.component.ChipFlow
import com.software.sello.designsystem.component.ColumnsChart
import com.software.sello.designsystem.component.ConfirmSlip
import com.software.sello.designsystem.component.EmptySlip
import com.software.sello.designsystem.component.KeyValue
import com.software.sello.designsystem.component.LeaderLine
import com.software.sello.designsystem.component.LineAction
import com.software.sello.designsystem.component.MoneyStyle
import com.software.sello.designsystem.component.MoneyText
import com.software.sello.designsystem.component.SegmentedSwitch
import com.software.sello.designsystem.component.SelloButton
import com.software.sello.designsystem.component.SelloChip
import com.software.sello.designsystem.component.Slip
import com.software.sello.designsystem.component.SlipHeading
import com.software.sello.designsystem.component.Stamp
import com.software.sello.designsystem.component.StampInk
import com.software.sello.designsystem.component.TotalLine
import com.software.sello.designsystem.theme.SelloTheme
import com.software.sello.prototype.ActionIcon
import com.software.sello.prototype.AnotarRoute
import com.software.sello.prototype.Bar
import com.software.sello.prototype.CategoryRoute
import com.software.sello.prototype.Desk
import com.software.sello.prototype.IncomeRoute
import com.software.sello.prototype.LinkRow
import com.software.sello.prototype.LocalNav
import com.software.sello.prototype.LocalProto
import com.software.sello.prototype.Overflow
import com.software.sello.prototype.PatrimonioTab
import com.software.sello.prototype.RecurringRoute
import com.software.sello.prototype.Sample
import com.software.sello.prototype.ScreenTag
import com.software.sello.prototype.SelloSheet
import com.software.sello.prototype.Shell
import com.software.sello.prototype.SlipTitle
import com.software.sello.prototype.Soft
import com.software.sello.prototype.anotar
import com.software.sello.prototype.cop
import com.software.sello.prototype.grouped
import com.software.sello.prototype.openTab
import kotlinx.coroutines.delay

private val ranges = listOf("Este mes", "3 meses", "6 meses", "12 meses", "Este año", "Todo")

/** Screen 6. One slip per question: a small question, a large answer, one chart. */
@Composable
fun ResumenScreen() {
    val nav = LocalNav.current
    val proto = LocalProto.current
    var range by rememberSaveable { mutableIntStateOf(0) }
    var rangeSheet by rememberSaveable { mutableStateOf(false) }
    var exportSheet by rememberSaveable { mutableStateOf(false) }
    var picked by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(picked) {
        if (picked != null) {
            delay(3_000)
            picked = null
        }
    }
    ScreenTag("6 · Resumen" + (if (proto.empty) " · vacío" else ""))
    Shell(
        title = "Resumen",
        subtitle = if (range == 0) "octubre, al día 22" else ranges[range].lowercase(),
        actions = {
            ActionIcon(Icons.Rounded.CalendarMonth, "Elegir periodo") { rangeSheet = true }
            Overflow("Exportar" to { exportSheet = true })
        }
    ) { padding ->
        Desk(padding) {
            if (proto.empty) {
                EmptySlip(
                    "Aún no hay nada que resumir",
                    "Anota tu primer gasto y aquí verás cómo va el mes.",
                    actionLabel = "Anotar un gasto",
                    onAction = { nav.anotar(proto) }
                )
                return@Desk
            }
            Slip {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (range ==
                            0
                        ) {
                            "Has gastado"
                        } else {
                            "Gastaste en ${ranges[range].lowercase()}"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = SelloTheme.colors.inkSoft,
                        modifier = Modifier.weight(1f)
                    )
                    Stamp("Bajo el límite", StampInk.Good, Modifier.padding(vertical = 6.dp))
                }
                MoneyText(cop(if (range == 0) 3_002_800 else 10_420_600), style = MoneyStyle.Total)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Fact("Entró", "6,05 M", Modifier.weight(1f)) { nav.navigate(IncomeRoute) }
                    Fact("Guardaste", "1,62 M", Modifier.weight(1f)) { nav.openTab(PatrimonioTab) }
                    Fact("Frente a sep", "+4,6 %", Modifier.weight(1f), null)
                }
            }
            Slip(pinked = false) {
                SlipHeading("Cada día gastas") {
                    MoneyText(
                        picked?.let { cop(it.substringAfter(':').toLong()) } ?: cop(69_723),
                        style = MoneyStyle.Card
                    )
                }
                Soft(
                    picked?.let { "día ${it.substringBefore(':')}" }
                        ?: "99 mil al día que te quedan · sin fijos"
                )
                ColumnsChart(
                    points = (1..31).map { day ->
                        val v = if (day <=
                            22
                        ) {
                            listOf(42, 86, 249, 31, 55, 120, 64)[day % 7] * 1_000L
                        } else {
                            99_000L
                        }
                        ChartPoint(
                            "$day:$v",
                            if (day in setOf(1, 11, 31)) "$day" else "",
                            v,
                            "día $day, ${grouped(v)} pesos",
                            when {
                                day < 22 -> ChartPeriod.Past
                                day == 22 -> ChartPeriod.Current
                                else -> ChartPeriod.Upcoming
                            }
                        )
                    },
                    summary = "Gasto de cada día de octubre",
                    selectedId = picked,
                    onSelect = { picked = it }
                )
            }
            Slip(pinked = false) {
                SlipHeading("Un mes normal") { MoneyText(cop(3_530_000), style = MoneyStyle.Card) }
                Soft("diciembre pasó el límite · lo que falta de octubre, rayado")
                val months =
                    listOf("nov", "dic", "ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct")
                val values = listOf(3.4, 4.31, 3.6, 3.05, 3.5, 3.3, 3.7, 3.4, 3.6, 3.5, 3.2, 3.0)
                ColumnsChart(
                    points = months.zip(values).map { (m, v) ->
                        val pesos = (v * 1_000_000).toLong()
                        ChartPoint(
                            m,
                            m,
                            pesos,
                            "$m, ${grouped(pesos)} pesos",
                            when {
                                m == "oct" -> ChartPeriod.Current
                                v > 4.0 -> ChartPeriod.Over
                                else -> ChartPeriod.Past
                            }
                        )
                    },
                    summary = "Doce meses frente al límite",
                    reference = ChartReference(4_000_000, "límite")
                )
            }
            Slip(pinked = false) {
                SlipTitle("Se fue en", "Hogar y comida, 60 %")
                listOf(
                    Triple("hogar", 1_180_000L, 0.39f),
                    Triple("alimentacion", 612_400L, 0.20f),
                    Triple("entretenimiento", 271_500L, 0.09f),
                    Triple("transporte", 248_600L, 0.08f),
                    Triple("educacion", 189_000L, 0.06f)
                ).forEach { (id, amount, share) ->
                    val c = Sample.category(id)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { nav.navigate(CategoryRoute(id)) }
                            .padding(vertical = 6.dp)
                    ) {
                        CategoryCircle(c.icon, null, size = 32.dp, animate = false)
                        Column(Modifier.weight(1f)) {
                            Row {
                                Text(
                                    c.name,
                                    Modifier.weight(1f),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                MoneyText(cop(amount))
                            }
                            Bar(share * 2.5f)
                        }
                        Text(
                            "${(share * 100).toInt()} %",
                            Modifier.width(40.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                LeaderLine("Otras 5 categorías") { MoneyText(cop(501_300)) }
                TotalLine("Total") { MoneyText(cop(3_002_800), style = MoneyStyle.Title) }
            }
            Slip(pinked = false) {
                SlipHeading("Tu día más caro") {
                    Text("Sábado", style = MaterialTheme.typography.headlineMedium)
                }
                Soft("miles por día, en promedio")
                val days = listOf(
                    "lun" to 62,
                    "mar" to 48,
                    "mié" to 55,
                    "jue" to 71,
                    "vie" to 96,
                    "sáb" to 138,
                    "dom" to 84
                )
                ColumnsChart(
                    points = days.map { (d, v) ->
                        ChartPoint(
                            d,
                            d,
                            v * 1_000L,
                            "$d, $v mil pesos",
                            if (d ==
                                "sáb"
                            ) {
                                ChartPeriod.Current
                            } else {
                                ChartPeriod.Past
                            },
                            "$v"
                        )
                    },
                    summary = "Gasto promedio por día de la semana"
                )
            }
            Slip(pinked = false) {
                SlipTitle("Récords")
                KeyValue("Mejor mes", "feb, 3,05 M")
                KeyValue("Peor mes", "dic, 4,31 M")
                KeyValue("Sobre el límite", "1 de 12")
                KeyValue("Gasto mayor", "Arriendo")
            }
            Slip(pinked = false) {
                LinkRow("Lo que se repite", "5 gastos fijos, $ 1.513.800 al mes") {
                    nav.navigate(RecurringRoute)
                }
            }
        }
    }
    if (rangeSheet) {
        SelloSheet("Periodo", { rangeSheet = false }, "6 · Hoja de periodo") {
            ChipFlow {
                ranges.forEachIndexed { i, label ->
                    SelloChip(label, i == range, {
                        range = i
                        rangeSheet = false
                    })
                }
            }
            Soft("Otro periodo cambia las dos primeras cifras por totales y un promedio mensual.")
        }
    }
    if (exportSheet) {
        ExportSheet { exportSheet = false }
    }
}

/** Exportar, from Resumen's overflow and from Configuración. */
@Composable
fun ExportSheet(onDismiss: () -> Unit) {
    val proto = LocalProto.current
    var where by rememberSaveable { mutableIntStateOf(0) }
    var format by rememberSaveable { mutableIntStateOf(0) }
    SelloSheet("Exportar", onDismiss, "6 · Hoja de exportar") {
        SegmentedSwitch(listOf("Guardar en Descargas", "Compartir"), where, { where = it })
        SegmentedSwitch(listOf("CSV", "JSON"), format, { format = it })
        SelloButton(
            "Exportar",
            {
                onDismiss()
                proto.say("Archivo listo")
            },
            Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Fact(label: String, value: String, modifier: Modifier, onClick: (() -> Unit)?) {
    Column(
        modifier.then(
            if (onClick !=
                null
            ) {
                Modifier.clickable(onClick = onClick)
            } else {
                Modifier
            }
        )
    ) {
        Soft(label)
        Text(value, style = MaterialTheme.typography.titleMedium)
    }
}

/** Screen 8. What came in, and where it went. */
@Composable
fun IncomeScreen() {
    val nav = LocalNav.current
    val proto = LocalProto.current
    ScreenTag("8 · Ingresos" + (if (proto.empty) " · vacío" else ""))
    Shell(
        title = "Ingresos",
        subtitle = "octubre",
        onBack = { nav.popBackStack() },
        actions = {
            ActionIcon(Icons.Rounded.AddCircle, "Anotar ingreso") {
                nav.navigate(AnotarRoute(income = true))
            }
        }
    ) { padding ->
        Desk(padding) {
            if (proto.empty) {
                EmptySlip(
                    "Aún no anotas ingresos en octubre.",
                    "La app funciona sin ingresos; el recibo nunca depende de ellos.",
                    actionLabel = "Anotar ingreso",
                    onAction = { nav.navigate(AnotarRoute(income = true)) }
                )
                return@Desk
            }
            Slip {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Entró este mes",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SelloTheme.colors.inkSoft,
                        modifier = Modifier.weight(1f)
                    )
                    Stamp("Recibido", StampInk.Good, Modifier.padding(vertical = 6.dp))
                }
                MoneyText(cop(6_050_000), style = MoneyStyle.Total)
                Sample.incomes.forEach { income ->
                    ActionLine(
                        label = income.name,
                        caption = income.detail,
                        onClick = { nav.navigate(AnotarRoute(income = true, edit = true)) },
                        actions = listOf(
                            LineAction("Editar") {
                                nav.navigate(AnotarRoute(income = true, edit = true))
                            },
                            LineAction("Borrar") { proto.say("Ingreso borrado", undo = true) }
                        ),
                        swipeAway = LineAction("Borrar") {
                            proto.say("Ingreso borrado", undo = true)
                        }
                    ) { MoneyText(cop(income.amount)) }
                }
            }
            Slip(pinked = false) {
                SlipTitle("A dónde fue", "92 % ya tiene destino")
                LeaderLine("Gastos del mes") { MoneyText(cop(3_940_000)) }
                LeaderLine("Metas") { MoneyText(cop(1_318_750)) }
                LeaderLine("Inversiones") { MoneyText(cop(300_000)) }
                LeaderLine("Sin destino") { MoneyText(cop(491_250)) }
                TotalLine("Total") { MoneyText(cop(6_050_000), style = MoneyStyle.Title) }
            }
            Slip(pinked = false) {
                SlipHeading("Últimos seis meses") {
                    Text("Tu mejor mes desde junio", style = MaterialTheme.typography.titleMedium)
                }
                Soft("millones")
                ColumnsChart(
                    points = listOf(
                        "may" to 5.2,
                        "jun" to 6.3,
                        "jul" to 5.2,
                        "ago" to 5.6,
                        "sep" to 5.4,
                        "oct" to 6.05
                    ).map { (m, v) ->
                        val pesos = (v * 1_000_000).toLong()
                        ChartPoint(
                            m,
                            m,
                            pesos,
                            "$m, ${grouped(pesos)} pesos",
                            if (m ==
                                "oct"
                            ) {
                                ChartPeriod.Current
                            } else {
                                ChartPeriod.Past
                            }
                        )
                    },
                    summary = "Ingresos de los últimos seis meses"
                )
            }
            Soft("próximo: Salario, dom 1 nov", Modifier.padding(horizontal = 4.dp))
        }
    }
}

/** Screen 7. The templates that create expenses and income on their own. */
@Composable
fun RecurringScreen() {
    val nav = LocalNav.current
    val proto = LocalProto.current
    var kind by rememberSaveable { mutableIntStateOf(0) }
    var deleting by rememberSaveable { mutableStateOf<String?>(null) }
    var paused by rememberSaveable { mutableStateOf(setOf<String>()) }
    ScreenTag("7 · Lo que se repite" + (if (proto.empty) " · vacío" else ""))
    Shell(
        title = "Lo que se repite",
        onBack = { nav.popBackStack() },
        tabs = false,
        actions = {
            ActionIcon(Icons.Rounded.AddCircle, "Anotar algo que se repite") {
                nav.navigate(AnotarRoute(recurring = true, income = kind == 1))
            }
        }
    ) { padding ->
        Desk(padding) {
            SegmentedSwitch(listOf("Gastos", "Ingresos"), kind, { kind = it })
            if (proto.empty) {
                EmptySlip(
                    "Nada se repite todavía.",
                    "Cuando anotes el arriendo o el salario, marca Se repite.",
                    actionLabel = "Anotar algo que se repite",
                    onAction = { nav.navigate(AnotarRoute(recurring = true)) }
                )
                return@Desk
            }
            val items = if (kind == 0) Sample.recurring else Sample.recurringIncome
            Slip {
                Answer2(
                    "Cada mes ${if (kind == 0) "salen" else "entran"} solos",
                    items.sumOf {
                        it.amount
                    }
                )
                LeaderLine("Ya pasaron", if (kind == 0) "4 de 5" else "1 de 1")
                if (kind == 0) LeaderLine("Falta este mes") { MoneyText(cop(44_900)) }
            }
            Slip(pinked = false) {
                SlipTitle(if (kind == 0) "Cinco gastos" else "Un ingreso", "por fecha")
                items.forEach { item ->
                    val isPaused = item.name in paused
                    Column {
                        ActionLine(
                            label = item.name,
                            caption = item.detail,
                            onClick = {
                                nav.navigate(
                                    AnotarRoute(
                                        edit = true,
                                        recurring = true,
                                        income =
                                            kind == 1
                                    )
                                )
                            },
                            actions = listOf(
                                LineAction(if (isPaused) "Reanudar" else "Pausar") {
                                    paused =
                                        if (isPaused) paused - item.name else paused + item.name
                                },
                                LineAction("Borrar") { deleting = item.name }
                            ),
                            swipeAway = LineAction("Pausar") { paused = paused + item.name }
                        ) {
                            MoneyText(
                                cop(item.amount),
                                color = if (item.due ||
                                    isPaused
                                ) {
                                    SelloTheme.colors.inkSoft
                                } else {
                                    SelloTheme.colors.ink
                                }
                            )
                        }
                        if (isPaused) {
                            Stamp(
                                "En pausa",
                                StampInk.Neutral,
                                Modifier.padding(start = 8.dp, bottom = 6.dp)
                            )
                        }
                    }
                }
            }
            Soft(
                if (kind ==
                    0
                ) {
                    "Próximo: Suscripciones, mié 28 oct"
                } else {
                    "Próximo: Salario, dom 1 nov"
                },
                Modifier.padding(horizontal = 4.dp)
            )
            SelloButton(
                "Anotar algo que se repite",
                { nav.navigate(AnotarRoute(recurring = true, income = kind == 1)) },
                Modifier.fillMaxWidth(),
                kind = ButtonKind.Outlined
            )
        }
    }
    deleting?.let { name ->
        ConfirmSlip(
            title = "¿Borrar $name?",
            message = AnnotatedString("Deja de crearse. Los gastos ya creados no se borran."),
            confirmLabel = "Borrar",
            onConfirm = {
                deleting = null
                proto.say("$name ya no se repite")
            },
            onDismiss = { deleting = null }
        )
    }
}

@Composable
private fun Answer2(question: String, amount: Long) {
    Soft(question)
    MoneyText(cop(amount), style = MoneyStyle.Total)
}
