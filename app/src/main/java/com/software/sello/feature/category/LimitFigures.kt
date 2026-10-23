package com.software.sello.feature.category

import com.software.sello.domain.model.BudgetLimit
import com.software.sello.domain.model.CategoryId
import com.software.sello.domain.model.Money
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.policy.CategoryBudgetPolicy
import com.software.sello.domain.policy.CategoryInMonth
import com.software.sello.domain.policy.CopAmountInput
import com.software.sello.domain.policy.MonthlyBudgetPolicy
import com.software.sello.domain.port.MonthBudget
import com.software.sello.presentation.money.MoneyFormatter

/**
 * What the limit section shows beside the draft, from the month the form read: the
 * suggested limits, and the month's total limit with the draft's in it. The figures
 * come from the domain; this only draws them.
 */
class LimitFigures(private val money: MoneyFormatter) {
    /** The person's most used limits, or the starting ones while they have too few. */
    fun suggestions(budget: MonthBudget?): List<LimitSuggestion> =
        CategoryBudgetPolicy.suggestedLimits(budget.inMonth(), STARTING_LIMITS.map(Money::cop))
            .map {
                val amount = money.format(it)
                LimitSuggestion(it.minorUnits, amount.digits, amount.spoken)
            }

    /**
     * Null when nothing was read, while the draft's limit is empty or not an amount,
     * when no category would have a limit, or when the total cannot be held exactly.
     * [categoryId] is the category being edited, null for a new one.
     */
    fun total(
        budget: MonthBudget?,
        categoryId: CategoryId?,
        unlimited: Boolean,
        limitText: String
    ): LimitTotal? {
        budget ?: return null
        val draft = when {
            unlimited -> BudgetLimit.Unlimited

            else -> (CopAmountInput.parse(limitText) as? Outcome.Success)?.value
                ?.let { (BudgetLimit.finite(it) as? Outcome.Success)?.value }
                ?: return null
        }
        val categories = budget.inMonth()
        val now = MonthlyBudgetPolicy.limitTotalWith(categories, null, null)
        val then = MonthlyBudgetPolicy.limitTotalWith(categories, categoryId, draft)
        if (now !is Outcome.Success || then !is Outcome.Success) return null
        val total = then.value ?: return null
        return LimitTotal(budget.month, money.format(total), changes = total != now.value)
    }

    private fun MonthBudget?.inMonth() =
        this?.categories.orEmpty().map { CategoryInMonth(it.category, it.state) }

    companion object {
        /** Limits offered before the person has three of their own, in pesos. */
        val STARTING_LIMITS = listOf(200_000L, 300_000L, 500_000L)
    }
}
