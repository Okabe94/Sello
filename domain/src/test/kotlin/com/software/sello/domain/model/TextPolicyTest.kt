package com.software.sello.domain.model

import com.software.sello.domain.errorOrFail
import com.software.sello.domain.valueOrFail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TextPolicyTest {
    private fun name(raw: String) = CategoryName.of(raw)

    private fun key(raw: String) = name(raw).valueOrFail().uniquenessKey

    /** One code point, two UTF-16 units. */
    private val emoji = "🍔"

    // ---- Category names: T01–T03 ----

    @Test
    fun namesAreTrimmedAndKeepTheirAccents() { // T01
        assertEquals("Café", name(" Café ").valueOrFail().value)
        assertEquals("Año Nuevo", name("\tAño Nuevo\n").valueOrFail().value)
    }

    @Test
    fun uniquenessIgnoresCaseAccentsAndHowAccentsAreEncoded() { // T01, T02
        val cafe = key(" Café ")
        assertEquals(cafe, key("cafe"))
        assertEquals(cafe, key("CAFE"))
        assertEquals(cafe, key("CAFÉ"))
        assertEquals(cafe, key("Café")) // decomposed e + combining acute
        assertEquals(cafe, key("ｃａｆｅ")) // full-width letters
        assertEquals(key("Año"), key("ano"))
        assertEquals(key("STRASSE"), key("straße"))
        assertEquals(key("Casa  Nueva"), key("casa nueva"))
        assertNotEquals(cafe, key("cafes"))
        assertNotEquals(cafe, key("ca fe"))
        assertNotEquals(key("$emoji"), key("🍕"))
    }

    @Test
    fun composedAndDecomposedAccentsDisplayAndCountTheSame() {
        val composed = name("Café").valueOrFail()
        val decomposed = name("Café").valueOrFail()
        assertEquals(composed, decomposed)
        assertEquals(4, composed.value.codePointCount(0, composed.value.length))
    }

    @Test
    fun namesAreOneTo24CodePointsNotUtf16Units() { // T03
        assertEquals("a".repeat(24), name("a".repeat(24)).valueOrFail().value)
        assertEquals(TextError.TooLong(24, 25), name("a".repeat(25)).errorOrFail())
        assertEquals("a", name("a").valueOrFail().value)

        val emoji24 = emoji.repeat(24) // 48 UTF-16 units
        assertEquals(emoji24, name(emoji24).valueOrFail().value)
        assertEquals(TextError.TooLong(24, 25), name(emoji.repeat(25)).errorOrFail())

        // 24 accented letters written decomposed are 48 code points before normalization.
        assertEquals("é".repeat(24), name("é".repeat(24)).valueOrFail().value)
        // Surrounding whitespace does not count towards the limit.
        assertEquals("b".repeat(24), name("  " + "b".repeat(24) + "  ").valueOrFail().value)
    }

    @Test
    fun whitespaceOnlyNamesAreRequiredNameFailures() { // T03
        for (raw in listOf("", " ", "   ", "\t\n", " ", " ")) {
            assertEquals(TextError.Required, name(raw).errorOrFail())
        }
    }

    @Test
    fun controlCharactersAreRejectedNotRemoved() {
        for (raw in listOf("Ca\u0000fé", "Ca\nfé", "Ca\tfé", "Café\u007F", "Ca\u0085fé", "Ca fé")) {
            assertEquals(TextError.ControlCharacter, name(raw).errorOrFail())
        }
    }

    // ---- Notes: T04 ----

    @Test
    fun notesAreOptionalAndUpTo60CodePoints() {
        assertNull(Note.of(null).valueOrFail())
        assertNull(Note.of("").valueOrFail())
        assertNull(Note.of("   ").valueOrFail())
        assertEquals("Almuerzo", Note.of(" Almuerzo ").valueOrFail()?.value)
        assertEquals("n".repeat(60), Note.of("n".repeat(60)).valueOrFail()?.value)
        assertEquals(emoji.repeat(60), Note.of(emoji.repeat(60)).valueOrFail()?.value)
        assertEquals(TextError.TooLong(60, 61), Note.of("n".repeat(61)).errorOrFail())
        assertEquals(TextError.TooLong(60, 61), Note.of(emoji.repeat(61)).errorOrFail())
        assertEquals(TextError.ControlCharacter, Note.of("línea\nnueva").errorOrFail())
    }

    // ---- Income sources: T05, T06 ----

    @Test
    fun theFiveApprovedSourceKeysAreStable() { // T06
        assertEquals(
            listOf("salary", "freelance", "passive_income", "transfer", "other"),
            IncomeSource.keys
        )
        assertEquals(IncomeSource.Salary, IncomeSource.of("salary", null).valueOrFail())
        assertEquals(IncomeSource.Freelance, IncomeSource.of("freelance", null).valueOrFail())
        assertEquals(
            IncomeSource.PassiveIncome,
            IncomeSource.of("passive_income", null).valueOrFail()
        )
        assertEquals(IncomeSource.Transfer, IncomeSource.of("transfer", null).valueOrFail())
    }

    @Test
    fun otherNeedsATrimmedNameOfOneTo24CodePoints() { // T05
        fun other(name: String?) = IncomeSource.of("other", name)
        assertEquals(
            IncomeSourceError.InvalidOtherName(TextError.Required),
            other(null).errorOrFail()
        )
        assertEquals(
            IncomeSourceError.InvalidOtherName(TextError.Required),
            other("  ").errorOrFail()
        )
        val source = other(" " + "x".repeat(24) + " ").valueOrFail() as IncomeSource.Other
        assertEquals("x".repeat(24), source.name.value)
        assertEquals("other", source.key)
        assertEquals(
            IncomeSourceError.InvalidOtherName(TextError.TooLong(24, 25)),
            other("x".repeat(25)).errorOrFail()
        )
        assertEquals(
            IncomeSourceError.InvalidOtherName(TextError.ControlCharacter),
            other("Ri\u0000fa").errorOrFail()
        )
    }

    @Test
    fun unknownKeysAndStrayNamesAreRejected() {
        for (key in listOf("", "Salary", "SALARY", "salario", "bonus", " salary")) {
            assertEquals(
                IncomeSourceError.UnknownKey(key),
                IncomeSource.of(key, null).errorOrFail()
            )
        }
        assertEquals(
            IncomeSourceError.UnexpectedName("salary"),
            IncomeSource.of("salary", "Nómina").errorOrFail()
        )
    }
}
