package com.software.sello.prototype.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.software.sello.designsystem.component.ActionLine
import com.software.sello.designsystem.component.Blotter
import com.software.sello.designsystem.component.ButtonKind
import com.software.sello.designsystem.component.CategoryCell
import com.software.sello.designsystem.component.CategoryCircle
import com.software.sello.designsystem.component.CategoryGrid
import com.software.sello.designsystem.component.ChartPeriod
import com.software.sello.designsystem.component.ChartPoint
import com.software.sello.designsystem.component.ChartReference
import com.software.sello.designsystem.component.ColumnsChart
import com.software.sello.designsystem.component.EmptySlip
import com.software.sello.designsystem.component.FormField
import com.software.sello.designsystem.component.KeyValue
import com.software.sello.designsystem.component.LeaderLine
import com.software.sello.designsystem.component.LineAction
import com.software.sello.designsystem.component.MoneyStyle
import com.software.sello.designsystem.component.MoneyText
import com.software.sello.designsystem.component.MonthCell
import com.software.sello.designsystem.component.MonthGrid
import com.software.sello.designsystem.component.MonthSwitcher
import com.software.sello.designsystem.component.SegmentedSwitch
import com.software.sello.designsystem.component.SelloButton
import com.software.sello.designsystem.component.Slip
import com.software.sello.designsystem.component.SlipHeading
import com.software.sello.designsystem.component.Stamp
import com.software.sello.designsystem.component.StampInk
import com.software.sello.designsystem.component.StampSize
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.theme.SelloTheme
import com.software.sello.prototype.ActionIcon
import com.software.sello.prototype.AnotarRoute
import com.software.sello.prototype.BackupRoute
import com.software.sello.prototype.CategoryEditorRoute
import com.software.sello.prototype.CategoryRoute
import com.software.sello.prototype.Desk
import com.software.sello.prototype.ExpenseListRoute
import com.software.sello.prototype.LocalNav
import com.software.sello.prototype.LocalProto
import com.software.sello.prototype.Sample
import com.software.sello.prototype.SampleCategory
import com.software.sello.prototype.ScreenTag
import com.software.sello.prototype.SelloSheet
import com.software.sello.prototype.SettingsRoute
import com.software.sello.prototype.Shell
import com.software.sello.prototype.SlipTitle
import com.software.sello.prototype.Soft
import com.software.sello.prototype.Step
import com.software.sello.prototype.anotar
import com.software.sello.prototype.cop
import com.software.sello.prototype.grouped

/** Screen 1. The start destination: what is left, and where it is going. */
@Composable
fun ReciboScreen() {
    val nav = LocalNav.current
    val proto = LocalProto.current
    var monthSheet by rememberSaveable { mutableStateOf(false) }
    var selected by rememberSaveable { mutableStateOf(Sample.categories.first().id) }
    // The reference puts list and detail side by side from 840dp.
    val wide = LocalConfiguration.current.screenWidthDp >= 840
    val past = proto.pastMonth
    ScreenTag(
        when {
            proto.firstRun -> "1 · Recibo · primer uso"
            proto.pastMonth != null -> "1 · Recibo · mes cerrado"
            else -> "1 · Recibo"
        }
    )
    Shell(
        title = past ?: Sample.MONTH,
        titleContent = { MonthSwitcher(past ?: Sample.MONTH, onClick = { monthSheet = true }) },
        blotter = Blotter.Tall,
        actions = {
            ActionIcon(Icons.Rounded.Settings, "Configuración") { nav.navigate(SettingsRoute) }
        },
        secondaryPane = if (wide && !proto.firstRun) {
            { padding -> CategoryDetail(selected, padding) }
        } else {
            null
        }
    ) { padding ->
        Desk(padding) {
            if (proto.firstRun) {
                FirstRun()
            } else {
                Summary(past)
                Categories(
                    selectedId = selected.takeIf { wide },
                    onOpen = { id -> if (wide) selected = id else nav.navigate(CategoryRoute(id)) }
                )
            }
        }
    }
    if (monthSheet) {
        MonthSheet(onDismiss = { monthSheet = false })
    }
}

@Composable
private fun Summary(past: String?) {
    Slip {
        if (past == null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Te quedan",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SelloTheme.colors.inkSoft,
                    modifier = Modifier.weight(1f)
                )
                Stamp("Al día", StampInk.Good, Modifier.padding(vertical = 6.dp))
            }
            MoneyText(cop(Sample.LEFT), style = MoneyStyle.Hero)
            LeaderLine("Para", "9 días")
            LeaderLine("Puedes gastar", caption = "lo que queda, menos los fijos que faltan") {
                MoneyText(cop(Sample.PER_DAY))
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Te sobraron",
                    style = MaterialTheme.typography.bodyLarge,
                    color = SelloTheme.colors.inkSoft,
                    modifier = Modifier.weight(1f)
                )
                Stamp("Bajo el límite", StampInk.Good, Modifier.padding(vertical = 6.dp))
            }
            MoneyText(cop(412_300), style = MoneyStyle.Hero)
            Soft("Un mes que ya terminó: no hay cifra por día.")
        }
    }
}

@Composable
private fun Categories(selectedId: String?, onOpen: (String) -> Unit) {
    val nav = LocalNav.current
    Slip(pinked = false) {
        SlipTitle("Tus categorías", "queda en cada una")
        Spacer(Modifier.height(SelloTheme.spacing.sm))
        CategoryGrid(Sample.categories) { index, category ->
            CategoryCell(
                name = category.name,
                icon = category.icon,
                amount = cop(category.left),
                fraction = category.fraction,
                description = "${category.name}. Quedan ${grouped(category.left)} pesos.",
                onClick = { onOpen(category.id) },
                over = category.over,
                staggerIndex = index,
                actions = listOf(
                    LineAction("Anotar aquí") {
                        nav.navigate(AnotarRoute(categoryId = category.id))
                    },
                    LineAction("Editar") { nav.navigate(CategoryEditorRoute(category.id)) },
                    LineAction("Cambiar límite") { nav.navigate(CategoryEditorRoute(category.id)) }
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (category.id ==
                            selectedId
                        ) {
                            SelloTheme.colors.brandSoft
                        } else {
                            SelloTheme.colors.paper
                        },
                        MaterialTheme.shapes.small
                    )
            )
        }
        Spacer(Modifier.height(SelloTheme.spacing.sm))
        SelloButton(
            "Agregar categoría",
            { nav.navigate(CategoryEditorRoute()) },
            kind = ButtonKind.Text,
            icon = SelloIcon.Add
        )
    }
}

@Composable
private fun FirstRun() {
    val nav = LocalNav.current
    Slip {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "Tu recibo de octubre",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            Stamp("En blanco", StampInk.Neutral)
        }
        Soft("Tres pasos y queda listo:")
        Spacer(Modifier.height(SelloTheme.spacing.sm))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Step(1, "Crea tus categorías", false)
            Step(2, "Ponle un límite a cada una", false)
            Step(3, "Anota tu primer gasto", false)
        }
        Spacer(Modifier.height(SelloTheme.spacing.md))
        SelloButton(
            "Crear categorías",
            { nav.navigate(CategoryEditorRoute()) },
            Modifier.fillMaxWidth(),
            icon = SelloIcon.Add
        )
        SelloButton(
            "Traer mis datos de un respaldo",
            { nav.navigate(BackupRoute) },
            Modifier.fillMaxWidth(),
            kind = ButtonKind.Text
        )
    }
}

/** The month sheet: months of this year up to the current one, and "Mes actual". */
@Composable
fun MonthSheet(onDismiss: () -> Unit) {
    val proto = LocalProto.current
    val names =
        listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")
    val long = listOf(
        "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    )
    SelloSheet("Elige el mes", onDismiss, "1 · Hoja del mes") {
        MonthGrid(
            yearLabel = "2026",
            months = names.mapIndexed { i, name ->
                MonthCell(id = "$i", label = name, enabled = i <= 9, isCurrent = i == 9)
            },
            selectedId =
                proto.pastMonth?.let { label ->
                    long.indexOfFirst { label.startsWith(it) }.toString()
                }
                    ?: "9",
            onPick = { id ->
                val i = id.toInt()
                proto.pastMonth = if (i == 9) null else "${long[i]} 2026"
                onDismiss()
            },
            previousYearLabel = "2025",
            nextYearLabel = "2027",
            onPreviousYear = {}
        )
        SelloButton(
            "Mes actual",
            {
                proto.pastMonth = null
                onDismiss()
            },
            Modifier.fillMaxWidth(),
            kind = ButtonKind.Outlined
        )
    }
}

/** Screen 4, on its own route: Back, the tabs and the dock with this category chosen. */
@Composable
fun CategoryScreen(id: String) {
    val nav = LocalNav.current
    val category = Sample.category(id)
    ScreenTag("4 · Detalle de categoría" + (if (LocalProto.current.empty) " · vacío" else ""))
    Shell(
        title = category.name,
        subtitle = "octubre",
        onBack = { nav.popBackStack() },
        dockCategoryId = id,
        actions = {
            ActionIcon(Icons.Rounded.Edit, "Editar categoría") {
                nav.navigate(CategoryEditorRoute(id))
            }
        }
    ) { padding -> CategoryDetail(id, padding) }
}

/** Everything about one category in the month. Also the second pane of a wide Recibo. */
@Composable
fun CategoryDetail(id: String, padding: PaddingValues) {
    val nav = LocalNav.current
    val proto = LocalProto.current
    val category = Sample.category(id)
    Desk(padding) {
        Header(category)
        if (proto.empty) {
            EmptySlip(
                "Sin gastos en octubre",
                "Cuando anotes algo en ${category.name} aparece aquí.",
                actionLabel = "Anotar aquí",
                onAction = { nav.anotar(proto, id) }
            )
            return@Desk
        }
        Slip(pinked = false) {
            SlipHeading("Cada semana gastas") { MoneyText(cop(204_000), style = MoneyStyle.Card) }
            Soft("lo que puedes gastar esta semana, rayado")
            ColumnsChart(
                points = listOf(
                    ChartPoint("s1", "sem 1", 231_000, "Semana 1, 231.000 pesos"),
                    ChartPoint("s2", "sem 2", 198_000, "Semana 2, 198.000 pesos"),
                    ChartPoint("s3", "sem 3", 183_000, "Semana 3, 183.000 pesos"),
                    ChartPoint(
                        "s4",
                        "sem 4",
                        96_000,
                        "Semana 4, 96.000 pesos",
                        ChartPeriod.Current
                    ),
                    ChartPoint("s5", "sem 5", 120_000, "Semana 5, por gastar", ChartPeriod.Upcoming)
                ),
                summary = "Gastos por semana en ${category.name}",
                reference = ChartReference(225_000, "por semana")
            )
        }
        Slip(pinked = false) {
            SlipTitle("En números", "octubre")
            KeyValue("Gastos", "13 en 22 días")
            KeyValue("Cada vez", caption = null) { MoneyText(cop(47_108)) }
            KeyValue("El mayor", caption = "Éxito, mercado quincenal, sáb 3") {
                MoneyText(cop(186_500))
            }
            KeyValue("Tu día más caro", "Sábado")
        }
        Slip(pinked = false) {
            SlipHeading("Un mes normal aquí") { MoneyText(cop(835_000), style = MoneyStyle.Card) }
            ColumnsChart(
                points =
                    listOf(
                        "may" to 790_000L,
                        "jun" to 860_000L,
                        "jul" to 920_000L,
                        "ago" to 810_000L,
                        "sep" to 780_000L
                    )
                        .map { (m, v) ->
                            ChartPoint(
                                m,
                                m,
                                v,
                                "$m, ${grouped(v)} pesos",
                                if (v >
                                    900_000
                                ) {
                                    ChartPeriod.Over
                                } else {
                                    ChartPeriod.Past
                                }
                            )
                        } +
                        ChartPoint(
                            "oct",
                            "oct",
                            612_400,
                            "octubre, 612.400 pesos",
                            ChartPeriod.Current
                        ),
                summary = "Seis meses de ${category.name} frente al límite",
                reference = ChartReference(category.limit, "límite")
            )
        }
        Slip(pinked = false) {
            SlipTitle("Últimos gastos") {
                SelloButton("ver los 13", {
                    nav.navigate(ExpenseListRoute(id))
                }, kind = ButtonKind.Text)
            }
            Sample.expenses.take(4).forEach { expense -> ExpenseLine(expense) }
        }
        SelloButton(
            "Anotar aquí",
            { nav.anotar(proto, id) },
            Modifier.fillMaxWidth(),
            kind = ButtonKind.Outlined,
            icon = SelloIcon.Add
        )
    }
}

@Composable
private fun Header(category: SampleCategory) {
    Slip {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (category.over) "Te pasaste aquí" else "Te quedan aquí",
                style = MaterialTheme.typography.bodyLarge,
                color = SelloTheme.colors.inkSoft,
                modifier = Modifier.weight(1f)
            )
            Stamp(
                if (category.over) "Pasado" else "Al día",
                if (category.over) StampInk.Over else StampInk.Good,
                Modifier.padding(vertical = 6.dp)
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CategoryCircle(category.icon, category.fraction, size = 56.dp, over = category.over)
            MoneyText(
                cop(category.left),
                style = MoneyStyle.Total,
                color = if (category.over) SelloTheme.colors.semantic.loss else SelloTheme.colors.ink
            )
        }
        Soft("Gastado $ ${grouped(category.spent)} de $ ${grouped(category.limit)}")
        Spacer(Modifier.height(SelloTheme.spacing.sm))
        LeaderLine("Para", "9 días")
        LeaderLine("Al día") { MoneyText(cop(31_956)) }
        LeaderLine("Frente a sep", "+6,7 %", valueColor = SelloTheme.colors.semantic.warn)
    }
}

@Composable
private fun ExpenseLine(expense: Sample.Expense) {
    val nav = LocalNav.current
    val proto = LocalProto.current
    ActionLine(
        label = expense.note,
        caption = expense.day,
        onClick = { nav.navigate(AnotarRoute(edit = true)) },
        actions = listOf(
            LineAction("Editar") { nav.navigate(AnotarRoute(edit = true)) },
            LineAction("Borrar") { proto.say("Gasto borrado", undo = true) }
        ),
        swipeAway = LineAction("Borrar") { proto.say("Gasto borrado", undo = true) }
    ) { MoneyText(cop(expense.amount)) }
}

/** "ver los 13": the whole month of one category, sortable and searchable. */
@Composable
fun ExpenseListScreen(id: String) {
    val nav = LocalNav.current
    val category = Sample.category(id)
    var sort by rememberSaveable { mutableIntStateOf(0) }
    var query by rememberSaveable { mutableStateOf("") }
    ScreenTag("4 · Todos los gastos")
    Shell(title = category.name, subtitle = "13 gastos en octubre", onBack = {
        nav.popBackStack()
    }, tabs = false) { padding ->
        val rows = Sample.expenses
            .filter { query.isBlank() || it.note.contains(query, ignoreCase = true) }
            .let { list ->
                when (sort) {
                    1 -> list.sortedByDescending { it.amount }
                    2 -> list.sortedBy { it.note }
                    else -> list
                }
            }
        LazyColumn(
            contentPadding = padding,
            verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.sm)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.sm)) {
                    SegmentedSwitch(listOf("Fecha", "Valor", "Nombre"), sort, { sort = it })
                    FormField("Buscar", query, { query = it })
                }
            }
            item {
                Slip(pinked = false) {
                    if (rows.isEmpty()) Soft("Nada coincide con “$query”.")
                    rows.forEach { ExpenseLine(it) }
                }
            }
        }
    }
}
