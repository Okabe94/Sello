package com.software.sello.navigation

import com.software.sello.feature.recibo.CategoryDetailState
import com.software.sello.feature.recibo.ReciboState

/*
 * A category's detail is asked for by identifier and read a moment later. Until then
 * the detail's state is either not ready or still the category shown before. These
 * decide what is shown for the category that was chosen, from what the two screens
 * have already read: nothing is kept apart from them and nothing is invented.
 */

/**
 * The name to put over the detail of [shownId]: the detail's own once it is the detail
 * of that category, otherwise the one [recibo] already lists it under. Null when
 * neither has read it yet, or when no category is chosen.
 */
internal fun detailTitle(
    shownId: String?,
    detail: CategoryDetailState,
    recibo: ReciboState
): String? {
    if (shownId == null) return null
    val own = (detail as? CategoryDetailState.Ready)?.category?.takeIf { it.id == shownId }
    val listed = (recibo as? ReciboState.Month)?.categories?.firstOrNull { it.id == shownId }
    return (own ?: listed)?.name
}

/**
 * [detail] as the detail of [shownId]. One that still holds another category is the
 * one left behind: under the category just chosen it is loading, not that.
 */
internal fun detailOf(shownId: String?, detail: CategoryDetailState): CategoryDetailState =
    if (detail is CategoryDetailState.Ready && detail.category.id != shownId) {
        CategoryDetailState.Loading
    } else {
        detail
    }
