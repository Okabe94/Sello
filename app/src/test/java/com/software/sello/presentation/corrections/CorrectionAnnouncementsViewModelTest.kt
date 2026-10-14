package com.software.sello.presentation.corrections

import com.software.sello.domain.model.CorrectionAnnouncementPreference
import com.software.sello.domain.port.CorrectionAnnouncementPreferences
import com.software.sello.domain.port.PreferenceRead
import com.software.sello.domain.port.PreferenceWrite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CorrectionAnnouncementsViewModelTest {
    private val preferences = HandCorrectionAnnouncements()

    @Before
    fun main() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun resetMain() = Dispatchers.resetMain()

    @Test
    fun eachDismissalIsRememberedIndependently() = runTest {
        val model = CorrectionAnnouncementsViewModel(preferences)
        runCurrent()

        model.dismissPastExpense()
        runCurrent()
        assertEquals(true, model.state.value.preference.pastExpenseSeen)
        assertEquals(false, model.state.value.preference.pastLimitSeen)

        model.dismissPastLimit()
        runCurrent()
        assertEquals(CorrectionAnnouncementPreference(true, true), model.state.value.preference)
    }
}

private class HandCorrectionAnnouncements : CorrectionAnnouncementPreferences {
    private val values = MutableStateFlow<PreferenceRead<CorrectionAnnouncementPreference>>(
        PreferenceRead.Ready(CorrectionAnnouncementPreference())
    )

    override fun observeCorrectionAnnouncements():
        Flow<PreferenceRead<CorrectionAnnouncementPreference>> =
        values

    override suspend fun dismissPastExpenseAnnouncement(): PreferenceWrite {
        val current = (values.value as PreferenceRead.Ready).value
        values.value = PreferenceRead.Ready(current.copy(pastExpenseSeen = true))
        return PreferenceWrite.Saved
    }

    override suspend fun dismissPastLimitAnnouncement(): PreferenceWrite {
        val current = (values.value as PreferenceRead.Ready).value
        values.value = PreferenceRead.Ready(current.copy(pastLimitSeen = true))
        return PreferenceWrite.Saved
    }
}
