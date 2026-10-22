package com.software.sello.data

import org.junit.Assert.assertEquals
import org.junit.Test

class StorageNamesTest {
    @Test
    fun theAppsOwnFilesKeepTheirNamesSoExistingInstallationsOpenTheirData() {
        assertEquals("sello.db", StorageNames.Normal.database)
        assertEquals("sello-preferences", StorageNames.Normal.preferences)
        assertEquals("sello-unreadable.db", StorageNames.Normal.setAsideDatabase)
    }

    @Test
    fun anotherSetIsKeptAsideUnderItsOwnName() {
        assertEquals(
            "sandbox-unreadable.db",
            StorageNames("sandbox.db", "sandbox-preferences").setAsideDatabase
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun aDatabaseNameMustEndInDb() {
        StorageNames("sandbox", "sandbox-preferences")
    }
}
