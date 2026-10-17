package com.software.sello.catalog.v2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.software.sello.catalog.components.DraftFixture
import com.software.sello.catalog.components.copFixture
import com.software.sello.designsystem.component.AmountKeypad
import com.software.sello.designsystem.component.ButtonKind
import com.software.sello.designsystem.component.ButtonTone
import com.software.sello.designsystem.component.FieldPill
import com.software.sello.designsystem.component.SegmentedSwitch
import com.software.sello.designsystem.component.SelloButton
import com.software.sello.designsystem.component.SelloChip
import com.software.sello.designsystem.component.SelloNotice
import com.software.sello.designsystem.component.Slip
import com.software.sello.designsystem.component.v2.EntryAmountSlip
import com.software.sello.designsystem.component.v2.FactLine
import com.software.sello.designsystem.component.v2.NoteComposer
import com.software.sello.designsystem.component.v2.V2ChipRows
import com.software.sello.designsystem.component.v2.V2Dock
import com.software.sello.designsystem.component.v2.V2EntryLayout
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.theme.SelloTheme

@Composable
internal fun V2Stack(content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) { content() }
}

@Composable
internal fun Caption(text: String) {
    Text(text, color = SelloTheme.colors.inkSoft, style = MaterialTheme.typography.bodySmall)
}

internal val CategoryNames =
    listOf(
        "Comida", "Transporte", "Café", "Casa", "Salud", "Ocio", "Ahorro", "Educación",
        "Mascotas", "Una categoría con nombre largo"
    )
private val Sources = listOf("Salario", "Freelance", "Ingresos pasivos", "Transferencia", "Otro")

@Composable
fun V2EntryExample() {
    val strings = LocalResources.current
    var income by rememberSaveable { mutableStateOf(false) }
    var digits by rememberSaveable { mutableStateOf("") }
    var choice by rememberSaveable { mutableIntStateOf(-1) }
    var note by rememberSaveable { mutableStateOf("") }
    var editingNote by rememberSaveable { mutableStateOf(false) }
    var noteDraft by rememberSaveable { mutableStateOf("") }
    var sourceName by rememberSaveable { mutableStateOf("") }
    var editingSource by rememberSaveable { mutableStateOf(false) }
    var scenario by rememberSaveable { mutableIntStateOf(0) }
    var confirmed by rememberSaveable { mutableStateOf("") }
    var limit by rememberSaveable { mutableStateOf(false) }
    var pulse by rememberSaveable { mutableIntStateOf(0) }
    val locked = scenario == 1 || scenario == 3
    val names = if (income) Sources else CategoryNames
    val amount = digits.toLongOrNull()?.let { copFixture(it, signed = income) }
    fun type(keys: String) {
        val next = DraftFixture(digits).type(keys)
        limit = next.limitPulse > 0
        if (limit) pulse++
        digits = next.digits
    }
    if (scenario == 5) {
        Column(
            Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Caption(strings.getString(R.string.v2_caller_preview))
            amount?.let {
                EntryAmountSlip(
                    if (income) {
                        strings.getString(
                            R.string.v2_demo_ingreso_en_pesos
                        )
                    } else {
                        strings.getString(R.string.v2_demo_gasto_en_pesos)
                    },
                    it
                )
            }
            Spacer(Modifier.weight(1f))
            SelloNotice(confirmed, confirmation = true)
            V2Dock(strings.getString(R.string.v2_demo_anotar_gasto), {
                scenario = 0
                digits = ""
                choice =
                    -1
                note = ""
            })
        }
        return
    }
    V2EntryLayout(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
        header = {
            SegmentedSwitch(listOf("Gasto", "Ingreso"), if (income) 1 else 0, {
                income = it == 1
                choice =
                    -1
            }, enabled = !locked)
        },
        amount = {
            EntryAmountSlip(
                if (income) {
                    strings.getString(
                        R.string.v2_demo_ingreso_en_pesos
                    )
                } else {
                    strings.getString(R.string.v2_demo_gasto_en_pesos)
                },
                amount,
                Modifier.testTag("v2:entry:amount"),
                error = if (limit) strings.getString(R.string.v2_demo_maximo_12_digitos) else null,
                date = {
                    FieldPill(strings.getString(R.string.v2_demo_hoy_vie_16), SelloIcon.Event, {
                    }, enabled = !locked)
                },
                icon = if (income) {
                    SelloIcon.AccountBalance
                } else if (choice >=
                    0
                ) {
                    SelloIcon.Restaurant
                } else {
                    null
                },
                tone = if (income) ButtonTone.Gain else ButtonTone.Brand,
                limitPulse = pulse
            )
        },
        choices = {
            V2ChipRows(names.size) { index ->
                SelloChip(names[index], choice == index, {
                    choice = index
                }, enabled = !locked, modifier = Modifier.testTag("v2:entry:choice:$index"))
            }
            FieldPill(
                note.ifEmpty {
                    strings.getString(R.string.v2_demo_agregar_nota)
                },
                SelloIcon.EditNote,
                {
                    if (!locked) {
                        noteDraft =
                            note
                        editingSource = false
                        editingNote = true
                    }
                },
                Modifier.testTag("v2:entry:note"),
                placeholder = note.isEmpty(),
                enabled = !locked
            )
            if (income && choice == 4) {
                FieldPill(
                    sourceName.ifEmpty {
                        strings.getString(R.string.v2_demo_nombre_de_la_fuente)
                    },
                    SelloIcon.EditNote,
                    {
                        if (!locked) {
                            noteDraft =
                                sourceName
                            editingSource = true
                            editingNote = true
                        }
                    },
                    enabled = !locked
                )
            }
            V2ChipRows(5, modifier = Modifier.testTag("v2:entry:states"), rows = 1) { index ->
                SelloChip(
                    listOf(
                        "Listo",
                        "Guardando",
                        strings.getString(R.string.v2_demo_no_guardado),
                        strings.getString(R.string.v2_demo_por_comprobar),
                        "Restaurado"
                    )[index],
                    scenario == index,
                    {
                        scenario = index
                        if (index == 4) {
                            digits = "48700"
                            choice = 0
                            note = "Almuerzo"
                        }
                    },
                    modifier = Modifier.testTag("v2:entry:state:$index")
                )
            }
            when (scenario) {
                2 -> Text(
                    strings.getString(R.string.v2_demo_no_se_guardo_tu_borrador_sigue_aqui),
                    color = SelloTheme.colors.semantic.loss
                )

                3 -> Text(
                    strings.getString(R.string.v2_demo_aun_no_sabemos_si_se_guardo_compruebalo)
                )

                4 -> Caption(
                    strings.getString(R.string.v2_demo_borrador_sintetico_restaurado_no_se_envia_al)
                )
            }
            Caption(
                strings.getString(
                    R.string.v2_demo_catalogo_datos_sinteticos_sin_almacenamiento_financiero
                )
            )
        },
        action = {
            SelloButton(
                when (scenario) {
                    1 -> strings.getString(R.string.v2_demo_guardando)

                    3 -> strings.getString(R.string.v2_demo_comprobar_si_se_guardo)

                    else -> if (income) {
                        strings.getString(
                            R.string.v2_demo_anotar_ingreso
                        )
                    } else {
                        strings.getString(R.string.v2_demo_anotar_gasto)
                    }
                },
                {
                    if (scenario == 3) {
                        scenario = 2
                    } else {
                        confirmed =
                            strings.getString(
                                if (income) {
                                    R.string.v2_income_confirmation
                                } else {
                                    R.string.v2_expense_confirmation
                                },
                                amount?.digits.orEmpty(),
                                if (income &&
                                    choice == 4
                                ) {
                                    sourceName
                                } else {
                                    names.getOrNull(choice).orEmpty()
                                }
                            )
                        scenario = 5
                    }
                },
                Modifier.fillMaxWidth().testTag("v2:entry:submit"),
                enabled =
                    scenario == 3 ||
                        (
                            amount != null && choice >= 0 &&
                                (!income || choice != 4 || sourceName.isNotBlank())
                            ),
                loading =
                    scenario == 1
            )
        },
        keypad = {
            AmountKeypad({ type(it.toString()) }, { type("000") }, {
                digits = digits.dropLast(1)
                limit =
                    false
            }, {
                digits = ""
                limit = false
            }, enabled = !locked, compact = true)
        }
    )
    if (editingNote) {
        val length = noteDraft.codePointCount(0, noteDraft.length)
        val maximum = if (editingSource) 24 else 60
        NoteComposer(
            if (editingSource) {
                strings.getString(R.string.v2_demo_nombre_de_la_fuente)
            } else {
                "Nota · ${names.getOrNull(
                    choice
                ) ?: "Gasto"}"
            },
            noteDraft, {
                noteDraft =
                    it
            },
            "$length/$maximum",
            if (length >
                maximum
            ) {
                "Máximo $maximum caracteres. Tu texto sigue aquí."
            } else if (editingSource &&
                noteDraft.isBlank()
            ) {
                strings.getString(R.string.v2_demo_escribe_el_nombre_de_la_fuente)
            } else {
                null
            },
            "Listo", "Cancelar", {
                if (editingSource) sourceName = noteDraft else note = noteDraft
                editingNote =
                    false
            }, { editingNote = false }
        )
    }
}

@Composable
fun V2AmountExample() {
    val strings = LocalResources.current
    V2Stack {
        listOf(999_999L, 12_345_678L, 999_999_999_999L).forEach { amount ->
            EntryAmountSlip(strings.getString(R.string.v2_demo_gasto_en_pesos), copFixture(amount))
        }
        EntryAmountSlip(
            strings.getString(R.string.v2_demo_ingreso_en_pesos),
            copFixture(48_700, signed = true),
            tone = ButtonTone.Gain
        )
        EntryAmountSlip(
            strings.getString(R.string.v2_demo_gasto_en_pesos),
            copFixture(999_999_999_999),
            error = strings.getString(R.string.v2_demo_maximo_12_digitos)
        )
        EntryAmountSlip(strings.getString(R.string.v2_demo_gasto_en_pesos), null)
    }
}

@Composable
fun V2KeypadExample() {
    val strings = LocalResources.current
    var digits by rememberSaveable { mutableStateOf("") }
    var limit by rememberSaveable { mutableStateOf(false) }
    var pulse by rememberSaveable { mutableIntStateOf(0) }
    fun type(keys: String) {
        val next = DraftFixture(digits).type(keys)
        limit = next.limitPulse > 0
        if (limit) pulse++
        digits = next.digits
    }
    V2Stack {
        EntryAmountSlip(
            strings.getString(R.string.v2_demo_gasto_en_pesos),
            digits.toLongOrNull()?.let {
                copFixture(it)
            },
            error = if (limit) strings.getString(R.string.v2_demo_maximo_12_digitos) else null,
            limitPulse = pulse
        )
        AmountKeypad({ type(it.toString()) }, { type("000") }, {
            digits = digits.dropLast(1)
            limit =
                false
        }, {
            digits = ""
            limit = false
        }, compact = true)
        Caption(strings.getString(R.string.v2_demo_manten_borrar_para_vaciar_el_monto_una))
        AmountKeypad({}, {}, {}, {}, enabled = false, compact = true)
    }
}

@Composable
fun V2ChipsExample() {
    val strings = LocalResources.current
    var choice by rememberSaveable { mutableIntStateOf(-1) }
    V2Stack {
        Caption(strings.getString(R.string.v2_demo_maximo_dos_filas_desplaza_para_ver_mas))
        V2ChipRows(CategoryNames.size) { index ->
            SelloChip(CategoryNames[index], choice == index, {
                choice =
                    index
            })
        }
        Caption(strings.getString(R.string.v2_demo_dos_grupos_comparten_el_espacio_una_fila))
        V2ChipRows(4, rows = 1) { index ->
            SelloChip(
                listOf(
                    "Aporte",
                    strings.getString(R.string.v2_demo_valor_de_hoy),
                    "Retiro",
                    strings.getString(R.string.v2_demo_dividendo_o_renta)
                )[index],
                choice == index,
                { choice = index }
            )
        }
        V2ChipRows(4, rows = 1) { index ->
            SelloChip(CategoryNames[index], choice == index, {
                choice =
                    index
            })
        }
        SelloChip("Deshabilitado", false, {}, enabled = false)
        FactLine(
            AnnotatedString(strings.getString(R.string.v2_demo_llevas_1_710_000_guardados_en_esta))
        )
    }
}

@Composable
fun V2ComposerExample() {
    val strings = LocalResources.current
    var open by rememberSaveable { mutableStateOf(false) }
    var value by rememberSaveable { mutableStateOf("") }
    var draft by rememberSaveable { mutableStateOf("") }
    V2Stack {
        FieldPill(
            value.ifEmpty {
                strings.getString(R.string.v2_demo_agregar_nota)
            },
            SelloIcon.EditNote,
            {
                draft = value
                open = true
            },
            Modifier.testTag("v2:composer:open")
        )
        SelloButton(strings.getString(R.string.v2_demo_probar_nota_larga), {
            draft =
                strings.getString(R.string.v2_demo_una_nota_sintetica_que_supera_el_limite)
            open =
                true
        }, kind = ButtonKind.Outlined)
        Text(value, Modifier.testTag("v2:composer:saved"))
    }
    if (open) {
        val length = draft.codePointCount(0, draft.length)
        NoteComposer(
            strings.getString(R.string.v2_demo_nota_comida), draft, {
                draft = it
            }, "$length/60",
            if (length >
                60
            ) {
                strings.getString(R.string.v2_demo_maximo_60_caracteres_tu_texto_sigue_aqui)
            } else {
                null
            },
            "Listo", "Cancelar", {
                value =
                    draft
                open = false
            }, { open = false }
        )
    }
}

@Composable
fun V2ButtonsExample() {
    val strings = LocalResources.current
    var presses by rememberSaveable { mutableIntStateOf(0) }
    V2Stack {
        ButtonTone.entries.forEach { tone ->
            SelloButton(tone.name, { presses++ }, Modifier.fillMaxWidth(), tone = tone)
            SelloButton(tone.name, {
                presses++
            }, Modifier.fillMaxWidth(), kind = ButtonKind.Outlined, tone = tone)
        }
        SelloButton(strings.getString(R.string.v2_demo_guardando), {}, loading = true)
        SelloButton(strings.getString(R.string.v2_demo_elige_una_meta), {
        }, enabled = false, tone = ButtonTone.Gain)
        Caption("Pulsaciones: $presses")
    }
}
