package com.software.sello.presentation.date

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.software.sello.R
import java.time.LocalDate

/**
 * How a day is written on a slip: "Hoy, vie 9 oct", "jue 8 oct", and with the year
 * when it is not this year's: "mié 31 dic 2025". Names come from resources.
 */
@Immutable
class DateLabels(
    private val weekdays: List<String>,
    private val months: List<String>,
    private val todayFormat: String
) {
    fun day(date: LocalDate, today: LocalDate): String {
        val weekday = weekdays[date.dayOfWeek.value - 1]
        val month = months[date.monthValue - 1]
        val plain = if (date.year == today.year) {
            "$weekday ${date.dayOfMonth} $month"
        } else {
            "$weekday ${date.dayOfMonth} $month ${date.year}"
        }
        return if (date == today) todayFormat.format(plain) else plain
    }
}

@Composable
fun dateLabels() = DateLabels(
    weekdays = stringArrayResource(R.array.weekday_names_short).toList(),
    months = stringArrayResource(R.array.month_names_abbreviated).toList(),
    todayFormat = stringResource(R.string.date_today)
)
