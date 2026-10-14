package com.software.sello.feature.income

import androidx.lifecycle.SavedStateHandle
import com.software.sello.designsystem.component.MoneySign
import com.software.sello.designsystem.component.MoneyTextValue
import com.software.sello.domain.model.Currency
import com.software.sello.domain.model.Income
import com.software.sello.domain.model.IncomeSource
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.ResumenSnapshot
import com.software.sello.domain.model.SnapshotFailure
import com.software.sello.domain.model.SourceName
import com.software.sello.domain.model.StorageFailure
import com.software.sello.domain.port.IncomeCommandOutcome
import com.software.sello.domain.port.IncomeCommandOutcome.Rejected
import com.software.sello.domain.port.IncomeMonthState
import com.software.sello.domain.port.IncomeRejection
import com.software.sello.domain.port.ResumenSnapshotState
import com.software.sello.domain.port.ResumenSnapshots
import com.software.sello.feature.expense.HandExpenseCommands
import com.software.sello.feature.expense.HandIncomeCommands
import com.software.sello.feature.expense.HandIncomeReads
import com.software.sello.feature.expense.incomeId
import com.software.sello.feature.expense.storedIncome
import com.software.sello.feature.expense.uuid
import com.software.sello.feature.resumen.resumenSnapshot
import com.software.sello.navigation.HandFinancialClock
import com.software.sello.presentation.corrections.CorrectionNotice
import com.software.sello.presentation.corrections.ExpenseCorrections
import com.software.sello.presentation.money.MoneyFormatter
import com.software.sello.presentation.money.MoneyLabels
import java.time.LocalDate
import java.time.YearMonth
import kotlin.time.Duration
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
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

/**
 * The income form over scripted commands. Today is 22 October 2026 and the generation
 * is 7. For corrections, storage holds income 100: 850.000 from "Otro: Diseño" on 5
 * October with the note "Logo", at version 3.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class IncomeFormViewModelTest {
    private val october = YearMonth.of(2026, 10)
    private val commands = HandIncomeCommands()
    private val reads = HandIncomeReads()
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
    private lateinit var corrections: ExpenseCorrections

    private val design = IncomeSource.Other((SourceName.of("Diseño") as Outcome.Success).value)
    private val logo = storedIncome(100, "2026-10-05", 850_000, design, "Logo").copy(version = 3)

    @Before
    fun mainDispatcher() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        corrections = ExpenseCorrections(
            HandExpenseCommands(),
            commands,
            { Duration.ZERO },
            { uuid(nextId++) },
            CoroutineScope(Dispatchers.Main)
        )
    }

    @After
    fun resetDispatcher() = Dispatchers.resetMain()

    private fun shown(digits: String) = MoneyTextValue(MoneySign.None, "", digits, "$digits pesos")

    private fun form(saved: SavedStateHandle = SavedStateHandle()) =
        IncomeFormViewModel(saved, commands, reads, snapshots, clock, {
            uuid(nextId++)
        }, money, corrections)

    private val previewOverride = MutableStateFlow<ResumenSnapshotState?>(null)
    private val previewRevision = MutableStateFlow<Long?>(null)
    private val snapshots = object : ResumenSnapshots {
        override suspend fun read(month: YearMonth): Outcome<ResumenSnapshot, SnapshotFailure> =
            error("The form observes")

        override fun observe(month: YearMonth) = combine(
            reads.observeMonth(month),
            clock.today,
            previewOverride,
            previewRevision
        ) { state, today, override, revision ->
            when {
                override != null -> override

                state is IncomeMonthState.Loading -> ResumenSnapshotState.Loading

                state is IncomeMonthState.Failed -> ResumenSnapshotState.Failed(
                    SnapshotFailure.Storage(state.failure),
                    null
                )

                state is IncomeMonthState.Ready -> {
                    val original = resumenSnapshot(income = state.month.total.minorUnits)
                    ResumenSnapshotState.Ready(
                        original.copy(
                            budget = original.budget.copy(
                                month = month,
                                asOf = today.date,
                                generation = state.month.generation,
                                revision = revision ?: state.month.revision
                            )
                        )
                    )
                }

                else -> error("Unknown income state")
            }
        }
    }

    /** A new-income form with the month already read. */
    private fun ready(saved: SavedStateHandle = SavedStateHandle()): IncomeFormViewModel {
        reads.publish(october, generation = 7)
        return form(saved)
    }

    private fun editing(income: Income = logo) =
        SavedStateHandle(mapOf(IncomeFormViewModel.KEY_EDITED_INCOME to income.id.value))

    @Test
    fun newIncomePreviewIsExactAndCorrectingDoesNotCountTheIncomeTwice() {
        reads.stored[logo.id] = logo
        val model = ready()
        model.onAction(IncomeFormAction.Paste("150000"))
        assertEquals(shown("1.000.000"), model.state.value.monthPreview)
        assertEquals(1, reads.stored.size)
        val edit = ready(editing())
        edit.onAction(IncomeFormAction.Paste("150000"))
        assertEquals(null, edit.state.value.monthPreview)
    }

    @Test
    fun invalidOrFutureDraftsAndReadFailuresNeverClaimAUsablePreview() {
        val model = ready()
        model.onAction(IncomeFormAction.Paste("0"))
        assertEquals(null, model.state.value.monthPreview)
        model.onAction(IncomeFormAction.Paste("150000"))
        clock.goTo("2026-10-21")
        assertEquals(null, model.state.value.monthPreview)
        clock.goTo("2026-10-22")
        reads.of(october).value = IncomeMonthState.Failed(StorageFailure.Unavailable("test"), null)
        assertEquals(null, model.state.value.monthPreview)
        assertTrue(model.state.value.loadFailed)
    }

    @Test
    fun anOverflowingMonthHasAnExplicitUnavailablePreviewInsteadOfAWrappedTotal() {
        val model = ready()
        reads.of(october).value = IncomeMonthState.Ready(
            com.software.sello.domain.port.IncomeMonth(
                october,
                7,
                3,
                com.software.sello.domain.model.Money.cop(Long.MAX_VALUE),
                1
            )
        )
        model.onAction(IncomeFormAction.Paste("1"))
        assertEquals(null, model.state.value.monthPreview)
        assertTrue(model.state.value.previewUnavailable)
    }

    @Test
    fun anUnavailableOptionalPreviewDoesNotBreakTheExistingIncomeWriteJourney() {
        val model = ready()
        model.onAction(IncomeFormAction.Paste("150000"))
        previewOverride.value = ResumenSnapshotState.Failed(
            SnapshotFailure.Unrepresentable,
            resumenSnapshot(income = 900_000)
        )
        assertFalse(model.state.value.loadFailed)
        assertTrue(model.state.value.canSubmit)
        assertEquals(null, model.state.value.monthPreview)
        assertTrue(model.state.value.previewUnavailable)

        reads.stored[incomeId(900)] =
            storedIncome(900, "2026-10-22", 150_000, IncomeSource.Salary)
        model.onAction(IncomeFormAction.Submit)

        assertEquals(1, commands.created.size)
        assertEquals(incomeId(900).value, model.state.value.receipt?.incomeId)
    }

    @Test
    fun previewWaitsUntilIncomeAndSummaryAreFromTheSameRevision() {
        reads.stored[logo.id] = logo
        previewRevision.value = 0
        val model = ready()

        model.onAction(IncomeFormAction.Paste("150000"))

        assertNull(model.state.value.monthPreview)
        assertTrue(model.state.value.previewLoading)

        previewRevision.value = null

        assertEquals(shown("1.000.000"), model.state.value.monthPreview)
        assertFalse(model.state.value.previewLoading)
    }

    /** The form opened on [income], with it in storage and its months read. */
    private fun opened(
        income: Income = logo,
        saved: SavedStateHandle = editing(income)
    ): IncomeFormViewModel {
        reads.stored[income.id] = income
        reads.publish(october, generation = 7)
        reads.publish(YearMonth.from(income.date), generation = 7)
        return form(saved)
    }

    private fun SavedStateHandle.afterProcessDeath() =
        SavedStateHandle(keys().associateWith { get<Any?>(it) })

    private fun IncomeFormViewModel.type(digits: String) =
        digits.forEach { onAction(IncomeFormAction.Digit(it.digitToInt())) }

    private fun IncomeFormViewModel.retype(digits: String) {
        onAction(IncomeFormAction.ClearAmount)
        type(digits)
    }

    private val IncomeFormViewModel.now get() = state.value

    // --- A new income ---

    @Test
    fun replacingHistoryBlocksTheExistingDraftInsteadOfSavingItInTheNewGeneration() {
        val form = ready()
        form.type("5000")
        reads.publish(october, generation = 8, revision = 2)
        form.onAction(IncomeFormAction.Submit)
        assertEquals(IncomeFormError.DataChanged, form.now.error)
        assertFalse(form.now.canSubmit || form.now.editable)
        assertTrue(commands.created.isEmpty())
    }

    @Test
    fun itLoadsThenOpensOnSalaryTodayWithOnlyTheAmountMissing() {
        val form = form()
        val loading = form.now

        reads.publish(october, generation = 7)

        assertTrue(loading.loading && !loading.canSubmit)
        assertEquals(listOf(october), reads.observed)
        assertEquals(
            listOf("salary", "freelance", "passive_income", "transfer", "other"),
            form.now.sourceKeys
        )
        assertEquals("salary" to LocalDate.of(2026, 10, 22), form.now.sourceKey to form.now.date)
        assertEquals(IncomeMissing.Amount, form.now.missing)
        assertFalse(form.now.editing || form.now.canDelete || form.now.loading)
    }

    @Test
    fun whenTheMonthCannotBeReadItSaysSoAndCanBeRetried() {
        val form = form()
        reads.of(october).value =
            IncomeMonthState.Failed(StorageFailure.Unavailable("x"), lastGood = null)
        val failed = form.now

        form.onAction(IncomeFormAction.Retry)
        reads.publish(october, generation = 7)

        assertTrue(failed.loadFailed && !failed.canSubmit)
        assertFalse(form.now.loadFailed)
    }

    @Test
    fun aValidIncomeSendsOneCommandWithExactlyWhatWasEnteredAndShowsWhatStorageHolds() {
        val form = ready()
        form.type("150000")
        form.onAction(IncomeFormAction.SourcePicked("freelance"))
        form.onAction(IncomeFormAction.DatePicked(LocalDate.of(2026, 10, 20)))
        form.onAction(IncomeFormAction.NoteChanged("Marca"))
        // What the receipt shows is read back, not taken from the draft.
        reads.stored[incomeId(900)] =
            storedIncome(900, "2026-10-20", 150_000, IncomeSource.Freelance, "Marca")

        form.onAction(IncomeFormAction.Submit)

        val sent = commands.created.single()
        assertEquals(
            listOf(
                uuid(500),
                7L,
                150_000L,
                IncomeSource.Freelance,
                LocalDate.of(2026, 10, 20),
                "Marca"
            ),
            listOf(
                sent.operationId.value,
                sent.generation,
                sent.amount.money.minorUnits,
                sent.source,
                sent.date,
                sent.note?.value
            )
        )
        assertEquals(
            IncomeReceiptView(
                uuid(900),
                IncomeReceiptView.Details(
                    shown("150.000"),
                    "freelance",
                    null,
                    LocalDate.of(2026, 10, 20),
                    "Marca"
                )
            ),
            form.now.receipt
        )
        assertFalse(form.now.closed)
    }

    @Test
    fun otroNeedsANameWhichIsSentWithItAndKeptButNotSentForAnotherSource() {
        val form = ready()
        form.type("5000")
        form.onAction(IncomeFormAction.SourcePicked("other"))
        val unnamed = form.now
        form.onAction(IncomeFormAction.OtherNameChanged("  Bono  "))
        val named = form.now
        form.onAction(IncomeFormAction.SourcePicked("salary"))
        val backToSalary = form.now

        form.onAction(IncomeFormAction.Submit)
        val other = ready()
        other.type("5000")
        other.onAction(IncomeFormAction.SourcePicked("other"))
        other.onAction(IncomeFormAction.OtherNameChanged("  Bono  "))
        other.onAction(IncomeFormAction.Submit)

        assertEquals(IncomeMissing.SourceName to false, unnamed.missing to unnamed.canSubmit)
        assertTrue(named.canSubmit)
        assertEquals("  Bono  " to null, backToSalary.otherName to backToSalary.missing)
        assertEquals(IncomeSource.Salary, commands.created[0].source)
        assertEquals("Bono", (commands.created[1].source as IncomeSource.Other).name.value)
    }

    @Test
    fun zeroOrSomethingThatIsNotAnAmountIsRefusedAndKeptAsItWasGiven() {
        val form = ready()
        form.onAction(IncomeFormAction.Paste("-150000"))
        val negative = form.now
        form.onAction(IncomeFormAction.Paste("0"))
        val zero = form.now
        form.onAction(IncomeFormAction.Submit)

        assertEquals(
            "-150000" to IncomeAmountProblem.NotAnAmount,
            negative.amountText to negative.amountProblem
        )
        assertEquals("0" to IncomeAmountProblem.Zero, zero.amountText to zero.amountProblem)
        assertNull(zero.amount)
        assertFalse(zero.canSubmit)
        assertEquals(emptyList<Any>(), commands.created)
    }

    @Test
    fun aDateAfterTodayCannotBePicked() {
        val form = ready()

        form.onAction(IncomeFormAction.DatePicked(LocalDate.of(2026, 10, 23)))

        assertEquals(LocalDate.of(2026, 10, 22), form.now.date)
    }

    @Test
    fun whileSavingASecondPressSendsNothingAndTheDraftIsLocked() {
        val form = ready()
        form.type("5000")
        commands.hold = CompletableDeferred()

        form.onAction(IncomeFormAction.Submit)
        val saving = form.now
        form.onAction(IncomeFormAction.Submit)
        form.type("9")
        form.onAction(IncomeFormAction.Back)

        assertTrue(saving.busy && !saving.editable)
        assertEquals(1, commands.created.size)
        assertEquals(shown("5.000"), form.now.amount)
        assertFalse(form.now.closed)
    }

    @Test
    fun aRefusedIncomeKeepsTheDraftAndSaysWhy() {
        val refusals = listOf(
            IncomeRejection.FutureDate(LocalDate.of(2026, 10, 22)) to IncomeFormError.FutureDate,
            IncomeRejection.InvalidDate to IncomeFormError.InvalidDate,
            IncomeRejection.StaleGeneration(8) to IncomeFormError.DataChanged,
            IncomeRejection.StorageFailed(StorageFailure.Unavailable("x")) to
                IncomeFormError.NotSaved
        )

        val seen = refusals.map { (reason, _) ->
            commands.createAnswer = { Rejected(reason) }
            val form = ready()
            form.type("5000")
            form.onAction(IncomeFormAction.Submit)
            Triple(form.now.error, form.now.amount, form.now.receipt)
        }

        assertEquals(refusals.map { Triple(it.second, shown("5.000"), null) }, seen)
    }

    @Test
    fun anUnknownSaveCannotBeDiscardedAndLoseTheOperationNeededForRecovery() {
        commands.createAnswer = { IncomeCommandOutcome.OutcomeUnknown(it.operationId) }
        commands.found = { Outcome.Failure(StorageFailure.Unavailable("x")) }
        val form = ready()
        form.type("5000")
        form.onAction(IncomeFormAction.Submit)
        form.onAction(IncomeFormAction.Back)
        assertFalse(form.now.confirmDiscard)
        form.onAction(IncomeFormAction.DiscardConfirmed)
        assertFalse(form.now.closed)
        commands.found = { Outcome.Success(commands.committed(it)) }
        form.onAction(IncomeFormAction.Retry)
        assertEquals(1, commands.created.size)
        assertEquals(uuid(900), form.now.receipt?.incomeId)
    }

    @Test
    fun anUnknownOutcomeIsLookedUpAndNeverSentAgain() {
        commands.createAnswer = { IncomeCommandOutcome.OutcomeUnknown(it.operationId) }
        commands.found = { Outcome.Failure(StorageFailure.Unavailable("x")) }
        val form = ready()
        form.type("5000")
        form.onAction(IncomeFormAction.Submit)
        val unknown = form.now
        commands.found = { Outcome.Success(commands.committed(it)) }

        form.onAction(IncomeFormAction.Retry)

        assertEquals(IncomeFormError.Unknown, unknown.error)
        assertFalse(unknown.canSubmit || unknown.editable)
        assertEquals(1, commands.created.size)
        assertEquals(uuid(900), form.now.receipt?.incomeId)
    }

    @Test
    fun afterTheProcessWasKilledTheDraftIsBackAndAnInterruptedSaveIsLookedUpNotSentAgain() {
        val saved = SavedStateHandle()
        val first = ready(saved)
        first.type("5000")
        first.onAction(IncomeFormAction.SourcePicked("other"))
        first.onAction(IncomeFormAction.OtherNameChanged("Bono"))
        first.onAction(IncomeFormAction.NoteChanged("Extra"))
        val draft = saved.afterProcessDeath()
        commands.hold = CompletableDeferred()
        first.onAction(IncomeFormAction.Submit)
        commands.found = { Outcome.Success(commands.committed(it)) }

        val restoredDraft = form(draft)
        val restoredMidSave = form(saved.afterProcessDeath())

        assertEquals(
            listOf(shown("5.000"), "other", "Bono", "Extra"),
            listOf(
                restoredDraft.now.amount,
                restoredDraft.now.sourceKey,
                restoredDraft.now.otherName,
                restoredDraft.now.note
            )
        )
        assertEquals(1, commands.created.size)
        assertEquals(listOf(commands.created.single().operationId), commands.looked)
        assertEquals(uuid(900), restoredMidSave.now.receipt?.incomeId)
    }

    @Test
    fun backAsksOnlyWhenSomethingWasTypedAndLeavingTheReceiptJustLeaves() {
        val empty = ready()
        empty.onAction(IncomeFormAction.Back)
        val typed = ready()
        typed.type("5")
        typed.onAction(IncomeFormAction.Back)
        val asked = typed.now
        typed.onAction(IncomeFormAction.KeepEditing)
        typed.onAction(IncomeFormAction.Submit)
        typed.onAction(IncomeFormAction.Done)

        assertTrue(empty.now.closed)
        assertTrue(asked.confirmDiscard && !asked.closed)
        assertTrue(typed.now.closed)
    }

    // --- Correcting a recorded income ---

    private val storedDraft =
        listOf(shown("850.000"), "other", "Diseño", LocalDate.of(2026, 10, 5), "Logo")

    private fun IncomeFormState.draft() = listOf(amount, sourceKey, otherName, date, note)

    @Test
    fun aRecordedIncomeOpensOnExactlyWhatIsStoredWithNothingToSaveYet() {
        val form = opened()

        assertTrue(form.now.editing && form.now.canDelete)
        assertEquals(storedDraft, form.now.draft())
        assertEquals(IncomeMissing.Changes, form.now.missing)
        assertFalse(form.now.canSubmit)
        assertEquals(emptyList<YearMonth>(), form.now.pastMonths)
        assertEquals(emptyList<Any>(), commands.edited + commands.created)
    }

    @Test
    fun anIncomeThatIsGoneOrCannotBeReadSaysSoAndTheSecondCanBeAskedForAgain() {
        reads.publish(october, generation = 7)
        val gone = form(editing())
        reads.failure = StorageFailure.Unavailable("x")
        val unreadable = form(editing())
        val failed = unreadable.now
        reads.failure = null
        reads.stored[logo.id] = logo

        unreadable.onAction(IncomeFormAction.Retry)

        assertEquals(IncomeFormError.IncomeGone, gone.now.error)
        assertFalse(gone.now.canSubmit || gone.now.canDelete || gone.now.editable)
        assertTrue(failed.loadFailed)
        assertEquals(storedDraft, unreadable.now.draft())
    }

    @Test
    fun savingSendsOneEditOfTheVersionReadThenClosesAndSaysItWasUpdated() {
        val form = opened()
        form.retype("900000")
        form.onAction(IncomeFormAction.SourcePicked("salary"))
        val ready = form.now

        form.onAction(IncomeFormAction.Submit)

        assertTrue(ready.canSubmit)
        val sent = commands.edited.single()
        assertEquals(
            listOf(
                uuid(500),
                7L,
                uuid(100),
                3L,
                900_000L,
                IncomeSource.Salary,
                LocalDate.of(2026, 10, 5),
                "Logo"
            ),
            listOf(
                sent.operationId.value,
                sent.generation,
                sent.incomeId.value,
                sent.expectedVersion,
                sent.amount.money.minorUnits,
                sent.source,
                sent.date,
                sent.note?.value
            )
        )
        assertTrue(form.now.closed)
        assertNull(form.now.receipt)
        assertEquals(CorrectionNotice.IncomeUpdated, corrections.notice.value)
        assertEquals(emptyList<Any>(), commands.created)
    }

    @Test
    fun puttingEverythingBackAsItWasLeavesNothingToSave() {
        val form = opened()
        form.retype("5")
        form.onAction(IncomeFormAction.OtherNameChanged("Otra cosa"))
        val changed = form.now.canSubmit

        form.retype("850000")
        form.onAction(IncomeFormAction.OtherNameChanged(" Diseño "))
        form.onAction(IncomeFormAction.Submit)

        assertTrue(changed)
        assertEquals(IncomeMissing.Changes, form.now.missing)
        assertEquals(emptyList<Any>(), commands.edited)
    }

    @Test
    fun aRefusedCorrectionKeepsWhatWasTypedAndOnceItChangedUnderneathNothingMoreIsSent() {
        val refusals = listOf(
            IncomeRejection.VersionConflict(4) to IncomeFormError.IncomeChanged,
            IncomeRejection.IncomeMissing to IncomeFormError.IncomeGone,
            IncomeRejection.StaleGeneration(8) to IncomeFormError.DataChanged,
            IncomeRejection.StorageFailed(StorageFailure.Unavailable("x")) to
                IncomeFormError.NotSaved
        )

        val seen = refusals.map { (reason, _) ->
            commands.editAnswer = { Rejected(reason) }
            val form = opened()
            form.retype("900000")
            form.onAction(IncomeFormAction.Submit)
            form.onAction(IncomeFormAction.Submit)
            Triple(form.now.error, form.now.amount, form.now.closed)
        }

        assertEquals(refusals.map { Triple(it.second, shown("900.000"), false) }, seen)
        // The two that block sent once; the two that can be retried sent twice.
        assertEquals(6, commands.edited.size)
        assertNull(corrections.notice.value)
    }

    @Test
    fun anIncomeOfAnEarlierMonthSaysWhichMonthsWouldChange() {
        val september = logo.copy(date = LocalDate.of(2026, 9, 12))
        reads.publish(YearMonth.of(2026, 8), generation = 7)
        val form = opened(september)
        val opened = form.now.pastMonths

        form.onAction(IncomeFormAction.DatePicked(LocalDate.of(2026, 8, 30)))

        assertEquals(listOf(YearMonth.of(2026, 9)), opened)
        assertEquals(listOf(YearMonth.of(2026, 9), YearMonth.of(2026, 8)), form.now.pastMonths)
    }

    @Test
    fun aDraftRestoredAfterTheProcessWasKilledKeepsTheChangesAndTheVersionItRead() {
        val saved = editing()
        val first = opened(saved = saved)
        first.retype("900000")
        reads.stored[logo.id] = logo.copy(version = 4)

        val restored = form(saved.afterProcessDeath())
        restored.onAction(IncomeFormAction.Submit)

        assertEquals(shown("900.000"), restored.now.amount)
        assertEquals(3L, commands.edited.single().expectedVersion)
    }

    @Test
    fun anUncertainDeletionIsRecoveredAsThatDeletionWithoutAnotherWriteOrEditNotice() {
        commands.deleteAnswer = { IncomeCommandOutcome.OutcomeUnknown(it.operationId) }
        commands.found = { Outcome.Failure(StorageFailure.Unavailable("x")) }
        val form = opened()
        form.onAction(IncomeFormAction.Delete)
        assertEquals(IncomeFormError.Unknown, form.now.error)
        form.onAction(IncomeFormAction.Delete)
        assertEquals(1, commands.deleted.size)
        commands.found = { Outcome.Success(commands.committed(it, logo.id)) }
        form.onAction(IncomeFormAction.Retry)
        assertTrue(form.now.closed)
        assertTrue(corrections.notice.value is CorrectionNotice.IncomeDeleted)
        assertEquals(1, commands.deleted.size)
        assertTrue(commands.edited.isEmpty())
    }

    @Test
    fun deletingFromTheFormDeletesTheVersionReadAndClosesAndARefusalKeepsItOpen() {
        val form = opened()
        form.onAction(IncomeFormAction.Delete)
        commands.deleteAnswer = { Rejected(IncomeRejection.VersionConflict(4)) }
        val refused = opened()
        refused.onAction(IncomeFormAction.Delete)

        val sent = commands.deleted.first()
        assertEquals(
            listOf(uuid(100), 3L, 7L),
            listOf(sent.incomeId.value, sent.expectedVersion, sent.generation)
        )
        assertTrue(form.now.closed)
        assertTrue(corrections.notice.value is CorrectionNotice.IncomeDeleted)
        assertEquals(
            IncomeFormError.IncomeChanged to false,
            refused.now.error to refused.now.closed
        )
    }
}
