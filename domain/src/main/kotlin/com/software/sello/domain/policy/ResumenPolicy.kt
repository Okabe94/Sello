package com.software.sello.domain.policy

import com.software.sello.domain.model.BudgetStanding
import com.software.sello.domain.model.Currency
import com.software.sello.domain.model.DailySpending
import com.software.sello.domain.model.ExactTotal
import com.software.sello.domain.model.Expense
import com.software.sello.domain.model.Income
import com.software.sello.domain.model.Money
import com.software.sello.domain.model.MonthlySnapshot
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.OverallBudget
import com.software.sello.domain.model.ResumenSnapshot
import com.software.sello.domain.model.SnapshotFailure
import com.software.sello.domain.model.SpendingMonth
import com.software.sello.domain.port.FinancialDay
import java.time.YearMonth

data class ResumenMonthInput(
    val month: YearMonth,
    val categories: List<CategoryInMonth>,
    val expenses: List<Expense>
)

object ResumenPolicy {
    fun monthsEndingIn(month: YearMonth): List<YearMonth> =
        (11 downTo 0).map { month.minusMonths(it.toLong()) }.filter { it.year >= 0 }

    fun snapshot(
        month: YearMonth,
        today: FinancialDay,
        generation: Long,
        revision: Long,
        months: List<ResumenMonthInput>,
        incomes: List<Income>
    ): Outcome<ResumenSnapshot, SnapshotFailure> {
        val currentMonth = YearMonth.from(today.date)
        if (month > currentMonth) return Outcome.Failure(SnapshotFailure.FutureMonth(currentMonth))
        require(months.map { it.month } == monthsEndingIn(month))
        val snapshots = mutableListOf<MonthlySnapshot>()
        for (input in months) {
            when (
                val snapshot = MonthlyBudgetPolicy.snapshot(
                    input.month,
                    today,
                    generation,
                    revision,
                    input.categories,
                    input.expenses
                )
            ) {
                is Outcome.Failure -> return snapshot
                is Outcome.Success -> snapshots += snapshot.value
            }
        }
        val budget = snapshots.last()
        val applicable = incomes.filter {
            YearMonth.from(it.date) == month && it.date <= today.date
        }
        val income = when (val sum = total(applicable.map { it.amount.money })) {
            is Outcome.Failure -> return sum
            is Outcome.Success -> sum.value
        }
        val limited = budget.overall as? OverallBudget.Limited
        val allocated = limited?.budget?.limit ?: Money.cop(0)
        val netFlow = when (val difference = income - budget.spent) {
            is Outcome.Failure -> return Outcome.Failure(SnapshotFailure.Unrepresentable)
            is Outcome.Success -> difference.value
        }
        val unallocated = when (val difference = income - allocated) {
            is Outcome.Failure -> return Outcome.Failure(SnapshotFailure.Unrepresentable)
            is Outcome.Success -> difference.value
        }
        val byDate = months.last().expenses
            .filter { YearMonth.from(it.date) == month && it.date <= today.date }
            .groupBy { it.date }
        val dayCount = if (month == currentMonth) today.date.dayOfMonth else month.lengthOfMonth()
        val daily = mutableListOf<DailySpending>()
        var running = ExactTotal.zero(Currency.COP)
        for (day in 1..dayCount) {
            val date = month.atDay(day)
            val spent = when (val sum = total(byDate[date].orEmpty().map { it.amount.money })) {
                is Outcome.Failure -> return sum
                is Outcome.Success -> sum.value
            }
            running += spent
            val cumulative = when (val sum = running.toMoney()) {
                is Outcome.Failure -> return Outcome.Failure(SnapshotFailure.Unrepresentable)
                is Outcome.Success -> sum.value
            }
            daily += DailySpending(date, spent, cumulative)
        }
        return Outcome.Success(
            ResumenSnapshot(
                budget,
                income,
                netFlow,
                allocated,
                unallocated,
                limited?.let { MonthlyBudgetPolicy.pastVerdict(it.budget.limit, budget.spent) },
                daily,
                snapshots.map { snapshot ->
                    val overall = snapshot.overall as? OverallBudget.Limited
                    SpendingMonth(
                        snapshot.month,
                        snapshot.spent,
                        overall?.budget?.limit,
                        (overall?.budget?.standing as? BudgetStanding.Current)?.forecast,
                        snapshot.categories.sumOf { it.expenseCount }
                    )
                },
                incomes.count { YearMonth.from(it.date) == month && it.date > today.date }
            )
        )
    }

    private fun total(amounts: List<Money>): Outcome<Money, SnapshotFailure> =
        when (val sum = ExactTotal.of(Currency.COP, amounts).toMoney()) {
            is Outcome.Success -> sum
            is Outcome.Failure -> Outcome.Failure(SnapshotFailure.Unrepresentable)
        }
}
