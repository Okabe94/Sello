package com.software.sello.composition

import androidx.lifecycle.ViewModelProvider
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure
import com.software.sello.domain.port.CheckedBackup
import com.software.sello.domain.port.DocumentSource
import com.software.sello.domain.port.RestoreOutcome
import com.software.sello.domain.port.RestoreRejection
import com.software.sello.domain.port.Restores
import com.software.sello.feature.startup.StartupRecovery
import com.software.sello.feature.startup.StartupRestore
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/** Whether the app has its data. There is no third case that shows the app without it. */
sealed interface StartupState {
    /** The data opened and the graph was built on it. */
    class Ready(val viewModels: ViewModelProvider.Factory) : StartupState

    /** The data could not be opened. No graph exists and nothing was written. */
    data class Failed(val failure: StorageFailure) : StartupState
}

/** A graph built on storage that opened: its view models, and restores into that storage. */
class Started(val viewModels: ViewModelProvider.Factory, val restores: Restores)

/** A chosen file that passed the whole-file check, held until it is restored or forgotten. */
class ChosenBackup(val bytes: ByteArray, val checked: CheckedBackup)

/**
 * Opens the data once for the process and keeps the result. [start] opens storage and
 * builds the graph on it, or says why it could not; a failure is a state to show, not
 * an exception. It is called again only while the start is failed, so a second store
 * is never opened beside one in use. [moveUnreadable] sets aside the file that would
 * not open and is likewise refused once the data has opened.
 *
 * A restore from the failure screen reads a backup with [readBackup] before anything
 * moves, then sets the data aside and opens the new store without showing it: its graph
 * is used only to restore, and the app starts again afterwards in a new process, which
 * reads the restored zone and history from the start. Once that store is open nothing
 * here opens or moves anything again.
 *
 * Everything here runs on the main thread, as the application's start does, which is
 * what keeps two attempts from overlapping.
 */
class Startup(
    private val start: () -> Outcome<Started, StorageFailure>,
    private val moveUnreadable: () -> Outcome<Unit, StorageFailure>,
    private val readBackup: suspend (DocumentSource) -> Outcome<ChosenBackup, RestoreRejection>
) : StartupRecovery {
    private val mutable = MutableStateFlow(attempt())
    val state: StateFlow<StartupState> = mutable.asStateFlow()

    private var chosen: ChosenBackup? = null

    /** The store a restore opened. It is never shown and never opened twice. */
    private var restoring: Started? = null

    override fun retry(): Boolean {
        if (mutable.value is StartupState.Failed && restoring == null) mutable.value = attempt()
        return mutable.value is StartupState.Ready
    }

    override fun setAside(): Boolean = mutable.value is StartupState.Failed &&
        restoring == null &&
        moveUnreadable() is Outcome.Success

    override suspend fun check(source: DocumentSource): Outcome<CheckedBackup, RestoreRejection> {
        chosen = null
        return when (val read = readBackup(source)) {
            is Outcome.Success -> {
                chosen = read.value
                Outcome.Success(read.value.checked)
            }

            is Outcome.Failure -> read
        }
    }

    override fun forget() {
        chosen = null
    }

    override suspend fun restore(): StartupRestore = withContext(NonCancellable) {
        val backup = chosen
        if (backup == null || mutable.value !is StartupState.Failed || restoring != null) {
            return@withContext StartupRestore.NotSetAside
        }
        if (moveUnreadable() !is Outcome.Success) return@withContext StartupRestore.NotSetAside
        val started = when (val opened = start()) {
            is Outcome.Failure -> {
                mutable.value = StartupState.Failed(opened.error)
                return@withContext StartupRestore.Interrupted
            }

            is Outcome.Success -> opened.value
        }
        restoring = started
        chosen = null
        val staged = when (val stage = started.restores.stage(backup.bytes)) {
            is Outcome.Success -> stage.value
            is Outcome.Failure -> return@withContext StartupRestore.Interrupted
        }
        when (val outcome = started.restores.replace(staged)) {
            is RestoreOutcome.Restored -> StartupRestore.Restored(outcome)
            else -> StartupRestore.Interrupted
        }
    }

    override suspend fun applyPending(): RestoreOutcome.Restored? =
        restoring?.restores?.applyPending()

    private fun attempt(): StartupState = when (val started = start()) {
        is Outcome.Success -> StartupState.Ready(started.value.viewModels)
        is Outcome.Failure -> StartupState.Failed(started.error)
    }
}
