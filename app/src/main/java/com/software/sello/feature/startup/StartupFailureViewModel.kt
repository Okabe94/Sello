package com.software.sello.feature.startup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * The choices on the screen shown when the data could not be opened. An open warning
 * and what was typed into it are kept in [saved], so a rotation loses neither; nothing
 * is moved until the word has been typed and the confirmation pressed.
 */
class StartupFailureViewModel(
    private val saved: SavedStateHandle,
    private val recovery: StartupRecovery
) : ViewModel() {
    private val mutable = MutableStateFlow(
        StartupFailureState(
            confirming = saved[KEY_CONFIRMING] ?: false,
            typed = saved[KEY_TYPED] ?: ""
        )
    )
    val state: StateFlow<StartupFailureState> = mutable.asStateFlow()

    fun onAction(action: StartupFailureAction) {
        val now = mutable.value
        // The data has been moved and the app is starting again: nothing else applies.
        if (now.startedOver) return
        when (action) {
            StartupFailureAction.Retry -> {
                val opened = recovery.retry()
                mutable.update { it.copy(stillFailing = !opened) }
            }

            StartupFailureAction.StartOver -> warning(open = true)

            is StartupFailureAction.Typed -> if (now.confirming) {
                saved[KEY_TYPED] = action.text
                mutable.update { it.copy(typed = action.text) }
            }

            StartupFailureAction.Confirm -> if (now.confirming && now.canConfirm) {
                val moved = recovery.setAside()
                warning(open = false)
                mutable.update { it.copy(startedOver = moved, notSetAside = !moved) }
            }

            StartupFailureAction.Cancel -> warning(open = false)
        }
    }

    private fun warning(open: Boolean) {
        saved[KEY_CONFIRMING] = open
        saved[KEY_TYPED] = ""
        mutable.update { it.copy(confirming = open, typed = "", notSetAside = false) }
    }

    private companion object {
        const val KEY_CONFIRMING = "startup.confirming"
        const val KEY_TYPED = "startup.typed"
    }
}
