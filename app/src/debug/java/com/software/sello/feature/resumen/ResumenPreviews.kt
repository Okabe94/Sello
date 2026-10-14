package com.software.sello.feature.resumen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.software.sello.designsystem.component.ChartPoint
import com.software.sello.designsystem.theme.SelloInk
import com.software.sello.designsystem.theme.SelloTheme
import com.software.sello.domain.model.Money
import com.software.sello.domain.model.PastVerdict
import com.software.sello.presentation.money.MoneyFormatter
import com.software.sello.presentation.money.ResourceMoneyLabels

internal fun resumenExample(money: MoneyFormatter): ResumenState.Ready {
    fun point(id: String, label: String, pesos: Long, day: Long? = null): ResumenPoint {
        val amount = money.format(Money.cop(pesos))
        return ResumenPoint(
            ChartPoint(id, label, pesos, "$label, ${amount.spoken}"),
            label,
            amount,
            day?.let { money.format(Money.cop(it)) }
        )
    }
    return ResumenState.Ready(
        month = "2026-10", income = money.format(Money.cop(150_000)),
        spent = money.format(Money.cop(30_000)), netFlow = money.format(Money.cop(120_000)),
        allocated = money.format(Money.cop(100_000)), unallocated = money.format(Money.cop(50_000)),
        actualVerdict = PastVerdict.WithinLimit, forecast = money.format(Money.cop(310_000)),
        daily = listOf(
            point("2026-10-01", "1 oct", 10_000, 10_000),
            point("2026-10-02", "2 oct", 25_000, 15_000),
            point("2026-10-03", "3 oct", 30_000, 5_000)
        ),
        history = listOf(
            point("2026-09", "sep", 20_000, 80_000),
            point("2026-10", "oct", 30_000, 100_000)
        ),
        categories = listOf(point("00000000-0000-4000-8000-000000000001", "Comida", 30_000)),
        empty = false, insufficientHistory = false, stale = false, asOf = "2026-10-03"
    )
}

@Composable
private fun SummaryPreview(ink: SelloInk, dark: Boolean, stale: Boolean = false) {
    val money = MoneyFormatter(ResourceMoneyLabels(LocalContext.current.resources))
    SelloTheme(ink = ink, darkTheme = dark, reducedMotion = true) {
        ResumenScreen(resumenExample(money).copy(stale = stale), {}, {}, {}, {}, PaddingValues())
    }
}

@Preview(name = "Cobalto claro", locale = "es", showBackground = true)
@Preview(name = "Fuente 2", locale = "es", fontScale = 2f)
@Composable
private fun CobaltoLightResumen() = SummaryPreview(SelloInk.Cobalto, false)

@Preview(name = "Cobalto oscuro", locale = "es")
@Composable
private fun CobaltoDarkResumen() = SummaryPreview(SelloInk.Cobalto, true)

@Preview(name = "Violeta claro ancho", locale = "es", widthDp = 840, heightDp = 900)
@Composable
private fun VioletaLightResumen() = SummaryPreview(SelloInk.Violeta, false)

@Preview(name = "Violeta oscuro, lectura anterior", locale = "es")
@Composable
private fun VioletaDarkResumen() = SummaryPreview(SelloInk.Violeta, true, stale = true)
