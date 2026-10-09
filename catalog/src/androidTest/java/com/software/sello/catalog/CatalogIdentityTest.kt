package com.software.sello.catalog

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CatalogIdentityTest {
    @Test
    fun catalogHasItsOwnPackageAndLabel() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.software.sello.catalog", context.packageName)
        assertEquals(
            "Sello Catalog",
            context.applicationInfo.loadLabel(context.packageManager).toString()
        )
    }
}
