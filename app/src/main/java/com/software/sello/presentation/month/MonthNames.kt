package com.software.sello.presentation.month

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.software.sello.R
import java.time.YearMonth

/** Month names from resources: [full] as in a title, [short] as in a grid of months. */
@Immutable
class MonthNames(
    private val full: List<String>,
    private val short: List<String>,
    private val titleFormat: String
) {
    /** "Octubre 2026". */
    fun title(month: YearMonth): String = titleFormat.format(full[month.monthValue - 1], month.year)

    fun short(month: YearMonth): String = short[month.monthValue - 1]
}

@Composable
fun monthNames() = MonthNames(
    full = stringArrayResource(R.array.month_names).toList(),
    short = stringArrayResource(R.array.month_names_short).toList(),
    titleFormat = stringResource(R.string.month_title)
)
