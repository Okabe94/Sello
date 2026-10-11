package com.software.sello.composition

import androidx.lifecycle.ViewModelProvider
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure
import com.software.sello.feature.startup.StartupRecovery
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Whether the app has its data. There is no third case that shows the app without it. */
sealed interface StartupState {
    /** The data opened and the graph was built on it. */
    class Ready(val viewModels: ViewModelProvider.Factory) : StartupState

    /** The data could not be opened. No graph exists and nothing was written. */
    data class Failed(val failure: StorageFailure) : StartupState
}

/**
 * Opens the data once for the process and keeps the result. [start] opens storage and
 * builds the graph on it, or says why it could not; a failure is a state to show, not
 * an exception. It is called again only while the start is failed, so a second store
 * is never opened beside one in use. [moveUnreadable] sets aside the file that would
 * not open and is likewise refused once the data has opened.
 *
 * Everything here runs on the main thread, as the application's start does, which is
 * what keeps two attempts from overlapping.
 */
class Startup(
    private val start: () -> Outcome<ViewModelProvider.Factory, StorageFailure>,
    private val moveUnreadable: () -> Outcome<Unit, StorageFailure>
) : StartupRecovery {
    private val mutable = MutableStateFlow(attempt())
    val state: StateFlow<StartupState> = mutable.asStateFlow()

    override fun retry(): Boolean {
        if (mutable.value is StartupState.Failed) mutable.value = attempt()
        return mutable.value is StartupState.Ready
    }

    override fun setAside(): Boolean =
        mutable.value is StartupState.Failed && moveUnreadable() is Outcome.Success

    private fun attempt(): StartupState = when (val started = start()) {
        is Outcome.Success -> StartupState.Ready(started.value)
        is Outcome.Failure -> StartupState.Failed(started.error)
    }
}
