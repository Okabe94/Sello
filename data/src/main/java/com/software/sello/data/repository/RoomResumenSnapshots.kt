package com.software.sello.data.repository

import android.database.SQLException
import androidx.room.withTransaction
import com.software.sello.data.local.SelloDatabase
import com.software.sello.domain.model.Income
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.ResumenSnapshot
import com.software.sello.domain.model.SnapshotFailure
import com.software.sello.domain.model.StorageFailure
import com.software.sello.domain.policy.CategoryInMonth
import com.software.sello.domain.policy.ResumenMonthInput
import com.software.sello.domain.policy.ResumenPolicy
import com.software.sello.domain.port.FinancialClock
import com.software.sello.domain.port.FinancialDay
import com.software.sello.domain.port.ResumenSnapshotState
import com.software.sello.domain.port.ResumenSnapshots
import java.time.YearMonth
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

class RoomResumenSnapshots(
    private val database: SelloDatabase,
    private val clock: FinancialClock,
    private val computation: CoroutineContext
) : ResumenSnapshots {
    private class Rows(
        val generation: Long,
        val revision: Long,
        val months: List<ResumenMonthInput>,
        val incomes: List<Income>
    )

    override suspend fun read(month: YearMonth): Outcome<ResumenSnapshot, SnapshotFailure> =
        readAsOf(month, clock.today.value)

    private suspend fun readAsOf(
        month: YearMonth,
        today: FinancialDay
    ): Outcome<ResumenSnapshot, SnapshotFailure> {
        val rows = when (val read = reading { database.withTransaction { rows(month) } }) {
            is Outcome.Success -> read.value
            is Outcome.Failure -> return Outcome.Failure(SnapshotFailure.Storage(read.error))
        }
        return withContext(computation) {
            ResumenPolicy.snapshot(
                month,
                today,
                rows.generation,
                rows.revision,
                rows.months,
                rows.incomes
            )
        }
    }

    private suspend fun rows(month: YearMonth): Outcome<Rows, StorageFailure> {
        val selected = when (val budget = database.monthBudget(month)) {
            is Outcome.Success -> budget.value
            is Outcome.Failure -> return budget
        }
        val months = mutableListOf<ResumenMonthInput>()
        for (period in ResumenPolicy.monthsEndingIn(month)) {
            val budget = if (period == month) {
                selected
            } else {
                when (val read = database.monthBudget(period)) {
                    is Outcome.Success -> read.value
                    is Outcome.Failure -> return read
                }
            }
            val expenses = when (val read = database.monthExpenses(period)) {
                is Outcome.Success -> read.value
                is Outcome.Failure -> return read
            }
            months += ResumenMonthInput(
                period,
                budget.categories.map { CategoryInMonth(it.category, it.state) },
                expenses
            )
        }
        val incomes = when (val read = database.monthIncomes(month)) {
            is Outcome.Success -> read.value
            is Outcome.Failure -> return read
        }
        return Outcome.Success(Rows(selected.generation, selected.revision, months, incomes))
    }

    override fun observe(month: YearMonth): Flow<ResumenSnapshotState> = flow {
        emit(ResumenSnapshotState.Loading)
        var lastGood: ResumenSnapshot? = null
        try {
            combine(clock.today, database.profileDao().changes()) { today, _ -> today }
                .conflate().collect { today ->
                    when (val read = readAsOf(month, today)) {
                        is Outcome.Success -> {
                            lastGood = read.value
                            emit(ResumenSnapshotState.Ready(read.value))
                        }

                        is Outcome.Failure -> emit(
                            ResumenSnapshotState.Failed(read.error, lastGood)
                        )
                    }
                }
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (failure: SQLException) {
            emit(ResumenSnapshotState.Failed(unavailable(failure), lastGood))
        } catch (failure: IllegalStateException) {
            emit(ResumenSnapshotState.Failed(unavailable(failure), lastGood))
        }
    }

    private fun unavailable(failure: Exception) =
        SnapshotFailure.Storage(StorageFailure.Unavailable(failure.javaClass.simpleName))
}
