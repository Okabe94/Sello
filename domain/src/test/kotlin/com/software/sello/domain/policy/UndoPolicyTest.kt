package com.software.sello.domain.policy

import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Approved U01: undo at 5.999 s is in time; at 6 s it is not. */
class UndoPolicyTest {
    private val deletedAt = 5.hours

    @Test
    fun theWindowIsSixSeconds() {
        assertEquals(6.seconds, UndoPolicy.WINDOW)
    }

    @Test
    fun aDeletionCanBeUndoneRightAwayAndUntilJustBeforeSixSeconds() {
        assertTrue(UndoPolicy.canUndo(deletedAt, deletedAt))
        assertTrue(UndoPolicy.canUndo(deletedAt, deletedAt + 4.seconds))
        assertTrue(UndoPolicy.canUndo(deletedAt, deletedAt + 5999.milliseconds))
        assertTrue(UndoPolicy.canUndo(deletedAt, deletedAt + 6.seconds - 1.nanoseconds))
    }

    @Test
    fun atSixSecondsExactlyAndAfterItCannot() {
        assertFalse(UndoPolicy.canUndo(deletedAt, deletedAt + 6.seconds))
        assertFalse(UndoPolicy.canUndo(deletedAt, deletedAt + 6.seconds + 1.nanoseconds))
        assertFalse(UndoPolicy.canUndo(deletedAt, deletedAt + 1.hours))
    }
}
