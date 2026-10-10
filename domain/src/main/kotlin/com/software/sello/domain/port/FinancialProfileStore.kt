package com.software.sello.domain.port

import com.software.sello.domain.model.FinancialProfile
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure
import java.time.ZoneId

/** Where the financial zone, generation and revision are kept. */
interface FinancialProfileStore {
    /**
     * Returns the stored profile, creating it on first use with [deviceZone],
     * generation 1 and revision 0 in one step. Once a profile exists [deviceZone] is
     * ignored: travelling does not move recorded days. Safe to call concurrently.
     */
    suspend fun establish(deviceZone: ZoneId): Outcome<FinancialProfile, StorageFailure>
}
