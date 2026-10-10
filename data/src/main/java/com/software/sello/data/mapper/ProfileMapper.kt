package com.software.sello.data.mapper

import com.software.sello.data.local.entity.ProfileEntity
import com.software.sello.domain.model.FinancialProfile
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure
import java.time.DateTimeException
import java.time.ZoneId

/** [rows] is everything the profile table holds: exactly one row, with the fixed identifier. */
internal fun profileFrom(
    rows: List<ProfileEntity>
): Outcome<FinancialProfile, StorageFailure.Integrity> = decodeRow(ProfileEntity.TABLE, null) {
    check("id", rows.size == 1 && rows[0].id == ProfileEntity.SINGLE_ID)
    val row = rows[0]
    val zone = try {
        ZoneId.of(row.financialZone)
    } catch (_: DateTimeException) {
        reject("financial_zone")
    }
    check("financial_zone", zone.id == row.financialZone)
    check("generation", row.generation >= 1)
    check("revision", row.revision >= 0)
    FinancialProfile(zone, row.generation, row.revision)
}
