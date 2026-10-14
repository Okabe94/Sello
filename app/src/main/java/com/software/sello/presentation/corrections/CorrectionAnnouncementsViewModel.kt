package com.software.sello.presentation.corrections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.software.sello.domain.model.CorrectionAnnouncementPreference
import com.software.sello.domain.port.CorrectionAnnouncementPreferences
import com.software.sello.domain.port.PreferenceFailure
import com.software.sello.domain.port.PreferenceRead
import com.software.sello.domain.port.PreferenceWrite
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The two one-time correction notices remain separate from financial correction state. */
@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class CorrectionAnnouncementsViewModel(private val preferences: CorrectionAnnouncementPreferences) :
    ViewModel() {
    private val retry = MutableStateFlow(0)
    private val busy = MutableStateFlow(false)
    private val writeFailure = MutableStateFlow<PreferenceFailure?>(null)

    private val read: StateFlow<CorrectionAnnouncementState> = retry.flatMapLatest {
        preferences.observeCorrectionAnnouncements()
    }.map { result ->
        when (result) {
            is PreferenceRead.Ready -> CorrectionAnnouncementState(
                preference = result.value,
                loading = false
            )

            is PreferenceRead.Failed -> CorrectionAnnouncementState(
                preference = result.lastGood ?: CorrectionAnnouncementPreference(),
                loading = false,
                failure = result.failure
            )
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, CorrectionAnnouncementState(loading = true))

    val state: StateFlow<CorrectionAnnouncementState> = read
        .combineState(busy, writeFailure)
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            CorrectionAnnouncementState(loading = true)
        )

    fun dismissPastExpense() = dismiss(preferences::dismissPastExpenseAnnouncement)

    fun dismissPastLimit() = dismiss(preferences::dismissPastLimitAnnouncement)

    fun retry() {
        writeFailure.value = null
        retry.update { it + 1 }
    }

    private fun dismiss(write: suspend () -> PreferenceWrite) {
        if (busy.value) return
        viewModelScope.launch {
            busy.value = true
            writeFailure.value = null
            val result = write()
            busy.value = false
            if (result is PreferenceWrite.Failed) writeFailure.value = result.failure
        }
    }
}

data class CorrectionAnnouncementState(
    val preference: CorrectionAnnouncementPreference = CorrectionAnnouncementPreference(),
    val loading: Boolean,
    val failure: PreferenceFailure? = null,
    val busy: Boolean = false
)

private fun StateFlow<CorrectionAnnouncementState>.combineState(
    busy: StateFlow<Boolean>,
    writeFailure: StateFlow<PreferenceFailure?>
) = kotlinx.coroutines.flow.combine(this, busy, writeFailure) { read, writing, writeError ->
    read.copy(failure = writeError ?: read.failure, busy = writing)
}
