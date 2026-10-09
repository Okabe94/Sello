package com.software.sello.domain.model

/** A typed result: a rule either yields a value or says exactly why it could not. */
sealed interface Outcome<out T, out E> {
    data class Success<out T>(val value: T) : Outcome<T, Nothing>

    data class Failure<out E>(val error: E) : Outcome<Nothing, E>
}
