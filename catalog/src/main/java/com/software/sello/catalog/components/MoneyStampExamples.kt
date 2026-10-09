package com.software.sello.catalog.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.software.sello.designsystem.component.ButtonKind
import com.software.sello.designsystem.component.CountingMoneyText
import com.software.sello.designsystem.component.MoneyStyle
import com.software.sello.designsystem.component.MoneyText
import com.software.sello.designsystem.component.SelloButton
import com.software.sello.designsystem.component.Slip
import com.software.sello.designsystem.component.Stamp
import com.software.sello.designsystem.component.StampInk
import com.software.sello.designsystem.component.StampSize
import com.software.sello.designsystem.theme.SelloTheme

@Composable
private fun Caption(text: String) {
    Text(text, style = MaterialTheme.typography.labelMedium, color = SelloTheme.colors.inkSoft)
}

@Composable
fun MoneyTextExample() {
    Column(verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.slipGap)) {
        Slip(modifier = Modifier.fillMaxWidth()) {
            MoneyStyle.entries.forEach { style ->
                Caption(style.name)
                MoneyText(copFixture(937_200), style = style)
            }
        }
        Slip(modifier = Modifier.fillMaxWidth()) {
            Caption("Cero, ganancia, pérdida, otra moneda")
            MoneyText(copFixture(0), style = MoneyStyle.Card)
            MoneyText(
                copFixture(850_000, signed = true),
                style = MoneyStyle.Card,
                color = SelloTheme.colors.semantic.gain
            )
            MoneyText(
                copFixture(-10_000),
                style = MoneyStyle.Card,
                color = SelloTheme.colors.semantic.loss
            )
            MoneyText(usdFixture, style = MoneyStyle.Card)
        }
        Slip(modifier = Modifier.fillMaxWidth()) {
            Caption("Valores límite: nunca se recortan")
            MoneyText(
                copFixture(999_999_999_999),
                style = MoneyStyle.Hero,
                modifier = Modifier.testTag("money:hero-max-transaction")
            )
            MoneyText(
                copFixture(Long.MAX_VALUE),
                style = MoneyStyle.Hero,
                modifier = Modifier.testTag("money:hero-long-max")
            )
            MoneyText(copFixture(Long.MIN_VALUE), style = MoneyStyle.Total)
            MoneyText(copFixture(Long.MAX_VALUE))
        }
    }
}

private val CountingSteps = listOf(0L, 937_200L, 126_000L, 999_999_999_999L)

@Composable
fun CountingExample() {
    var step by rememberSaveable { mutableIntStateOf(1) }
    Slip(modifier = Modifier.fillMaxWidth()) {
        Caption("Cuenta hasta el valor nuevo; con movimiento reducido cambia de inmediato")
        CountingMoneyText(
            amountMinor = CountingSteps[step],
            format = { copFixture(it) },
            style = MoneyStyle.Total,
            modifier = Modifier.testTag("money:counting")
        )
        SelloButton(
            text = "Cambiar valor",
            onClick = { step = (step + 1) % CountingSteps.size },
            kind = ButtonKind.Outlined,
            modifier = Modifier.padding(top = SelloTheme.spacing.lg).testTag("money:next")
        )
    }
}

@Composable
fun StampExample() {
    var landings by rememberSaveable { mutableIntStateOf(0) }
    Column(verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.slipGap)) {
        Slip(modifier = Modifier.fillMaxWidth()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(SelloTheme.spacing.xl),
                modifier = Modifier.padding(SelloTheme.spacing.md)
            ) {
                Stamp("Al día", StampInk.Good)
                Stamp("Aportado", StampInk.Done)
                Stamp("Pasado", StampInk.Over)
                Stamp("Justo", StampInk.Neutral)
                Stamp("Recibido", StampInk.Good, size = StampSize.Large)
            }
        }
        Slip(modifier = Modifier.fillMaxWidth()) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(40.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = SelloTheme.spacing.xl)
            ) {
                key(landings) {
                    Stamp("Lograda", StampInk.Good, size = StampSize.Hero, land = landings > 0)
                }
                SelloButton(
                    text = "Sellar",
                    onClick = { landings++ },
                    kind = ButtonKind.Outlined,
                    modifier = Modifier.testTag("stamp:land")
                )
            }
        }
    }
}
