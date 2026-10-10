package com.software.sello.domain.model

import java.time.Instant

sealed interface IconKeyError {
    data class Malformed(val original: String) : IconKeyError
}

/**
 * The stable key of a category's icon, such as `shopping_cart`. Which keys have a
 * drawing is presentation's concern; an unfamiliar but well-formed key is still valid
 * here, so a file written by a newer version does not become unreadable.
 */
@ConsistentCopyVisibility
data class IconKey private constructor(val value: String) {
    companion object {
        const val MAX_LENGTH = 40
        private val wellFormed = Regex("[a-z][a-z0-9_]{0,${MAX_LENGTH - 1}}")

        fun of(raw: String): Outcome<IconKey, IconKeyError> = if (wellFormed.matches(raw)) {
            Outcome.Success(IconKey(raw))
        } else {
            Outcome.Failure(IconKeyError.Malformed(raw))
        }
    }
}

/**
 * A spending category. [version] counts committed changes to the category and its
 * limits, starting at 1. [createdAt] and [updatedAt] are audit time, not financial dates.
 */
data class Category(
    val id: CategoryId,
    val name: CategoryName,
    val icon: IconKey,
    val archived: Boolean,
    val version: Long,
    val createdAt: Instant,
    val updatedAt: Instant
)
