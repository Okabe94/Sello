package com.software.sello.data.backup

import com.software.sello.domain.port.BackupPreparationFailure
import org.junit.Assert.assertEquals
import org.junit.Test

/** What the backup screen is told when the app refuses the file it just made. */
class BackupPreparationFailureTest {
    @Test
    fun onlyAHistoryBeyondTheFormatsLimitsIsTooLarge() {
        listOf(
            BackupRejection.TooLarge(10, 11),
            BackupRejection.TooManyRecords(100_000),
            BackupRejection.HistoryTooLong("financial.defaultLimits[1200]")
        ).forEach { assertEquals("$it", BackupPreparationFailure.TooLarge, preparationFailure(it)) }
    }

    @Test
    fun anyOtherRefusalIsTheAppsOwnFaultNotTheSizeOfTheData() {
        listOf(
            // The Android 16 clock's microseconds in exportedAt, SELLO-036.
            BackupRejection.InvalidValue("exportedAt"),
            BackupRejection.Truncated,
            BackupRejection.Malformed(null),
            BackupRejection.TooDeep(8),
            BackupRejection.FieldTooLong(10_000),
            BackupRejection.DuplicateKey("id"),
            BackupRejection.NotSelloBackup,
            BackupRejection.UnsupportedVersion(2),
            BackupRejection.UnsupportedCurrency("financial.expenses[0].currency"),
            BackupRejection.AmountOutOfRange("financial.expenses[0].amount"),
            BackupRejection.DuplicateId("financial.expenses[1].id"),
            BackupRejection.DuplicateCategoryName("financial.categories[1].name"),
            BackupRejection.DuplicateEntry("financial.monthLimits[1]"),
            BackupRejection.DanglingReference("financial.expenses[0].categoryId")
        ).forEach {
            assertEquals("$it", BackupPreparationFailure.Unrestorable, preparationFailure(it))
        }
    }
}
