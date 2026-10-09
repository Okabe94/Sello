package com.software.sello.domain.policy

import com.software.sello.domain.model.Money
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.TransactionAmount
import com.software.sello.domain.model.TransactionAmountError

/** Why a draft is not yet an amount that can be saved. */
sealed interface AmountDraftError {
    /** Nothing typed. An empty draft is not zero. */
    data object Empty : AmountDraftError

    /** Pasted text that is not an amount; the draft still holds it unchanged. */
    data class Input(val error: AmountInputError) : AmountDraftError

    /** A readable amount outside what one expense or income may be. */
    data class Range(val error: TransactionAmountError) : AmountDraftError
}

/**
 * The amount being entered for one expense or income, before it is a valid
 * [TransactionAmount]. Expense and income entry share it. It is a draft: saving still
 * goes through the command's own validation.
 */
sealed interface AmountDraft {
    /** Nothing entered yet. */
    data object Empty : AmountDraft

    /** One to twelve keypad digits with no leading zero. */
    data class Digits(val digits: String) : AmountDraft

    /** Pasted text that was refused. It is kept exactly as received, never cleaned. */
    data class Rejected(val original: String, val error: AmountDraftError) : AmountDraft

    /** [draft] after a key press; [limitReached] when the press was refused for length. */
    data class Change(val draft: AmountDraft, val limitReached: Boolean = false)

    /** A keypad digit 0–9. A leading zero is ignored; a thirteenth digit is refused. */
    fun digit(digit: Int): Change {
        require(digit in 0..9) { "not a keypad digit: $digit" }
        return append(digit.toString())
    }

    /** The `000` key. It is applied whole or not at all. */
    fun tripleZero(): Change = append("000")

    fun backspace(): AmountDraft = when (this) {
        is Digits -> if (digits.length > 1) Digits(digits.dropLast(1)) else Empty

        // A refused paste is removed whole; part of it is never kept.
        Empty, is Rejected -> Empty
    }

    /** The amount to submit, or why there is none. */
    fun toTransactionAmount(): Outcome<TransactionAmount, AmountDraftError> = when (this) {
        Empty -> Outcome.Failure(AmountDraftError.Empty)

        is Rejected -> Outcome.Failure(error)

        is Digits -> when (val amount = TransactionAmount.of(Money.cop(digits.toLong()))) {
            is Outcome.Success -> amount
            is Outcome.Failure -> Outcome.Failure(AmountDraftError.Range(amount.error))
        }
    }

    private fun append(keys: String): Change {
        // Typing after a refused paste starts again; it never builds on the refused text.
        val current = (this as? Digits)?.digits.orEmpty()
        return when {
            current.isEmpty() && keys.all { it == '0' } -> Change(Empty)

            current.length + keys.length > TransactionAmount.MAX_DIGITS ->
                Change(this, limitReached = true)

            else -> Change(Digits(current + keys))
        }
    }

    companion object {
        /**
         * Reads pasted text as a whole through the original-input rules. It becomes
         * digits only if it is a valid amount for one record; otherwise it is kept
         * exactly as received with the reason.
         */
        fun paste(text: String): AmountDraft {
            val money = when (val parsed = CopAmountInput.parse(text)) {
                is Outcome.Success -> parsed.value
                is Outcome.Failure -> return Rejected(text, AmountDraftError.Input(parsed.error))
            }
            return when (val amount = TransactionAmount.of(money)) {
                is Outcome.Success -> Digits(money.minorUnits.toString())
                is Outcome.Failure -> Rejected(text, AmountDraftError.Range(amount.error))
            }
        }
    }
}
