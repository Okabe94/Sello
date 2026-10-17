package com.software.sello.catalog.v2

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.software.sello.catalog.R
import com.software.sello.catalog.components.copFixture
import com.software.sello.designsystem.component.ButtonKind
import com.software.sello.designsystem.component.ButtonTone
import com.software.sello.designsystem.component.CategoryCell
import com.software.sello.designsystem.component.CategoryGrid
import com.software.sello.designsystem.component.LeaderLine
import com.software.sello.designsystem.component.MoneyStyle
import com.software.sello.designsystem.component.MoneyText
import com.software.sello.designsystem.component.SelloButton
import com.software.sello.designsystem.component.SelloChip
import com.software.sello.designsystem.component.SelloDestination
import com.software.sello.designsystem.component.SelloDock
import com.software.sello.designsystem.component.SelloNavigation
import com.software.sello.designsystem.component.SelloNavigationRail
import com.software.sello.designsystem.component.Slip
import com.software.sello.designsystem.component.Stamp
import com.software.sello.designsystem.component.StampInk
import com.software.sello.designsystem.component.TearLine
import com.software.sello.designsystem.component.TotalLine
import com.software.sello.designsystem.component.v2.AnswerSlip
import com.software.sello.designsystem.component.v2.ComparisonBar
import com.software.sello.designsystem.component.v2.ComparisonSlip
import com.software.sello.designsystem.component.v2.FactLine
import com.software.sello.designsystem.component.v2.Footnote
import com.software.sello.designsystem.component.v2.PastMonthRibbon
import com.software.sello.designsystem.component.v2.ReviewInvitation
import com.software.sello.designsystem.component.v2.SpendingRankRow
import com.software.sello.designsystem.component.v2.SplitBar
import com.software.sello.designsystem.component.v2.SplitSegment
import com.software.sello.designsystem.component.v2.SuggestionNote
import com.software.sello.designsystem.component.v2.V2ChipRows
import com.software.sello.designsystem.component.v2.V2Dock
import com.software.sello.designsystem.component.v2.V2MonthBar
import com.software.sello.designsystem.component.v2.V2NavigationBar
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.theme.SelloTheme

@Composable
fun V2DockExample() {
    val strings = LocalResources.current
    var taps by rememberSaveable { mutableIntStateOf(0) }
    V2Stack {
        V2Dock("Anotar", { taps++ })
        V2Dock(strings.getString(R.string.v2_demo_anotar_ingreso), { taps++ })
        V2Dock(strings.getString(R.string.v2_demo_aportar_a_una_meta), {
            taps++
        }, tone = ButtonTone.Gain, icon = SelloIcon.Flag)
        V2Dock(strings.getString(R.string.v2_demo_anotar_movimiento), {
            taps++
        }, tone = ButtonTone.Ink)
        V2Dock(strings.getString(R.string.v2_demo_crear_una_meta), {
            taps++
        }, tone = ButtonTone.Gain, icon = SelloIcon.Flag)
        V2Dock(strings.getString(R.string.v2_demo_elige_una_meta), {
        }, tone = ButtonTone.Gain, enabled = false)
        V2Dock(strings.getString(R.string.v2_demo_guardando), {}, busy = true)
        Caption("Acciones: $taps")
    }
}

private val Tabs = listOf(
    SelloDestination("recibo", "Recibo", SelloIcon.ReceiptLong),
    SelloDestination("resumen", "Resumen", SelloIcon.Insights),
    SelloDestination("metas", "Metas", SelloIcon.Flag),
    SelloDestination("inversiones", "Inversiones", SelloIcon.AccountBalance)
)

@Composable
fun V2NavigationExample() {
    val strings = LocalResources.current
    var selected by rememberSaveable { mutableStateOf("recibo") }
    V2Stack {
        for (count in 2..4) {
            Caption("$count destinos · ${Tabs.firstOrNull { it.id == selected }?.label}")
            V2NavigationBar(
                SelloNavigation(
                    Tabs.take(count),
                    if (Tabs.take(count).any {
                            it.id ==
                                selected
                        }
                    ) {
                        selected
                    } else {
                        "recibo"
                    },
                    { selected = it }
                ),
                Modifier.testTag("v2:navigation:$count")
            )
        }
        Caption(
            strings.getString(R.string.v2_demo_riel_existente_para_ventanas_amplias_la_integracion)
        )
        SelloNavigationRail(
            SelloNavigation(Tabs, selected, {
                selected = it
            }),
            modifier = Modifier.height(340.dp),
            dock = SelloDock("Anotar", "Anotar", {})
        )
    }
}

@Composable
fun V2MonthExample() {
    var taps by rememberSaveable { mutableIntStateOf(0) }
    V2Stack {
        V2MonthBar("Octubre", "2026", { taps++ })
        V2MonthBar("Septiembre", "2026", { taps++ })
        Caption("Abrir selector: $taps")
    }
}

@Composable
fun V2RibbonExample() {
    val strings = LocalResources.current
    var past by rememberSaveable { mutableStateOf(true) }
    V2Stack {
        if (past) {
            PastMonthRibbon(
                AnnotatedString(
                    strings.getString(R.string.v2_demo_estas_viendo_septiembre)
                ),
                strings.getString(R.string.v2_demo_volver_a_noviembre),
                {
                    past =
                        false
                },
                Modifier.testTag("v2:ribbon")
            )
        } else {
            Caption(strings.getString(R.string.v2_demo_noviembre_mes_actual))
        }
        SelloButton(strings.getString(R.string.v2_demo_ver_septiembre), {
            past = true
        }, kind = ButtonKind.Outlined)
    }
}

@Composable
fun V2StampsExample() {
    val strings = LocalResources.current
    var replay by rememberSaveable { mutableIntStateOf(0) }
    V2Stack {
        key(replay) { Stamp("Lograda", StampInk.Good, land = replay > 0, v2Motion = true) }
        SelloButton(strings.getString(R.string.v2_demo_repetir_aterrizaje), {
            replay++
        }, kind = ButtonKind.Outlined)
        listOf(
            strings.getString(R.string.v2_demo_al_dia) to StampInk.Good,
            strings.getString(R.string.v2_demo_al_limite) to StampInk.Neutral,
            "Excedido" to StampInk.Over,
            strings.getString(R.string.v2_demo_sin_margen) to StampInk.Neutral,
            "Revisar" to StampInk.Warn,
            strings.getString(R.string.v2_demo_sin_limite) to StampInk.Neutral,
            "Cumplido" to StampInk.Good, "Gana" to StampInk.Good,
            "Pierde" to StampInk.Over, "Lograda" to StampInk.Good
        ).forEach { (label, ink) ->
            Slip(Modifier.fillMaxWidth(), pinked = false) {
                Stamp(label, ink, Modifier.padding(8.dp), angle = -5f)
            }
        }
    }
}

@Composable
fun V2FootnotesExample() {
    val strings = LocalResources.current
    var opened by rememberSaveable { mutableStateOf("") }
    V2Stack {
        Slip(Modifier.fillMaxWidth()) {
            Footnote(
                AnnotatedString(
                    strings.getString(R.string.v2_demo_incluye_200_000_separado_para_1_pago)
                ),
                {
                    opened =
                        strings.getString(R.string.v2_demo_pagos_previstos)
                }
            )
            Footnote(
                AnnotatedString(
                    strings.getString(R.string.v2_demo_valores_anotados_entre_el_12_jun_y)
                ),
                {
                    opened =
                        strings.getString(R.string.v2_demo_fechas_de_valoracion)
                }
            )
        }
        FactLine(
            AnnotatedString(strings.getString(R.string.v2_demo_llevas_1_710_000_guardados_en_esta))
        )
        ReviewInvitation(strings.getString(R.string.v2_demo_ver_como_cerraste_octubre), {
            opened =
                strings.getString(R.string.v2_demo_resumen_de_octubre)
        })
        Caption(opened)
    }
}

@Composable
fun V2SummaryExample() {
    val strings = LocalResources.current
    var state by rememberSaveable { mutableIntStateOf(0) }
    val labels =
        listOf(
            "Actual",
            strings.getString(R.string.v2_demo_al_limite),
            "Excedido",
            strings.getString(R.string.v2_demo_sin_margen),
            "Revisar",
            strings.getString(R.string.v2_demo_sin_limite),
            "Cerrado"
        )
    V2Stack {
        V2ChipRows(labels.size) { index ->
            SelloChip(labels[index], state == index, { state = index })
        }
        val stamp = listOf(
            strings.getString(R.string.v2_demo_al_dia),
            strings.getString(R.string.v2_demo_al_limite),
            "Excedido",
            strings.getString(R.string.v2_demo_sin_margen),
            "Revisar",
            strings.getString(R.string.v2_demo_sin_limite),
            "Cumplido"
        )[state]
        val ink = when (state) {
            0, 6 -> StampInk.Good
            2 -> StampInk.Over
            4 -> StampInk.Warn
            else -> StampInk.Neutral
        }
        val value = listOf(400_000L, 0L, -80_000L, 0L, 50_000L, 920_000L, 80_000L)[state]
        AnswerSlip(
            if (state ==
                5
            ) {
                strings.getString(R.string.v2_demo_has_gastado_este_mes)
            } else if (state ==
                4
            ) {
                strings.getString(R.string.v2_demo_falta_para_tus_pagos)
            } else {
                strings.getString(R.string.v2_demo_restante_del_presupuesto)
            },
            copFixture(value),
            stamp,
            ink
        ) {
            if (state != 5) {
                TearLine()
                when (state) {
                    6 -> Text(strings.getString(R.string.v2_demo_mes_cerrado_gastaste_920_000_de_1))

                    4 -> Text(
                        strings.getString(R.string.v2_demo_quedan_100_000_y_tus_pagos_previstos)
                    )

                    else -> {
                        MoneyText(
                            copFixture(
                                if (state ==
                                    0
                                ) {
                                    40_000
                                } else {
                                    0
                                }
                            ),
                            style = MoneyStyle.Card
                        )
                        Text(strings.getString(R.string.v2_demo_por_dia))
                        Caption(strings.getString(R.string.v2_demo_10_dias_incluido_hoy))
                    }
                }
            }
            if (state == 2 ||
                state == 4
            ) {
                SelloButton(strings.getString(R.string.v2_demo_ajustar_presupuesto), {
                }, kind = ButtonKind.Text)
            }
            if (state ==
                0
            ) {
                Footnote(
                    AnnotatedString(
                        strings.getString(R.string.v2_demo_incluye_200_000_separado_para_1_pago)
                    ),
                    {
                    }
                )
            }
        }
        Slip(Modifier.fillMaxWidth(), pinked = false) {
            Text(strings.getString(R.string.v2_demo_tus_categorias))
            CategoryGrid(listOf("Comida", strings.getString(R.string.v2_demo_cafe))) {
                    index,
                    name
                ->
                CategoryCell(
                    name,
                    if (index ==
                        0
                    ) {
                        SelloIcon.Restaurant
                    } else {
                        SelloIcon.LocalCafe
                    },
                    copFixture(
                        if (index ==
                            0
                        ) {
                            120_000
                        } else {
                            -10_000
                        }
                    ),
                    if (index ==
                        0
                    ) {
                        0.4f
                    } else {
                        1f
                    },
                    if (index ==
                        0
                    ) {
                        "Comida, quedan 120.000 pesos"
                    } else {
                        "Café, excedido por 10.000 pesos"
                    },
                    {
                    },
                    over =
                        index == 1
                )
            }
        }
    }
}

@Composable
fun V2ComparisonExample() {
    val strings = LocalResources.current
    var mode by rememberSaveable { mutableIntStateOf(0) }
    V2Stack {
        V2ChipRows(3, rows = 1) { index ->
            SelloChip(
                listOf(
                    "Menos",
                    strings.getString(R.string.v2_demo_mas),
                    strings.getString(R.string.v2_demo_mes_cerrado)
                )[index],
                mode == index,
                { mode = index }
            )
        }
        ComparisonSlip(
            strings.getString(
                if (mode ==
                    2
                ) {
                    R.string.v2_demo_como_cerro_septiembre
                } else {
                    R.string.v2_demo_como_va_este_mes
                }
            ),
            copFixture(if (mode == 0) 50_000 else 160_000),
            strings.getString(
                when (mode) {
                    0 -> R.string.v2_demo_menos_que_en_septiembre
                    1 -> R.string.v2_demo_more_than_september
                    else -> R.string.v2_demo_mas_que_en_agosto
                }
            ),
            strings.getString(
                if (mode ==
                    0
                ) {
                    R.string.v2_demo_11_1_al_mismo_dia_del_mes
                } else {
                    R.string.v2_demo_21_1_respecto_al_mes_anterior
                }
            ),
            strings.getString(
                if (mode ==
                    2
                ) {
                    R.string.v2_demo_meses_completos
                } else {
                    R.string.v2_demo_del_1_al_22_de_cada_mes
                }
            ),
            when (mode) {
                0 -> listOf(
                    ComparisonBar("Octubre", copFixture(400_000), 0.8f, true),
                    ComparisonBar("Septiembre", copFixture(450_000), 0.9f, false)
                )

                1 -> listOf(
                    ComparisonBar("Octubre", copFixture(920_000), 0.92f, true),
                    ComparisonBar("Septiembre", copFixture(760_000), 0.76f, false)
                )

                else -> listOf(
                    ComparisonBar("Septiembre", copFixture(920_000), 0.92f, true),
                    ComparisonBar("Agosto", copFixture(760_000), 0.76f, false)
                )
            }
        )
        IncomeSlipExample(mode)
    }
}

@Composable
private fun IncomeSlipExample(mode: Int) {
    val strings = LocalResources.current
    Slip(Modifier.fillMaxWidth(), pinked = false) {
        Text(
            strings.getString(
                if (mode ==
                    2
                ) {
                    R.string.v2_september_income
                } else {
                    R.string.v2_demo_ingresos_de_octubre
                }
            )
        )
        LeaderLine(strings.getString(R.string.v2_demo_ingresos_anotados)) {
            MoneyText(copFixture(2_000_000))
        }
        LeaderLine(strings.getString(R.string.v2_demo_ingresos_menos_gastos)) {
            MoneyText(copFixture(if (mode == 0) 1_600_000 else 1_080_000))
        }
        LeaderLine(strings.getString(R.string.v2_demo_sin_asignar_a_categorias)) {
            MoneyText(copFixture(1_000_000))
        }
        SelloButton(strings.getString(R.string.v2_demo_ver_ingresos_y_su_destino), {
        }, kind = ButtonKind.Text)
    }
}

@Composable
fun V2RankingExample() {
    val strings = LocalResources.current
    var opened by rememberSaveable { mutableStateOf("") }
    Slip(Modifier.fillMaxWidth(), pinked = false) {
        Text(strings.getString(R.string.v2_demo_donde_se_va))
        SpendingRankRow("Comida", SelloIcon.Restaurant, copFixture(300_000), 0.75f, {
            opened =
                "Comida"
        })
        SpendingRankRow(
            strings.getString(
                R.string.v2_demo_cafe
            ),
            SelloIcon.LocalCafe,
            copFixture(100_000),
            0.25f,
            {
                opened = strings.getString(R.string.v2_demo_cafe)
            }
        )
        TotalLine("Total") { MoneyText(copFixture(400_000)) }
        Caption(opened)
    }
}

@Composable
fun V2SuggestionExample() {
    val strings = LocalResources.current
    var dismissed by rememberSaveable { mutableStateOf(false) }
    var action by rememberSaveable { mutableStateOf(false) }
    V2Stack {
        if (!dismissed) {
            SuggestionNote(
                strings.getString(
                    R.string.v2_demo_una_sugerencia
                ),
                strings.getString(
                    R.string.v2_demo_cafe_supero_su_limite_dos_meses_seguidos
                ),
                strings.getString(
                    R.string.v2_demo_podrias_ajustar_su_limite_para_que_refleje
                ),
                strings.getString(
                    R.string.v2_demo_ajustar_el_presupuesto_de_cafe
                ),
                strings.getString(R.string.v2_demo_descartar_sugerencia),
                {
                    action =
                        true
                },
                { dismissed = true }
            )
        }
        if (action) Caption(strings.getString(R.string.v2_demo_accion_sintetica_ajustar_cafe))
        SelloButton(strings.getString(R.string.v2_demo_mostrar_sugerencia), {
            dismissed = false
            action = false
        }, kind = ButtonKind.Text)
        Caption(
            strings.getString(R.string.v2_demo_la_elegibilidad_y_el_descarte_mensual_pertenecen)
        )
    }
}
