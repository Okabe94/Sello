package com.software.sello.feature.reset

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.software.sello.domain.port.AppRestart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Borrar todos los datos: counts first, then the exact word. Which confirmation is
 * open and what was typed are kept in [saved], so a rotation or process death loses
 * neither; the counts are read again. Counting and erasing belong to [ResetSession].
 * After an erase, going on restarts the app, whose zone and history are new.
 */
class ResetViewModel(
    private val saved: SavedStateHandle,
    private val session: ResetSession,
    private val restart: AppRestart
) : ViewModel() {
    private val confirming = MutableStateFlow(saved[KEY_CONFIRMING] ?: false)
    private val typed = MutableStateFlow(saved[KEY_TYPED] ?: "")

    init {
        session.open()
    }

    val state: StateFlow<ResetState> = combine(session.step, confirming, typed) {
            step,
            open,
            text
        ->
        ResetState(step = step, confirming = open && step is ResetStep.Ready, typed = text)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(STOP_MILLIS),
        ResetState(session.step.value, confirming.value, typed.value)
    )

    fun onAction(action: ResetAction) {
        val now = state.value
        when (action) {
            ResetAction.Continue -> if (now.step is ResetStep.Ready) secondStep(open = true)

            is ResetAction.Typed -> if (now.confirming) {
                saved[KEY_TYPED] = action.text
                typed.value = action.text
            }

            ResetAction.Confirm -> {
                val ready = now.step as? ResetStep.Ready ?: return
                if (!now.confirming || !now.canConfirm) return
                // A refused erase comes back as new counts on the first confirmation.
                secondStep(open = false)
                session.erase(ready.preview)
            }

            ResetAction.Cancel -> secondStep(open = false)

            ResetAction.Retry -> session.retry()

            ResetAction.Check -> session.check()

            ResetAction.ApplyPreferences -> session.applyPreferences()

            ResetAction.Leave -> if (now.erased) {
                session.finish()
                restart.restart()
            }
        }
    }

    private fun secondStep(open: Boolean) {
        saved[KEY_CONFIRMING] = open
        saved[KEY_TYPED] = ""
        confirming.value = open
        typed.value = ""
    }

    private companion object {
        const val KEY_CONFIRMING = "reset.confirming"
        const val KEY_TYPED = "reset.typed"
        const val STOP_MILLIS = 5_000L
    }
}

sealed interface ResetAction {
    /** Continuar: from the counts to the word. */
    data object Continue : ResetAction

    data class Typed(val text: String) : ResetAction

    /** Borrar todo, once the word is typed. */
    data object Confirm : ResetAction

    /** Closes the second confirmation, back to the counts. */
    data object Cancel : ResetAction

    data object Retry : ResetAction

    data object Check : ResetAction

    data object ApplyPreferences : ResetAction

    /** Empezar de nuevo, or Back, once the history is or may be erased. */
    data object Leave : ResetAction
}

data class ResetState(
    val step: ResetStep = ResetStep.Idle,
    /** The second confirmation is open. */
    val confirming: Boolean = false,
    /** Exactly what was typed to confirm. */
    val typed: String = ""
) {
    val canConfirm: Boolean get() = typed.trim() == CONFIRM_WORD

    val busy: Boolean get() = step == ResetStep.Counting || step is ResetStep.Erasing

    /** Once the history is or may be erased, every way out starts the app again. */
    val erased: Boolean get() = step is ResetStep.Erased || step is ResetStep.Unknown

    companion object {
        /** The word D09 requires before financial data is replaced by nothing. */
        const val CONFIRM_WORD = "BORRAR"
    }
}
