package com.software.sello.domain.port

import com.software.sello.domain.model.CategoryId
import com.software.sello.domain.model.CategoryInsights
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.SnapshotFailure
import java.time.YearMonth
import kotlinx.coroutines.flow.Flow

sealed interface CategoryInsightsState {
    data object Loading : CategoryInsightsState
    data class Ready(val snapshot: CategoryInsights) : CategoryInsightsState
    data class Failed(val failure: SnapshotFailure, val lastGood: CategoryInsights?) :
        CategoryInsightsState
}

interface CategoryInsightsSnapshots {
    suspend fun read(
        categoryId: CategoryId,
        month: YearMonth
    ): Outcome<CategoryInsights, SnapshotFailure>
    fun observe(categoryId: CategoryId, month: YearMonth): Flow<CategoryInsightsState>
}
