package com.software.sello.domain.model

sealed interface RecordIdError {
    /** Not a canonical identifier. [original] is what was received. */
    data class Malformed(val original: String) : RecordIdError
}

private val canonicalUuid =
    Regex("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}")

/**
 * The one accepted spelling of an identifier: a lower-case hyphenated UUID. A stored
 * or imported identifier in any other form is rejected, never normalized, so the same
 * record cannot exist under two spellings.
 */
private fun canonical(raw: String): Outcome<String, RecordIdError> =
    if (canonicalUuid.matches(raw)) {
        Outcome.Success(raw)
    } else {
        Outcome.Failure(RecordIdError.Malformed(raw))
    }

private inline fun <T> canonical(raw: String, wrap: (String) -> T): Outcome<T, RecordIdError> =
    when (val id = canonical(raw)) {
        is Outcome.Success -> Outcome.Success(wrap(id.value))
        is Outcome.Failure -> id
    }

/** A category's identity. It survives renaming and archiving. */
@ConsistentCopyVisibility
data class CategoryId private constructor(val value: String) {
    companion object {
        fun of(raw: String): Outcome<CategoryId, RecordIdError> = canonical(raw, ::CategoryId)
    }
}

@ConsistentCopyVisibility
data class ExpenseId private constructor(val value: String) {
    companion object {
        fun of(raw: String): Outcome<ExpenseId, RecordIdError> = canonical(raw, ::ExpenseId)
    }
}

@ConsistentCopyVisibility
data class IncomeId private constructor(val value: String) {
    companion object {
        fun of(raw: String): Outcome<IncomeId, RecordIdError> = canonical(raw, ::IncomeId)
    }
}
