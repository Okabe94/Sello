package com.software.sello.composition

import android.content.Context
import com.software.sello.data.FinancialStorage
import com.software.sello.data.openFinancialStorage
import com.software.sello.domain.model.FinancialProfile
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure
import com.software.sello.domain.port.AuditClock
import com.software.sello.domain.port.DispatcherProvider
import com.software.sello.domain.port.FinancialProfileStore
import java.time.ZoneId
import kotlinx.coroutines.runBlocking
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.dsl.onClose

/** The stored profile could not be read at startup. Carries no stored value. */
class FinancialStorageUnavailable(val failure: StorageFailure) :
    IllegalStateException("Financial storage could not be opened: $failure")

/** Open storage and the profile it holds, established before the graph is built. */
class OpenedStorage(val storage: FinancialStorage, val profile: FinancialProfile)

/**
 * Opens the database and establishes the profile: stored on first use with
 * [deviceZone], read back on every later start. It waits for that one small read
 * because the financial zone must be known before anything computes a date.
 *
 * A database that cannot be read stops startup. It is never replaced by an empty one
 * and the device zone is never used in its place.
 */
fun openStorage(
    context: Context,
    deviceZone: ZoneId,
    dispatchers: DispatcherProvider,
    audit: AuditClock
): OpenedStorage {
    val storage = openFinancialStorage(context, dispatchers.io, audit)
    return when (val profile = runBlocking { storage.profiles.establish(deviceZone) }) {
        is Outcome.Success -> OpenedStorage(storage, profile.value)

        is Outcome.Failure -> {
            storage.close()
            throw FinancialStorageUnavailable(profile.error)
        }
    }
}

/** Ports backed by the database. Closing the graph closes the database. */
fun storageModule(storage: FinancialStorage): Module = module {
    single { storage } onClose { it?.close() }
    single<FinancialProfileStore> { storage.profiles }
}
