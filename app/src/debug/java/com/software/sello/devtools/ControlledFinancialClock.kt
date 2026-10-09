package com.software.sello.devtools

import com.software.sello.domain.port.FinancialClock
import com.software.sello.domain.port.FinancialDay
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Financial time under direct control, for tests and the future sandbox. Every
 * change is published, so observers recompute exactly as they do at a real
 * rollover. It holds no audit or monotonic time and cannot affect either.
 */
class ControlledFinancialClock(start: FinancialDay) : FinancialClock {
    private val state = MutableStateFlow(start)

    override val today: StateFlow<FinancialDay> = state.asStateFlow()

    fun jumpTo(date: LocalDate) = state.update { it.copy(date = date) }

    fun advanceDays(days: Long) = state.update { it.copy(date = it.date.plusDays(days)) }

    fun advanceMonths(months: Long) = state.update { it.copy(date = it.date.plusMonths(months)) }
}

/** Load after the production modules: replaces financial time and nothing else. */
fun financialTimeOverride(clock: ControlledFinancialClock): Module = module {
    single<FinancialClock> { clock }
}
