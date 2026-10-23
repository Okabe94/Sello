package com.software.sello.feature.recibo

import com.software.sello.domain.model.CategoryId
import com.software.sello.domain.model.CategoryInsights
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.SnapshotFailure
import com.software.sello.domain.port.CategoryInsightsSnapshots
import com.software.sello.domain.port.CategoryInsightsState
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow

internal class HandCategoryInsights : CategoryInsightsSnapshots {
    val state = MutableStateFlow<CategoryInsightsState>(CategoryInsightsState.Loading)
    override fun observe(categoryId: CategoryId, month: YearMonth) = state
    override suspend fun read(
        categoryId: CategoryId,
        month: YearMonth
    ): Outcome<CategoryInsights, SnapshotFailure> = when (val value = state.value) {
        is CategoryInsightsState.Ready -> Outcome.Success(value.snapshot)
        is CategoryInsightsState.Failed -> Outcome.Failure(value.failure)
        CategoryInsightsState.Loading -> error("not prepared")
    }
}
