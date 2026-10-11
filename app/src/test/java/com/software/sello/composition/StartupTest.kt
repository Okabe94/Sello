package com.software.sello.composition

import androidx.lifecycle.ViewModelProvider
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/** Starting the app against storage a test scripts: each open and each move is counted. */
class StartupTest {
    private val corrupt = StorageFailure.Unavailable("SQLiteDatabaseCorruptException")
    private val full = StorageFailure.Unavailable("SQLiteFullException")
    private val factory = object : ViewModelProvider.Factory {}

    private val opens = mutableListOf<Outcome<ViewModelProvider.Factory, StorageFailure>>()
    private var opened = 0
    private var moves = 0
    private var move: Outcome<Unit, StorageFailure> = Outcome.Success(Unit)

    /** Answers each open with the next scripted result; a test never opens more than it scripted. */
    private fun startup(
        vararg results: Outcome<ViewModelProvider.Factory, StorageFailure>
    ): Startup {
        opens += results
        return Startup(
            start = { opens[opened++] },
            moveUnreadable = {
                moves++
                move
            }
        )
    }

    @Test
    fun dataThatCannotBeOpenedIsAFailedStartNotACrash() {
        val startup = startup(Outcome.Failure(corrupt))

        assertEquals(StartupState.Failed(corrupt), startup.state.value)
        assertEquals(1, opened)
    }

    @Test
    fun dataThatOpensIsReadyWithTheViewModelsBuiltForIt() {
        val startup = startup(Outcome.Success(factory))

        assertSame(factory, (startup.state.value as StartupState.Ready).viewModels)
    }

    @Test
    fun tryingAgainOpensOnceMoreAndBecomesReadyWhenItWorks() {
        val startup = startup(Outcome.Failure(corrupt), Outcome.Success(factory))

        assertTrue(startup.retry())

        assertSame(factory, (startup.state.value as StartupState.Ready).viewModels)
        assertEquals(2, opened)
    }

    @Test
    fun tryingAgainThatFailsStaysFailedWithTheLatestReason() {
        val startup = startup(Outcome.Failure(full), Outcome.Failure(corrupt))

        assertFalse(startup.retry())

        assertEquals(StartupState.Failed(corrupt), startup.state.value)
        assertEquals(2, opened)
    }

    @Test
    fun onceReadyTryingAgainNeverOpensASecondStore() {
        val startup = startup(Outcome.Failure(corrupt), Outcome.Success(factory))
        startup.retry()

        assertTrue(startup.retry())
        assertTrue(startup.retry())

        assertEquals(2, opened)
        assertSame(factory, (startup.state.value as StartupState.Ready).viewModels)
    }

    @Test
    fun dataThatOpenedIsNeverSetAside() {
        val startup = startup(Outcome.Success(factory))

        assertFalse(startup.setAside())

        assertEquals(0, moves)
        assertSame(factory, (startup.state.value as StartupState.Ready).viewModels)
    }

    @Test
    fun settingAsideMovesTheDataAndOpensNothingByItself() {
        val startup = startup(Outcome.Failure(corrupt))

        assertTrue(startup.setAside())

        assertEquals(1, moves)
        assertEquals(1, opened)
        assertEquals(StartupState.Failed(corrupt), startup.state.value)
    }

    @Test
    fun dataThatCannotBeMovedIsReportedAndLeavesTheStartFailed() {
        move = Outcome.Failure(StorageFailure.Unavailable("AccessDeniedException"))
        val startup = startup(Outcome.Failure(corrupt))

        assertFalse(startup.setAside())

        assertEquals(1, moves)
        assertEquals(StartupState.Failed(corrupt), startup.state.value)
    }
}
