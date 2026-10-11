package com.software.sello.domain.policy

import com.software.sello.domain.model.CategoryMonth

/**
 * The order categories are shown in (decision D06). Only the default order exists so
 * far: "Más usadas", most expenses in the selected month first.
 *
 * Whatever the order, a category with a finite limit that month comes before one
 * without, and equal categories are ordered by name ignoring case and accents, then
 * by identifier, so the result never depends on the order rows were read in.
 */
object CategoryOrderPolicy {
    fun mostUsed(categories: List<CategoryMonth>): List<CategoryMonth> = categories.sortedWith(
        compareBy<CategoryMonth> { it.budget == null }
            .thenByDescending { it.expenseCount }
            .thenBy { it.category.name.uniquenessKey }
            .thenBy { it.category.id.value }
    )
}
