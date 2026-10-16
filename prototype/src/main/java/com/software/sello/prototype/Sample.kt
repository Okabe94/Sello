package com.software.sello.prototype

import com.software.sello.designsystem.component.MoneySign
import com.software.sello.designsystem.component.MoneyTextValue
import com.software.sello.designsystem.icon.SelloIcon

/*
 * The reference's sample month: October 2026, day 22 of 31. Every figure is a fixed
 * illustration copied from the design; nothing here is calculated or stored.
 */

/** Groups pesos for display only. Not a formatter to copy into the product. */
fun cop(pesos: Long, signed: Boolean = false): MoneyTextValue {
    val digits = pesos.toString().removePrefix("-")
        .reversed().chunked(3).joinToString(".").reversed()
    val sign = when {
        pesos < 0 -> MoneySign.Minus
        signed && pesos > 0 -> MoneySign.Plus
        else -> MoneySign.None
    }
    val words = when (sign) {
        MoneySign.Minus -> "menos $digits pesos"
        MoneySign.Plus -> "más $digits pesos"
        MoneySign.None -> "$digits pesos"
    }
    return MoneyTextValue(sign, "", digits, words)
}

fun grouped(pesos: Long): String = cop(pesos).digits.let { if (pesos < 0) "−$it" else it }

data class SampleCategory(
    val id: String,
    val name: String,
    val icon: SelloIcon,
    val left: Long,
    val limit: Long
) {
    val spent: Long get() = limit - left
    val over: Boolean get() = left < 0
    val fraction: Float get() = (spent.toFloat() / limit).coerceIn(0f, 1f)
}

object Sample {
    const val MONTH = "Octubre 2026"
    const val LEFT = 937_200L
    const val PER_DAY = 99_144L

    val categories = listOf(
        SampleCategory("entretenimiento", "Entretenimiento", SelloIcon.Theaters, -21_500, 250_000),
        SampleCategory("gimnasio", "Gimnasio", SelloIcon.FitnessCenter, 10_100, 110_000),
        SampleCategory("cafe", "Café", SelloIcon.LocalCafe, 8_800, 80_000),
        SampleCategory("hogar", "Hogar", SelloIcon.Home, 220_000, 1_400_000),
        SampleCategory("ropa", "Ropa", SelloIcon.Checkroom, 50_100, 150_000),
        SampleCategory("transporte", "Transporte", SelloIcon.DirectionsBus, 101_400, 350_000),
        SampleCategory("alimentacion", "Alimentación", SelloIcon.Restaurant, 287_600, 900_000),
        SampleCategory("educacion", "Educación", SelloIcon.School, 111_000, 300_000),
        SampleCategory("mascotas", "Mascotas", SelloIcon.Pets, 55_700, 150_000),
        SampleCategory("salud", "Salud", SelloIcon.MedicalServices, 114_000, 200_000)
    )

    fun category(id: String): SampleCategory = categories.firstOrNull { it.id == id } ?: categories[6]

    data class Expense(
        val note: String,
        val day: String,
        val amount: Long,
        val repeats: Boolean = false
    )

    val expenses = listOf(
        Expense("Rappi, almuerzo", "mié 21", 20_600),
        Expense("Carulla", "mar 20", 33_500),
        Expense("Plaza Minorista", "dom 18", 28_700),
        Expense("Éxito, mercado quincenal", "sáb 17", 142_000),
        Expense("Panadería", "vie 16", 12_400),
        Expense("D1", "jue 15", 46_900),
        Expense("Rappi, cena", "mar 13", 38_200),
        Expense("Carulla", "dom 11", 51_300),
        Expense("Almuerzo trabajo", "vie 9", 22_000),
        Expense("Plaza Minorista", "mar 6", 31_500),
        Expense("Éxito, mercado quincenal", "sáb 3", 186_500),
        Expense("Panadería", "vie 2", 9_800),
        Expense("Rappi, almuerzo", "jue 1", 18_500)
    )

    data class Recurring(val name: String, val detail: String, val amount: Long, val due: Boolean)

    val recurring = listOf(
        Recurring("Arriendo", "Hogar · el 5 de cada mes", 950_000, false),
        Recurring("Gimnasio", "Gimnasio · el 9 de cada mes", 99_900, false),
        Recurring("Servicios públicos", "Hogar · el 15 de cada mes", 230_000, false),
        Recurring("Curso de inglés", "Educación · el 16 de cada mes", 189_000, false),
        Recurring("Suscripciones", "Entretenimiento · el 28 de cada mes", 44_900, true)
    )

    val recurringIncome = listOf(
        Recurring("Salario", "Salario · el 1 de cada mes", 5_200_000, false)
    )

    data class Income(val name: String, val detail: String, val amount: Long)

    val incomes = listOf(
        Income("Salario", "jue 1 oct, se repite", 5_200_000),
        Income("Diseño freelance", "mié 14 oct", 850_000)
    )

    data class Investment(
        val id: String,
        val name: String,
        val kind: String,
        val value: Long,
        val gain: Long,
        val percent: String
    )

    val investments = listOf(
        Investment("parqueadero", "Parqueadero", "Finca raíz", 19_350_000, 1_340_000, "+7,4 %"),
        Investment("cdt", "CDT", "CDT a 360 días", 10_746_000, 750_000, "+7,5 %"),
        Investment("etf", "ETF S&P 500", "ETF en dólares", 9_635_000, 1_435_000, "+17,5 %"),
        Investment("fic", "FIC Acciones", "Fondo colectivo", 5_742_000, -252_000, "−4,2 %"),
        Investment("ecopetrol", "Ecopetrol", "Acciones", 3_228_000, 228_000, "+7,6 %")
    )

    fun investment(id: String): Investment = investments.firstOrNull { it.id == id } ?: investments[2]

    data class Movement(val type: String, val day: String, val amount: Long)

    val movements = listOf(
        Movement("Aporte", "sáb 3 oct", 300_000),
        Movement("Dividendo", "mar 29 sep", 24_100),
        Movement("Aporte", "jue 3 sep", 300_000),
        Movement("Valor actual", "lun 31 ago", 9_210_000)
    )

    data class Milestone(val label: String, val month: String, val reached: Boolean)

    data class Goal(
        val id: String,
        val name: String,
        val saved: Long,
        val target: Long,
        val percent: Int,
        val by: String,
        val milestones: List<Milestone>,
        val done: Boolean = false
    )

    val goals = listOf(
        Goal(
            "emergencia",
            "Fondo de emergencia",
            8_500_000,
            15_000_000,
            57,
            "jun 2027",
            listOf(
                Milestone("25 %", "mar", true),
                Milestone("50 %", "sep", true),
                Milestone("75 %", "feb 27", false),
                Milestone("Meta", "jun 27", false)
            )
        ),
        Goal(
            "europa",
            "Vacaciones Europa",
            4_550_000,
            12_000_000,
            38,
            "ene 2028",
            listOf(
                Milestone("25 %", "jul", true),
                Milestone("50 %", "ene 27", false),
                Milestone("75 %", "jul 27", false),
                Milestone("Meta", "ene 28", false)
            )
        ),
        Goal(
            "bicicleta",
            "Bicicleta",
            2_400_000,
            2_400_000,
            100,
            "30 sep",
            listOf(
                Milestone("25 %", "mar", true),
                Milestone("50 %", "may", true),
                Milestone("75 %", "jul", true),
                Milestone("Meta", "sep", true)
            ),
            done = true
        )
    )

    fun goal(id: String): Goal = goals.firstOrNull { it.id == id } ?: goals[0]
}
