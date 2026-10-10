package com.software.sello.feature.expense

import androidx.lifecycle.SavedStateHandle
import com.software.sello.designsystem.component.MoneySign
import com.software.sello.designsystem.component.MoneyTextValue
import com.software.sello.domain.model.Currency
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.SnapshotFailure
import com.software.sello.domain.model.StorageFailure
import com.software.sello.domain.port.CreateExpense
import com.software.sello.domain.port.ExpenseCommandOutcome
import com.software.sello.domain.port.ExpenseRejection
import com.software.sello.domain.port.SnapshotState
import com.software.sello.navigation.HandFinancialClock
import com.software.sello.presentation.money.MoneyFormatter
import com.software.sello.presentation.money.MoneyLabels
import java.time.LocalDate
import java.time.YearMonth
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
class ExpenseFormViewModelTest {
    private val october = YearMonth.of(2026, 10)
    private val commands = HandExpenseCommands()
    private val reads = HandExpenseReads()
    private val snapshots = HandSnapshots()
    private val clock = HandFinancialClock("2026-10-22")
    private var nextId = 500
    private val money = MoneyFormatter(
        object : MoneyLabels {
            override fun amount(currency: Currency, digits: String, isOne: Boolean) =
                "$digits pesos"

            override fun negative(amount: String) = "menos $amount"

            override fun positive(amount: String) = "más $amount"
        }
    )

    /** The reference's month: 937.200 left overall, 287.600 left in Alimentación. */
    private val food = categoryIn(1, "Alimentación", 900_000, icon = "restaurant")
    private val coffee = categoryIn(2, "Café", 50_000, icon = "local_cafe")
    private val transport = categoryIn(3, "Transporte", null)
    private val bills = categoryIn(4, "Servicios", 650_000)
    private val archived = categoryIn(5, "Vieja", 100_000, archived = true)
    private val spending = listOf(
        storedExpense(100, 1, "2026-10-05", 612_400),
        storedExpense(101, 2, "2026-10-06", 41_200),
        storedExpense(102, 3, "2026-10-07", 86_000),
        storedExpense(103, 4, "2026-10-08", 400)
    )

    @Before
    fun mainDispatcher() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun resetDispatcher() = Dispatchers.resetMain()

    private fun shown(digits: String) = MoneyTextValue(MoneySign.None, "", digits, "$digits pesos")

    private fun form(saved: SavedStateHandle = SavedStateHandle()) =
        ExpenseEntryViewModel(saved, commands, reads, snapshots, clock, { uuid(nextId++) }, money)

    /** A form over a month with the given categories, already read. */
    private fun ready(
        vararg categories: com.software.sello.domain.policy.CategoryInMonth = arrayOf(food, coffee),
        saved: SavedStateHandle = SavedStateHandle()
    ): ExpenseEntryViewModel {
        snapshots.ready(monthOf("2026-10-22", categories.toList()))
        return form(saved)
    }

    private fun SavedStateHandle.afterProcessDeath() =
        SavedStateHandle(keys().associateWith { get<Any?>(it) })

    private fun ExpenseEntryViewModel.type(digits: String) =
        digits.forEach { onAction(ExpenseEntryAction.Digit(it.digitToInt())) }

    private fun ExpenseEntryViewModel.pick(number: Int) =
        onAction(ExpenseEntryAction.CategoryPicked(uuid(number)))

    private fun ExpenseEntryViewModel.submit() = onAction(ExpenseEntryAction.Submit)

    private val ExpenseEntryViewModel.now get() = state.value

    // --- Opening ---

    @Test
    fun itLoadsThenOffersOnlyCategoriesThatCanTakeAnExpense() {
        val form = form()
        val loading = form.now

        snapshots.ready(monthOf("2026-10-22", listOf(food, archived, coffee)))

        assertTrue(loading.loading && !loading.canSubmit)
        assertEquals(listOf(october), snapshots.observed)
        assertEquals(
            listOf("Alimentación" to "restaurant", "Café" to "local_cafe"),
            form.now.categories.map { it.name to it.iconKey }
        )
        assertEquals(LocalDate.of(2026, 10, 22), form.now.date)
        assertEquals(Missing.Amount, form.now.missing)
        assertNull(form.now.amount)
        assertNull(form.now.receipt)
    }

    @Test
    fun whenTheCategoriesCannotBeReadItSaysSoAndCanBeRetried() {
        val form = form()
        val failure = SnapshotFailure.Storage(StorageFailure.Unavailable("SQLiteException"))
        snapshots.of(october).value = SnapshotState.Failed(failure, lastGood = null)
        val failed = form.now

        form.onAction(ExpenseEntryAction.Retry)
        snapshots.ready(monthOf("2026-10-22", listOf(food)))

        assertTrue(failed.loadFailed && !failed.canSubmit)
        assertEquals(listOf(october, october), snapshots.observed)
        assertFalse(form.now.loadFailed)
    }

    // --- Which category starts selected ---

    @Test
    fun theOnlyCategoryIsChosenAndWithSeveralNoneIsUntilOneWasUsed() {
        val single = ready(food)
        val several = ready(food, coffee)
        reads.lastUsed = categoryId(2)
        val withHistory = ready(food, coffee)

        assertEquals(uuid(1), single.now.categoryId)
        assertNull(several.now.categoryId)
        assertEquals(uuid(2), withHistory.now.categoryId)
    }

    @Test
    fun aCategoryAskedForByTheCallerIsChosenOnlyIfItCanTakeAnExpense() {
        reads.lastUsed = categoryId(1)
        fun asking(id: String) = ready(
            food,
            coffee,
            archived,
            saved = SavedStateHandle(mapOf(ExpenseEntryViewModel.KEY_PREFILL_CATEGORY to id))
        ).now.categoryId

        assertEquals(uuid(2), asking(uuid(2)))
        // Archived, unknown or malformed: nothing is selected, not even the last used.
        assertNull(asking(uuid(5)))
        assertNull(asking(uuid(77)))
        assertNull(asking("' OR 1=1 --"))
    }

    @Test
    fun aLastUsedCategoryThatWasArchivedIsNotChosen() {
        reads.lastUsed = categoryId(5)

        assertNull(ready(food, coffee, archived).now.categoryId)
    }

    @Test
    fun aValidPrefilledAmountIsShownAndAnInvalidOneIsSimplyAbsent() {
        fun asking(amount: String) = ready(
            saved = SavedStateHandle(mapOf(ExpenseEntryViewModel.KEY_PREFILL_AMOUNT to amount))
        ).now

        assertEquals(shown("48.700"), asking("48700").amount)
        for (bad in listOf("1e3", "0", "-5", "1000000000000", "12.34")) {
            val state = asking(bad)
            assertNull(bad, state.amount)
            assertNull(bad, state.amountText)
            assertNull(bad, state.amountProblem)
        }
    }

    // --- The amount ---

    @Test
    fun theKeypadBuildsTheAmountDigitByDigit() {
        val form = ready()

        form.type("48")
        form.onAction(ExpenseEntryAction.TripleZero)
        val typed = form.now.amount
        form.onAction(ExpenseEntryAction.Backspace)
        val shorter = form.now.amount
        form.onAction(ExpenseEntryAction.ClearAmount)

        assertEquals(shown("48.000"), typed)
        assertEquals(shown("4.800"), shorter)
        assertNull(form.now.amount)
        assertEquals(Missing.Amount, form.now.missing)
    }

    @Test
    fun aThirteenthDigitIsRefusedAndSaidSoWithoutChangingTheAmount() {
        val form = ready()
        form.type("999999999999")
        val full = form.now

        form.type("9")
        form.onAction(ExpenseEntryAction.TripleZero)

        assertEquals(shown("999.999.999.999"), full.amount)
        assertEquals(0, full.limitPulse)
        assertEquals(shown("999.999.999.999"), form.now.amount)
        assertEquals(2, form.now.limitPulse)
    }

    @Test
    fun pastedTextIsUsedWholeOrKeptAsItWasWithTheReason() {
        val form = ready()
        form.onAction(ExpenseEntryAction.Paste("48.700"))
        val good = form.now.amount
        val cases = listOf(
            "1e3" to AmountProblem.NotAnAmount,
            "12.34" to AmountProblem.NotAnAmount,
            "$48.700" to AmountProblem.NotAnAmount,
            "0" to AmountProblem.Zero,
            "1000000000000" to AmountProblem.TooLarge
        )

        assertEquals(shown("48.700"), good)
        for ((text, problem) in cases) {
            form.onAction(ExpenseEntryAction.Paste(text))

            assertNull(text, form.now.amount)
            assertEquals(text, text, form.now.amountText)
            assertEquals(text, problem, form.now.amountProblem)
            assertEquals(text, Missing.Amount, form.now.missing)
        }
        // Typing after a refused paste starts again; it never builds on the refused text.
        form.type("7")
        assertEquals(shown("7"), form.now.amount)
        assertNull(form.now.amountText)
    }

    // --- What is missing ---

    @Test
    fun theButtonStaysOffAndSaysWhatIsMissingInOrder() {
        val form = ready()
        val nothing = form.now
        form.type("5000")
        val noCategory = form.now
        form.pick(1)
        val complete = form.now
        form.onAction(ExpenseEntryAction.NoteChanged("a".repeat(61)))
        val longNote = form.now
        form.submit()

        assertEquals(Missing.Amount to false, nothing.missing to nothing.canSubmit)
        assertEquals(Missing.Category to false, noCategory.missing to noCategory.canSubmit)
        assertEquals(null to true, complete.missing to complete.canSubmit)
        assertEquals(Missing.Note to NoteProblem.TooLong, longNote.missing to longNote.noteProblem)
        assertEquals("a".repeat(61), longNote.note)
        assertEquals(emptyList<CreateExpense>(), commands.created)
    }

    @Test
    fun aCategoryThatIsNotOfferedCannotBePicked() {
        val form = ready(food, coffee, archived)

        form.pick(5)
        form.pick(77)

        assertNull(form.now.categoryId)
    }

    // --- The preview ---

    @Test
    fun thePreviewShowsWhatWouldBeLeftInTheMonthAndInTheCategory() {
        // Limits 900.000 and 650.000, spent 612.400 and 400: 937.200 left in the month
        // and 287.600 in Alimentación, as on the reference's screen.
        snapshots.ready(
            monthOf("2026-10-22", listOf(food, bills), listOf(spending[0], spending[3]))
        )
        val form = form()
        form.type("48700")
        val monthOnly = form.now.preview
        form.pick(1)

        assertEquals(
            EntryPreview(PreviewLine(PreviewLine.Kind.Remaining, shown("888.500")), null),
            monthOnly
        )
        assertEquals(
            EntryPreview(
                PreviewLine(PreviewLine.Kind.Remaining, shown("888.500")),
                PreviewLine(PreviewLine.Kind.Remaining, shown("238.900"))
            ),
            form.now.preview
        )
    }

    @Test
    fun goingOverALimitIsShownAsHowFarOverAndNothingBlocksSaving() {
        snapshots.ready(monthOf("2026-10-22", listOf(food, coffee), spending.take(2)))
        val form = form()
        form.pick(2)

        // Café has 8.800 left; 21.100 is 12.300 over.
        form.type("21100")

        assertEquals(
            PreviewLine(PreviewLine.Kind.Over, shown("12.300")),
            form.now.preview?.category
        )
        assertTrue(form.now.canSubmit)
    }

    @Test
    fun aCategoryWithoutALimitPreviewsWhatWouldHaveBeenSpent() {
        snapshots.ready(monthOf("2026-10-22", listOf(food, transport), spending.take(3).drop(2)))
        val form = form()
        form.pick(3)

        form.type("14000")

        assertEquals(
            PreviewLine(PreviewLine.Kind.Spent, shown("100.000")),
            form.now.preview?.category
        )
    }

    @Test
    fun thereIsNoPreviewWithoutAValidAmount() {
        val form = ready(food)

        form.onAction(ExpenseEntryAction.Paste("1e3"))

        assertNull(form.now.preview)
    }

    @Test
    fun aDateInAnotherMonthPreviewsThatMonth() {
        val september = YearMonth.of(2026, 9)
        snapshots.ready(monthOf("2026-10-22", listOf(food)))
        snapshots.ready(
            monthOf(
                "2026-10-22",
                listOf(categoryIn(1, "Alimentación", 500_000)),
                listOf(storedExpense(100, 1, "2026-09-10", 100_000)),
                month = september
            )
        )
        val form = form()
        form.type("50000")

        form.onAction(ExpenseEntryAction.DatePicked(LocalDate.of(2026, 9, 30)))

        assertEquals(listOf(october, september), snapshots.observed)
        assertEquals(
            PreviewLine(PreviewLine.Kind.Remaining, shown("350.000")),
            form.now.preview?.month
        )
    }

    // --- The date ---

    @Test
    fun aDayAfterTodayCannotBeChosenAndAnEarlierOneCan() {
        val form = ready(food)
        form.onAction(ExpenseEntryAction.OpenDatePicker)
        val open = form.now.datePickerOpen

        form.onAction(ExpenseEntryAction.DatePicked(LocalDate.of(2026, 10, 23)))
        val afterFuture = form.now
        form.onAction(ExpenseEntryAction.OpenDatePicker)
        form.onAction(ExpenseEntryAction.DatePicked(LocalDate.of(2024, 2, 29)))

        assertTrue(open)
        assertEquals(
            LocalDate.of(2026, 10, 22) to false,
            afterFuture.date to afterFuture.datePickerOpen
        )
        assertEquals(LocalDate.of(2024, 2, 29), form.now.date)
        assertEquals(LocalDate.of(2026, 10, 22), form.now.today)
    }

    @Test
    fun aDraftKeepsTheDayItWasStartedWhenTheDayChanges() {
        val form = ready(food)

        clock.goTo("2026-10-23")

        assertEquals(LocalDate.of(2026, 10, 22), form.now.date)
        assertEquals(LocalDate.of(2026, 10, 23), form.now.today)
    }

    // --- Saving ---

    @Test
    fun aCompleteDraftSendsOneCommandWithExactlyWhatWasEntered() {
        snapshots.ready(monthOf("2026-10-22", listOf(food, coffee), generation = 4))
        val form = form()
        form.type("48700")
        form.pick(2)
        form.onAction(ExpenseEntryAction.DatePicked(LocalDate.of(2026, 10, 20)))
        form.onAction(ExpenseEntryAction.NoteChanged("  Pan y café "))

        form.submit()

        val command = commands.created.single()
        assertEquals(uuid(500), command.operationId.value)
        assertEquals(4L, command.generation)
        assertEquals(categoryId(2), command.categoryId)
        assertEquals(pesos(48_700), command.amount)
        assertEquals(LocalDate.of(2026, 10, 20), command.date)
        assertEquals("Pan y café", command.note?.value)
    }

    @Test
    fun anEmptyNoteIsNoNote() {
        val form = ready(food)
        form.type("5000")
        form.onAction(ExpenseEntryAction.NoteChanged("   "))

        form.submit()

        assertNull(commands.created.single().note)
    }

    @Test
    fun theReceiptShowsWhatStorageHoldsOnlyAfterTheSaveIsConfirmed() {
        commands.hold = CompletableDeferred()
        // Storage is the authority: what it returns is shown, whatever the draft said.
        reads.stored[expenseId(900)] = storedExpense(900, 1, "2026-10-21", 10_000, "Pan")
        val form = ready(food)
        form.type("10000")

        form.submit()
        val saving = form.now
        commands.hold!!.complete(Unit)

        assertTrue(saving.busy)
        assertNull(saving.receipt)
        assertEquals(
            ExpenseReceiptView(
                uuid(900),
                ExpenseReceiptView.Details(
                    amount = shown("10.000"),
                    categoryName = "Alimentación",
                    iconKey = "restaurant",
                    date = LocalDate.of(2026, 10, 21),
                    note = "Pan"
                )
            ),
            form.now.receipt
        )
        assertFalse(form.now.busy)
    }

    @Test
    fun whileSavingASecondTapAndAnyEditAreIgnored() {
        commands.hold = CompletableDeferred()
        val form = ready(food, coffee)
        form.type("10000")
        form.pick(1)

        form.submit()
        form.submit()
        form.type("9")
        form.pick(2)
        form.onAction(ExpenseEntryAction.NoteChanged("otra"))
        form.onAction(ExpenseEntryAction.Back)

        assertEquals(1, commands.created.size)
        assertEquals(shown("10.000"), form.now.amount)
        assertEquals(uuid(1) to "", form.now.categoryId to form.now.note)
        assertFalse(form.now.confirmDiscard || form.now.closed)
        assertFalse(form.now.canSubmit || form.now.editable)
    }

    @Test
    fun aSavedExpenseThatCannotBeReadBackIsStillShownAsSaved() {
        reads.failure = StorageFailure.Unavailable("SQLiteException")
        val form = ready(food)
        form.type("10000")

        form.submit()

        assertEquals(ExpenseReceiptView(uuid(900), details = null), form.now.receipt)
    }

    // --- Rejections: certain, nothing saved, the draft stays ---

    @Test
    fun aCategoryThatStoppedAcceptingExpensesIsDeselectedAndTheNextTryIsANewOperation() {
        commands.answer = { ExpenseCommandOutcome.Rejected(ExpenseRejection.CategoryArchived) }
        val form = ready(food, coffee)
        form.type("10000")
        form.pick(1)

        form.submit()
        val refused = form.now
        commands.answer =
            { ExpenseCommandOutcome.Committed(commands.committed(it.operationId), false) }
        form.pick(2)
        form.submit()

        assertEquals(EntryError.CategoryGone, refused.error)
        assertNull(refused.categoryId)
        assertEquals(shown("10.000"), refused.amount)
        assertNull(refused.receipt)
        assertEquals(listOf(uuid(500), uuid(501)), commands.created.map { it.operationId.value })
    }

    @Test
    fun everyOtherRejectionKeepsTheDraftAndSaysNothingWasSaved() {
        val cases = listOf(
            ExpenseRejection.CategoryMissing to EntryError.CategoryGone,
            ExpenseRejection.FutureDate(LocalDate.of(2026, 10, 21)) to EntryError.FutureDate,
            ExpenseRejection.StaleGeneration(current = 2) to EntryError.DataChanged,
            ExpenseRejection.OperationConflict to EntryError.NotSaved,
            ExpenseRejection.StorageFailed(StorageFailure.Unavailable("SQLiteException")) to
                EntryError.NotSaved
        )

        for ((reason, error) in cases) {
            commands.answer = { ExpenseCommandOutcome.Rejected(reason) }
            val form = ready(food)
            form.type("10000")
            form.onAction(ExpenseEntryAction.NoteChanged("Pan"))

            form.submit()

            assertEquals("$reason", error, form.now.error)
            assertEquals("$reason", shown("10.000") to "Pan", form.now.amount to form.now.note)
            assertNull("$reason", form.now.receipt)
            assertFalse("$reason", form.now.busy)
        }
    }

    @Test
    fun afterTheDataWasReplacedTheNextAttemptUsesTheNewGeneration() {
        commands.answer = { ExpenseCommandOutcome.Rejected(ExpenseRejection.StaleGeneration(2)) }
        val form = ready(food)
        form.type("10000")
        form.submit()

        snapshots.ready(monthOf("2026-10-22", listOf(food), generation = 2))
        commands.answer =
            { ExpenseCommandOutcome.Committed(commands.committed(it.operationId), false) }
        form.submit()

        assertEquals(listOf(1L, 2L), commands.created.map { it.generation })
    }

    // --- An outcome that is not known ---

    @Test
    fun anUnknownOutcomeIsSettledByAskingAboutTheSameOperationNeverBySendingAgain() {
        commands.answer = { ExpenseCommandOutcome.OutcomeUnknown(it.operationId) }
        commands.found = { Outcome.Success(commands.committed(it)) }
        val form = ready(food)
        form.type("10000")

        form.submit()

        assertEquals(1, commands.created.size)
        assertEquals(listOf(uuid(500)), commands.looked.map { it.value })
        assertEquals(uuid(900), form.now.receipt?.expenseId)
    }

    @Test
    fun anUnknownOutcomeThatWasNotSavedReturnsToTheDraftWithNothingRecorded() {
        commands.answer = { ExpenseCommandOutcome.OutcomeUnknown(it.operationId) }
        val form = ready(food)
        form.type("10000")

        form.submit()

        assertNull(form.now.receipt)
        assertNull(form.now.error)
        assertTrue(form.now.editable && form.now.canSubmit)
        assertEquals(shown("10.000"), form.now.amount)
    }

    @Test
    fun whileTheOutcomeCannotBeReadTheDraftIsLockedAndRetryOnlyAsksAgain() {
        commands.answer = { ExpenseCommandOutcome.OutcomeUnknown(it.operationId) }
        commands.found = { Outcome.Failure(StorageFailure.Unavailable("SQLiteException")) }
        val form = ready(food)
        form.type("10000")

        form.submit()
        val unknown = form.now
        form.type("9")
        form.submit()
        commands.found = { Outcome.Success(commands.committed(it)) }
        form.onAction(ExpenseEntryAction.Retry)

        assertEquals(EntryError.Unknown, unknown.error)
        assertFalse(unknown.editable || unknown.canSubmit)
        assertNull(unknown.receipt)
        assertEquals(1, commands.created.size)
        assertEquals(listOf(uuid(500), uuid(500)), commands.looked.map { it.value })
        assertEquals(uuid(900), form.now.receipt?.expenseId)
    }

    // --- Rotation and the process being killed ---

    @Test
    fun theDraftComesBackAfterTheProcessIsKilled() {
        val saved = SavedStateHandle()
        val first = ready(food, coffee, saved = saved)
        first.type("48700")
        first.pick(2)
        first.onAction(ExpenseEntryAction.DatePicked(LocalDate.of(2026, 10, 20)))
        first.onAction(ExpenseEntryAction.NoteChanged("Pan"))

        clock.goTo("2026-10-25")
        val restored = form(saved.afterProcessDeath()).now

        assertEquals(shown("48.700"), restored.amount)
        assertEquals(uuid(2), restored.categoryId)
        assertEquals(LocalDate.of(2026, 10, 20), restored.date)
        assertEquals("Pan", restored.note)
        assertEquals(emptyList<CreateExpense>(), commands.created)
    }

    @Test
    fun aDraftStartedTodayIsStillDatedThatDayWhenItComesBackTomorrow() {
        val saved = SavedStateHandle()
        ready(food, saved = saved).type("5000")

        clock.goTo("2026-10-23")
        val restored = form(saved.afterProcessDeath()).now

        assertEquals(LocalDate.of(2026, 10, 22), restored.date)
        assertEquals(LocalDate.of(2026, 10, 23), restored.today)
    }

    @Test
    fun aRejectedAttemptLeavesNothingToRecoverAfterARestart() {
        commands.answer = { ExpenseCommandOutcome.Rejected(ExpenseRejection.CategoryArchived) }
        val saved = SavedStateHandle()
        val first = ready(food, saved = saved)
        first.type("10000")
        first.submit()

        val restored = form(saved.afterProcessDeath())

        // The rejection was certain, so there is nothing to ask storage about.
        assertEquals(emptyList<Any>(), commands.looked)
        assertNull(restored.now.receipt)
        assertEquals(shown("10.000"), restored.now.amount)
    }

    @Test
    fun aRefusedPasteAlsoComesBackAsItWas() {
        val saved = SavedStateHandle()
        ready(food, saved = saved).onAction(ExpenseEntryAction.Paste("1e3"))

        val restored = form(saved.afterProcessDeath()).now

        assertEquals(
            "1e3" to AmountProblem.NotAnAmount,
            restored.amountText to restored.amountProblem
        )
        assertNull(restored.amount)
    }

    @Test
    fun aSaveInterruptedByTheProcessDyingIsRecoveredFromItsReceiptWithoutSendingAgain() {
        commands.hold = CompletableDeferred()
        reads.stored[expenseId(900)] = storedExpense(900, 1, "2026-10-22", 10_000)
        val saved = SavedStateHandle()
        val first = ready(food, saved = saved)
        first.type("10000")
        first.submit()
        // The process dies here: the command left, its answer never arrived.
        val afterDeath = saved.afterProcessDeath()
        commands.found = { Outcome.Success(commands.committed(it)) }

        val restored = form(afterDeath)

        assertEquals(1, commands.created.size)
        assertEquals(listOf(uuid(500)), commands.looked.map { it.value })
        assertEquals(shown("10.000"), restored.now.receipt?.details?.amount)
    }

    @Test
    fun anInterruptedSaveThatNeverCommittedLeavesTheDraftAndTheNextTryIsANewOperation() {
        commands.hold = CompletableDeferred()
        val saved = SavedStateHandle()
        val first = ready(food, saved = saved)
        first.type("10000")
        first.submit()
        val afterDeath = saved.afterProcessDeath()
        commands.hold = null

        val restored = form(afterDeath)
        val waiting = restored.now
        restored.submit()

        assertNull(waiting.receipt)
        assertEquals(shown("10.000"), waiting.amount)
        assertTrue(waiting.canSubmit)
        assertEquals(listOf(uuid(500), uuid(501)), commands.created.map { it.operationId.value })
    }

    @Test
    fun aReceiptShownAgainAfterARestartSendsNothingAndIsReadFromStorage() {
        reads.stored[expenseId(900)] = storedExpense(900, 1, "2026-10-22", 10_000)
        val saved = SavedStateHandle()
        val first = ready(food, saved = saved)
        first.type("10000")
        first.submit()
        val readsBefore = reads.reads

        val restored = form(saved.afterProcessDeath())
        restored.submit()
        restored.type("5")

        assertEquals(first.now.receipt, restored.now.receipt)
        assertEquals(1, commands.created.size)
        assertEquals(emptyList<Any>(), commands.looked)
        assertEquals(readsBefore + 1, reads.reads)
    }

    // --- Leaving ---

    @Test
    fun backLeavesAnUntouchedFormAsksAboutATypedOneAndLeavesAReceiptAtOnce() {
        val untouched = ready(food, coffee)
        untouched.pick(1)
        untouched.onAction(ExpenseEntryAction.Back)

        val typed = ready(food)
        typed.type("5")
        typed.onAction(ExpenseEntryAction.Back)
        val asked = typed.now
        typed.onAction(ExpenseEntryAction.KeepEditing)
        val kept = typed.now
        typed.onAction(ExpenseEntryAction.Back)
        typed.onAction(ExpenseEntryAction.DiscardConfirmed)

        val saved = ready(food)
        saved.type("10000")
        saved.submit()
        saved.onAction(ExpenseEntryAction.Back)

        assertTrue(untouched.now.closed)
        assertTrue(asked.confirmDiscard && !asked.closed)
        assertEquals(false to shown("5"), kept.confirmDiscard to kept.amount)
        assertTrue(typed.now.closed)
        assertTrue(saved.now.closed && !saved.now.confirmDiscard)
        assertEquals(1, commands.created.size)
    }

    @Test
    fun aNoteAloneAlsoCountsAsSomethingTyped() {
        val form = ready(food)
        form.onAction(ExpenseEntryAction.NoteChanged("Pan"))

        form.onAction(ExpenseEntryAction.Back)

        assertTrue(form.now.confirmDiscard)
    }

    @Test
    fun listoLeavesOnlyOnceThereIsAReceipt() {
        val form = ready(food)
        form.onAction(ExpenseEntryAction.Done)
        val before = form.now.closed
        form.type("10000")
        form.submit()

        form.onAction(ExpenseEntryAction.Done)

        assertFalse(before)
        assertTrue(form.now.closed)
    }
}
