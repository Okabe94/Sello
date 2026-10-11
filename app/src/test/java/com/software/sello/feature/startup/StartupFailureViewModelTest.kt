package com.software.sello.feature.startup

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StartupFailureViewModelTest {
    /** Recovery a test scripts: what each request answers, and how often it was asked. */
    private class HandRecovery : StartupRecovery {
        var opens = false
        var moves = true
        var retries = 0
        var setAsides = 0

        override fun retry(): Boolean {
            retries++
            return opens
        }

        override fun setAside(): Boolean {
            setAsides++
            return moves
        }
    }

    private val recovery = HandRecovery()
    private val saved = SavedStateHandle()

    private fun viewModel(handle: SavedStateHandle = saved) =
        StartupFailureViewModel(handle, recovery)

    private fun StartupFailureViewModel.confirmWith(text: String) {
        onAction(StartupFailureAction.StartOver)
        onAction(StartupFailureAction.Typed(text))
        onAction(StartupFailureAction.Confirm)
    }

    @Test
    fun itStartsWithNothingAskedAndNothingTouched() {
        val viewModel = viewModel()

        assertEquals(StartupFailureState(), viewModel.state.value)
        assertEquals(0, recovery.retries)
        assertEquals(0, recovery.setAsides)
    }

    @Test
    fun tryingAgainThatStillFailsSaysSo() {
        val viewModel = viewModel()

        viewModel.onAction(StartupFailureAction.Retry)

        assertEquals(1, recovery.retries)
        assertTrue(viewModel.state.value.stillFailing)
    }

    @Test
    fun tryingAgainThatWorksReportsNoFailure() {
        recovery.opens = true
        val viewModel = viewModel()

        viewModel.onAction(StartupFailureAction.Retry)

        assertEquals(1, recovery.retries)
        assertFalse(viewModel.state.value.stillFailing)
    }

    @Test
    fun askingToStartOverOnlyOpensTheWarning() {
        val viewModel = viewModel()

        viewModel.onAction(StartupFailureAction.StartOver)

        assertTrue(viewModel.state.value.confirming)
        assertFalse(viewModel.state.value.canConfirm)
        assertEquals(0, recovery.setAsides)
    }

    @Test
    fun confirmingWithoutTheWordMovesNothing() {
        val viewModel = viewModel()

        for (text in listOf("", "borrar", "BORRA", "BORRAR ya", "B O R R A R")) {
            viewModel.confirmWith(text)
        }

        assertEquals(0, recovery.setAsides)
        assertFalse(viewModel.state.value.startedOver)
        assertTrue(viewModel.state.value.confirming)
    }

    @Test
    fun confirmingWithTheWordMovesTheDataOnceAndStartsOver() {
        val viewModel = viewModel()

        viewModel.confirmWith("BORRAR")
        viewModel.onAction(StartupFailureAction.Confirm)

        assertEquals(1, recovery.setAsides)
        assertTrue(viewModel.state.value.startedOver)
        assertEquals(0, recovery.retries)
    }

    @Test
    fun theWordIsAcceptedWithSpacesAroundItAsAKeyboardLeavesThem() {
        val viewModel = viewModel()

        viewModel.confirmWith(" BORRAR ")

        assertEquals(1, recovery.setAsides)
    }

    @Test
    fun dataThatCouldNotBeMovedIsReportedAndCanBeConfirmedAgain() {
        recovery.moves = false
        val viewModel = viewModel()

        viewModel.confirmWith("BORRAR")

        assertTrue(viewModel.state.value.notSetAside)
        assertFalse(viewModel.state.value.startedOver)
        assertFalse(viewModel.state.value.confirming)

        recovery.moves = true
        viewModel.confirmWith("BORRAR")

        assertEquals(2, recovery.setAsides)
        assertTrue(viewModel.state.value.startedOver)
        assertFalse(viewModel.state.value.notSetAside)
    }

    @Test
    fun cancellingClosesTheWarningAndForgetsWhatWasTyped() {
        val viewModel = viewModel()
        viewModel.onAction(StartupFailureAction.StartOver)
        viewModel.onAction(StartupFailureAction.Typed("BORRAR"))

        viewModel.onAction(StartupFailureAction.Cancel)
        viewModel.onAction(StartupFailureAction.Confirm)

        assertEquals(StartupFailureState(), viewModel.state.value)
        assertEquals(0, recovery.setAsides)
    }

    @Test
    fun anOpenWarningAndWhatWasTypedSurviveRecreationWithoutConfirmingAnything() {
        val first = viewModel()
        first.onAction(StartupFailureAction.StartOver)
        first.onAction(StartupFailureAction.Typed("BORR"))

        val restored = viewModel(saved)

        assertTrue(restored.state.value.confirming)
        assertEquals("BORR", restored.state.value.typed)
        assertEquals(0, recovery.setAsides)
    }
}
