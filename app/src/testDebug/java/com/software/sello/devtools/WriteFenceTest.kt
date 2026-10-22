package com.software.sello.devtools

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WriteFenceTest {
    private val fence = WriteFence()

    @Test
    fun closingWaitsForTheWriteInProgressToFinish() = runTest {
        val release = CompletableDeferred<Unit>()
        val events = mutableListOf<String>()
        launch {
            fence.write {
                events += "write started"
                release.await()
                events += "write finished"
            }
        }
        runCurrent()
        val closing = launch {
            fence.close()
            events += "closed"
        }
        runCurrent()
        assertFalse(closing.isCompleted)

        release.complete(Unit)
        runCurrent()

        assertEquals(listOf("write started", "write finished", "closed"), events)
    }

    @Test
    fun aWriteAskedForAfterClosingNeverStartsAndNeverReturns() = runTest {
        fence.close()
        var started = false

        val late = launch { fence.write { started = true } }
        runCurrent()

        assertFalse(started)
        assertFalse(late.isCompleted)
        late.cancel()
    }

    @Test
    fun aWriteThatFailsOrIsCancelledStillLetsTheFenceClose() = runTest {
        runCatching { fence.write { error("storage refused") } }
        val cancelled = launch { fence.write { CompletableDeferred<Unit>().await() } }
        runCurrent()
        cancelled.cancel()
        runCurrent()

        val closing = launch { fence.close() }
        runCurrent()

        assertTrue(closing.isCompleted)
    }
}
