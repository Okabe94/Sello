package com.software.sello.feature.recibo

import com.software.sello.designsystem.component.MoneyTextValue

/** What Recibo shows. A failed read is its own state; it is never shown as zero. */
sealed interface ReciboState {
    data object Loading : ReciboState

    /** Read successfully and there are no categories yet. */
    data object FirstRun : ReciboState

    /** Read successfully. [spent] is the month's exact total. */
    data class Spending(val spent: MoneyTextValue) : ReciboState

    data object Failed : ReciboState
}

sealed interface ReciboAction {
    data object Retry : ReciboAction
}
