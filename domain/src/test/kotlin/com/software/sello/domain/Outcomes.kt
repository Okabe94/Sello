package com.software.sello.domain

import com.software.sello.domain.model.Outcome
import org.junit.Assert.fail

fun <T> Outcome<T, *>.valueOrFail(): T = when (this) {
    is Outcome.Success -> value
    is Outcome.Failure -> throw AssertionError("expected a value, got failure $error")
}

fun <E> Outcome<*, E>.errorOrFail(): E = when (this) {
    is Outcome.Failure -> error

    is Outcome.Success -> {
        fail("expected a failure, got value $value")
        throw IllegalStateException()
    }
}
