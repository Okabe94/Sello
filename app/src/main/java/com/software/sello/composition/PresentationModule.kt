package com.software.sello.composition

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.software.sello.feature.category.CategoryEditorViewModel
import com.software.sello.feature.recibo.ReciboViewModel
import com.software.sello.navigation.MonthSession
import com.software.sello.navigation.ShellViewModel
import com.software.sello.presentation.money.MoneyFormatter
import com.software.sello.presentation.money.ResourceMoneyLabels
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.dsl.module

/** What screens share: the one month session and the money formatter. */
fun presentationModule(): Module = module {
    single { MonthSession(get(), get()) }
    single { MoneyFormatter(ResourceMoneyLabels(get<Context>().resources)) }
}

/**
 * How every view model is built. Screens ask for a view model by type and never see
 * the graph; each initializer names its collaborators here, so a missing one fails
 * when the screen opens instead of silently defaulting.
 */
fun viewModels(koin: Koin): ViewModelProvider.Factory = viewModelFactory {
    initializer { ShellViewModel(createSavedStateHandle(), koin.get(), koin.get()) }
    initializer { ReciboViewModel(koin.get(), koin.get(), koin.get()) }
    initializer {
        CategoryEditorViewModel(
            createSavedStateHandle(),
            koin.get(),
            koin.get(),
            koin.get(),
            koin.get(),
            koin.get()
        )
    }
}
