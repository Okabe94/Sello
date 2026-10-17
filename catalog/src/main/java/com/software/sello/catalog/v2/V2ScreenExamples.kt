package com.software.sello.catalog.v2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.software.sello.catalog.components.copFixture
import com.software.sello.designsystem.component.ButtonKind
import com.software.sello.designsystem.component.ButtonTone
import com.software.sello.designsystem.component.CategoryCell
import com.software.sello.designsystem.component.LeaderLine
import com.software.sello.designsystem.component.MoneyStyle
import com.software.sello.designsystem.component.MoneyText
import com.software.sello.designsystem.component.SelloButton
import com.software.sello.designsystem.component.SelloDestination
import com.software.sello.designsystem.component.SelloNavigation
import com.software.sello.designsystem.component.SelloNotice
import com.software.sello.designsystem.component.Slip
import com.software.sello.designsystem.component.StampInk
import com.software.sello.designsystem.component.TearLine
import com.software.sello.designsystem.component.v2.AnswerSlip
import com.software.sello.designsystem.component.v2.CelebrationOverlay
import com.software.sello.designsystem.component.v2.CompletedGoalRow
import com.software.sello.designsystem.component.v2.Footnote
import com.software.sello.designsystem.component.v2.GoalCard
import com.software.sello.designsystem.component.v2.GoalPill
import com.software.sello.designsystem.component.v2.GoalsBandTotal
import com.software.sello.designsystem.component.v2.InvestmentRow
import com.software.sello.designsystem.component.v2.MilestoneState
import com.software.sello.designsystem.component.v2.PerformanceLine
import com.software.sello.designsystem.component.v2.SplitBar
import com.software.sello.designsystem.component.v2.SplitSegment
import com.software.sello.designsystem.component.v2.V2Dock
import com.software.sello.designsystem.component.v2.V2MonthBar
import com.software.sello.designsystem.component.v2.V2NavigationBar
import com.software.sello.designsystem.component.v2.V2TitleBar
import com.software.sello.designsystem.component.v2.investmentShade
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.icon.painter
import com.software.sello.designsystem.theme.SelloTheme

/*
 * Whole screens from docs/design/v2/evidence, put together only from existing
 * components so they can be compared with the reference side by side. Synthetic data
 * only.
 */

private val ThreeTabs = listOf(
    SelloDestination("recibo", "Recibo", SelloIcon.ReceiptLong),
    SelloDestination("resumen", "Resumen", SelloIcon.Insights),
    SelloDestination("metas", "Metas", SelloIcon.Flag)
)
private val FourTabs = ThreeTabs +
    SelloDestination("inversiones", "Inversiones", SelloIcon.AccountBalance)

/**
 * The tab shell: the band with its [top] bar, the [content] scrolling over the band's
 * lower edge as in the reference, then the dock and the tab bar. [overlay] floats above
 * the content, where a toast sits.
 */
@Composable
private fun ScreenFrame(
    tabs: List<SelloDestination>,
    selected: String,
    dock: @Composable () -> Unit,
    top: @Composable ColumnScope.() -> Unit,
    overlay: @Composable () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val colors = SelloTheme.colors
    Column(Modifier.fillMaxSize().background(colors.desk)) {
        Box(Modifier.weight(1f)) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Column(Modifier.fillMaxWidth().background(colors.band), content = top)
                Box {
                    Box(Modifier.fillMaxWidth().height(56.dp).background(colors.band))
                    Column(
                        Modifier.padding(horizontal = 12.dp).padding(bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        content = content
                    )
                }
            }
            Box(
                Modifier.align(Alignment.BottomCenter).padding(12.dp)
            ) { overlay() }
        }
        Box(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) { dock() }
        V2NavigationBar(SelloNavigation(tabs, selected) {})
    }
}

@Composable
private fun SettingsAction() {
    IconButton({}) { Icon(SelloIcon.Settings.painter(), "Configuración") }
}

private fun bold(plain: String, strong: String, after: String = "") = buildAnnotatedString {
    append(plain)
    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(strong) }
    append(after)
}

/** Recibo · Default: a payment is reserved. */
@Composable
fun V2ReciboScreen() {
    ScreenFrame(
        ThreeTabs,
        "recibo",
        dock = { V2Dock("Anotar", {}) },
        top = {
            V2MonthBar("Octubre", "2026", {}, description = "Cambiar mes, octubre 2026") {
                SettingsAction()
            }
        }
    ) {
        AnswerSlip("Restante del presupuesto", copFixture(400_000), "Al día", StampInk.Good) {
            TearLine()
            Row(verticalAlignment = Alignment.Bottom) {
                MoneyText(copFixture(40_000), style = MoneyStyle.Card)
                Text(
                    " por día",
                    Modifier.padding(bottom = 4.dp),
                    color = SelloTheme.colors.inkSoft
                )
            }
            Text(
                "10 días, incluido hoy",
                color = SelloTheme.colors.inkSoft,
                style = MaterialTheme.typography.bodySmall
            )
            Footnote(bold("Incluye ", "200.000", " separado para 1 pago previsto"), {})
        }
        GoalPill("Viaje a la costa", "57 %", 0.57f, SelloIcon.Flight, "Aportar", {}, {})
        Slip(Modifier.fillMaxWidth(), pinked = false) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Tus categorías",
                    Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.W800)
                )
                IconButton({}) { Icon(SelloIcon.Add.painter(), "Nueva categoría") }
            }
            listOf(
                listOf(
                    Category("Hogar", SelloIcon.Home, 200_000, 0.5f),
                    Category("Comida", SelloIcon.Restaurant, 120_000, 0.6f)
                ),
                listOf(
                    Category("Transporte", SelloIcon.DirectionsBus, 60_000, 0.6f),
                    Category("Café", SelloIcon.LocalCafe, -10_000, 1.1f, over = true)
                ),
                listOf(
                    Category("Salud", SelloIcon.MedicalServices, 80_000, 0.2f),
                    Category("Ocio", SelloIcon.Theaters, 80_000, 0.2f)
                )
            ).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach {
                        CategoryCell(
                            it.name,
                            it.icon,
                            copFixture(it.left),
                            it.fraction,
                            "${it.name}, quedan ${it.left}",
                            {},
                            Modifier.weight(1f),
                            over = it.over
                        )
                    }
                }
            }
        }
    }
}

private class Category(
    val name: String,
    val icon: SelloIcon,
    val left: Long,
    val fraction: Float,
    val over: Boolean = false
)

@Composable
private fun MetasTop(saved: Long, change: String) {
    V2TitleBar("Metas") {
        IconButton({}) { Icon(SelloIcon.Add.painter(), "Crear meta") }
        SettingsAction()
    }
    GoalsBandTotal("Guardado en metas", copFixture(saved), change)
}

@Composable
private fun ColumnScope.ActiveGoals(includeCelular: Boolean) {
    GoalCard(
        "Viaje a la costa", SelloIcon.Flight, "57 %", copFixture(1_710_000), "de 3.000.000",
        "dic 2026", 0.57f, placesUpTo(2), {}
    )
    if (includeCelular) {
        GoalCard(
            "Celular nuevo", SelloIcon.PhoneIphone, "37 %", copFixture(450_000),
            "de 1.200.000", "Sin fecha", 0.37f, placesUpTo(1), {}, flagged = false
        )
    }
    GoalCard(
        "Curso de inglés", SelloIcon.School, "15 %", copFixture(300_000), "de 2.000.000",
        "jun 2027", 0.15f, placesUpTo(0), {}
    )
}

private fun placesUpTo(earned: Int) = GoalPlaces.mapIndexed { index, place ->
    if (index < earned) {
        place.copy(state = MilestoneState.Earned, description = "${place.label}, logrado")
    } else {
        place.copy(state = MilestoneState.Empty, description = "${place.label}, por alcanzar")
    }
}

@Composable
private fun MetasDock() {
    V2Dock("Aportar a una meta", {}, tone = ButtonTone.Gain, icon = SelloIcon.Flag)
}

/** Metas · the goal list, highest completion first, completed goals last. */
@Composable
fun V2MetasScreen() {
    ScreenFrame(
        ThreeTabs,
        "metas",
        dock = { MetasDock() },
        top = { MetasTop(2_460_000, "350.000 en octubre") }
    ) {
        ActiveGoals(includeCelular = true)
        CompletedGoalRow("Vacunas de Luna", "400.000, 30 sep", SelloIcon.Pets, "Lograda", {})
    }
}

/** Metas · after Listo: back on the list, the toast offers Deshacer. */
@Composable
fun V2MetasUndoScreen() {
    ScreenFrame(
        ThreeTabs,
        "metas",
        dock = { MetasDock() },
        top = { MetasTop(2_010_000, "500.000 en octubre") },
        overlay = {
            SelloNotice(
                "Celular nuevo, meta lograda.",
                actionLabel = "Deshacer",
                onAction = {},
                confirmation = true
            )
        }
    ) {
        ActiveGoals(includeCelular = false)
        CompletedGoalRow("Celular nuevo", "1.200.000, 16 oct", SelloIcon.PhoneIphone, "Lograda", {})
        CompletedGoalRow("Vacunas de Luna", "400.000, 30 sep", SelloIcon.Pets, "Lograda", {})
    }
}

/** Inversiones · overview: the value, its return and where the money is. */
@Composable
fun V2InversionesScreen() {
    val colors = SelloTheme.colors
    ScreenFrame(
        FourTabs,
        "inversiones",
        dock = {
            V2Dock("Anotar movimiento", {}, tone = ButtonTone.Ink)
        },
        top = {
            V2TitleBar("Inversiones") {
                IconButton({}) { Icon(SelloIcon.Add.painter(), "Agregar inversión") }
                SettingsAction()
            }
        }
    ) {
        AnswerSlip("Tus inversiones valen", copFixture(38_038_000), "Gana", StampInk.Good) {
            PerformanceLine("+5,6 %", "+2.066.000 de ganancia", false, Modifier.padding(top = 8.dp))
            Text(
                "En total, desde mar 2025",
                color = colors.inkSoft,
                style = MaterialTheme.typography.bodySmall
            )
            TearLine()
            LeaderLine("Aportaste") { MoneyText(copFixture(37_000_000)) }
            LeaderLine("Retiraste") { MoneyText(copFixture(500_000)) }
            LeaderLine("Dividendos y rentas") { MoneyText(copFixture(528_000)) }
            LeaderLine("Ganancia") {
                MoneyText(copFixture(2_066_000, signed = true), color = colors.semantic.gain)
            }
            Footnote(bold("Valores anotados entre el ", "12 jun", " y el 14 oct"), {})
        }
        Slip(Modifier.fillMaxWidth(), pinked = false) {
            Text("Dónde está, de mayor a menor", color = colors.inkSoft)
            val shades = List(4) { investmentShade(it) }
            SplitBar(
                listOf(
                    SplitSegment(0.5f, "Parqueadero, 19.000.000 pesos", shades[0]),
                    SplitSegment(0.28f, "CDT Banco, 10.746.000 pesos", shades[1]),
                    SplitSegment(0.14f, "Fondo de acciones, 5.242.000 pesos", shades[2]),
                    SplitSegment(0.08f, "Ecopetrol, 3.050.000 pesos", shades[3])
                ),
                Modifier.padding(vertical = 8.dp)
            )
            Investment(
                "Parqueadero",
                "Finca raíz · valor del 12 jun",
                SelloIcon.Home,
                shades[0],
                19_000_000,
                "+7,5 %"
            )
            Investment(
                "CDT Banco",
                "CDT a 360 días · valor del 30 sep",
                SelloIcon.AccountBalance,
                shades[1],
                10_746_000,
                "+7,5 %"
            )
            Investment(
                "Fondo de acciones",
                "Fondo colectivo · valor del 14 oct",
                SelloIcon.Insights,
                shades[2],
                5_242_000,
                "−4,3 %",
                loss = true
            )
            Investment(
                "Ecopetrol",
                "Acciones · valor del 9 oct",
                SelloIcon.TrendingUp,
                shades[3],
                3_050_000,
                "+7,6 %",
                last = true
            )
        }
    }
}

@Composable
private fun Investment(
    name: String,
    detail: String,
    icon: SelloIcon,
    shade: Color,
    value: Long,
    change: String,
    loss: Boolean = false,
    last: Boolean = false
) {
    InvestmentRow(
        name, detail, icon, shade, copFixture(value), "Sin valor", change, {},
        loss = loss,
        divider = !last
    )
}

/** Celebration · Lograda (live): opens over the whole window; Listo closes it. */
@Composable
fun V2CelebrationScreen() {
    var shown by rememberSaveable { mutableStateOf(true) }
    Column(
        Modifier.fillMaxSize().padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Caption("La celebración cubre toda la ventana, como en la referencia.")
        SelloButton("Mostrar celebración", { shown = true }, kind = ButtonKind.Outlined)
    }
    if (shown) {
        CelebrationOverlay(
            "¡Lo lograste!",
            "Meta lograda",
            "Vacunas de Luna",
            SelloIcon.Pets,
            copFixture(400_000),
            "30 sep 2026 · 4 aportes",
            placesUpTo(4),
            "Listo",
            "Ver mi meta",
            { shown = false },
            { shown = false }
        )
    }
}
