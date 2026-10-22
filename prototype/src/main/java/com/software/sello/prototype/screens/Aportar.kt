package com.software.sello.prototype.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.software.sello.designsystem.component.ButtonKind
import com.software.sello.designsystem.component.ButtonTone
import com.software.sello.designsystem.component.ChipFlow
import com.software.sello.designsystem.component.EntryAmountSlip
import com.software.sello.designsystem.component.FieldPill
import com.software.sello.designsystem.component.LeaderLine
import com.software.sello.designsystem.component.MoneyStyle
import com.software.sello.designsystem.component.MoneyText
import com.software.sello.designsystem.component.SelloButton
import com.software.sello.designsystem.component.SelloChip
import com.software.sello.designsystem.component.SelloScaffold
import com.software.sello.designsystem.component.Slip
import com.software.sello.designsystem.component.Stamp
import com.software.sello.designsystem.component.StampInk
import com.software.sello.designsystem.component.StampSize
import com.software.sello.designsystem.component.TearLine
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.theme.SelloTheme
import com.software.sello.prototype.AportarRoute
import com.software.sello.prototype.AporteReceiptRoute
import com.software.sello.prototype.CloseButton
import com.software.sello.prototype.DiscardGuard
import com.software.sello.prototype.EntryControls
import com.software.sello.prototype.EntryLayout
import com.software.sello.prototype.GoalDoneRoute
import com.software.sello.prototype.GoalEditorRoute
import com.software.sello.prototype.GoalRoute
import com.software.sello.prototype.LocalNav
import com.software.sello.prototype.Milestones
import com.software.sello.prototype.ReceiptDesk
import com.software.sello.prototype.Sample
import com.software.sello.prototype.ScreenTag
import com.software.sello.prototype.Soft
import com.software.sello.prototype.cop
import com.software.sello.prototype.grouped
import com.software.sello.prototype.rememberDraft

/** Screen 15. Put money toward a goal; quick amounts sit above the keys. */
@Composable
fun AportarScreen(route: AportarRoute) {
    val nav = LocalNav.current
    val goal = Sample.goal(route.id)
    val draft = rememberDraft(route.amount ?: 818_750)
    val leave = DiscardGuard(dirty = false, question = "") { nav.popBackStack() }
    val remaining = goal.target - goal.saved
    ScreenTag("15 · Aportar")
    SelloScaffold(title = "Aportar", subtitle = goal.name, navigationIcon = {
        CloseButton(leave)
    }) { padding ->
        EntryLayout(
            padding = padding,
            draft = {
                EntryAmountSlip(
                    label = "vie 2 oct, comprobante · ${goal.percent} % hoy",
                    amount = draft.digits.toLongOrNull()?.let { cop(it) },
                    tone = ButtonTone.Gain
                )
                Slip {
                    LeaderLine("Tendrás") { MoneyText(cop(goal.saved + draft.amount)) }
                    LeaderLine("Próximo sello, 75 %") {
                        MoneyText(
                            cop((goal.target * 3 / 4 - goal.saved - draft.amount).coerceAtLeast(0))
                        )
                    }
                }
                ChipFlow {
                    listOf(
                        818_750L to "lo de siempre",
                        500_000L to null,
                        1_000_000L to null,
                        491_250L to "todo lo libre"
                    ).forEach { (amount, note) ->
                        SelloChip(
                            listOfNotNull(grouped(amount), note).joinToString(" · "),
                            draft.amount == amount,
                            { draft.digits = amount.toString() }
                        )
                    }
                    SelloChip("Lo que falta", draft.amount == remaining, {
                        draft.digits =
                            remaining.toString()
                    })
                }
                ChipFlow {
                    FieldPill("Hoy, vie 2", SelloIcon.Event, {})
                    FieldPill("Nota", SelloIcon.EditNote, {}, placeholder = true)
                    FieldPill("Se repite cada mes", SelloIcon.Event, {})
                }
            },
            controls = {
                EntryControls(draft, "Aportar", enabled = draft.amount > 0, missing = null, onSubmit = {
                    nav.popBackStack()
                    if (draft.amount >= remaining) {
                        nav.navigate(GoalDoneRoute(goal.id))
                    } else {
                        nav.navigate(AporteReceiptRoute(goal.id, draft.amount))
                    }
                })
            }
        )
    }
}

/** The contribution receipt, with the round stamp when a quarter is crossed. */
@Composable
fun AporteReceiptScreen(route: AporteReceiptRoute) {
    val nav = LocalNav.current
    val goal = Sample.goal(route.id)
    val after = goal.saved + route.amount
    val crossed = after >= goal.target * 3 / 4
    ScreenTag(if (crossed) "15 · Aporte guardado · llega al 75 %" else "15 · Aporte guardado")
    SelloScaffold(title = "Aporte guardado", navigationIcon = {
        CloseButton { nav.popBackStack() }
    }) { padding ->
        ReceiptDesk(padding) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Comprobante de aporte", style = MaterialTheme.typography.titleMedium)
                    Soft("${goal.name} · vie 2 oct 2026")
                }
                Stamp(
                    if (crossed) "75 %" else "Aportado",
                    StampInk.Done,
                    size = StampSize.Large,
                    land = true
                )
            }
            MoneyText(
                cop(route.amount, signed = true),
                style = MoneyStyle.Total,
                color = SelloTheme.colors.brand
            )
            TearLine()
            LeaderLine("Tenías") { MoneyText(cop(goal.saved)) }
            LeaderLine("Ahora tienes") { MoneyText(cop(after), style = MoneyStyle.Title) }
            Soft(
                "${(after * 100 / goal.target)} % de $ ${grouped(
                    goal.target
                )} · llegas en ${goal.by}"
            )
            TearLine()
            Text(
                if (crossed) "Llegaste al 75 %" else "Con 4 aportes más llega el sello del 75 %",
                style = MaterialTheme.typography.bodyLarge
            )
            SelloButton("Listo", { nav.popBackStack() }, Modifier.fillMaxWidth())
            SelloButton(
                "Ver la meta",
                {
                    nav.popBackStack()
                    nav.navigate(GoalRoute(goal.id))
                },
                Modifier.fillMaxWidth(),
                kind = ButtonKind.Text
            )
        }
    }
}

/** "Meta lograda": all four stamps, then the heavy one. */
@Composable
fun GoalDoneScreen(id: String) {
    val nav = LocalNav.current
    val goal = Sample.goal(id).let { g ->
        if (g.done) g else g.copy(milestones = g.milestones.map { it.copy(reached = true) })
    }
    ScreenTag("15 · Meta lograda")
    SelloScaffold(title = "", navigationIcon = { CloseButton { nav.popBackStack() } }) { padding ->
        ReceiptDesk(padding) {
            Text("Meta lograda", style = MaterialTheme.typography.headlineSmall)
            Text(goal.name, style = MaterialTheme.typography.titleMedium)
            Stamp(
                "Lograda",
                StampInk.Done,
                Modifier.align(Alignment.CenterHorizontally).padding(vertical = 12.dp),
                size = StampSize.Hero,
                land = true
            )
            MoneyText(cop(goal.target), style = MoneyStyle.Total)
            Soft("completos el 30 sep 2026")
            TearLine()
            Milestones(goal.milestones)
            TearLine()
            LeaderLine("Aportes", "8 de 300.000")
            LeaderLine("Tiempo", "8 meses (feb a sep)")
            LeaderLine("Lo lograste", "a tiempo", valueColor = SelloTheme.colors.semantic.gain)
            SelloButton("Usar la plata", { nav.popBackStack() }, Modifier.fillMaxWidth())
            SelloButton(
                "Empezar otra meta",
                {
                    nav.popBackStack()
                    nav.navigate(GoalEditorRoute())
                },
                Modifier.fillMaxWidth(),
                kind = ButtonKind.Text
            )
        }
    }
}
