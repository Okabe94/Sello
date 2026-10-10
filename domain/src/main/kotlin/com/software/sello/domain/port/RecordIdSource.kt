package com.software.sello.domain.port

/** Supplies new identifiers in the canonical form: a lower-case hyphenated UUID. */
fun interface RecordIdSource {
    fun next(): String
}
