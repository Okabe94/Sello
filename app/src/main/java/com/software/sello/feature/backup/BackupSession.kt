package com.software.sello.feature.backup

import com.software.sello.domain.model.Outcome
import com.software.sello.domain.port.AuditClock
import com.software.sello.domain.port.BackupExports
import com.software.sello.domain.port.BackupPreparationFailure
import com.software.sello.domain.port.BackupRecords
import com.software.sello.domain.port.DocumentDestination
import com.software.sello.domain.port.DocumentWrite
import com.software.sello.domain.port.DocumentWriter
import com.software.sello.domain.port.LastBackup
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Why a save did not produce a backup. */
enum class BackupFailure {
    /** The data could not be read. */
    Storage,

    /** The data is more than a backup may hold. */
    TooLarge,

    /** The chosen document could not be written or closed. */
    Write
}

sealed interface BackupProgress {
    data object Idle : BackupProgress

    data object Saving : BackupProgress

    /** Written, flushed and closed. */
    data class Saved(val backup: LastBackup) : BackupProgress

    data class Failed(val failure: BackupFailure) : BackupProgress
}

/**
 * One save at a time, for the life of the process, so leaving the screen does not
 * cut a file short. The start is recorded before any byte is written and the outcome
 * after the document is closed: a process that dies in between leaves the start
 * behind, and the screen reports the outcome as unknown instead of saved.
 */
class BackupSession(
    private val exports: BackupExports,
    private val writer: DocumentWriter,
    private val records: BackupRecords,
    private val auditClock: AuditClock,
    private val scope: CoroutineScope
) {
    private val state = MutableStateFlow<BackupProgress>(BackupProgress.Idle)
    val progress: StateFlow<BackupProgress> = state.asStateFlow()

    suspend fun suggestedName(): String = exports.suggestedName()

    /** Saves to [destination] unless a save is already running. */
    fun save(destination: DocumentDestination, suggestedName: String) {
        var started = false
        state.update { current ->
            if (current == BackupProgress.Saving) {
                current
            } else {
                started = true
                BackupProgress.Saving
            }
        }
        if (!started) return
        scope.launch { state.value = run(destination, suggestedName) }
    }

    /** Forgets a finished outcome once it has been shown. */
    fun acknowledge() {
        state.update { if (it == BackupProgress.Saving) it else BackupProgress.Idle }
    }

    private suspend fun run(
        destination: DocumentDestination,
        suggestedName: String
    ): BackupProgress {
        // Even if this cannot be recorded the save goes ahead; only the unknown-outcome
        // notice after a crash depends on it.
        records.backupStarted()
        val prepared = when (val made = exports.prepare()) {
            is Outcome.Success -> made.value

            is Outcome.Failure -> {
                records.backupAbandoned()
                return BackupProgress.Failed(
                    when (made.error) {
                        BackupPreparationFailure.StorageUnavailable -> BackupFailure.Storage
                        BackupPreparationFailure.Unrestorable -> BackupFailure.TooLarge
                    }
                )
            }
        }
        return when (val written = writer.write(destination, prepared.bytes)) {
            DocumentWrite.Failed -> {
                records.backupAbandoned()
                BackupProgress.Failed(BackupFailure.Write)
            }

            is DocumentWrite.Completed -> {
                val backup = LastBackup(
                    at = auditClock.now(),
                    name = written.name ?: suggestedName,
                    bytes = written.bytes,
                    summary = prepared.summary
                )
                // The file is complete whether or not this note of it can be kept.
                records.backupCompleted(backup)
                BackupProgress.Saved(backup)
            }
        }
    }
}
