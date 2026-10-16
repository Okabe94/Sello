package com.software.sello.prototype.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Edit
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
import androidx.compose.ui.unit.dp
import com.software.sello.designsystem.component.ActionLine
import com.software.sello.designsystem.component.Blotter
import com.software.sello.designsystem.component.ButtonKind
import com.software.sello.designsystem.component.ChartPeriod
import com.software.sello.designsystem.component.ChartPoint
import com.software.sello.designsystem.component.ChartReference
import com.software.sello.designsystem.component.ChipFlow
import com.software.sello.designsystem.component.ColumnsChart
import com.software.sello.designsystem.component.EmptySlip
import com.software.sello.designsystem.component.FormField
import com.software.sello.designsystem.component.KeyValue
import com.software.sello.designsystem.component.LeaderLine
import com.software.sello.designsystem.component.LineAction
import com.software.sello.designsystem.component.LineChart
import com.software.sello.designsystem.component.MoneyStyle
import com.software.sello.designsystem.component.MoneyText
import com.software.sello.designsystem.component.SegmentedSwitch
import com.software.sello.designsystem.component.SelloButton
import com.software.sello.designsystem.component.SelloChip
import com.software.sello.designsystem.component.Slip
import com.software.sello.designsystem.component.SlipHeading
import com.software.sello.designsystem.component.Stamp
import com.software.sello.designsystem.component.StampInk
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.theme.SelloTheme
import com.software.sello.prototype.ActionIcon
import com.software.sello.prototype.Answer
import com.software.sello.prototype.AportarRoute
import com.software.sello.prototype.Bar
import com.software.sello.prototype.Desk
import com.software.sello.prototype.GoalDoneRoute
import com.software.sello.prototype.GoalEditorRoute
import com.software.sello.prototype.GoalRoute
import com.software.sello.prototype.InvestmentEditorRoute
import com.software.sello.prototype.InvestmentRoute
import com.software.sello.prototype.LocalNav
import com.software.sello.prototype.LocalProto
import com.software.sello.prototype.Milestones
import com.software.sello.prototype.MovementRoute
import com.software.sello.prototype.Overflow
import com.software.sello.prototype.Sample
import com.software.sello.prototype.ScreenTag
import com.software.sello.prototype.SelloSheet
import com.software.sello.prototype.Shell
import com.software.sello.prototype.SlipTitle
import com.software.sello.prototype.Soft
import com.software.sello.prototype.VariosRoute
import com.software.sello.prototype.cop
import com.software.sello.prototype.grouped

/** Screens 9 and 13. The third tab: investments and goals behind one switch. */
@Composable
fun PatrimonioScreen() {
    val nav = LocalNav.current
    val proto = LocalProto.current
    val goals = proto.patrimonioGoals
    ScreenTag((if (goals) "13 · Patrimonio › Metas" else "9 · Patrimonio › Inversiones") + (if (proto.empty) " · vacío" else ""))
    Shell(
        title = "Patrimonio",
        blotter = Blotter.Tall,
        actions = {
            ActionIcon(Icons.Rounded.AddCircle, if (goals) "Nueva meta" else "Nueva inversión") {
                nav.navigate(if (goals) GoalEditorRoute() else InvestmentEditorRoute())
            }
        }
    ) { padding ->
        Desk(padding) {
            SegmentedSwitch(listOf("Inversiones", "Metas"), if (goals) 1 else 0, {
                proto.patrimonioGoals =
                    it == 1
            })
            if (goals) Goals() else Investments()
        }
    }
}

@Composable
private fun Investments() {
    val nav = LocalNav.current
    val proto = LocalProto.current
    var rates by rememberSaveable { mutableStateOf(false) }
    if (proto.empty) {
        EmptySlip(
            "Aún no registras inversiones",
            "Anota lo que tienes y cuánto vale; aquí verás cómo crece.",
            actionLabel = "Agregar inversión",
            onAction = { nav.navigate(InvestmentEditorRoute()) }
        )
        return
    }
    Slip {
        Answer("Tus inversiones valen", cop(48_701_000))
        LeaderLine("Has ganado") {
            MoneyText(cop(3_501_000, signed = true), color = SelloTheme.colors.semantic.gain)
        }
        LeaderLine("Rendimiento", "+7,7 %", valueColor = SelloTheme.colors.semantic.gain)
        ValueChart("Valor y aportado, últimos doce meses")
        Soft("valor · aportado, 45,2 M")
        SelloButton("Incluye USD a $ 4.050", { rates = true }, kind = ButtonKind.Text)
    }
    Column(Modifier.padding(horizontal = 4.dp)) { SlipTitle("Cinco inversiones", "de mayor a menor") }
    Sample.investments.forEach { inv ->
        Ticket(inv) { nav.navigate(InvestmentRoute(inv.id)) }
    }
    if (rates) {
        SelloSheet("Tasas de cambio", { rates = false }, "9 · Hoja de tasas") {
            LeaderLine("USD", "$ 4.050")
            LeaderLine("EUR", "$ 4.390")
            LeaderLine("GBP", "$ 5.120")
            Soft("Actualizadas hoy, 8:12")
            SelloButton("Actualizar", {
                rates = false
            }, Modifier.fillMaxWidth(), kind = ButtonKind.Outlined)
            SelloButton("Usar mis propias tasas", {
                rates = false
            }, Modifier.fillMaxWidth(), kind = ButtonKind.Text)
        }
    }
}

/** One investment as a ticket: return, name, value and gain. */
@Composable
private fun Ticket(inv: Sample.Investment, onClick: () -> Unit) {
    val gain = inv.gain >= 0
    Slip(pinked = false, onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(inv.name, style = MaterialTheme.typography.titleMedium)
                Soft(inv.kind)
            }
            Stamp(
                "${inv.percent} ${if (gain) "gana" else "pierde"}",
                if (gain) StampInk.Good else StampInk.Over
            )
        }
        Row(verticalAlignment = Alignment.Bottom) {
            MoneyText(cop(inv.value), style = MoneyStyle.Title, modifier = Modifier.weight(1f))
            MoneyText(
                cop(inv.gain, signed = true),
                color = if (gain) SelloTheme.colors.semantic.gain else SelloTheme.colors.semantic.loss
            )
        }
    }
}

@Composable
private fun ValueChart(summary: String) {
    val months =
        listOf("nov", "dic", "ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct")
    LineChart(
        points = months.mapIndexed { i, m ->
            val v = 40_000_000L + i * 790_000L + (if (i % 3 == 0) -600_000 else 0)
            ChartPoint(
                m,
                if (i % 3 ==
                    0
                ) {
                    m
                } else {
                    ""
                },
                v,
                "$m, ${grouped(v)} pesos",
                if (i ==
                    11
                ) {
                    ChartPeriod.Current
                } else {
                    ChartPeriod.Past
                }
            )
        },
        summary = summary,
        reference = ChartReference(45_200_000, "aportado")
    )
}

/** Screen 10. What one investment has done. */
@Composable
fun InvestmentScreen(id: String) {
    val nav = LocalNav.current
    val proto = LocalProto.current
    val inv = Sample.investment(id)
    var range by rememberSaveable { mutableIntStateOf(1) }
    var benchmark by rememberSaveable { mutableStateOf(false) }
    ScreenTag("10 · Detalle de inversión")
    Shell(
        title = inv.name,
        subtitle = if (inv.id == "etf") "VOO, en dólares" else inv.kind,
        onBack = { nav.popBackStack() },
        actions = {
            ActionIcon(Icons.Rounded.Edit, "Editar inversión") {
                nav.navigate(InvestmentEditorRoute(id))
            }
            Overflow("Anotar varios movimientos" to { nav.navigate(VariosRoute(movements = true)) })
        }
    ) { padding ->
        Desk(padding) {
            Slip {
                Answer("Vale hoy", cop(inv.value), stamp = {
                    Stamp("Le gana al CDT", StampInk.Good)
                })
                LeaderLine("Has ganado") {
                    MoneyText(cop(inv.gain, signed = true), color = SelloTheme.colors.semantic.gain)
                }
                LeaderLine("Rendimiento", inv.percent, valueColor = SelloTheme.colors.semantic.gain)
                SegmentedSwitch(listOf("6 M", "1 A", "Todo"), range, { range = it })
                ValueChart("Valor, un CDT de referencia y lo aportado")
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Soft("9,63 M valor")
                    SelloButton("8,95 M en un CDT", { benchmark = true }, kind = ButtonKind.Text)
                    Soft("8,20 M aportado")
                }
            }
            Slip(Modifier.fillMaxWidth(), pinked = false) {
                KeyValue("Aportado") { MoneyText(cop(8_200_000)) }
                KeyValue("Ganancia") { MoneyText(cop(1_435_000, signed = true)) }
                KeyValue("Dividendos recibidos") { MoneyText(cop(96_400)) }
                KeyValue("En dólares", "USD 2.340")
                KeyValue("Desde", "mar 2025")
                KeyValue("Aporte fijo", "300.000 al mes")
            }
            Slip(pinked = false) {
                SlipHeading("Meta de valor") { MoneyText(cop(12_000_000), style = MoneyStyle.Card) }
                Bar(0.8f)
                LeaderLine("80 %", "Faltan $ 2.365.000")
                LeaderLine("Llegas", "abr 2027 a este ritmo")
            }
            Slip(pinked = false) {
                SlipTitle("Movimientos") {
                    SelloButton("ver los 23", {
                        proto.say("Aquí se abre la lista completa")
                    }, kind = ButtonKind.Text)
                }
                Sample.movements.forEach { m ->
                    ActionLine(
                        label = m.type,
                        caption = m.day,
                        onClick = { nav.navigate(MovementRoute(id)) },
                        actions = listOf(
                            LineAction("Editar") { nav.navigate(MovementRoute(id)) },
                            LineAction("Borrar") { proto.say("Movimiento borrado", undo = true) }
                        ),
                        swipeAway = LineAction("Borrar") {
                            proto.say("Movimiento borrado", undo = true)
                        }
                    ) { MoneyText(cop(m.amount, signed = m.type != "Valor actual")) }
                }
            }
            SelloButton("Anotar movimiento", {
                nav.navigate(MovementRoute(id))
            }, Modifier.fillMaxWidth(), icon = SelloIcon.Add)
        }
    }
    if (benchmark) {
        var choice by rememberSaveable { mutableIntStateOf(1) }
        SelloSheet("Comparar con", { benchmark = false }, "10 · Hoja de comparación") {
            ChipFlow {
                listOf("Nada", "Un CDT", "Inflación más un margen").forEachIndexed { i, label ->
                    SelloChip(label, i == choice, { choice = i })
                }
            }
            if (choice >
                0
            ) {
                FormField(
                    if (choice ==
                        1
                    ) {
                        "Tasa del CDT"
                    } else {
                        "Margen sobre la inflación"
                    },
                    "10,8 %",
                    {}
                )
            }
            SelloButton("Listo", { benchmark = false }, Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun Goals() {
    val nav = LocalNav.current
    val proto = LocalProto.current
    if (proto.empty) {
        Slip {
            Text("Ponle nombre a algo que quieras.", style = MaterialTheme.typography.headlineSmall)
            Milestones(
                listOf("25 %", "50 %", "75 %", "Meta").map {
                    Sample.Milestone(it, "", false)
                }
            )
            SelloButton("Crear una meta", {
                nav.navigate(GoalEditorRoute())
            }, Modifier.fillMaxWidth())
        }
        return
    }
    Slip {
        Answer("Guardado en metas", cop(13_050_000))
        LeaderLine("En octubre") { MoneyText(cop(1_318_750, signed = true)) }
    }
    Sample.goals.forEach { goal ->
        if (goal.done) {
            Slip(pinked = false, onClick = { nav.navigate(GoalDoneRoute(goal.id)) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(goal.name, style = MaterialTheme.typography.titleMedium)
                        Soft("$ ${grouped(goal.target)}, ${goal.by}")
                    }
                    Stamp("Lograda", StampInk.Done)
                }
            }
        } else {
            Slip(onClick = { nav.navigate(GoalRoute(goal.id)) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        goal.name,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        "${goal.percent} %",
                        style = MaterialTheme.typography.titleMedium,
                        color = SelloTheme.colors.brand
                    )
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    MoneyText(
                        cop(goal.saved),
                        style = MoneyStyle.Title,
                        modifier = Modifier.weight(1f)
                    )
                    Soft("de $ ${grouped(goal.target)} · ${goal.by}")
                }
                Bar(goal.percent / 100f)
                Milestones(goal.milestones, Modifier.padding(top = 8.dp))
                SelloButton("Aportar", {
                    nav.navigate(AportarRoute(goal.id))
                }, kind = ButtonKind.Text, icon = SelloIcon.Add)
            }
        }
    }
}

/** Goal detail: the four stamps, when it arrives, and its contributions. */
@Composable
fun GoalScreen(id: String) {
    val nav = LocalNav.current
    val goal = Sample.goal(id)
    ScreenTag("13 · Detalle de meta")
    Shell(
        title = goal.name,
        onBack = { nav.popBackStack() },
        actions = {
            ActionIcon(Icons.Rounded.Edit, "Editar meta") { nav.navigate(GoalEditorRoute(id)) }
        }
    ) { padding ->
        Desk(padding) {
            Slip {
                Answer("Ya guardaste", cop(goal.saved), stamp = {
                    Stamp("${goal.percent} %", StampInk.Done)
                })
                Soft("de $ ${grouped(goal.target)} · faltan $ ${grouped(goal.target - goal.saved)}")
                Bar(goal.percent / 100f)
            }
            Slip(pinked = false) {
                SlipTitle("Sellos", "${goal.milestones.count { it.reached }} de 4")
                Milestones(goal.milestones)
                Soft("Próximo sello: 75 %, faltan $ 2.750.000")
            }
            Slip(Modifier.fillMaxWidth(), pinked = false) {
                KeyValue("Llegas en", goal.by)
                KeyValue("Al mes", caption = "se repite") { MoneyText(cop(818_750)) }
                KeyValue("Desde", "oct 2025")
            }
            Slip(pinked = false) {
                SlipHeading("Cada mes aportas más") {
                    MoneyText(cop(818_750), style = MoneyStyle.Card)
                }
                Soft("miles por mes")
                ColumnsChart(
                    points = listOf(
                        "may" to 600,
                        "jun" to 650,
                        "jul" to 700,
                        "ago" to 750,
                        "sep" to 800,
                        "oct" to 819
                    ).map { (m, v) ->
                        ChartPoint(
                            m,
                            m,
                            v * 1_000L,
                            "$m, $v mil pesos",
                            if (m ==
                                "oct"
                            ) {
                                ChartPeriod.Current
                            } else {
                                ChartPeriod.Past
                            }
                        )
                    },
                    summary = "Aportes de los últimos seis meses"
                )
            }
            SelloButton("Aportar a esta meta", {
                nav.navigate(AportarRoute(id))
            }, Modifier.fillMaxWidth(), icon = SelloIcon.Add)
            Slip(pinked = false) {
                SlipTitle("Aportes", "ver los 13")
                LeaderLine("Octubre", caption = "vie 2 oct, se repite") {
                    MoneyText(cop(818_750, signed = true))
                }
                LeaderLine("Septiembre", caption = "mié 2 sep") {
                    MoneyText(cop(800_000, signed = true))
                }
            }
        }
    }
}
