package com.software.sello.composition

import com.software.sello.data.StorageNames
import com.software.sello.domain.model.FinancialProfile
import org.koin.core.module.Module

/**
 * What the build type adds to the app's composition. A release build always uses
 * [ProductionComposition]; only the debug build's own source set can return anything
 * else, so developer tools never reach a customer build.
 */
interface VariantComposition {
    /** Which data this process opens. It is chosen once, before storage opens. */
    val names: StorageNames

    /**
     * Bindings loaded after the production modules for the profile that opened. They
     * may replace the financial clock and wrap commands, and nothing else.
     */
    fun modules(profile: FinancialProfile): List<Module>
}

/** The app as customers get it: its own data and real financial time. */
object ProductionComposition : VariantComposition {
    override val names: StorageNames = StorageNames.Normal

    override fun modules(profile: FinancialProfile): List<Module> = emptyList()
}
