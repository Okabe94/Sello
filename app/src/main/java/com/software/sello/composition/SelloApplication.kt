package com.software.sello.composition

import android.app.Application
import androidx.lifecycle.ViewModelProvider
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure
import com.software.sello.platform.ProcessFinancialZone
import com.software.sello.platform.SystemAuditClock
import com.software.sello.platform.SystemDispatcherProvider
import com.software.sello.platform.deviceZone
import org.koin.core.context.startKoin

class SelloApplication : Application() {
    /** Whether the data opened, and with it how every screen's view model is built. */
    lateinit var startup: Startup
        private set

    override fun onCreate() {
        super.onCreate()
        startup = Startup(
            start = ::openAndBuildGraph,
            moveUnreadable = { setAsideStorage(applicationContext) }
        )
    }

    /** The graph is started only on data that opened, so it is started at most once. */
    private fun openAndBuildGraph(): Outcome<ViewModelProvider.Factory, StorageFailure> {
        // The financial zone comes from the database: the device zone is stored on first
        // use and never read again, so a later trip does not move recorded days.
        val opened = openStorage(
            applicationContext,
            deviceZone(),
            SystemDispatcherProvider(),
            SystemAuditClock()
        )
        return when (opened) {
            is Outcome.Failure -> opened

            is Outcome.Success -> {
                val financialZone = ProcessFinancialZone().apply {
                    initialize(opened.value.profile.zone)
                }
                val application = startKoin {
                    modules(
                        productionModules(applicationContext, financialZone, opened.value.storage)
                    )
                }
                Outcome.Success(viewModels(application.koin))
            }
        }
    }
}
