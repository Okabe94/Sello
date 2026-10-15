package com.software.sello.prototype.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Map
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
import androidx.compose.ui.window.Dialog
import com.software.sello.designsystem.component.ButtonKind
import com.software.sello.designsystem.component.ChipFlow
import com.software.sello.designsystem.component.ConfirmSlip
import com.software.sello.designsystem.component.ErrorSlip
import com.software.sello.designsystem.component.FieldMessage
import com.software.sello.designsystem.component.FormField
import com.software.sello.designsystem.component.KeyValue
import com.software.sello.designsystem.component.SegmentedSwitch
import com.software.sello.designsystem.component.SelloButton
import com.software.sello.designsystem.component.SelloChip
import com.software.sello.designsystem.component.Slip
import com.software.sello.designsystem.component.Stamp
import com.software.sello.designsystem.component.StampInk
import com.software.sello.designsystem.component.StampSize
import com.software.sello.designsystem.component.SwitchRow
import com.software.sello.designsystem.theme.SelloInk
import com.software.sello.prototype.AnotarRoute
import com.software.sello.prototype.AportarRoute
import com.software.sello.prototype.AporteReceiptRoute
import com.software.sello.prototype.BackupRoute
import com.software.sello.prototype.CategoryEditorRoute
import com.software.sello.prototype.CategoryRoute
import com.software.sello.prototype.Desk
import com.software.sello.prototype.ExpenseListRoute
import com.software.sello.prototype.GoalDoneRoute
import com.software.sello.prototype.GoalEditorRoute
import com.software.sello.prototype.GoalRoute
import com.software.sello.prototype.IncomeReceiptRoute
import com.software.sello.prototype.IncomeRoute
import com.software.sello.prototype.InvestmentEditorRoute
import com.software.sello.prototype.InvestmentRoute
import com.software.sello.prototype.LinkRow
import com.software.sello.prototype.LocalNav
import com.software.sello.prototype.LocalProto
import com.software.sello.prototype.MapRoute
import com.software.sello.prototype.MovementRoute
import com.software.sello.prototype.PatrimonioTab
import com.software.sello.prototype.ReciboRoute
import com.software.sello.prototype.ReciboTab
import com.software.sello.prototype.RecurringRoute
import com.software.sello.prototype.ResumenTab
import com.software.sello.prototype.ScreenTag
import com.software.sello.prototype.SettingsRoute
import com.software.sello.prototype.Shell
import com.software.sello.prototype.SlipTitle
import com.software.sello.prototype.Soft
import com.software.sello.prototype.ThemeMode
import com.software.sello.prototype.VariosRoute
import com.software.sello.prototype.jumpTo
import kotlinx.coroutines.delay

/** Screen 16. Reached from the gear on Recibo. */
@Composable
fun SettingsScreen() {
    val nav = LocalNav.current
    val proto = LocalProto.current
    var order by rememberSaveable { mutableIntStateOf(0) }
    var descending by rememberSaveable { mutableIntStateOf(0) }
    var currency by rememberSaveable { mutableIntStateOf(0) }
    var listOrder by rememberSaveable { mutableIntStateOf(0) }
    var ownRates by rememberSaveable { mutableStateOf(false) }
    var alerts by rememberSaveable { mutableStateOf(true) }
    var export by rememberSaveable { mutableStateOf(false) }
    var wipeStep by rememberSaveable { mutableIntStateOf(0) }
    ScreenTag("16 · Configuración")
    Shell(title = "Configuración", onBack = { nav.popBackStack() }, tabs = false) { padding ->
        Desk(padding) {
            Slip(pinked = false) {
                SlipTitle("Apariencia")
                Soft("Tema")
                SegmentedSwitch(ThemeMode.entries.map { it.name }, proto.theme.ordinal, {
                    proto.theme =
                        ThemeMode.entries[it]
                })
                Soft("Tinta")
                ChipFlow {
                    SelloInk.entries.forEach { ink ->
                        SelloChip(ink.name, proto.ink == ink, { proto.ink = ink })
                    }
                }
            }
            Slip(pinked = false) {
                SlipTitle("Recibo", "orden de las categorías")
                ChipFlow {
                    listOf(
                        "Más usadas",
                        "Alfabético",
                        "Monto gastado",
                        "Límite",
                        "Última actualización"
                    )
                        .forEachIndexed { i, label -> SelloChip(label, i == order, { order = i }) }
                }
                SegmentedSwitch(listOf("Descendente", "Ascendente"), descending, {
                    descending = it
                })
            }
            Slip(pinked = false) {
                SlipTitle("Inversiones")
                Soft("Moneda al crear")
                SegmentedSwitch(listOf("COP", "USD", "EUR", "GBP"), currency, { currency = it })
                Soft("Orden de la lista")
                ChipFlow {
                    listOf(
                        "Mayor valor",
                        "Rendimiento",
                        "Alfabético",
                        "Más reciente",
                        "Última actualización"
                    )
                        .forEachIndexed { i, label ->
                            SelloChip(label, i == listOrder, {
                                listOrder =
                                    i
                            })
                        }
                }
                SwitchRow("Usar mis propias tasas", ownRates, {
                    ownRates = it
                }, subtitle = "Actualizadas hoy, 8:12")
                listOf(
                    "USD" to "4.050",
                    "EUR" to "4.390",
                    "GBP" to "5.120"
                ).forEach { (code, rate) ->
                    FormField(code, rate, {}, enabled = ownRates)
                }
            }
            Slip(pinked = false) {
                SlipTitle("Avisos")
                SwitchRow(
                    "Alertas de presupuesto",
                    alerts,
                    { alerts = it },
                    subtitle = "Cuando una categoría se acerca a su límite o se pasa"
                )
            }
            Slip(pinked = false) {
                SlipTitle("Tus datos")
                LinkRow("Lo que se repite", "5 gastos, 1 ingreso", Icons.Rounded.Autorenew) {
                    nav.navigate(RecurringRoute)
                }
                LinkRow("Respaldo y restauración", "hace 6 días", Icons.Rounded.Backup) {
                    nav.navigate(BackupRoute)
                }
                LinkRow("Exportar a CSV o JSON", icon = Icons.Rounded.Download) { export = true }
                LinkRow(
                    "Borrar todos los datos",
                    "Pide confirmación dos veces",
                    Icons.Rounded.DeleteForever
                ) {
                    wipeStep =
                        1
                }
            }
            Soft("Sello · tus datos no salen de este teléfono", Modifier.padding(horizontal = 4.dp))
            Slip(pinked = false) {
                SlipTitle("Prototipo", "no es parte del diseño")
                LinkRow(
                    "Mapa de pantallas",
                    "Salta a cualquier pantalla o estado",
                    Icons.Rounded.Map
                ) {
                    nav.navigate(MapRoute)
                }
                SwitchRow("Simular primer uso", proto.firstRun, {
                    proto.firstRun = it
                }, subtitle = "Recibo sin categorías")
                SwitchRow("Mostrar estados vacíos", proto.empty, {
                    proto.empty = it
                }, subtitle = "Resumen, Ingresos, Patrimonio…")
            }
        }
    }
    if (export) ExportSheet { export = false }
    when (wipeStep) {
        1 -> ConfirmSlip(
            title = "¿Borrar todos los datos?",
            message = AnnotatedString(
                "Se borran 10 categorías, 412 gastos, 23 ingresos, 3 metas con 29 aportes y 5 inversiones con 61 movimientos."
            ),
            confirmLabel = "Seguir",
            onConfirm = { wipeStep = 2 },
            onDismiss = { wipeStep = 0 }
        )

        2 -> TypeToConfirm(
            onConfirm = {
                wipeStep = 0
                proto.firstRun = true
                nav.popBackStack(ReciboRoute, inclusive = false)
            },
            onDismiss = { wipeStep = 0 }
        )
    }
}

/** The second confirmation: type BORRAR. */
@Composable
private fun TypeToConfirm(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    var typed by rememberSaveable { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Slip {
            Text("Escribe BORRAR para confirmar", style = MaterialTheme.typography.titleMedium)
            Soft("No se puede deshacer.")
            FormField(
                "Confirmación",
                typed,
                { typed = it },
                message = FieldMessage("Escribe la palabra exacta", isError = false)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SelloButton("Cancelar", onDismiss, Modifier.weight(1f), kind = ButtonKind.Outlined)
                SelloButton(
                    "Borrar",
                    onConfirm,
                    Modifier.weight(1f),
                    kind = ButtonKind.Destructive,
                    enabled = typed.trim() == "BORRAR"
                )
            }
        }
    }
}

private enum class BackupStep { Idle, Creating, Created, Confirming, Restored, Broken }

/** Screen 17. When the last backup was made, and restoring from a file. */
@Composable
fun BackupScreen() {
    val nav = LocalNav.current
    val proto = LocalProto.current
    var step by rememberSaveable { mutableStateOf(BackupStep.Idle) }
    LaunchedEffect(step) {
        if (step == BackupStep.Creating) {
            delay(1_200)
            step = BackupStep.Created
        }
    }
    ScreenTag("17 · Respaldo y restauración")
    Shell(title = "Respaldo", onBack = { nav.popBackStack() }, tabs = false) { padding ->
        Desk(padding) {
            Slip {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Soft("Último respaldo")
                        Text(
                            if (step == BackupStep.Created) "22 oct 2026" else "2 oct 2026",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    if (step == BackupStep.Created || step == BackupStep.Idle) {
                        Stamp("Guardado", StampInk.Good, land = step == BackupStep.Created)
                    }
                }
                KeyValue("Categorías", "10")
                KeyValue("Gastos", "412")
                KeyValue("Ingresos", "23")
                KeyValue("Metas y aportes", "3 y 29")
                KeyValue("Inversiones y movimientos", "5 y 61")
                KeyValue("Archivo", "148 KB")
                SelloButton(
                    if (step == BackupStep.Creating) "Creando…" else "Crear respaldo ahora",
                    { step = BackupStep.Creating },
                    Modifier.fillMaxWidth(),
                    loading = step == BackupStep.Creating
                )
            }
            Slip(pinked = false) {
                SlipTitle("Restaurar")
                Soft("Elige un archivo de respaldo. Reemplaza todo lo que hay en este teléfono.")
                if (step == BackupStep.Restored) {
                    Stamp("Restaurado", StampInk.Done, size = StampSize.Large, land = true)
                    SelloButton(
                        "Ir al recibo",
                        {
                            proto.firstRun = false
                            nav.popBackStack(ReciboRoute, inclusive = false)
                        },
                        Modifier.fillMaxWidth()
                    )
                } else {
                    SelloButton("Elegir archivo", {
                        step = BackupStep.Confirming
                    }, Modifier.fillMaxWidth(), kind = ButtonKind.Outlined)
                    SelloButton("Probar con un archivo dañado", {
                        step = BackupStep.Broken
                    }, kind = ButtonKind.Text)
                }
            }
            if (step == BackupStep.Broken) {
                ErrorSlip(
                    "Ese archivo no es un respaldo de Sello. Nada cambió.",
                    onRetry = { step = BackupStep.Idle }
                )
            }
        }
    }
    if (step == BackupStep.Confirming) {
        ConfirmSlip(
            title = "¿Restaurar este respaldo?",
            message = AnnotatedString(
                "Reemplaza 412 gastos, 23 ingresos… por los del 2 de octubre."
            ),
            confirmLabel = "Restaurar",
            onConfirm = { step = BackupStep.Restored },
            onDismiss = { step = BackupStep.Idle }
        )
    }
}

/** Prototype only: every screen and state in one list. */
@Composable
fun MapScreen() {
    val nav = LocalNav.current
    val proto = LocalProto.current
    ScreenTag("Prototipo · Mapa de pantallas")
    Shell(title = "Mapa de pantallas", subtitle = "solo en el prototipo", onBack = {
        nav.popBackStack()
    }, tabs = false) { padding ->
        Desk(padding) {
            Group("Recibo") {
                LinkRow("1 · Recibo") { nav.jumpTo(ReciboTab) }
                LinkRow("1 · Primer uso") {
                    proto.firstRun = true
                    nav.jumpTo(ReciboTab)
                }
                LinkRow("4 · Detalle de categoría") { nav.navigate(CategoryRoute("alimentacion")) }
                LinkRow("4 · Categoría pasada del límite") {
                    nav.navigate(CategoryRoute("entretenimiento"))
                }
                LinkRow("4 · Todos los gastos") { nav.navigate(ExpenseListRoute("alimentacion")) }
                LinkRow("5 · Nueva categoría") { nav.navigate(CategoryEditorRoute()) }
                LinkRow("5 · Editar categoría") { nav.navigate(CategoryEditorRoute("cafe")) }
                LinkRow("16 · Configuración") { nav.navigate(SettingsRoute) }
                LinkRow("17 · Respaldo y restauración") { nav.navigate(BackupRoute) }
            }
            Group("Anotar") {
                LinkRow("2 · Anotar gasto") { nav.navigate(AnotarRoute()) }
                LinkRow("2 · Anotar ingreso") { nav.navigate(AnotarRoute(income = true)) }
                LinkRow("2 · Editar gasto") { nav.navigate(AnotarRoute(edit = true)) }
                LinkRow("2 · Se repite") { nav.navigate(AnotarRoute(recurring = true)) }
                LinkRow("3 · Anotar varios") { nav.navigate(VariosRoute()) }
                LinkRow("3 · Varios movimientos") { nav.navigate(VariosRoute(movements = true)) }
                LinkRow("8 · Comprobante de ingreso") { nav.navigate(IncomeReceiptRoute) }
            }
            Group("Resumen") {
                LinkRow("6 · Resumen") { nav.jumpTo(ResumenTab) }
                LinkRow("7 · Lo que se repite") { nav.navigate(RecurringRoute) }
                LinkRow("8 · Ingresos") { nav.navigate(IncomeRoute) }
            }
            Group("Patrimonio") {
                LinkRow("9 · Inversiones") {
                    proto.patrimonioGoals = false
                    nav.jumpTo(PatrimonioTab)
                }
                LinkRow("10 · Detalle de inversión") { nav.navigate(InvestmentRoute("etf")) }
                LinkRow("11 · Nueva inversión") { nav.navigate(InvestmentEditorRoute()) }
                LinkRow("12 · Movimiento") { nav.navigate(MovementRoute("etf")) }
                LinkRow("13 · Metas") {
                    proto.patrimonioGoals = true
                    nav.jumpTo(PatrimonioTab)
                }
                LinkRow("13 · Detalle de meta") { nav.navigate(GoalRoute("emergencia")) }
                LinkRow("14 · Nueva meta") { nav.navigate(GoalEditorRoute()) }
                LinkRow("15 · Aportar") { nav.navigate(AportarRoute("emergencia")) }
                LinkRow("15 · Aporte guardado") {
                    nav.navigate(AporteReceiptRoute("emergencia", 818_750))
                }
                LinkRow("15 · Aporte que cruza el 75 %") {
                    nav.navigate(AporteReceiptRoute("emergencia", 2_800_000))
                }
                LinkRow("15 · Meta lograda") { nav.navigate(GoalDoneRoute("bicicleta")) }
            }
            SwitchRow("Mostrar estados vacíos", proto.empty, { proto.empty = it })
            SwitchRow("Simular primer uso", proto.firstRun, { proto.firstRun = it })
        }
    }
}

@Composable
private fun Group(title: String, content: @Composable () -> Unit) {
    Slip(pinked = false) {
        SlipTitle(title)
        content()
    }
}
