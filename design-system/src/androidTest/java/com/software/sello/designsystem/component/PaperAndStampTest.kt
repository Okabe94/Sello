package com.software.sello.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PaperAndStampTest {
    @get:Rule
    val rule = createComposeRule()

    private val isButton = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Button)

    @Test
    fun stampsAnnounceTheOrdinaryWordWhateverTheirInkOrSize() {
        rule.inEveryScheme(
            content = {
                Column {
                    Stamp("Al día", StampInk.Good, Modifier.testTag("small"))
                    Stamp("Pasado", StampInk.Over, Modifier.testTag("large"), StampSize.Large)
                    Stamp(
                        "Lograda",
                        StampInk.Done,
                        Modifier.testTag("hero"),
                        StampSize.Hero,
                        land = true
                    )
                }
            }
        ) {
            rule.onNodeWithTag("small").assertContentDescriptionEquals("Al día")
            rule.onNodeWithTag("large").assertContentDescriptionEquals("Pasado")
            rule.onNodeWithTag("hero").assertContentDescriptionEquals("Lograda")
            // A stamp is a verdict, never a control.
            rule.onNodeWithTag("hero").assertHasNoClickAction()
        }
    }

    @Test
    fun aLandingStampIsFullyReadableWhileItComesDown() {
        rule.mainClock.autoAdvance = false
        rule.inEveryScheme(
            reducedMotion = false,
            content = { Stamp("Recibido", StampInk.Good, Modifier.testTag("stamp"), land = true) }
        ) {
            rule.onNodeWithTag("stamp").assertContentDescriptionEquals("Recibido")
        }
    }

    @Test
    fun aClickableSlipIsOneButtonThatReportsEachPress() {
        var presses = 0
        rule.inEveryScheme(
            reset = { presses = 0 },
            content = {
                Slip(modifier = Modifier.testTag("slip"), onClick = { presses++ }) {
                    Text("Te quedan")
                    MoneyText(MoneyTextValue(MoneySign.None, "$", "937.200", "937.200 pesos"))
                }
            }
        ) {
            rule.onNodeWithTag("slip").assert(isButton).assertHasClickAction()
            rule.onNodeWithTag("slip").assertTextContains("Te quedan")
            rule.onNodeWithTag("slip").assertContentDescriptionEquals("937.200 pesos")
            rule.onNodeWithTag("slip").performClick()
            assertEquals(it, 1, presses)
        }
    }

    @Test
    fun receiptLinesReadAsLabelThenValue() {
        rule.inEveryScheme(
            content = {
                Slip {
                    LeaderLine("Uber", Modifier.testTag("leader"), caption = "Transporte") {
                        MoneyText(MoneyTextValue(MoneySign.None, "$", "18.400", "18.400 pesos"))
                    }
                    LeaderLine("Fecha", "jue 22 oct", Modifier.testTag("plain"))
                    LeaderLine("Abrir", "Detalle", Modifier.testTag("action"), onClick = {})
                    TotalLine("Total", Modifier.testTag("total")) {
                        MoneyText(
                            MoneyTextValue(MoneySign.None, "$", "126.000", "126.000 pesos"),
                            style = MoneyStyle.Title
                        )
                    }
                    KeyValue("Límite", "Sin límite", Modifier.testTag("kv"), caption = "este mes")
                }
            }
        ) {
            rule.onNodeWithTag("leader").assertTextContains("Uber")
            rule.onNodeWithTag("leader").assertTextContains("Transporte")
            rule.onNodeWithTag("leader").assertContentDescriptionEquals("18.400 pesos")
            rule.onNodeWithTag("plain").assertTextContains("Fecha")
            rule.onNodeWithTag("plain").assertTextContains("jue 22 oct")
            rule.onNodeWithTag("total").assertTextContains("Total")
            rule.onNodeWithTag("total").assertContentDescriptionEquals("126.000 pesos")
            rule.onNodeWithTag("kv").assertTextContains("Límite")
            rule.onNodeWithTag("kv").assertTextContains("Sin límite")
            rule.onNodeWithTag("action").assert(isButton)
            val action = rule.onNodeWithTag("action").getUnclippedBoundsInRoot().height
            assertTrue("$it action line is $action", action >= 48.dp)
        }
    }
}
