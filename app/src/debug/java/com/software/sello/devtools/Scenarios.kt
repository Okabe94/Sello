package com.software.sello.devtools

import com.software.sello.devtools.ScenarioStep.AdvanceTo
import com.software.sello.devtools.ScenarioStep.Category
import com.software.sello.devtools.ScenarioStep.Expense
import com.software.sello.devtools.ScenarioStep.Income
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlin.random.Random

private val Bogota = ZoneId.of("America/Bogota")

/** The scenarios the tools offer, all synthetic. */
object Scenarios {
    /**
     * G01 in the financial examples: cumulative spending 10.000, 25.000 and 30.000 of
     * a 100.000 limit, so 70.000 remain; income 200.000, so income less spending is
     * 170.000 and 100.000 is not assigned to the budget.
     */
    val G01 = Scenario(
        id = "g01",
        version = 1,
        seed = 1,
        zone = Bogota,
        start = LocalDate.of(2026, 10, 1),
        title = "G01 · Tres gastos y un ingreso",
        summary = "1–3 oct 2026: quedan 70.000 de 100.000; ingresos menos gastos 170.000.",
        steps = listOf(
            Category("comida", "Alimentación", "restaurant", 100_000),
            Expense("comida", 10_000),
            AdvanceTo(LocalDate.of(2026, 10, 2)),
            Expense("comida", 15_000),
            AdvanceTo(LocalDate.of(2026, 10, 3)),
            Expense("comida", 5_000),
            Income(200_000, "salary")
        )
    )

    /**
     * Month and year changes: Comida goes over its 50.000 in October and starts again in
     * November; Ocio has no limit; the year turns from 31 Dec to 1 Jan.
     */
    val MonthAndYearEnds = Scenario(
        id = "fin-de-mes",
        version = 1,
        seed = 1,
        zone = Bogota,
        start = LocalDate.of(2026, 10, 30),
        title = "Fin de mes y de año",
        summary = "Comida se pasa de su límite en octubre, vuelve a empezar en noviembre; " +
            "Ocio sin límite; 31 dic → 1 ene.",
        steps = listOf(
            Category("comida", "Comida", "restaurant", 50_000),
            Category("ocio", "Ocio", "theaters", null),
            Expense("comida", 30_000),
            AdvanceTo(LocalDate.of(2026, 10, 31)),
            Expense("comida", 35_000),
            Expense("ocio", 20_000),
            AdvanceTo(LocalDate.of(2026, 11, 1)),
            Expense("comida", 10_000),
            AdvanceTo(LocalDate.of(2026, 12, 31)),
            Expense("comida", 12_000),
            AdvanceTo(LocalDate.of(2027, 1, 1)),
            Expense("comida", 8_000)
        )
    )

    /**
     * Six months, May to October 2026, of salary on the 1st and spending on the 3rd, 12th
     * and 24th, so Resumen's month-by-month columns grow one month at a time. The
     * amounts come from [Scenario.seed], so they are the same on every replay.
     */
    val SixMonths: Scenario = run {
        val seed = 20_261_001L
        val random = Random(seed)
        val steps = mutableListOf<ScenarioStep>(
            Category("comida", "Comida", "restaurant", 600_000),
            Category("hogar", "Hogar", "home", 400_000)
        )
        var month = YearMonth.of(2026, 5)
        repeat(6) { index ->
            if (index > 0) steps += AdvanceTo(month.atDay(1))
            steps += Income(1_800_000, "salary")
            for (day in listOf(3, 12, 24)) {
                steps += AdvanceTo(month.atDay(day))
                steps += Expense("comida", 1_000L * random.nextInt(40, 180))
                steps += Expense("hogar", 1_000L * random.nextInt(20, 140))
            }
            month = month.plusMonths(1)
        }
        Scenario(
            id = "seis-meses",
            version = 1,
            seed = seed,
            zone = Bogota,
            start = LocalDate.of(2026, 5, 1),
            title = "Seis meses",
            summary = "Mayo a octubre de 2026: un salario y seis gastos al mes.",
            steps = steps
        )
    }

    val all: List<Scenario> = listOf(G01, MonthAndYearEnds, SixMonths)

    fun find(key: String?): Scenario? = all.firstOrNull { it.key == key }
}
