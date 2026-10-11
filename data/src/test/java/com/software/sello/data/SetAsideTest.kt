package com.software.sello.data

import com.software.sello.data.local.setAside
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure
import java.io.File
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

/** Moving a database that could not be opened out of the way, on real files. */
class SetAsideTest {
    @get:Rule
    val folder = TemporaryFolder()

    private val database get() = File(folder.root, "sello.db")
    private val kept get() = File(folder.root, "kept.db")

    private fun File.beside(suffix: String) = File(path + suffix)

    private fun names() = folder.root.list()!!.sorted()

    @Test
    fun theFileAndItsCompanionsAreKeptUnderTheOtherNameByteForByte() {
        database.writeBytes(byteArrayOf(1, 2, 3))
        database.beside("-wal").writeBytes(byteArrayOf(4, 5))
        database.beside("-shm").writeBytes(byteArrayOf(6))
        database.beside("-journal").writeBytes(byteArrayOf(7, 8, 9, 10))

        val result = setAside(database, kept)

        assertEquals(Outcome.Success(Unit), result)
        assertEquals(listOf("kept.db", "kept.db-journal", "kept.db-shm", "kept.db-wal"), names())
        assertArrayEquals(byteArrayOf(1, 2, 3), kept.readBytes())
        assertArrayEquals(byteArrayOf(4, 5), kept.beside("-wal").readBytes())
        assertArrayEquals(byteArrayOf(6), kept.beside("-shm").readBytes())
        assertArrayEquals(byteArrayOf(7, 8, 9, 10), kept.beside("-journal").readBytes())
    }

    @Test
    fun anEarlierKeptCopyIsReplacedWholeSoItsCompanionsNeverMixWithTheNewOne() {
        kept.writeBytes(byteArrayOf(20))
        kept.beside("-wal").writeBytes(byteArrayOf(21))
        database.writeBytes(byteArrayOf(1))

        val result = setAside(database, kept)

        assertEquals(Outcome.Success(Unit), result)
        assertEquals(listOf("kept.db"), names())
        assertArrayEquals(byteArrayOf(1), kept.readBytes())
    }

    @Test
    fun withNoFileThereIsNothingToMoveAndAnEarlierKeptCopyStays() {
        kept.writeBytes(byteArrayOf(20))

        val result = setAside(database, kept)

        assertEquals(Outcome.Success(Unit), result)
        assertEquals(listOf("kept.db"), names())
        assertArrayEquals(byteArrayOf(20), kept.readBytes())
    }

    @Test
    fun aMoveThatCannotFinishIsAFailureAndLeavesTheFileWhereItWas() {
        database.writeBytes(byteArrayOf(1, 2, 3))
        database.beside("-wal").writeBytes(byteArrayOf(4, 5))
        // A folder with something in it cannot be replaced by a file.
        kept.beside("-wal").mkdir()
        File(kept.beside("-wal"), "inside").writeBytes(byteArrayOf(0))

        val result = setAside(database, kept)

        assertTrue("$result", (result as Outcome.Failure).error is StorageFailure.Unavailable)
        assertArrayEquals(byteArrayOf(1, 2, 3), database.readBytes())
        assertArrayEquals(byteArrayOf(4, 5), database.beside("-wal").readBytes())
        assertFalse(kept.exists())
    }
}
