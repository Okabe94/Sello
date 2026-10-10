package com.software.sello.domain.policy

import com.software.sello.domain.model.BudgetLimit
import com.software.sello.domain.model.BudgetStanding
import com.software.sello.domain.model.Category
import com.software.sello.domain.model.CategoryMonth
import com.software.sello.domain.model.Currency
import com.software.sello.domain.model.DailyAllowance
import com.software.sello.domain.model.ExactTotal
import com.software.sello.domain.model.Expense
import com.software.sello.domain.model.Forecast
import com.software.sello.domain.model.LimitedBudget
import com.software.sello.domain.model.Money
import com.software.sello.domain.model.MonthlySnapshot
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.OverallBudget
import com.software.sello.domain.model.PastVerdict
import com.software.sello.domain.model.SnapshotFailure
import com.software.sello.domain.model.Verdict
import com.software.sello.domain.port.FinancialDay
import java.math.BigInteger
import java.time.YearMonth

/** A category with its budget state in the month being read. */
data class CategoryInMonth(val category: Category, val state: MonthBudgetState)

/**
 * The one place a month's figures are worked out (decision D03). Screens, charts and
 * later alerts take them from the snapshot and never recompute them.
 *
 * With `S` spent, `L` the sum of finite limits, `d` today's day of the month and `N`
 * the month's length: the forecast is `S × N / d`, today counting towards the pace
 * but not towards the days still to come; the days available are `N − d + 1`, today
 * included. All arithmetic is exact: no floating point, and comparisons are made on
 * whole numbers before anything is rounded for display.
 */
object MonthlyBudgetPolicy {
    private val FIFTY = BigInteger.valueOf(50)
    private val HUNDRED = BigInteger.valueOf(100)
    private val NINETY_FIVE = BigInteger.valueOf(95)

    // Not BigInteger.TWO: that constant does not exist on Android 11.
    private val TWO = BigInteger.valueOf(2)

    /** `S × N / d` exactly, with the half-up whole-peso figure that is shown. */
    fun forecast(
        spent: Money,
        dayOfMonth: Int,
        monthLength: Int
    ): Outcome<Forecast, SnapshotFailure> {
        val numerator = spent.minorUnits.toBigInteger() * monthLength.toBigInteger()
        val denominator = dayOfMonth.toBigInteger()
        // Half up for a value that is never negative: floor((2n + d) / 2d).
        val rounded = (numerator * TWO + denominator) / (denominator * TWO)
        if (rounded.bitLength() >= Long.SIZE_BITS) {
            return Outcome.Failure(SnapshotFailure.Unrepresentable)
        }
        return Outcome.Success(
            Forecast(numerator, dayOfMonth, Money(rounded.toLong(), spent.currency))
        )
    }

    /**
     * Over if what was actually spent, or the exact forecast, is above the limit. On
     * track if the forecast is at most 95% of the limit. Tight in between. A zero
     * limit needs no division: nothing spent is on track, anything spent is over.
     */
    fun verdict(limit: Money, spent: Money, forecast: Forecast): Verdict {
        val scaledLimit = limit.minorUnits.toBigInteger() * forecast.denominator.toBigInteger()
        return when {
            spent.minorUnits > limit.minorUnits -> Verdict.Over
            forecast.numerator > scaledLimit -> Verdict.Over
            forecast.numerator * HUNDRED <= scaledLimit * NINETY_FIVE -> Verdict.OnTrack
            else -> Verdict.Tight
        }
    }

    fun pastVerdict(limit: Money, spent: Money): PastVerdict =
        if (spent.minorUnits > limit.minorUnits) PastVerdict.Over else PastVerdict.WithinLimit

    /** `50 × floor(max(remaining, 0) / (50 × availableDays))`: down, never to the nearest. */
    fun allowance(remaining: Money, availableDays: Int): DailyAllowance {
        val available = remaining.minorUnits.toBigInteger().max(BigInteger.ZERO)
        val perDay = available / (FIFTY * availableDays.toBigInteger()) * FIFTY
        // Never more than what remains, so it always fits.
        return DailyAllowance(Money(perDay.toLong(), remaining.currency), availableDays)
    }

    /**
     * [categories] is every category with its state in [month] and [expenses] every
     * expense dated in [month], all read at the same [revision]. A category is listed
     * when it is not archived, or has a limit or spending that month, so an archived
     * one stays wherever it explains a figure and nowhere else.
     */
    fun snapshot(
        month: YearMonth,
        today: FinancialDay,
        generation: Long,
        revision: Long,
        categories: List<CategoryInMonth>,
        expenses: List<Expense>
    ): Outcome<MonthlySnapshot, SnapshotFailure> {
        val currentMonth = YearMonth.from(today.date)
        if (month > currentMonth) return Outcome.Failure(SnapshotFailure.FutureMonth(currentMonth))
        val isCurrent = month == currentMonth
        val counted = expenses.filter { YearMonth.from(it.date) == month && it.date <= today.date }
        val future = expenses.count { YearMonth.from(it.date) == month && it.date > today.date }
        val byCategory = counted.groupBy { it.categoryId }

        val lines = ArrayList<CategoryMonth>(categories.size)
        var limits: ExactTotal? = null
        for (line in categories) {
            val of = byCategory[line.category.id].orEmpty()
            val spent = when (val total = total(of.map { it.amount.money })) {
                is Outcome.Success -> total.value
                is Outcome.Failure -> return total
            }
            val limit =
                ((line.state as? MonthBudgetState.Limited)?.limit as? BudgetLimit.Finite)?.amount
            val budget = when (
                val assessed = limit?.let {
                    budget(it, spent, month, today, isCurrent)
                }
            ) {
                null -> null
                is Outcome.Success -> assessed.value
                is Outcome.Failure -> return assessed
            }
            if (limit != null) limits = (limits ?: ExactTotal.zero(Currency.COP)) + limit
            val shown = !line.category.archived ||
                line.state is MonthBudgetState.Limited ||
                of.isNotEmpty()
            if (shown) lines += CategoryMonth(line.category, line.state, spent, budget)
        }

        val spent = when (val total = total(counted.map { it.amount.money })) {
            is Outcome.Success -> total.value
            is Outcome.Failure -> return total
        }
        val overall = when (val limit = limits?.toMoney()) {
            null -> OverallBudget.NoLimit

            is Outcome.Failure -> return Outcome.Failure(SnapshotFailure.Unrepresentable)

            is Outcome.Success -> {
                val budget = when (
                    val assessed = budget(limit.value, spent, month, today, isCurrent)
                ) {
                    is Outcome.Success -> assessed.value
                    is Outcome.Failure -> return assessed
                }
                val availableDays = month.lengthOfMonth() - today.date.dayOfMonth + 1
                OverallBudget.Limited(
                    budget,
                    if (isCurrent) allowance(budget.remaining, availableDays) else null
                )
            }
        }
        return Outcome.Success(
            MonthlySnapshot(
                month = month,
                asOf = today.date,
                zone = today.zone,
                generation = generation,
                revision = revision,
                categories = lines,
                spent = spent,
                overall = overall,
                futureDatedExpenses = future
            )
        )
    }

    private fun total(amounts: List<Money>): Outcome<Money, SnapshotFailure> =
        when (val total = ExactTotal.of(Currency.COP, amounts).toMoney()) {
            is Outcome.Success -> total
            is Outcome.Failure -> Outcome.Failure(SnapshotFailure.Unrepresentable)
        }

    private fun budget(
        limit: Money,
        spent: Money,
        month: YearMonth,
        today: FinancialDay,
        isCurrent: Boolean
    ): Outcome<LimitedBudget, SnapshotFailure> {
        val remaining = when (val difference = limit - spent) {
            is Outcome.Success -> difference.value
            is Outcome.Failure -> return Outcome.Failure(SnapshotFailure.Unrepresentable)
        }
        if (!isCurrent) {
            val standing = BudgetStanding.Past(pastVerdict(limit, spent))
            return Outcome.Success(LimitedBudget(limit, spent, remaining, standing))
        }
        return when (val estimate = forecast(spent, today.date.dayOfMonth, month.lengthOfMonth())) {
            is Outcome.Failure -> estimate

            is Outcome.Success -> {
                val standing =
                    BudgetStanding.Current(estimate.value, verdict(limit, spent, estimate.value))
                Outcome.Success(LimitedBudget(limit, spent, remaining, standing))
            }
        }
    }
}
