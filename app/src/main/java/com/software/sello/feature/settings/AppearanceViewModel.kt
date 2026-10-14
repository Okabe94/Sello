package com.software.sello.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.software.sello.domain.model.AppearancePreference
import com.software.sello.domain.port.AppearancePreferences
import com.software.sello.domain.port.PreferenceFailure
import com.software.sello.domain.port.PreferenceRead
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/** The root needs only the usable appearance value, never the settings write API. */
class AppearanceViewModel(preferences: AppearancePreferences) : ViewModel() {
    val state: StateFlow<AppearanceState> = preferences.observeAppearance()
        .map { read ->
            when (read) {
                is PreferenceRead.Ready -> AppearanceState(read.value, loading = false)

                is PreferenceRead.Failed -> AppearanceState(
                    appearance = read.lastGood ?: AppearancePreference(),
                    loading = false,
                    failure = read.failure
                )
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            AppearanceState(loading = true)
        )
}

data class AppearanceState(
    val appearance: AppearancePreference = AppearancePreference(),
    val loading: Boolean = false,
    val failure: PreferenceFailure? = null
)
