package com.software.sello.domain.model

/**
 * Why stored data could not be used. A failed read is always one of these, never an
 * empty list, a zero or a default. No stored value is carried, so a failure can be
 * logged or shown without exposing financial content.
 */
sealed interface StorageFailure {
    /**
     * A stored row breaks a rule the app always enforces before writing, so it was
     * damaged or written by something else. [record] and [field] name the table and
     * column; [key] is the row's identifier when it has a usable one.
     */
    data class Integrity(val record: String, val key: String?, val field: String) : StorageFailure

    /** The database could not be opened, read or written. [reason] names the error type only. */
    data class Unavailable(val reason: String) : StorageFailure
}
