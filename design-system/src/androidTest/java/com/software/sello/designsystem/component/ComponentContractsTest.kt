package com.software.sello.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.test.isHeading
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
import androidx.compose.ui.unit.width
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.icon.painter
import com.software.sello.designsystem.theme.SelloTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ComponentContractsTest {
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
    fun anAnswerSlipWithADescriptionIsReadAsThatOneSentenceAndItsDetailOnItsOwn() {
        rule.inEveryScheme(fontScale = 2f, width = 320.dp, content = {
            AnswerSlip(
                "Restante del presupuesto",
                MoneyTextValue(MoneySign.Minus, "", "999.999.999.999", "menos 999.999.999.999"),
                "Excedido",
                StampInk.Over,
                description = "Restante del presupuesto: menos 999.999.999.999. Excedido."
            ) { Text("detalle", Modifier.testTag("detail")) }
        }) {
            rule.onNodeWithContentDescription(
                "Restante del presupuesto: menos 999.999.999.999. Excedido."
            ).assertIsDisplayed()
            // Only the sentence: the label and stamp are not read a second time.
            rule.onNodeWithText("Restante del presupuesto").assertDoesNotExist()
            rule.onNodeWithText("Excedido").assertDoesNotExist()
            rule.onNodeWithTag("detail").assertTextContains("detalle")
        }
    }

    @Test
    fun anEntryAmountSlipShowsRefusedPastedTextAsItWasAndOffersPasteOnlyWhenAsked() {
        var paste by mutableStateOf(true)
        rule.inEveryScheme(content = {
            EntryAmountSlip(
                "Gasto en pesos",
                amount = null,
                error = "Eso no es un monto.",
                rejectedText = "1e3",
                onPaste = if (paste) {
                    {}
                } else {
                    null
                }
            )
        }) {
            rule.onNodeWithText("1e3").assertIsDisplayed()
            rule.onNodeWithText("Eso no es un monto.").assertIsDisplayed()
            rule.onNodeWithText("Gasto en pesos").assertDoesNotExist()
            rule.onNodeWithContentDescription("Pegar").assertIsDisplayed()
        }
        paste = false
        rule.waitForIdle()

        rule.onNodeWithContentDescription("Pegar").assertDoesNotExist()
    }

    @Test
    fun anAnswerSlipCanHaveNoStampAndThenShowsOnlyItsLabelAndAmount() {
        rule.inEveryScheme(content = {
            AnswerSlip(
                "Entró en octubre",
                MoneyTextValue(MoneySign.None, "", "150.000", "150.000 pesos"),
                stamp = null,
                ink = StampInk.Neutral
            )
        }) {
            rule.onNodeWithText("Entró en octubre").assertIsDisplayed()
            rule.onNodeWithContentDescription("150.000 pesos").assertIsDisplayed()
            rule.onNodeWithText("Al día").assertDoesNotExist()
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
            val node = button.fetchSemanticsNode()
            assertTrue(
                with(node.layoutInfo.density) {
                    node.touchBoundsInRoot.height.toDp()
                } >= 47.9.dp
            )
        }
        assertEquals(4, returns)
    }

    @Test
    fun atFontScale13TheRibbonPutsItsReturnUnderAMessageThatKeepsTheWidth() = ribbonStacksAt(1.3f)

    @Test
    fun atTwiceTheFontSizeTheRibbonPutsItsReturnUnderAMessageThatKeepsTheWidth() =
        ribbonStacksAt(2f)

    private fun ribbonStacksAt(scale: Float) {
        rule.inEveryScheme(fontScale = scale, width = 320.dp, content = {
            PastMonthRibbon(
                AnnotatedString("Estás viendo septiembre de 2025"),
                "Volver a noviembre",
                {},
                Modifier.testTag("ribbon")
            )
        }) { scheme ->
            val ribbon = rule.onNodeWithTag("ribbon").getUnclippedBoundsInRoot()
            val message = rule.onNodeWithText("Estás viendo septiembre de 2025")
                .getUnclippedBoundsInRoot()
            val button = rule.onNodeWithText("Volver a noviembre").getUnclippedBoundsInRoot()
            // Squeezed beside the button, the message became a column of letters.
            assertTrue("$scheme: $message in $ribbon", message.width > ribbon.width / 2)
            assertTrue("$scheme: $button under $message", button.top >= message.bottom)
            assertTrue("$scheme: $button in $ribbon", button.right <= ribbon.right)
        }
    }

    @Test
    fun ribbonKeepsItsReturnButtonOnTheMessagesLine() {
        rule.inEveryScheme(content = {
            PastMonthRibbon(AnnotatedString("Estás viendo septiembre"), "Volver a octubre", {})
        }) {
            val message = rule.onNodeWithText("Estás viendo septiembre").getUnclippedBoundsInRoot()
            val button = rule.onNodeWithText("Volver a octubre").getUnclippedBoundsInRoot()
            val middle = (button.top + button.bottom) / 2
            assertTrue(middle > message.top && middle < message.bottom)
            assertTrue(button.left > message.right)
        }
    }

    @Test
    fun monthBarButtonHugsTheMonthAndLeavesRoomForActions() {
        rule.inEveryScheme(content = {
            MonthBar("Octubre", "2026", {}, Modifier.testTag("bar"))
        }) {
            val bar = rule.onNodeWithTag("bar").getUnclippedBoundsInRoot()
            val button = rule.onNodeWithContentDescription(
                "Octubre 2026"
            ).getUnclippedBoundsInRoot()
            // Two 48dp actions still fit beside the month button on the narrowest bar.
            assertTrue(button.right - button.left <= (bar.right - bar.left) - 96.dp)
        }
    }

    @Test
    fun allFourDestinationsExposeSelectionAndLabelsAtDoubleFont() {
        var selected by mutableStateOf("recibo")
        rule.inEveryScheme(fontScale = 2f, width = 320.dp, content = {
            SelloNavigationBar(
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
            ChipRows(4) { SelloChip("Categoría $it", false, {}) }
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
            ChipRows(names.size) { SelloChip(names[it], false, {}) }
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
            ChipRows(2, Modifier.testTag("rows")) {
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
                EntryLayout(
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
    fun completedGoalRowCarriesItsStampOnTheRight() {
        rule.inEveryScheme(content = {
            CompletedGoalRow("Vacunas de Luna", "400.000, 30 sep", SelloIcon.Pets, "Lograda", {})
        }) {
            val name = rule.onNodeWithText(
                "Vacunas de Luna",
                useUnmergedTree = true
            ).getUnclippedBoundsInRoot()
            val stamp = rule.onNodeWithContentDescription(
                "Lograda",
                useUnmergedTree = true
            ).getUnclippedBoundsInRoot()
            assertTrue(stamp.left > name.right)
            assertTrue(stamp.top < name.bottom)
        }
    }

    @Test
    fun activeGoalCardPutsTheDateBesideTheTargetAndSpreadsItsPlaces() {
        val places = listOf("25 %", "50 %", "75 %", "Meta").map {
            MilestoneSpec(it, "$it, por alcanzar", MilestoneState.Empty)
        }
        rule.inEveryScheme(content = {
            GoalCard(
                "Curso de inglés", SelloIcon.School, "15 %",
                MoneyTextValue(MoneySign.None, "", "300.000", "300.000 pesos"),
                "de 2.000.000", "jun 2027", 0.15f, places, {}, Modifier.testTag("card")
            )
        }) {
            val target = rule.onNodeWithText(
                "de 2.000.000",
                useUnmergedTree = true
            ).getUnclippedBoundsInRoot()
            val date = rule.onNodeWithText(
                "jun 2027",
                useUnmergedTree = true
            ).getUnclippedBoundsInRoot()
            val middle = (date.top + date.bottom) / 2
            assertTrue(middle > target.top && middle < target.bottom)
            val card = rule.onNodeWithTag("card").getUnclippedBoundsInRoot()
            val meta = rule.onNodeWithText(
                "Meta",
                useUnmergedTree = true
            ).getUnclippedBoundsInRoot()
            // The last place reaches the card's trailing padding, and the teeth follow closely.
            assertTrue(card.right - meta.right < 40.dp)
            assertTrue(card.bottom - meta.bottom < 44.dp)
        }
    }

    @Test
    fun returnShowsItsPercentageAndAmountOnOneLine() {
        rule.inEveryScheme(content = {
            PerformanceLine("+5,6 %", "+2.066.000 de ganancia", false)
        }) {
            val percentage = rule.onNodeWithText(
                "+5,6 %",
                useUnmergedTree = true
            ).getUnclippedBoundsInRoot()
            val amount = rule.onNodeWithText("+2.066.000 de ganancia", useUnmergedTree = true)
                .getUnclippedBoundsInRoot()
            val middle = (amount.top + amount.bottom) / 2
            assertTrue(middle > percentage.top && middle < percentage.bottom)
            assertTrue(amount.left > percentage.right)
        }
    }

    @Test
    fun investmentRowCarriesItsValueAndChangeOnTheRight() {
        rule.inEveryScheme(content = {
            InvestmentRow(
                "Parqueadero",
                "Finca raíz · valor del 12 jun",
                SelloIcon.Home,
                SelloTheme.colors.brand,
                MoneyTextValue(MoneySign.None, "", "19.000.000", "19.000.000 pesos"),
                "Sin valor",
                "+7,5 %",
                {}
            )
        }) {
            val name = rule.onNodeWithText(
                "Parqueadero",
                useUnmergedTree = true
            ).getUnclippedBoundsInRoot()
            val change = rule.onNodeWithText(
                "+7,5 %",
                useUnmergedTree = true
            ).getUnclippedBoundsInRoot()
            assertTrue(change.left > name.right)
            assertTrue(change.top < name.bottom + 24.dp)
        }
    }

    @Test
    fun titleBarNamesTheTabAsAHeadingAndKeepsActionsReachable() {
        var added = 0
        rule.inEveryScheme(fontScale = 2f, width = 320.dp, content = {
            TitleBar("Inversiones") {
                IconButton({ added++ }) { Icon(SelloIcon.Add.painter(), "Agregar inversión") }
            }
        }) {
            rule.onNode(isHeading()).assertTextContains("Inversiones")
            val add = rule.onNodeWithContentDescription("Agregar inversión")
            add.assertIsDisplayed().performClick()
            val node = add.fetchSemanticsNode()
            assertTrue(
                with(node.layoutInfo.density) {
                    node.touchBoundsInRoot.height.toDp()
                } >= 47.9.dp
            )
        }
        assertEquals(4, added)
    }

    @Test
    fun goalsBandTotalSpeaksTheExactSavedAmount() {
        rule.inEveryScheme(content = {
            GoalsBandTotal(
                "Guardado en metas",
                MoneyTextValue(MoneySign.None, "", "2.460.000", "2.460.000 pesos"),
                "350.000 en octubre"
            )
        }) {
            rule.onNodeWithContentDescription("2.460.000 pesos").assertIsDisplayed()
            rule.onNodeWithText("350.000 en octubre").assertIsDisplayed()
        }
    }

    @Test
    fun addChipIsAReachableButtonAtDoubleFont() {
        var added = 0
        rule.inEveryScheme(fontScale = 2f, width = 320.dp, content = {
            AddChip("Nueva", { added++ })
        }) {
            val chip = rule.onNodeWithText("Nueva")
            chip.assertIsDisplayed().performClick()
            val node = chip.fetchSemanticsNode()
            assertTrue(
                with(node.layoutInfo.density) {
                    node.touchBoundsInRoot.height.toDp()
                } >= 47.9.dp
            )
        }
        assertEquals(4, added)
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
