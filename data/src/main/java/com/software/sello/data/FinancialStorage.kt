package com.software.sello.data

import android.content.Context
import com.software.sello.data.local.SelloDatabase
import com.software.sello.data.local.openSelloDatabase
import com.software.sello.data.repository.RoomCategoryCommands
import com.software.sello.data.repository.RoomCategoryReads
import com.software.sello.data.repository.RoomExpenseCommands
import com.software.sello.data.repository.RoomFinancialProfileStore
import com.software.sello.data.repository.RoomMonthlySnapshots
import com.software.sello.domain.port.AuditClock
import com.software.sello.domain.port.CategoryCommands
import com.software.sello.domain.port.CategoryReads
import com.software.sello.domain.port.ExpenseCommands
import com.software.sello.domain.port.FinancialClock
import com.software.sello.domain.port.FinancialProfileStore
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
        ids: RecordIdSource
    ): ExpenseCommands = RoomExpenseCommands(database, audit, clock, ids)

    fun categoryCommands(
        audit: AuditClock,
        clock: FinancialClock,
        ids: RecordIdSource
    ): CategoryCommands = RoomCategoryCommands(database, audit, clock, ids)

    val categoryReads: CategoryReads = RoomCategoryReads(database)

    /** Takes the financial clock for the same reason the commands do. */
    fun monthlySnapshots(clock: FinancialClock): MonthlySnapshots =
        RoomMonthlySnapshots(database, clock)

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
