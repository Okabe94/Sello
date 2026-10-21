package com.software.sello.feature.resumen

import com.software.sello.designsystem.component.ChartPeriod
import com.software.sello.designsystem.component.ChartPoint
import com.software.sello.domain.model.ResumenSnapshot
import com.software.sello.domain.port.ResumenSnapshotState
import com.software.sello.presentation.money.MoneyFormatter
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class ResumenRenderModel(private val money: MoneyFormatter) {
    fun render(state: ResumenSnapshotState): ResumenState = when (state) {
        ResumenSnapshotState.Loading -> ResumenState.Loading

        is ResumenSnapshotState.Ready -> month(state.snapshot, false)

        is ResumenSnapshotState.Failed -> state.lastGood?.let { month(it, true) }
            ?: ResumenState.Failed
    }

    private fun month(snapshot: ResumenSnapshot, stale: Boolean): ResumenState.Ready {
        val budget = snapshot.budget
        val firstRecordedMonth = snapshot.history.indexOfFirst { it.expenseCount > 0 }
        val history = if (firstRecordedMonth < 0) {
            emptyList()
        } else {
            snapshot.history.drop(firstRecordedMonth)
        }
        return ResumenState.Ready(
            month = budget.month.toString(),
            income = money.format(snapshot.income),
            spent = money.format(budget.spent),
            netFlow = money.format(snapshot.netFlow),
            allocated = money.format(snapshot.allocated),
            unallocated = money.format(snapshot.unallocated),
            daily = snapshot.daily.map { day ->
                val label = day.date.format(dayFormat)
                val amount = money.format(day.cumulative)
                ResumenPoint(
                    ChartPoint(
                        day.date.toString(),
                        day.date.dayOfMonth.toString(),
                        day.cumulative.minorUnits,
                        "$label, ${amount.spoken}"
                    ),
                    label,
                    amount,
                    money.format(day.spent)
                )
            },
            history = history.map { entry ->
                val label = entry.month.format(monthFormat)
                val amount = money.format(entry.spent)
                val limit = entry.limit
                // The month in progress is always drawn as such, over its limit or not:
                // it is never a whole month.
                val current = entry.month == YearMonth.from(budget.asOf)
                ResumenPoint(
                    ChartPoint(
                        entry.month.toString(),
                        entry.month.format(axisMonthFormat),
                        entry.spent.minorUnits,
                        "$label, ${amount.spoken}",
                        when {
                            current -> ChartPeriod.Current

                            limit != null && entry.spent.minorUnits > limit.minorUnits ->
                                ChartPeriod.Over

                            else -> ChartPeriod.Past
                        }
                    ),
                    label,
                    amount,
                    entry.limit?.let(money::format),
                    current
                )
            },
            monthLimit = snapshot.history.lastOrNull { it.month == budget.month }?.limit?.let {
                ResumenLimit(it.minorUnits, money.format(it))
            },
            categories = budget.categories.filter { it.expenseCount > 0 }
                .sortedByDescending { it.spent.minorUnits }.map { category ->
                    ResumenCategory(
                        category.category.id.value,
                        category.category.name.value,
                        category.category.icon.value,
                        money.format(category.spent),
                        share(category.spent.minorUnits, budget.spent.minorUnits)
                    )
                },
            empty = history.isEmpty(),
            insufficientHistory = snapshot.history.count { it.expenseCount > 0 } < 2,
            stale = stale,
            asOf = budget.asOf.toString()
        )
    }

    /** How much of a bar to fill: geometry only, never shown or turned back into money. */
    private fun share(part: Long, whole: Long): Float = when {
        whole <= 0 || part <= 0 -> 0f
        part >= whole -> 1f
        else -> (part.toDouble() / whole.toDouble()).toFloat()
    }

    private companion object {
        val locale: Locale = Locale.forLanguageTag("es-CO")
        val dayFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM", locale)
        val monthFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM uuuu", locale)
        val axisMonthFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM", locale)
    }
}
