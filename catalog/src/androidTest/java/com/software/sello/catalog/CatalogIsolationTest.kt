package com.software.sello.catalog

import android.content.pm.PackageManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CatalogIsolationTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun catalogRequestsNoPlatformPermission() {
        val info = context.packageManager.getPackageInfo(
            context.packageName,
            PackageManager.GET_PERMISSIONS
        )
        // Storage, network and every other Android permission live in this namespace.
        val platform = info.requestedPermissions.orEmpty().filter { it.startsWith("android.") }
        assertEquals(emptyList<String>(), platform)
    }

    @Test
    fun catalogHasItsOwnLauncherEntry() {
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
        assertNotNull(launch)
        assertEquals(CatalogActivity::class.java.name, launch!!.component!!.className)
    }
}
