package com.software.sello.data

import android.content.Context
import com.software.sello.data.local.SelloDatabase
import com.software.sello.data.local.openSelloDatabase
import com.software.sello.data.local.setAside
import com.software.sello.data.repository.RoomCategoryCommands
import com.software.sello.data.repository.RoomCategoryReads
import com.software.sello.data.repository.RoomExpenseCommands
import com.software.sello.data.repository.RoomExpenseReads
import com.software.sello.data.repository.RoomFinancialProfileStore
import com.software.sello.data.repository.RoomIncomeCommands
import com.software.sello.data.repository.RoomIncomeReads
import com.software.sello.data.repository.RoomMonthlySnapshots
import com.software.sello.data.repository.UndoWindow
import com.software.sello.data.repository.discardKeptCopies
import com.software.sello.data.repository.reading
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure
import com.software.sello.domain.port.AuditClock
import com.software.sello.domain.port.CategoryCommands
import com.software.sello.domain.port.CategoryReads
import com.software.sello.domain.port.ExpenseCommands
import com.software.sello.domain.port.ExpenseReads
import com.software.sello.domain.port.FinancialClock
import com.software.sello.domain.port.FinancialProfileStore
import com.software.sello.domain.port.IncomeCommands
import com.software.sello.domain.port.IncomeReads
import com.software.sello.domain.port.MonotonicClock
import com.software.sello.domain.port.MonthlySnapshots
import com.software.sello.domain.port.RecordIdSource
import kotlin.coroutines.CoroutineContext

/**
 * What the composition root gets from storage: domain ports backed by the one real
 * database, and nothing of Room. Each workflow that ships adds its port here.
 * Closing it closes the database.
 */
class FinancialStorage internal constructor(
    private val database: SelloDatabase,
    val profiles: FinancialProfileStore
) : AutoCloseable {
    /**
     * The expense workflow. It takes its clocks and identifier source here because the
     * financial clock can only exist once the stored zone has been read from this storage.
     */
    fun expenseCommands(
        audit: AuditClock,
        clock: FinancialClock,
        ids: RecordIdSource,
        monotonic: MonotonicClock
    ): ExpenseCommands = RoomExpenseCommands(database, audit, clock, ids, undoWindow(monotonic))

    /**
     * The income workflow. It shares the one offer to undo with the expense workflow:
     * a deletion of either kind makes an earlier one of either kind final.
     */
    fun incomeCommands(
        audit: AuditClock,
        clock: FinancialClock,
        ids: RecordIdSource,
        monotonic: MonotonicClock
    ): IncomeCommands = RoomIncomeCommands(database, audit, clock, ids, undoWindow(monotonic))

    val incomeReads: IncomeReads = RoomIncomeReads(database)

    /** One offer to undo for the whole storage, whichever workflow asks for it first. */
    private var undo: UndoWindow? = null

    @Synchronized
    private fun undoWindow(monotonic: MonotonicClock): UndoWindow =
        undo ?: UndoWindow(monotonic).also { undo = it }

    fun categoryCommands(
        audit: AuditClock,
        clock: FinancialClock,
        ids: RecordIdSource
    ): CategoryCommands = RoomCategoryCommands(database, audit, clock, ids)

    val categoryReads: CategoryReads = RoomCategoryReads(database)

    val expenseReads: ExpenseReads = RoomExpenseReads(database)

    /**
     * Takes the financial clock for the same reason the commands do. [computation] must
     * carry the dispatcher the month's figures are worked out on; it is never the caller's.
     */
    fun monthlySnapshots(clock: FinancialClock, computation: CoroutineContext): MonthlySnapshots =
        RoomMonthlySnapshots(database, clock, computation)

    /**
     * Makes every deletion final by removing what was kept to undo it. The time to undo
     * is counted in the process that deleted, so whatever a new process finds here can
     * no longer be undone. Call it once when storage is opened. It removes no expense
     * and changes no figure: a deleted expense was already gone.
     */
    suspend fun discardUndoCopies(): Outcome<Unit, StorageFailure> = reading {
        database.discardKeptCopies()
        Outcome.Success(Unit)
    }

    override fun close() = database.close()
}

/**
 * Prepares the app's financial database without touching the file yet; the first
 * read or write opens it, creating it on first use. [queries] must carry the
 * dispatcher database work runs on.
 */
fun openFinancialStorage(
    context: Context,
    queries: CoroutineContext,
    audit: AuditClock
): FinancialStorage {
    val database = openSelloDatabase(context, queries)
    return FinancialStorage(database, RoomFinancialProfileStore(database, audit))
}

/**
 * For a database file that cannot be opened, once its storage is closed: moves it to
 * `sello-unreadable.db` in the same private folder so the next open starts an empty
 * one. The file is kept, not deleted, because a file this build cannot read may be
 * intact: one written by a newer version reads again once the app is updated.
 */
fun setAsideFinancialStorage(context: Context): Outcome<Unit, StorageFailure> = setAside(
    context.getDatabasePath(SelloDatabase.FILE_NAME),
    context.getDatabasePath(SelloDatabase.SET_ASIDE_FILE_NAME)
)
