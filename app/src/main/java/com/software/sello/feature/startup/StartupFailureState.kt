package com.software.sello.feature.startup

/**
 * What the person can do when their data could not be opened at start. Implemented
 * by the composition root; nothing here opens or moves a file itself.
 */
interface StartupRecovery {
    /** Opens the data again. True when it opened and the app can be shown. */
    fun retry(): Boolean

    /**
     * Moves the data that could not be opened out of the way, keeping it, so the next
     * start is an empty one. True when it was moved. Never touches data that opened.
     */
    fun setAside(): Boolean
}

/**
 * The screen shown instead of the app when the data could not be opened. It holds no
 * figure and no reason: the failure does not say whether the file is damaged or was
 * written by a newer version, so the screen does not guess.
 */
data class StartupFailureState(
    /** Trying again was asked for and the data still did not open. */
    val stillFailing: Boolean = false,
    /** The warning before starting from zero is open. */
    val confirming: Boolean = false,
    /** Exactly what was typed to confirm. */
    val typed: String = "",
    /** Starting from zero was confirmed and the data could not be moved. Nothing changed. */
    val notSetAside: Boolean = false,
    /** The data was moved; the app must now start again from nothing. */
    val startedOver: Boolean = false
) {
    val canConfirm: Boolean get() = typed.trim() == CONFIRM_WORD

    companion object {
        /** The word D09 requires before financial data is replaced by nothing. */
        const val CONFIRM_WORD = "BORRAR"
    }
}

sealed interface StartupFailureAction {
    data object Retry : StartupFailureAction

    data object StartOver : StartupFailureAction

    data class Typed(val text: String) : StartupFailureAction

    data object Confirm : StartupFailureAction

    data object Cancel : StartupFailureAction
}
