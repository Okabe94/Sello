package com.software.sello.composition

import android.app.Application
import com.software.sello.platform.ProcessFinancialZone
import com.software.sello.platform.deviceZone
import org.koin.core.context.startKoin

class SelloApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Explicit initialization: the device zone becomes the financial zone once per
        // process. SELLO-011 supplies the persisted zone here instead.
        val financialZone = ProcessFinancialZone().apply { initialize(deviceZone()) }
        startKoin { modules(productionModules(applicationContext, financialZone)) }
    }
}
