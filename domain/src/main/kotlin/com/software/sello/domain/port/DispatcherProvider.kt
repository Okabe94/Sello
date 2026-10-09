package com.software.sello.domain.port

import kotlinx.coroutines.CoroutineDispatcher

/** Injected dispatchers, so logic never names a global dispatcher. */
interface DispatcherProvider {
    val main: CoroutineDispatcher
    val default: CoroutineDispatcher
    val io: CoroutineDispatcher
}
