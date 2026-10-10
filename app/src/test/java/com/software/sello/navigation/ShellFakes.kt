package com.software.sello.navigation

import com.software.sello.domain.model.Category
import com.software.sello.domain.model.CategoryId
import com.software.sello.domain.model.CategoryName
import com.software.sello.domain.model.IconKey
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure
import com.software.sello.domain.policy.MonthBudgetState
import com.software.sello.domain.port.CategoryBudget
import com.software.sello.domain.port.CategoryReads
import com.software.sello.domain.port.FinancialClock
import com.software.sello.domain.port.FinancialDay
import com.software.sello.domain.port.MonotonicClock
import com.software.sello.domain.port.MonthBudget
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlinx.coroutines.flow.MutableStateFlow

/** Financial time a test moves by hand, as a month change or the sandbox would. */
class HandFinancialClock(date: String) : FinancialClock {
    private val zone = ZoneId.of("America/Bogota")
    override val today = MutableStateFlow(FinancialDay(LocalDate.parse(date), zone))

    fun goTo(date: String) {
        today.value = FinancialDay(LocalDate.parse(date), zone)
    }
}

/** Elapsed time a test moves by hand. It starts well after "boot". */
class HandMonotonicClock : MonotonicClock {
    var now: Duration = 5.hours

    override fun elapsed(): Duration = now
}

/** Categories a test sets by hand. [failure] makes every read fail instead. */
class HandCategoryReads : CategoryReads {
    var generation = 1L
    var categories: List<CategoryBudget> = emptyList()
    var failure: StorageFailure? = null
    var reads = 0

    override suspend fun monthBudget(month: YearMonth): Outcome<MonthBudget, StorageFailure> {
        reads++
        failure?.let { return Outcome.Failure(it) }
        return Outcome.Success(MonthBudget(month, generation, revision = 0, categories))
    }

    fun category(number: Int, name: String, icon: String = "home", archived: Boolean = false) =
        CategoryBudget(
            Category(
                id = (
                    CategoryId.of(
                        "00000000-0000-4000-8000-%012d".format(number)
                    ) as Outcome.Success
                    )
                    .value,
                name = (CategoryName.of(name) as Outcome.Success).value,
                icon = (IconKey.of(icon) as Outcome.Success).value,
                archived = archived,
                version = 1,
                createdAt = Instant.EPOCH,
                updatedAt = Instant.EPOCH
            ),
            MonthBudgetState.Unconfigured
        )
}
