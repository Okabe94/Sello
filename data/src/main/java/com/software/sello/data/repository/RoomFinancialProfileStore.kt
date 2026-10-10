package com.software.sello.data.repository

import com.software.sello.data.local.SelloDatabase
import com.software.sello.data.local.entity.ProfileEntity
import com.software.sello.data.mapper.profileFrom
import com.software.sello.domain.model.FinancialProfile
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure
import com.software.sello.domain.port.AuditClock
import com.software.sello.domain.port.FinancialProfileStore
import java.time.ZoneId

class RoomFinancialProfileStore(
    private val database: SelloDatabase,
    private val audit: AuditClock
) : FinancialProfileStore {
    override suspend fun establish(deviceZone: ZoneId): Outcome<FinancialProfile, StorageFailure> =
        reading {
            val candidate = ProfileEntity(
                id = ProfileEntity.SINGLE_ID,
                financialZone = deviceZone.id,
                generation = 1,
                revision = 0,
                createdAt = audit.now().toEpochMilli()
            )
            profileFrom(database.profileDao().establish(candidate))
        }
}
