package com.software.sello.feature.category

import androidx.lifecycle.SavedStateHandle
import com.software.sello.domain.model.BudgetLimit
import com.software.sello.domain.model.CategoryId
import com.software.sello.domain.model.Currency
import com.software.sello.domain.model.Money
import com.software.sello.domain.model.OperationId
import com.software.sello.domain.model.OperationKind
import com.software.sello.domain.model.OperationReceipt
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure
import com.software.sello.domain.port.CategoryCommand
import com.software.sello.domain.port.CategoryCommandOutcome
import com.software.sello.domain.port.CategoryCommands
import com.software.sello.domain.port.CategoryRejection
import com.software.sello.domain.port.CommittedCategoryChange
import com.software.sello.domain.port.CreateCategory
import com.software.sello.navigation.HandCategoryReads
import com.software.sello.navigation.HandFinancialClock
import com.software.sello.navigation.HandMonotonicClock
import com.software.sello.navigation.MonthSession
import com.software.sello.presentation.money.MoneyFormatter
import com.software.sello.presentation.money.MoneyLabels
import java.time.Instant
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CategoryEditorViewModelTest {
    private fun uuid(number: Int) = "00000000-0000-4000-8000-%012d".format(number)

    private val created = (CategoryId.of(uuid(900)) as Outcome.Success).value

    /** Commands a test scripts: what `submit` and `find` answer, and what they were sent. */
    private inner class HandCommands : CategoryCommands {
        val submitted = mutableListOf<CreateCategory>()
        val looked = mutableListOf<OperationId>()
        var answer: (CreateCategory) -> CategoryCommandOutcome = { committed(it.operationId) }
        var found: (OperationId) -> Outcome<CommittedCategoryChange?, StorageFailure> =
            { Outcome.Success(null) }

        /** When set, `submit` waits here: the save is in flight. */
        var hold: CompletableDeferred<Unit>? = null

        override suspend fun submit(command: CategoryCommand): CategoryCommandOutcome {
            submitted += command as CreateCategory
            hold?.await()
            return answer(command)
        }

        override suspend fun find(
            operationId: OperationId
        ): Outcome<CommittedCategoryChange?, StorageFailure> {
            looked += operationId
            return found(operationId)
        }
    }

    private fun change(operation: OperationId) = CommittedCategoryChange(
        OperationReceipt(operation, OperationKind.CreateCategory, 1, 1, Instant.EPOCH),
        created
    )

    private fun committed(operation: OperationId) =
        CategoryCommandOutcome.Committed(change(operation), replayed = false)

    private fun rejected(reason: CategoryRejection) = CategoryCommandOutcome.Rejected(reason)

    private val commands = HandCommands()
    private val reads = HandCategoryReads()
    private val session = MonthSession(HandFinancialClock("2026-10-09"), HandMonotonicClock())
    private var nextId = 500
    private val money = MoneyFormatter(
        object : MoneyLabels {
            override fun amount(currency: Currency, digits: String, isOne: Boolean) = digits

            override fun negative(amount: String) = amount

            override fun positive(amount: String) = amount
        }
    )

    @Before
    fun mainDispatcher() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun resetDispatcher() = Dispatchers.resetMain()

    private fun editor(saved: SavedStateHandle = SavedStateHandle()) =
        CategoryEditorViewModel(saved, commands, reads, session, { uuid(nextId++) }, money)

    private fun SavedStateHandle.afterProcessDeath() =
        SavedStateHandle(keys().associateWith { get<Any?>(it) })

    private fun CategoryEditorViewModel.type(name: String) =
        onAction(CategoryEditorAction.NameChanged(name))

    private fun CategoryEditorViewModel.limit(text: String) {
        onAction(CategoryEditorAction.UnlimitedChanged(false))
        onAction(CategoryEditorAction.LimitChanged(text))
    }

    private fun CategoryEditorViewModel.submit() = onAction(CategoryEditorAction.Submit)

    private fun finite(pesos: Long) =
        (BudgetLimit.finite(Money.cop(pesos)) as Outcome.Success).value

    @Test
    fun aNewFormIsEmptyUnlimitedAndOnTheFirstIconNobodyUses() {
        reads.categories = listOf(
            reads.category(1, "Comida", icon = "restaurant"),
            reads.category(2, "Casa", icon = "home", archived = true)
        )

        val state = editor().state.value

        assertEquals("" to "", state.name to state.limitText)
        assertTrue(state.unlimited)
        assertEquals("directions_bus", state.iconKey)
        assertEquals(14, state.iconKeys.size)
        assertEquals(listOf("200.000", "300.000", "500.000"), state.suggestions.map { it.label })
        assertFalse(state.loading || state.loadFailed || state.forEntry || state.canSubmit)
        assertNull(state.result)
    }

    @Test
    fun nothingIsSentWithoutAName() {
        val editor = editor()

        editor.submit()
        editor.type("   ")
        editor.submit()

        assertEquals(emptyList<CreateCategory>(), commands.submitted)
        assertFalse(editor.state.value.canSubmit)
        // The button is off, so pressing it is not an attempt and shows no error.
        assertNull(editor.state.value.nameError)
    }

    @Test
    fun aValidFormSendsOneCommandWithExactlyWhatWasChosen() {
        reads.generation = 4
        val editor = editor()
        editor.type("  Mercado de plaza ")
        editor.onAction(CategoryEditorAction.IconPicked("shopping_cart"))
        editor.limit("300.000")

        editor.submit()

        val command = commands.submitted.single()
        assertEquals(uuid(500), command.operationId.value)
        assertEquals(4L, command.generation)
        assertEquals("Mercado de plaza", command.name.value)
        assertEquals("shopping_cart", command.icon.value)
        assertEquals(finite(300_000), command.limit)
        assertEquals(EditorResult.Created(uuid(900)), editor.state.value.result)
    }

    @Test
    fun withoutALimitTheCategoryIsUnlimitedNotZero() {
        val editor = editor()
        editor.type("Mercado")

        editor.submit()

        assertEquals(BudgetLimit.Unlimited, commands.submitted.single().limit)
    }

    @Test
    fun aTypedZeroIsARealZeroBudget() {
        val editor = editor()
        editor.type("Mercado")
        editor.limit("0")

        editor.submit()

        assertEquals(finite(0), commands.submitted.single().limit)
    }

    @Test
    fun turningTheLimitOffWithNoAmountAsksForOneInsteadOfAssumingZero() {
        val editor = editor()
        editor.type("Mercado")
        editor.limit("")

        editor.submit()

        assertEquals(LimitError.Required, editor.state.value.limitError)
        assertEquals(emptyList<CreateCategory>(), commands.submitted)
    }

    @Test
    fun aLimitThatIsNotAnAmountIsRefusedAndTheTextIsKeptAsTyped() {
        for (text in listOf("1e3", "12.34", "-5", "$300", "300 000", "trescientos", "1,5")) {
            val editor = editor()
            editor.type("Mercado")
            editor.limit(text)

            editor.submit()

            assertEquals(text, LimitError.NotAnAmount, editor.state.value.limitError)
            assertEquals(text, text, editor.state.value.limitText)
        }
        assertEquals(emptyList<CreateCategory>(), commands.submitted)
    }

    @Test
    fun choosingSinLimiteClearsTheAmountAndIgnoresTyping() {
        val editor = editor()
        editor.limit("300000")

        editor.onAction(CategoryEditorAction.UnlimitedChanged(true))
        editor.onAction(CategoryEditorAction.LimitChanged("999"))

        assertEquals(true to "", editor.state.value.run { unlimited to limitText })
    }

    @Test
    fun aSuggestionFillsTheAmountAndTurnsTheLimitOn() {
        val editor = editor()
        editor.type("Mercado")

        editor.onAction(CategoryEditorAction.SuggestionPicked(500_000))
        editor.submit()

        assertEquals(false to "500000", editor.state.value.run { unlimited to limitText })
        assertEquals(finite(500_000), commands.submitted.single().limit)
    }

    @Test
    fun aNameThatBreaksTheRulesIsRefusedBeforeAnythingIsSentAndKeptAsTyped() {
        val cases = listOf(
            "a".repeat(25) to NameError.TooLong,
            "🎉".repeat(25) to NameError.TooLong,
            "Mer\ncado" to NameError.NotAllowed
        )

        for ((text, error) in cases) {
            val editor = editor()
            editor.type(text)

            editor.submit()

            assertEquals(text, error, editor.state.value.nameError)
            assertEquals(text, text, editor.state.value.name)
        }
        assertEquals(emptyList<CreateCategory>(), commands.submitted)
    }

    @Test
    fun twentyFourCharactersOfAnyKindAreAccepted() {
        val editor = editor()
        editor.type("🎉".repeat(24))

        editor.submit()

        assertEquals("🎉".repeat(24), commands.submitted.single().name.value)
    }

    @Test
    fun whileSavingTheFormIsBusyLockedAndASecondTapSendsNothing() {
        commands.hold = CompletableDeferred()
        val editor = editor()
        editor.type("Mercado")

        editor.submit()
        val saving = editor.state.value
        editor.submit()
        editor.type("Otro nombre")
        editor.onAction(CategoryEditorAction.IconPicked("pets"))
        editor.onAction(CategoryEditorAction.Back)

        assertTrue(saving.busy && !saving.canSubmit && !saving.editable)
        assertNull(saving.result)
        assertEquals(1, commands.submitted.size)
        assertEquals("Mercado", editor.state.value.name)
        assertNull(editor.state.value.result)
        // Back does nothing at all while saving: no leaving and no question either.
        assertFalse(editor.state.value.confirmDiscard)

        commands.hold!!.complete(Unit)

        // Created only now, when the receipt arrived.
        assertEquals(EditorResult.Created(uuid(900)), editor.state.value.result)
        assertFalse(editor.state.value.busy)
    }

    @Test
    fun aTakenNameIsShownOnTheFieldAndTheNextAttemptIsANewOperation() {
        commands.answer = { rejected(CategoryRejection.NameTaken) }
        val editor = editor()
        editor.type("mercado")

        editor.submit()
        val refused = editor.state.value
        commands.answer = { committed(it.operationId) }
        editor.type("Plaza")
        editor.submit()

        assertEquals(NameError.Taken, refused.nameError)
        assertEquals("mercado", refused.name)
        assertNull(refused.result)
        assertEquals(listOf(uuid(500), uuid(501)), commands.submitted.map { it.operationId.value })
        assertEquals(EditorResult.Created(uuid(900)), editor.state.value.result)
    }

    @Test
    fun aReplacedHistoryIsExplainedAndTheNextAttemptUsesTheNewGeneration() {
        commands.answer = {
            // The history is replaced between the form opening and the save.
            reads.generation = 2
            rejected(CategoryRejection.StaleGeneration(current = 2))
        }
        val editor = editor()
        editor.type("Mercado")

        editor.submit()
        val refused = editor.state.value
        commands.answer = { committed(it.operationId) }
        editor.submit()

        assertEquals(SaveError.DataChanged, refused.saveError)
        assertNull(refused.result)
        assertEquals(listOf(1L, 2L), commands.submitted.map { it.generation })
    }

    @Test
    fun aStorageFailureSaysNothingWasSavedAndKeepsTheDraft() {
        commands.answer = {
            rejected(CategoryRejection.StorageFailed(StorageFailure.Unavailable("SQLiteException")))
        }
        val editor = editor()
        editor.type("Mercado")
        editor.limit("300000")

        editor.submit()

        val state = editor.state.value
        assertEquals(SaveError.NotSaved, state.saveError)
        assertEquals("Mercado" to "300000", state.name to state.limitText)
        assertTrue(state.editable && state.canSubmit)
        assertNull(state.result)
    }

    @Test
    fun anUnknownOutcomeIsSettledByAskingAboutTheSameOperationNotBySendingAgain() {
        commands.answer = { CategoryCommandOutcome.OutcomeUnknown(it.operationId) }
        commands.found = { Outcome.Success(change(it)) }
        val editor = editor()
        editor.type("Mercado")

        editor.submit()

        assertEquals(1, commands.submitted.size)
        assertEquals(listOf(uuid(500)), commands.looked.map { it.value })
        assertEquals(EditorResult.Created(uuid(900)), editor.state.value.result)
    }

    @Test
    fun anUnknownOutcomeThatTurnsOutNotSavedReturnsToTheFormWithNothingCreated() {
        commands.answer = { CategoryCommandOutcome.OutcomeUnknown(it.operationId) }
        val editor = editor()
        editor.type("Mercado")

        editor.submit()

        val state = editor.state.value
        assertNull(state.result)
        assertNull(state.saveError)
        assertTrue(state.editable && state.canSubmit)
    }

    @Test
    fun whileTheOutcomeCannotBeReadTheFormStaysLockedAndRetryOnlyAsksAgain() {
        commands.answer = { CategoryCommandOutcome.OutcomeUnknown(it.operationId) }
        commands.found = { Outcome.Failure(StorageFailure.Unavailable("SQLiteException")) }
        val editor = editor()
        editor.type("Mercado")

        editor.submit()
        val unknown = editor.state.value
        editor.type("Otro")
        editor.submit()
        commands.found = { Outcome.Success(change(it)) }
        editor.onAction(CategoryEditorAction.Retry)

        assertEquals(SaveError.Unknown, unknown.saveError)
        assertFalse(unknown.editable)
        assertNull(unknown.result)
        // The draft did not change, nothing was sent twice, and the same operation was asked about.
        assertEquals(1, commands.submitted.size)
        assertEquals(listOf(uuid(500), uuid(500)), commands.looked.map { it.value })
        assertEquals(EditorResult.Created(uuid(900)), editor.state.value.result)
    }

    @Test
    fun theDraftComesBackAfterTheProcessIsKilled() {
        val saved = SavedStateHandle(mapOf(CategoryEditorViewModel.KEY_FOR_ENTRY to true))
        val first = editor(saved)
        first.type("Mercado de pla")
        first.onAction(CategoryEditorAction.IconPicked("pets"))
        first.limit("12.34")

        val restored = editor(saved.afterProcessDeath()).state.value

        assertEquals("Mercado de pla", restored.name)
        assertEquals("pets", restored.iconKey)
        assertEquals(false to "12.34", restored.unlimited to restored.limitText)
        assertTrue(restored.forEntry)
        assertEquals(emptyList<CreateCategory>(), commands.submitted)
    }

    @Test
    fun aSaveInterruptedByTheProcessDyingIsRecoveredFromItsReceiptWithoutSendingAgain() {
        commands.hold = CompletableDeferred()
        val saved = SavedStateHandle()
        val first = editor(saved)
        first.type("Mercado")
        first.submit()
        // The process dies here: the command left, its answer never arrived.
        val afterDeath = saved.afterProcessDeath()
        commands.found = { Outcome.Success(change(it)) }

        val restored = editor(afterDeath)

        assertEquals(1, commands.submitted.size)
        assertEquals(listOf(uuid(500)), commands.looked.map { it.value })
        assertEquals(EditorResult.Created(uuid(900)), restored.state.value.result)
    }

    @Test
    fun anInterruptedSaveThatNeverCommittedLeavesTheDraftReadyToSend() {
        commands.hold = CompletableDeferred()
        val saved = SavedStateHandle()
        val first = editor(saved)
        first.type("Mercado")
        first.submit()
        val afterDeath = saved.afterProcessDeath()
        commands.hold = null

        val restored = editor(afterDeath)
        val waiting = restored.state.value
        restored.submit()

        assertNull(waiting.result)
        assertEquals("Mercado", waiting.name)
        assertTrue(waiting.canSubmit)
        // The second attempt is a new operation; the first is known not to have committed.
        assertEquals(listOf(uuid(500), uuid(501)), commands.submitted.map { it.operationId.value })
    }

    @Test
    fun aCreatedResultSurvivesTheScreenBeingRebuilt() {
        val saved = SavedStateHandle()
        val first = editor(saved)
        first.type("Mercado")
        first.submit()

        val restored = editor(saved.afterProcessDeath())

        assertEquals(EditorResult.Created(uuid(900)), restored.state.value.result)
        assertEquals(1, commands.submitted.size)
    }

    @Test
    fun backLeavesAnUntouchedFormAndAsksBeforeDiscardingADraft() {
        val untouched = editor()
        untouched.onAction(CategoryEditorAction.Back)

        val drafted = editor()
        drafted.type("Mer")
        drafted.onAction(CategoryEditorAction.Back)
        val asked = drafted.state.value
        drafted.onAction(CategoryEditorAction.KeepEditing)
        val kept = drafted.state.value
        drafted.onAction(CategoryEditorAction.Back)
        drafted.onAction(CategoryEditorAction.DiscardConfirmed)

        assertEquals(EditorResult.Discarded, untouched.state.value.result)
        assertTrue(asked.confirmDiscard)
        assertNull(asked.result)
        assertEquals(false to "Mer", kept.confirmDiscard to kept.name)
        assertEquals(EditorResult.Discarded, drafted.state.value.result)
        assertEquals(emptyList<CreateCategory>(), commands.submitted)
    }

    @Test
    fun changingOnlyTheIconOrTheLimitAlsoCountsAsADraft() {
        val icon = editor()
        icon.onAction(CategoryEditorAction.IconPicked("pets"))
        icon.onAction(CategoryEditorAction.Back)
        val limit = editor()
        limit.onAction(CategoryEditorAction.SuggestionPicked(200_000))
        limit.onAction(CategoryEditorAction.Back)

        assertTrue(icon.state.value.confirmDiscard)
        assertTrue(limit.state.value.confirmDiscard)
    }

    @Test
    fun aFormThatCannotBePreparedSaysSoAndCanBeRetried() {
        reads.failure = StorageFailure.Unavailable("SQLiteException")
        val editor = editor()
        val failed = editor.state.value
        editor.type("Mercado")
        editor.submit()
        reads.failure = null

        editor.onAction(CategoryEditorAction.Retry)
        editor.submit()

        assertTrue(failed.loadFailed && !failed.canSubmit)
        assertEquals(1, commands.submitted.size)
        assertEquals("Mercado", commands.submitted.single().name.value)
    }

    @Test
    fun anIconThatIsNotOfferedCannotBeChosen() {
        val editor = editor()
        val before = editor.state.value.iconKey

        editor.onAction(CategoryEditorAction.IconPicked("skull"))

        assertEquals(before, editor.state.value.iconKey)
    }
}
