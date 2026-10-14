package com.software.sello.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.datastore.preferences.core.stringPreferencesKey
import com.software.sello.domain.model.AppearancePreference
import com.software.sello.domain.model.CategoryOrder
import com.software.sello.domain.model.CategoryOrderDirection
import com.software.sello.domain.model.CategoryOrderingPreference
import com.software.sello.domain.model.CorrectionAnnouncementPreference
import com.software.sello.domain.model.InkPreference
import com.software.sello.domain.model.ThemePreference
import com.software.sello.domain.port.PreferenceFailure
import com.software.sello.domain.port.PreferenceRead
import com.software.sello.domain.port.PreferenceWrite
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class DataStoreUserPreferencesTest {
    private class HandDataStore(initial: Preferences = emptyPreferences()) :
        DataStore<Preferences> {
        private val values = MutableStateFlow(initial)
        override val data: Flow<Preferences> = values

        override suspend fun updateData(
            transform: suspend (t: Preferences) -> Preferences
        ): Preferences = transform(values.value).also { values.value = it }
    }

    private class UnavailableDataStore : DataStore<Preferences> {
        override val data: Flow<Preferences> = flow { throw IOException("not readable") }

        override suspend fun updateData(
            transform: suspend (t: Preferences) -> Preferences
        ): Preferences = throw IOException("not writable")
    }

    @Test
    fun missingValuesHaveTheApprovedDefaults() = runTest {
        val preferences = DataStoreUserPreferences(HandDataStore())

        assertEquals(
            PreferenceRead.Ready(AppearancePreference()),
            preferences.observeAppearance().first()
        )
        assertEquals(
            PreferenceRead.Ready(CategoryOrderingPreference()),
            preferences.observeCategoryOrdering().first()
        )
    }

    @Test
    fun stableCodesSurviveANewAdapterReadingTheSameStore() = runTest {
        val data = HandDataStore()
        val first = DataStoreUserPreferences(data)

        assertEquals(PreferenceWrite.Saved, first.setTheme(ThemePreference.Dark))
        assertEquals(PreferenceWrite.Saved, first.setInk(InkPreference.Violeta))
        assertEquals(
            PreferenceWrite.Saved,
            first.setCategoryOrder(CategoryOrder.LatestExpenseAdded)
        )
        assertEquals(
            PreferenceWrite.Saved,
            first.setCategoryOrderDirection(CategoryOrderDirection.Ascending)
        )

        val reopened = DataStoreUserPreferences(data)
        assertEquals(
            PreferenceRead.Ready(AppearancePreference(ThemePreference.Dark, InkPreference.Violeta)),
            reopened.observeAppearance().first()
        )
        assertEquals(
            PreferenceRead.Ready(
                CategoryOrderingPreference(
                    CategoryOrder.LatestExpenseAdded,
                    CategoryOrderDirection.Ascending
                )
            ),
            reopened.observeCategoryOrdering().first()
        )
    }

    @Test
    fun correctionDismissalsSurviveANewAdapterIndependently() = runTest {
        val data = HandDataStore()
        val first = DataStoreUserPreferences(data)

        assertEquals(PreferenceWrite.Saved, first.dismissPastExpenseAnnouncement())

        val reopened = DataStoreUserPreferences(data)
        assertEquals(
            PreferenceRead.Ready(CorrectionAnnouncementPreference(pastExpenseSeen = true)),
            reopened.observeCorrectionAnnouncements().first()
        )
        assertEquals(PreferenceWrite.Saved, reopened.dismissPastLimitAnnouncement())
        assertEquals(
            PreferenceRead.Ready(CorrectionAnnouncementPreference(true, true)),
            DataStoreUserPreferences(data).observeCorrectionAnnouncements().first()
        )
    }

    @Test
    fun anUnknownStoredCodeIsVisibleAndIsNotSilentlyRewritten() = runTest {
        val raw = mutablePreferencesOf(stringPreferencesKey("appearance_theme_v1") to "neon")
        val preferences = DataStoreUserPreferences(HandDataStore(raw))

        assertEquals(
            PreferenceRead.Failed<AppearancePreference>(PreferenceFailure.InvalidValue, null),
            preferences.observeAppearance().first()
        )
    }

    @Test
    fun readAndWriteFailuresStayNonfinancialAndObservable() = runTest {
        val preferences = DataStoreUserPreferences(UnavailableDataStore())

        assertEquals(
            PreferenceRead.Failed<AppearancePreference>(PreferenceFailure.Unavailable, null),
            preferences.observeAppearance().first()
        )
        assertEquals(
            PreferenceWrite.Failed(PreferenceFailure.Unavailable),
            preferences.setTheme(ThemePreference.Light)
        )
    }
}
