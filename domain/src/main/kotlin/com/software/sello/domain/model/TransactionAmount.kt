package com.software.sello.domain.model

sealed interface TransactionAmountError {
    data object NotPositive : TransactionAmountError

    data object AboveMaximum : TransactionAmountError

    data class UnsupportedCurrency(val currency: Currency) : TransactionAmountError
}

/**
 * The amount of one expense or income: 1 to 999.999.999.999 COP. The same rule holds
 * for typed input, commands, stored rows and backup files (ADR 0003). It is a bound
 * on a single record, not on totals.
 */
@ConsistentCopyVisibility
data class TransactionAmount private constructor(val money: Money) {
    companion object {
        const val MIN_UNITS = 1L
        const val MAX_UNITS = 999_999_999_999L

        /** The keypad stops accepting digits here. */
        const val MAX_DIGITS = 12

        fun of(money: Money): Outcome<TransactionAmount, TransactionAmountError> = when {
            !money.currency.isMvpEntryCurrency ->
                Outcome.Failure(TransactionAmountError.UnsupportedCurrency(money.currency))

            money.minorUnits < MIN_UNITS -> Outcome.Failure(TransactionAmountError.NotPositive)

            money.minorUnits > MAX_UNITS -> Outcome.Failure(TransactionAmountError.AboveMaximum)

            else -> Outcome.Success(TransactionAmount(money))
        }
    }
}
