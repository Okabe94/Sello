package com.software.sello.composition

import android.content.pm.ApplicationInfo
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.software.sello.R
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.xmlpull.v1.XmlPullParser

/** Reads the installed app's merged manifest flag and packaged rules, not the source files. */
@RunWith(AndroidJUnit4::class)
class BackupDisabledTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val everyDomain = setOf(
        "root",
        "file",
        "database",
        "sharedpref",
        "external",
        "device_root",
        "device_file",
        "device_database",
        "device_sharedpref"
    )

    /** Every rule in the file as "section/tag/domain/path". */
    private fun rules(resource: Int): Set<String> {
        val found = mutableSetOf<String>()
        val parser = context.resources.getXml(resource)
        var section = ""
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            when (parser.name) {
                "include", "exclude" -> found += listOf(
                    section,
                    parser.name,
                    parser.getAttributeValue(null, "domain"),
                    parser.getAttributeValue(null, "path")
                ).joinToString("/")

                else -> section = parser.name
            }
        }
        return found
    }

    private fun excluded(section: String) = everyDomain.map { "$section/exclude/$it/." }.toSet()

    @Test
    fun theInstalledAppDoesNotAllowBackup() {
        assertEquals(0, context.applicationInfo.flags and ApplicationInfo.FLAG_ALLOW_BACKUP)
    }

    @Test
    fun cloudBackupAndDeviceTransferExcludeEveryStorageDomain() {
        assertEquals(
            excluded("cloud-backup") + excluded("device-transfer"),
            rules(R.xml.data_extraction_rules)
        )
    }

    @Test
    fun theOlderBackupRulesExcludeEveryStorageDomain() {
        assertEquals(excluded("full-backup-content"), rules(R.xml.backup_rules))
    }
}
