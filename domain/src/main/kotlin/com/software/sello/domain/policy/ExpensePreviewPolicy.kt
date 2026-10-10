package com.software.sello.domain.policy

import com.software.sello.domain.model.CategoryId
import com.software.sello.domain.model.Money
import com.software.sello.domain.model.MonthlySnapshot
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.OverallBudget
import com.software.sello.domain.model.SnapshotFailure
import com.software.sello.domain.model.TransactionAmount

/** One figure of a preview: what would be left, or what would have been spent. */
sealed interface PreviewFigure {
    /** There is a limit and it would not be passed. [amount] is what would remain, zero or more. */
    data class Remaining(val amount: Money) : PreviewFigure

    /** The limit would be passed. [by] is how far, always more than zero. */
    data class Over(val by: Money) : PreviewFigure

    /** There is no limit to measure against. [total] is what would have been spent. */
    data class Spent(val total: Money) : PreviewFigure
}

/**
 * What the month and the chosen category would look like if a draft expense were
 * saved now. It is an illustration for the entry form: it is never stored, and once
 * something is saved the next snapshot is the truth, not this.
 */
data class ExpensePreview(val month: PreviewFigure, val category: PreviewFigure?)

object ExpensePreviewPolicy {
    /**
     * [snapshot] must be the month the expense would be dated in. [categoryId] may be
     * null while none is chosen, or name a category the snapshot does not list; then
     * only the month's figure is given.
     */
    fun preview(
        snapshot: MonthlySnapshot,
        amount: TransactionAmount,
        categoryId: CategoryId?
    ): Outcome<ExpensePreview, SnapshotFailure> {
        val month = when (val overall = snapshot.overall) {
            is OverallBudget.Limited -> remaining(overall.budget.remaining, amount)
            OverallBudget.NoLimit -> spent(snapshot.spent, amount)
        }
        val line = snapshot.categories.firstOrNull { it.category.id == categoryId }
        val category = when {
            line == null -> null
            line.budget != null -> remaining(line.budget.remaining, amount)
            else -> spent(line.spent, amount)
        }
        if (month == null || (line != null && category == null)) {
            return Outcome.Failure(SnapshotFailure.Unrepresentable)
        }
        return Outcome.Success(ExpensePreview(month, category))
    }

    private fun remaining(now: Money, amount: TransactionAmount): PreviewFigure? {
        val left = ((now - amount.money) as? Outcome.Success)?.value ?: return null
        if (left.minorUnits >= 0) return PreviewFigure.Remaining(left)
        // How far over, as a positive amount; the one value that has no positive twin fails.
        val by = (Money(0, left.currency) - left) as? Outcome.Success ?: return null
        return PreviewFigure.Over(by.value)
    }

    private fun spent(now: Money, amount: TransactionAmount): PreviewFigure? =
        ((now + amount.money) as? Outcome.Success)?.let { PreviewFigure.Spent(it.value) }
}
