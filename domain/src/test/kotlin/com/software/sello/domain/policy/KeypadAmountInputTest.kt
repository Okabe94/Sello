package com.software.sello.domain.policy

import com.software.sello.domain.errorOrFail
import com.software.sello.domain.model.Money
import com.software.sello.domain.model.TransactionAmountError
import com.software.sello.domain.valueOrFail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class KeypadAmountInputTest {
    private fun typed(keys: String): AmountDraft =
        keys.fold(AmountDraft.Empty as AmountDraft) { draft, key ->
            when (key) {
                'k' -> draft.tripleZero().draft
                '<' -> draft.backspace()
                else -> draft.digit(key.digitToInt()).draft
            }
        }

    private fun digits(text: String) = AmountDraft.Digits(text)

    // ---- Keypad ----

    @Test
    fun digitsBuildTheAmountInOrder() {
        assertEquals(digits("1"), typed("1"))
        assertEquals(digits("1234"), typed("1234"))
        assertEquals(digits("18000"), typed("18k"))
        assertEquals(digits("105"), typed("105"))
    }

    @Test
    fun anAmountNeverStartsWithZero() {
        assertEquals(AmountDraft.Empty, typed("0"))
        assertEquals(AmountDraft.Empty, typed("000"))
        assertEquals(AmountDraft.Empty, typed("k"))
        assertEquals(digits("5"), typed("005"))
        assertFalse(AmountDraft.Empty.digit(0).limitReached)
    }

    @Test
    fun theTwelfthDigitIsAcceptedAndTheThirteenthChangesNothing() { // M04
        val twelve = typed("999999999999")
        assertEquals(digits("999999999999"), twelve)
        val refused = twelve.digit(9)
        assertEquals(twelve, refused.draft)
        assertTrue(refused.limitReached)
        assertFalse(typed("99999999999").digit(9).limitReached)
    }

    @Test
    fun tripleZeroIsAllOrNothingAtTheLimit() {
        assertEquals(digits("123456789000"), typed("123456789k"))
        val ten = typed("1234567890")
        val refused = ten.tripleZero()
        assertEquals(ten, refused.draft)
        assertTrue(refused.limitReached)
    }

    @Test
    fun deletingEveryDigitLeavesAnEmptyDraftNotZero() {
        assertEquals(digits("12"), typed("123<"))
        assertEquals(AmountDraft.Empty, typed("1<"))
        assertEquals(AmountDraft.Empty, typed("12<<<"))
        assertEquals(AmountDraft.Empty, AmountDraft.Empty.backspace())
        assertEquals(AmountDraftError.Empty, typed("1<").toTransactionAmount().errorOrFail())
    }

    @Test
    fun onlyATypedPositiveAmountBecomesATransactionAmount() {
        assertEquals(AmountDraftError.Empty, AmountDraft.Empty.toTransactionAmount().errorOrFail())
        assertEquals(Money.cop(18_400), typed("18400").toTransactionAmount().valueOrFail().money)
        assertEquals(
            Money.cop(999_999_999_999),
            typed("999999999999").toTransactionAmount().valueOrFail().money
        )
    }

    // ---- Paste ----

    @Test
    fun aValidPasteBecomesPlainDigits() {
        assertEquals(digits("1234"), AmountDraft.paste("1.234")) // M01
        assertEquals(digits("1234"), AmountDraft.paste(" 1234 "))
        assertEquals(digits("999999999999"), AmountDraft.paste("999.999.999.999"))
    }

    @Test
    fun anInvalidPasteIsKeptAsWrittenWithItsReason() { // M02, M03
        val cases = mapOf(
            "1e3" to AmountDraftError.Input(AmountInputError.NotDigits("1e3")),
            "-100" to AmountDraftError.Input(AmountInputError.NotDigits("-100")),
            "$1234" to AmountDraftError.Input(AmountInputError.NotDigits("$1234")),
            "1,5" to AmountDraftError.Input(AmountInputError.NotDigits("1,5")),
            "12.34" to AmountDraftError.Input(AmountInputError.MalformedGrouping("12.34")),
            "007" to AmountDraftError.Input(AmountInputError.LeadingZero("007")),
            "" to AmountDraftError.Input(AmountInputError.Empty("")),
            "9.223.372.036.854.775.808" to
                AmountDraftError.Input(AmountInputError.OutOfRange("9.223.372.036.854.775.808")),
            "0" to AmountDraftError.Range(TransactionAmountError.NotPositive),
            "1000000000000" to AmountDraftError.Range(TransactionAmountError.AboveMaximum),
            "1.000.000.000.000" to AmountDraftError.Range(TransactionAmountError.AboveMaximum)
        )
        for ((text, error) in cases) {
            val draft = AmountDraft.paste(text)
            assertEquals(text, AmountDraft.Rejected(text, error), draft)
            assertEquals(text, error, draft.toTransactionAmount().errorOrFail())
        }
    }

    @Test
    fun aRefusedPasteIsNeverTurnedIntoAnotherAmountByTyping() {
        val refused = AmountDraft.paste("1e3")
        // Typing starts a new amount; it does not append to or reuse the refused text.
        assertEquals(digits("5"), refused.digit(5).draft)
        assertEquals(AmountDraft.Empty, refused.digit(0).draft)
        assertEquals(AmountDraft.Empty, refused.tripleZero().draft)
        assertEquals(AmountDraft.Empty, refused.backspace())
    }
}
