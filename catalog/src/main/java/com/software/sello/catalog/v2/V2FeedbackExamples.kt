package com.software.sello.catalog.v2

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalAccessibilityManager
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.AnnotatedString
import com.software.sello.catalog.R
import com.software.sello.catalog.components.copFixture
import com.software.sello.catalog.foundations.ColorsExample
import com.software.sello.catalog.foundations.IconsExample
import com.software.sello.catalog.foundations.MotionExample
import com.software.sello.catalog.foundations.TypographyExample
import com.software.sello.designsystem.component.ButtonKind
import com.software.sello.designsystem.component.ConfirmSlip
import com.software.sello.designsystem.component.EmptySlip
import com.software.sello.designsystem.component.ErrorSlip
import com.software.sello.designsystem.component.SelloButton
import com.software.sello.designsystem.component.SelloNotice
import com.software.sello.designsystem.component.Slip
import com.software.sello.designsystem.component.SlipSkeleton
import com.software.sello.designsystem.component.v2.CelebrationOverlay
import com.software.sello.designsystem.component.v2.DangerSlip
import com.software.sello.designsystem.component.v2.MilestoneState
import com.software.sello.designsystem.component.v2.StampTicket
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.theme.SelloTheme
import kotlinx.coroutines.delay

@Composable
fun V2FeedbackExample() {
    val strings = LocalResources.current
    var confirm by rememberSaveable { mutableStateOf(false) }
    var result by rememberSaveable { mutableStateOf("") }
    V2Stack {
        SelloNotice(
            strings.getString(R.string.v2_demo_gasto_anotado_48_700_en_comida),
            confirmation = true
        )
        SelloNotice(
            strings.getString(
                R.string.v2_demo_celular_nuevo_meta_lograda
            ),
            actionLabel = "Deshacer",
            onAction = {
                result =
                    strings.getString(R.string.v2_demo_100_pendiente_de_marcar)
            }
        )
        SelloNotice(
            strings.getString(
                R.string.v2_demo_gasto_borrado
            ),
            actionLabel = "Deshacer",
            onAction = {
            },
            busy = true
        )
        StampTicket(
            GoalPlaces.take(2),
            strings.getString(R.string.v2_demo_ganaste_los_sellos_del_25_y_del),
            strings.getString(R.string.v2_demo_viaje_a_la_costa_faltan_1_290)
        )
        DangerSlip(
            strings.getString(R.string.v2_demo_borrar_esta_meta),
            AnnotatedString(
                strings.getString(R.string.v2_demo_se_borran_la_meta_y_sus_6)
            ),
            strings.getString(R.string.v2_demo_borrar_meta),
            {
                confirm =
                    true
            }
        )
        Caption(result)
    }
    if (confirm) {
        ConfirmSlip(
            strings.getString(R.string.v2_demo_borrar_viaje_a_la_costa),
            AnnotatedString(
                strings.getString(R.string.v2_demo_se_borran_la_meta_y_sus_6)
            ),
            "Borrar",
            {
                result =
                    strings.getString(
                        R.string.v2_demo_confirmacion_sintetica_el_catalogo_no_borra_registros
                    )
                confirm = false
            },
            {
                confirm =
                    false
            }
        )
    }
}

@Composable
fun V2CelebrationExample() {
    val strings = LocalResources.current
    var open by rememberSaveable { mutableStateOf(false) }
    var long by rememberSaveable { mutableStateOf(false) }
    var notice by rememberSaveable { mutableStateOf(false) }
    var outcome by rememberSaveable { mutableStateOf("") }
    val accessibility = LocalAccessibilityManager.current
    val duration =
        accessibility?.calculateRecommendedTimeoutMillis(
            10_000,
            containsIcons = true,
            containsText = true,
            containsControls = true
        )
            ?: 10_000
    LaunchedEffect(notice) {
        if (notice) {
            delay(duration)
            notice = false
        }
    }
    V2Stack {
        Caption(
            strings.getString(
                R.string.v2_demo_celebracion_sintetica_de_una_finalizacion_confirmada_sin
            )
        )
        SelloButton(strings.getString(R.string.v2_demo_marcar_como_lograda), {
            long = false
            open = true
        })
        SelloButton(strings.getString(R.string.v2_demo_probar_nombre_y_monto_largos), {
            long = true
            open = true
        }, kind = ButtonKind.Outlined)
        if (notice) {
            SelloNotice(
                strings.getString(
                    R.string.v2_demo_celular_nuevo_meta_lograda
                ),
                actionLabel = "Deshacer",
                onAction = {
                    notice =
                        false
                    outcome = strings.getString(R.string.v2_demo_100_pendiente_de_marcar)
                }
            )
        }
        Caption(outcome)
    }
    if (open) {
        CelebrationOverlay(
            strings.getString(
                R.string.v2_demo_lo_lograste
            ),
            strings.getString(
                R.string.v2_demo_meta_lograda
            ),
            if (long) {
                strings.getString(
                    R.string.v2_demo_un_viaje_con_toda_la_familia_para
                )
            } else {
                strings.getString(R.string.v2_demo_celular_nuevo)
            },
            SelloIcon.PhoneIphone,
            copFixture(
                if (long) 999_999_999_999 else 1_200_000
            ),
            strings.getString(R.string.v2_demo_16_oct_2026_6_aportes),
            GoalPlaces.map {
                it.copy(state = MilestoneState.Earned, description = "${it.label}, logrado")
            },
            "Listo", strings.getString(R.string.v2_demo_ver_mi_meta), {
                open = false
                notice = true
            }, {
                open = false
                outcome =
                    strings.getString(R.string.v2_demo_meta_lograda_historial_de_solo_lectura)
            }
        )
    }
}

@Composable
fun V2StatesExample() {
    val strings = LocalResources.current
    V2Stack {
        EmptySlip(
            strings.getString(
                R.string.v2_demo_dale_forma_a_tu_mes
            ),
            strings.getString(
                R.string.v2_demo_crea_una_categoria_para_lo_que_importa
            ),
            actionLabel = strings.getString(R.string.v2_demo_crear_categoria),
            onAction = {
            }
        )
        EmptySlip(
            strings.getString(R.string.v2_demo_ponle_nombre_a_algo_que_quieras),
            strings.getString(R.string.v2_demo_tus_metas_apareceran_aqui)
        )
        Caption(strings.getString(R.string.v2_demo_sin_metas_la_accion_crear_una_meta))
        ErrorSlip(
            strings.getString(
                R.string.v2_demo_tus_registros_siguen_guardados_vuelve_a_intentarlo
            ),
            {
            },
            title = strings.getString(R.string.v2_demo_no_pudimos_leer_tu_mes)
        )
        SlipSkeleton()
        Slip(Modifier.fillMaxWidth(), pinked = false) {
            Text(strings.getString(R.string.v2_demo_primer_mes_aun_no_hay_un_mes))
        }
        EmptySlip(
            strings.getString(R.string.v2_demo_sello_funciona_sin_ingresos),
            strings.getString(R.string.v2_demo_puedes_empezar_con_tus_categorias_y_gastos)
        )
    }
}

@Composable
fun V2FoundationsExample() {
    val strings = LocalResources.current
    V2Stack {
        Caption(
            strings.getString(
                R.string.v2_demo_tokens_existentes_nuevos_simbolos_y_movimiento_reducido
            )
        )
        ColorsExample()
        TypographyExample()
        IconsExample()
        MotionExample()
    }
}
