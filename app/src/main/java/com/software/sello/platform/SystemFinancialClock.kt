package com.software.sello.platform

import com.software.sello.domain.port.AuditClock
import com.software.sello.domain.port.FinancialClock
import com.software.sello.domain.port.FinancialDay
import java.time.Duration
import java.time.ZoneId
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * Production financial clock: real time read in the fixed financial zone.
 *
 * It recomputes at each midnight of that zone and on every [TimeSignals] change
 * (resume, device time, date or zone change), which also reschedules the midnight
 * timer. The observer lives in [scope]; cancelling that scope stops the timer and
 * unsubscribes from the signals, after which [today] keeps its last value.
 */
class SystemFinancialClock(
    financialZone: ProcessFinancialZone,
    private val audit: AuditClock,
    signals: TimeSignals,
    scope: CoroutineScope
) : FinancialClock {
    private val zone: ZoneId = financialZone.zone
    private val state = MutableStateFlow(read())

    override val today: StateFlow<FinancialDay> = state.asStateFlow()

    init {
        scope.launch {
            signals.changes().onStart { emit(Unit) }.collectLatest {
                while (true) {
                    state.value = read()
                    delay(untilNextMidnight())
                }
            }
        }
    }

    private fun read() = FinancialDay(audit.now().atZone(zone).toLocalDate(), zone)

    private fun untilNextMidnight(): Long {
        val now = audit.now()
        val nextMidnight = now.atZone(zone).toLocalDate().plusDays(1).atStartOfDay(zone).toInstant()
        return Duration.between(now, nextMidnight).toMillis().coerceAtLeast(1)
    }
}
