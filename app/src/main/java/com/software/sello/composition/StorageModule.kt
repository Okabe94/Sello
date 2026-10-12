package com.software.sello.composition

import android.content.Context
import com.software.sello.data.FinancialStorage
import com.software.sello.data.openFinancialStorage
import com.software.sello.data.setAsideFinancialStorage
import com.software.sello.domain.model.FinancialProfile
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure
import com.software.sello.domain.port.AuditClock
import com.software.sello.domain.port.CategoryCommands
import com.software.sello.domain.port.CategoryReads
import com.software.sello.domain.port.DispatcherProvider
import com.software.sello.domain.port.ExpenseCommands
import com.software.sello.domain.port.ExpenseReads
import com.software.sello.domain.port.FinancialProfileStore
import com.software.sello.domain.port.MonthlySnapshots
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.dsl.onClose

/** Open storage and the profile it holds, established before the graph is built. */
class OpenedStorage(val storage: FinancialStorage, val profile: FinancialProfile)

/**
 * Opens the database and establishes the profile: stored on first use with
 * [deviceZone], read back on every later start. It waits for that one small read
 * because the financial zone must be known before anything computes a date.
 *
 * A database that cannot be read is a failure with the storage closed again. It is
 * never replaced by an empty one and the device zone is never used in its place.
 */
fun openStorage(
    context: Context,
    deviceZone: ZoneId,
    dispatchers: DispatcherProvider,
    audit: AuditClock
): Outcome<OpenedStorage, StorageFailure> {
    val storage = openFinancialStorage(context, dispatchers.io, audit)
    val opened = runBlocking {
        when (val profile = storage.profiles.establish(deviceZone)) {
            is Outcome.Failure -> profile

            // Whatever an earlier process kept to undo a deletion can no longer be undone.
            is Outcome.Success -> when (val discarded = storage.discardUndoCopies()) {
                is Outcome.Failure -> discarded
                is Outcome.Success -> Outcome.Success(OpenedStorage(storage, profile.value))
            }
        }
    }
    if (opened is Outcome.Failure) storage.close()
    return opened
}

/** Keeps the file that could not be opened under another name; see `:data`. */
fun setAsideStorage(context: Context): Outcome<Unit, StorageFailure> =
    setAsideFinancialStorage(context)

/** Ports backed by the database. Closing the graph closes the database. */
fun storageModule(storage: FinancialStorage): Module = module {
    single { storage } onClose { it?.close() }
    single<FinancialProfileStore> { storage.profiles }
    single<ExpenseCommands> { storage.expenseCommands(get(), get(), get(), get()) }
    single<CategoryCommands> { storage.categoryCommands(get(), get(), get()) }
    single<CategoryReads> { storage.categoryReads }
    single<MonthlySnapshots> {
        storage.monthlySnapshots(get(), get<DispatcherProvider>().default)
    }
    single<ExpenseReads> { storage.expenseReads }
}
