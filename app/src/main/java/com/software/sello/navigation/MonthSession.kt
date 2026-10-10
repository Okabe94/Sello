package com.software.sello.navigation

import com.software.sello.domain.model.Outcome
import com.software.sello.domain.policy.EffectiveDates
import com.software.sello.domain.port.FinancialClock
import com.software.sello.domain.port.MonotonicClock
import java.time.YearMonth
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** What survives the process being killed: small values only. */
data class SavedMonthSession(val pinnedMonth: String?, val backgroundedAtMillis: Long?)

/**
 * The one month every screen shows. Recibo, Resumen and category detail read it from
 * here, so they cannot drift apart.
 *
 * The month is either the current financial month, which it then follows across a
 * month change, or an earlier month the person picked. It can never be a later one.
 * After more than [RESET_AFTER] in the background it goes back to the current month.
 *
 * Two clocks, each for one job: [clock] says which month is current, and [monotonic]
 * measures the time spent in the background. Moving financial time, real or
 * simulated, is not time in the background and never triggers the reset. Nothing here
 * touches a draft.
 */
class MonthSession(private val clock: FinancialClock, private val monotonic: MonotonicClock) {
    private val pinned = MutableStateFlow<YearMonth?>(null)
    private var backgroundedAt: Duration? = null

    val currentMonth: Flow<YearMonth> =
        clock.today.map { YearMonth.from(it.date) }.distinctUntilChanged()

    val selectedMonth: Flow<YearMonth> =
        combine(currentMonth, pinned) { current, pick -> effective(current, pick) }
            .distinctUntilChanged()

    val currentMonthNow: YearMonth get() = YearMonth.from(clock.today.value.date)

    val selectedMonthNow: YearMonth get() = effective(currentMonthNow, pinned.value)

    // A pick that is no longer in the past (the clock moved back) falls back to current.
    private fun effective(current: YearMonth, pick: YearMonth?) =
        pick?.takeIf { it < current } ?: current

    /** Returns false, changing nothing, for a month after the current one. */
    fun select(month: YearMonth): Boolean {
        val today = clock.today.value.date
        if (EffectiveDates.forSelection(month, today) is Outcome.Failure) return false
        pinned.value = month.takeIf { it < YearMonth.from(today) }
        return true
    }

    fun enteredBackground() {
        backgroundedAt = monotonic.elapsed()
    }

    fun returnedToForeground() {
        val since = backgroundedAt ?: return
        backgroundedAt = null
        val away = monotonic.elapsed() - since
        // A negative time away means the device restarted: treat it as a long absence.
        if (away > RESET_AFTER || away < Duration.ZERO) pinned.value = null
    }

    fun save() = SavedMonthSession(pinned.value?.toString(), backgroundedAt?.inWholeMilliseconds)

    /** Restores what [save] produced. A month that does not parse is ignored. */
    fun restore(saved: SavedMonthSession) {
        val month = (saved.pinnedMonth?.let(EffectiveDates::parseMonth) as? Outcome.Success)?.value
        pinned.value = month
        backgroundedAt = saved.backgroundedAtMillis?.milliseconds
    }

    companion object {
        /** Exactly this long away keeps the selection; anything longer resets it. */
        val RESET_AFTER: Duration = 30.minutes
    }
}
