package com.software.sello.devtools

import java.time.LocalDate
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SandboxProfilesTest {
    private val october = LocalDate.of(2026, 10, 31)
    private val store = InMemorySessionStore(SandboxSession(active = false, date = october))
    private val fence = WriteFence()
    private val events = mutableListOf<String>()
    private val profiles = SandboxProfiles(store, fence) { events += "restart ${store.session}" }

    @Test
    fun enteringWaitsForTheWriteInProgressThenRestartsIntoTheSandboxWhereItWasLeft() = runTest {
        val release = CompletableDeferred<Unit>()
        launch { fence.write { release.await().also { events += "write finished" } } }
        runCurrent()

        launch { profiles.enter() }
        runCurrent()
        assertTrue(events.isEmpty())

        release.complete(Unit)
        runCurrent()

        assertEquals(
            listOf(
                "write finished",
                "restart ${SandboxSession(active = true, date = october)}"
            ),
            events
        )
    }

    @Test
    fun leavingKeepsTheSandboxDataAndItsDateForNextTime() = runTest {
        store.session = SandboxSession(active = true, date = october, changed = true)

        profiles.leave()

        assertEquals(SandboxSession(active = false, date = october, changed = true), store.session)
        assertEquals(1, events.size)
    }

    @Test
    fun resettingAsksForTheSandboxToBeErasedAtTheNextStartAndForgetsItsDate() = runTest {
        store.session = SandboxSession(active = true, date = october, changed = true)

        profiles.reset()

        assertEquals(SandboxSession(active = true, wipe = true), store.session)
    }
}
