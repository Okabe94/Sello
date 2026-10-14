package com.software.sello.composition

import com.software.sello.data.preferences.DataStoreUserPreferences
import com.software.sello.domain.port.AppearancePreferences
import com.software.sello.domain.port.CategoryOrderingPreferences
import com.software.sello.domain.port.CorrectionAnnouncementPreferences
import kotlinx.coroutines.CoroutineScope
import org.koin.core.module.Module
import org.koin.dsl.module

/** The composition-only bindings for the nonfinancial preference adapter. */
fun preferencesModule(): Module = module {
    single { DataStoreUserPreferences(get(), get<CoroutineScope>()) }
    single<AppearancePreferences> { get<DataStoreUserPreferences>() }
    single<CategoryOrderingPreferences> { get<DataStoreUserPreferences>() }
    single<CorrectionAnnouncementPreferences> { get<DataStoreUserPreferences>() }
}
