package com.software.sello.domain.model

import java.math.BigInteger

/**
 * A currency and the number of decimal places its minor unit has. Only
 * [isMvpEntryCurrency] currencies can be typed in or stored by the MVP; the others
 * exist so later policies have something to name, with no rates behind them.
 */
enum class Currency(val code: String, val scale: Int, val isMvpEntryCurrency: Boolean) {
    COP("COP", 0, true),
    USD("USD", 2, false),
    EUR("EUR", 2, false),
    GBP("GBP", 2, false)
}

sealed interface MoneyError {
    /** The exact result does not fit the representable range. Never wrapped or clipped. */
    data object Overflow : MoneyError

    data class CurrencyMismatch(val expected: Currency, val actual: Currency) : MoneyError
}

/**
 * An exact amount in whole minor units. It may be zero or negative (a remaining
 * budget can be); rules that need a positive, bounded amount use [TransactionAmount].
 */
data class Money(val minorUnits: Long, val currency: Currency) {
    operator fun plus(other: Money): Outcome<Money, MoneyError> =
        combine(other) { a, b -> Math.addExact(a, b) }

    operator fun minus(other: Money): Outcome<Money, MoneyError> =
        combine(other) { a, b -> Math.subtractExact(a, b) }

    private inline fun combine(
        other: Money,
        exact: (Long, Long) -> Long
    ): Outcome<Money, MoneyError> {
        if (other.currency != currency) {
            return Outcome.Failure(MoneyError.CurrencyMismatch(currency, other.currency))
        }
        return try {
            Outcome.Success(Money(exact(minorUnits, other.minorUnits), currency))
        } catch (_: ArithmeticException) {
            Outcome.Failure(MoneyError.Overflow)
        }
    }

    companion object {
        fun cop(pesos: Long) = Money(pesos, Currency.COP)
    }
}

/**
 * A running total that cannot overflow while it is being built, so the order of the
 * terms never matters. Only [toMoney] checks whether the final figure is representable.
 */
class ExactTotal private constructor(
    val currency: Currency,
    private val units: BigInteger,
    private val foreign: Currency?
) {
    operator fun plus(amount: Money) = with(amount, units + amount.minorUnits.toBigInteger())

    operator fun minus(amount: Money) = with(amount, units - amount.minorUnits.toBigInteger())

    private fun with(amount: Money, total: BigInteger) = ExactTotal(
        currency,
        total,
        foreign ?: amount.currency.takeIf { it != currency }
    )

    fun toMoney(): Outcome<Money, MoneyError> = when {
        foreign != null -> Outcome.Failure(MoneyError.CurrencyMismatch(currency, foreign))
        units.bitLength() >= Long.SIZE_BITS -> Outcome.Failure(MoneyError.Overflow)
        else -> Outcome.Success(Money(units.toLong(), currency))
    }

    companion object {
        fun zero(currency: Currency) = ExactTotal(currency, BigInteger.ZERO, null)

        fun of(currency: Currency, amounts: Iterable<Money>): ExactTotal =
            amounts.fold(zero(currency)) { total, amount -> total + amount }
    }
}
