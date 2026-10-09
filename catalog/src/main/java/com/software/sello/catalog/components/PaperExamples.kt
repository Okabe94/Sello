package com.software.sello.catalog.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import com.software.sello.designsystem.component.KeyValue
import com.software.sello.designsystem.component.LeaderLine
import com.software.sello.designsystem.component.MoneyStyle
import com.software.sello.designsystem.component.MoneyText
import com.software.sello.designsystem.component.Slip
import com.software.sello.designsystem.component.Stamp
import com.software.sello.designsystem.component.StampInk
import com.software.sello.designsystem.component.TearLine
import com.software.sello.designsystem.component.TotalLine
import com.software.sello.designsystem.icon.SelloIcon
import com.software.sello.designsystem.theme.SelloTheme

@Composable
private fun Stack(content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.slipGap)) { content() }
}

@Composable
private fun Soft(text: String) {
    Text(text, style = MaterialTheme.typography.bodyLarge, color = SelloTheme.colors.inkSoft)
}

@Composable
fun SlipExample() {
    var presses by rememberSaveable { mutableIntStateOf(0) }
    Stack {
        Slip(modifier = Modifier.fillMaxWidth()) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Soft("Te quedan")
                Stamp("Al día", StampInk.Good)
            }
            MoneyText(copFixture(937_200), style = MoneyStyle.Hero)
        }
        Slip(
            modifier = Modifier.fillMaxWidth().testTag("slip:clickable"),
            onClick = { presses++ }
        ) {
            Soft("Slip con acción")
            Text("Pulsado $presses veces", style = MaterialTheme.typography.titleLarge)
        }
        Slip(modifier = Modifier.fillMaxWidth(), pinked = false) {
            Soft("Sin dientes: diálogos y tiquetes")
        }
    }
}

@Composable
fun TearLineExample() {
    Slip(modifier = Modifier.fillMaxWidth()) {
        Soft("Te quedan")
        MoneyText(copFixture(937_200), style = MoneyStyle.Total)
        TearLine()
        Soft("Para 9 días")
        MoneyText(copFixture(99_144), style = MoneyStyle.Title)
        TearLine()
        Soft("Como máximo dos por slip")
    }
}

@Composable
fun LeaderLineExample() {
    Stack {
        Slip(modifier = Modifier.fillMaxWidth()) {
            LeaderLine("Salario", icon = SelloIcon.Home, caption = "jue 1 oct") {
                MoneyText(copFixture(5_200_000))
            }
            LeaderLine("Diseño freelance") {
                MoneyText(
                    copFixture(850_000, signed = true),
                    color = SelloTheme.colors.semantic.gain
                )
            }
            LeaderLine("Entretenimiento") {
                MoneyText(copFixture(-21_500), color = SelloTheme.colors.semantic.loss)
            }
            LeaderLine("Fecha", "jue 22 oct")
            LeaderLine("Con acción", "Abrir", onClick = {})
        }
        Slip(modifier = Modifier.fillMaxWidth()) {
            LeaderLine("Una categoría con un nombre muy largo que ocupa la línea") {
                MoneyText(copFixture(999_999_999_999))
            }
            LeaderLine("Total acumulado") { MoneyText(copFixture(Long.MAX_VALUE)) }
        }
    }
}

@Composable
fun TotalLineExample() {
    Slip(modifier = Modifier.fillMaxWidth()) {
        LeaderLine("Uber", caption = "Transporte") { MoneyText(copFixture(18_400)) }
        LeaderLine("Farmacia Pasteur", caption = "Salud") { MoneyText(copFixture(75_100)) }
        LeaderLine("Carulla", caption = "Alimentación") { MoneyText(copFixture(32_500)) }
        TotalLine("Total") {
            MoneyText(copFixture(126_000), style = MoneyStyle.Title, fontWeight = FontWeight.W900)
        }
    }
}

@Composable
fun KeyValueExample() {
    Slip(modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(SelloTheme.spacing.xl)) {
            KeyValue("Gastado", icon = SelloIcon.ReceiptLong) {
                MoneyText(copFixture(1_062_800), style = MoneyStyle.Title)
            }
            KeyValue("Límite", caption = "de este mes") {
                MoneyText(copFixture(2_000_000), style = MoneyStyle.Title)
            }
        }
        TearLine()
        KeyValue("Tu día más caro", "Sábado", caption = "Una nota que puede ocupar dos líneas")
    }
}
