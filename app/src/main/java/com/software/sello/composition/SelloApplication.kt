package com.software.sello.composition

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import com.software.sello.platform.ProcessFinancialZone
import com.software.sello.platform.SystemAuditClock
import com.software.sello.platform.SystemDispatcherProvider
import com.software.sello.platform.deviceZone
import org.koin.core.context.startKoin

class SelloApplication : Application() {
    /** Builds every screen's view model from the graph started below. */
    lateinit var viewModels: ViewModelProvider.Factory
        private set

    override fun onCreate() {
        super.onCreate()
        // The financial zone comes from the database: the device zone is stored on first
        // use and never read again, so a later trip does not move recorded days.
        val opened = openStorage(
            applicationContext,
            deviceZone(),
            SystemDispatcherProvider(),
            SystemAuditClock()
        )
        val financialZone = ProcessFinancialZone().apply { initialize(opened.profile.zone) }
        val application = startKoin {
            modules(productionModules(applicationContext, financialZone, opened.storage))
        }
        viewModels = viewModels(application.koin)
    }
}
