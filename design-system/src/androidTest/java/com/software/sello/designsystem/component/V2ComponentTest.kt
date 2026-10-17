package com.software.sello.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import com.software.sello.designsystem.component.v2.FittedMoneyText
import com.software.sello.designsystem.component.v2.MilestonePlace
import com.software.sello.designsystem.component.v2.MilestoneSpec
import com.software.sello.designsystem.component.v2.MilestoneState
import com.software.sello.designsystem.component.v2.NoteComposer
import com.software.sello.designsystem.component.v2.PastMonthRibbon
import com.software.sello.designsystem.component.v2.V2ChipRows
import com.software.sello.designsystem.component.v2.V2EntryLayout
import com.software.sello.designsystem.component.v2.V2NavigationBar
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.theme.SelloTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class V2ComponentTest {
    @get:Rule
    val rule = createComposeRule()

    @Test
    fun compactPadHasFour48dpRowsAndClearAllIsAccessible() {
        var cleared = 0
        rule.inEveryScheme(content = {
            AmountKeypad({}, {}, {}, { cleared++ }, Modifier.testTag("pad"), compact = true)
        }) {
            assertEquals(192.dp, rule.onNodeWithTag("pad").getUnclippedBoundsInRoot().height)
            val key = rule.onNodeWithText("1").fetchSemanticsNode()
            val target = with(key.layoutInfo.density) { key.touchBoundsInRoot.height.toDp() }
            assertTrue(target >= 47.9.dp)
            rule.onNodeWithContentDescription(
                "Borrar"
            ).performSemanticsAction(SemanticsActions.OnLongClick) {
                it()
            }
        }
        assertEquals(4, cleared)
    }

    @Test
    fun twelveDigitSignedAmountFitsOnOneLineAt320AndDoubleFont() {
        val results = mutableListOf<TextLayoutResult>()
        rule.inEveryScheme(fontScale = 2f, width = 320.dp, content = {
            FittedMoneyText(
                MoneyTextValue(
                    MoneySign.Plus,
                    "",
                    "999.999.999.999",
                    "más 999.999.999.999 pesos"
                ),
                entry = true,
                onTextLayout = {
                    results.add(it)
                }
            )
        }) { scheme ->
            rule.onNodeWithContentDescription("más 999.999.999.999 pesos").assertIsDisplayed()
            assertEquals(scheme, 1, results.last().lineCount)
            assertTrue(scheme, !results.last().hasVisualOverflow)
        }
    }

    @Test
    fun pastMonthReturnRemainsReachableAtDoubleFont() {
        var returns = 0
        rule.inEveryScheme(fontScale = 2f, width = 320.dp, content = {
            PastMonthRibbon(AnnotatedString("Estás viendo septiembre"), "Volver a noviembre", {
                returns++
            })
        }) {
            val button = rule.onNodeWithText("Volver a noviembre")
            button.assertIsDisplayed().performClick()
            assertTrue(button.getUnclippedBoundsInRoot().height >= 48.dp)
        }
        assertEquals(4, returns)
    }

    @Test
    fun allFourDestinationsExposeSelectionAndLabelsAtDoubleFont() {
        var selected by mutableStateOf("recibo")
        rule.inEveryScheme(fontScale = 2f, width = 320.dp, content = {
            V2NavigationBar(
                SelloNavigation(
                    listOf(
                        SelloDestination("recibo", "Recibo", SelloIcon.ReceiptLong),
                        SelloDestination("resumen", "Resumen", SelloIcon.Insights),
                        SelloDestination("metas", "Metas", SelloIcon.Flag),
                        SelloDestination("inversiones", "Inversiones", SelloIcon.AccountBalance)
                    ),
                    selected,
                    {
                        selected =
                            it
                    }
                )
            )
        }) {
            rule.onNodeWithText("Inversiones").assertIsDisplayed().performClick().assertIsSelected()
        }
    }

    @Test
    fun overlengthNoteIsPreservedAndDoneCannotSubmit() {
        var submitted = 0
        val note = "Una nota de prueba que supera el límite y conserva todas las palabras escritas."
        rule.setContent {
            SelloTheme {
                NoteComposer(
                    AnnotatedString("Gasto en Comida"), note, {}, "${note.length}/60",
                    "Opcional", "Máximo 60 caracteres", "Listo", "Cancelar", { submitted++ }, {}
                )
            }
        }
        rule.onNode(hasSetTextAction()).assertTextContains(note)
        rule.onNodeWithContentDescription("Listo").assertIsNotEnabled().performClick()
        assertEquals(0, submitted)
    }

    @Test
    fun editingAnExistingNoteStartsWithTheCursorAtTheEnd() {
        var note by mutableStateOf("Almuerzo")
        rule.setContent {
            SelloTheme {
                NoteComposer(
                    AnnotatedString("Gasto en Comida"), note, { note = it }, "${note.length}/60",
                    "Opcional", null, "Listo", "Cancelar", {}, {}
                )
            }
        }
        rule.onNode(hasSetTextAction()).performTextInput(" del equipo")
        assertEquals("Almuerzo del equipo", note)
    }

    @Test
    fun longNoteKeepsCancelAndDoneVisibleAtDoubleFont() {
        val note = "Nota sintética que conserva todas sus palabras. ".repeat(8)
        rule.inEveryScheme(fontScale = 2f, width = 320.dp, content = {
            NoteComposer(
                AnnotatedString("Gasto en Comida"), note, {}, "${note.length}/60",
                "Opcional", "Máximo 60 caracteres", "Listo", "Cancelar", {}, {}
            )
        }) {
            rule.onNode(hasSetTextAction()).assertTextContains(note)
            rule.onNodeWithText("Cancelar").assertIsDisplayed()
            rule.onNodeWithContentDescription("Listo").assertIsDisplayed().assertIsNotEnabled()
        }
    }

    @Test
    fun twoChipRowsAreVisiblyApartAndKeep48dpTargets() {
        rule.inEveryScheme(content = {
            V2ChipRows(4) { SelloChip("Categoría $it", false, {}) }
        }) {
            // A horizontal grid fills down first: 0 and 1 share the first column.
            val upper = rule.onNodeWithText("Categoría 0").getUnclippedBoundsInRoot()
            val lower = rule.onNodeWithText("Categoría 1").getUnclippedBoundsInRoot()
            assertTrue(lower.top - upper.bottom >= 6.dp)
            val chip = rule.onNodeWithText("Categoría 0").fetchSemanticsNode()
            val target = with(chip.layoutInfo.density) { chip.touchBoundsInRoot.height.toDp() }
            assertTrue(target >= 47.9.dp)
        }
    }

    @Test
    fun chipsInAColumnTakeTheWidestChipsWidth() {
        val names = listOf("Ocio", "Una categoría larga", "Café", "Ropa")
        rule.inEveryScheme(content = {
            V2ChipRows(names.size) { SelloChip(names[it], false, {}) }
        }) {
            val short = rule.onNodeWithText("Ocio").getUnclippedBoundsInRoot()
            val long = rule.onNodeWithText("Una categoría larga").getUnclippedBoundsInRoot()
            assertEquals(long.left, short.left)
            assertEquals(long.right, short.right)
        }
    }

    @Test
    fun aFewChipsStartAtTheLeadingEdge() {
        rule.inEveryScheme(content = {
            V2ChipRows(2, Modifier.testTag("rows")) {
                SelloChip(listOf("Salario", "Otro")[it], false, {})
            }
        }) {
            val rows = rule.onNodeWithTag("rows").getUnclippedBoundsInRoot()
            assertEquals(rows.left, rule.onNodeWithText("Salario").getUnclippedBoundsInRoot().left)
        }
    }

    @Test
    fun landscapeEntryKeepsAmountActionAndPadFixedWhileChoicesScroll() {
        rule.inEveryScheme(fontScale = 2f, width = 760.dp, densityScale = 0.4f, content = {
            Box(Modifier.height(400.dp)) {
                V2EntryLayout(
                    header = { Text("Gasto / Ingreso") },
                    amount = { Text("48.700", Modifier.testTag("amount")) },
                    choices = {
                        repeat(40) { Text("Categoría $it", Modifier.testTag("choice:$it")) }
                    },
                    action = { Text("Anotar gasto", Modifier.height(56.dp).testTag("action")) },
                    keypad = { Text("Sello pad", Modifier.height(192.dp).testTag("pad")) }
                )
            }
        }) {
            val amount = rule.onNodeWithTag("amount")
            val before = amount.getUnclippedBoundsInRoot().top
            val padBefore = rule.onNodeWithTag("pad").getUnclippedBoundsInRoot().top
            rule.onNodeWithTag("choice:39").performScrollTo().assertIsDisplayed()
            amount.assertIsDisplayed()
            assertEquals(before, amount.getUnclippedBoundsInRoot().top)
            assertEquals(padBefore, rule.onNodeWithTag("pad").getUnclippedBoundsInRoot().top)
            rule.onNodeWithTag("action").assertIsDisplayed()
        }
    }

    @Test
    fun reducedMotionRetainsTheEarnedMilestoneMeaningAfterWithdrawal() {
        rule.inEveryScheme(reducedMotion = true, content = {
            MilestonePlace(
                MilestoneSpec(
                    "50 %",
                    "50 por ciento, sello conservado tras un retiro",
                    MilestoneState.Kept
                ),
                land = true
            )
        }) {
            rule.onNodeWithContentDescription(
                "50 por ciento, sello conservado tras un retiro"
            ).assertIsDisplayed()
        }
    }
}
