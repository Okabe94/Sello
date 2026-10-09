package com.software.sello.platform

import com.software.sello.domain.port.DispatcherProvider
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Owns work that outlives a screen, such as the financial clock's observer.
 * One child failing does not cancel the others. [close] cancels everything it
 * owns; a closed scope is not reused, a new one is created with its dependents.
 */
class ApplicationScope(dispatchers: DispatcherProvider) : CoroutineScope {
    override val coroutineContext: CoroutineContext = SupervisorJob() + dispatchers.default

    fun close() = cancel()
}
