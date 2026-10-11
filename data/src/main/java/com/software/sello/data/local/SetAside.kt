package com.software.sello.data.local

import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption.ATOMIC_MOVE

/** The database file first, then the files SQLite keeps beside it. */
private val Companions = listOf("", "-journal", "-wal", "-shm")

/**
 * Moves a closed [database] that could not be opened, with its companion files, to
 * [kept]. Nothing is deleted except an earlier kept copy, which the new one replaces
 * whole: there is one kept copy, and its companions always belong to it.
 *
 * If a file cannot be moved, those already moved are put back and the failure is
 * returned, so the next start finds the same files it found this time.
 */
internal fun setAside(database: File, kept: File): Outcome<Unit, StorageFailure> {
    if (!database.exists()) return Outcome.Success(Unit)
    val moved = mutableListOf<String>()
    return try {
        for (companion in Companions) {
            val from = File(database.path + companion)
            val to = File(kept.path + companion)
            if (from.exists()) {
                Files.move(from.toPath(), to.toPath(), ATOMIC_MOVE)
                moved += companion
            } else {
                Files.deleteIfExists(to.toPath())
            }
        }
        Outcome.Success(Unit)
    } catch (failure: IOException) {
        for (companion in moved.asReversed()) {
            File(kept.path + companion).renameTo(File(database.path + companion))
        }
        Outcome.Failure(StorageFailure.Unavailable(failure.javaClass.simpleName))
    }
}
