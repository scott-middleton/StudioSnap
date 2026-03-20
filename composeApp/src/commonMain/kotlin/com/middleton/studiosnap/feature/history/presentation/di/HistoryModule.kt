package com.middleton.studiosnap.feature.history.presentation.di

import com.middleton.studiosnap.core.presentation.navigation.NavigationStrategy
import com.middleton.studiosnap.core.presentation.navigation.SharedNavigationStrategy
import com.middleton.studiosnap.feature.history.data.repository.HistoryRepositoryImpl
import com.middleton.studiosnap.feature.history.domain.repository.HistoryRepository
import com.middleton.studiosnap.feature.history.presentation.navigation.HistoryNavigationAction
import com.middleton.studiosnap.feature.history.presentation.viewmodel.HistoryViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val historyModule = module {

    single<HistoryRepository> {
        HistoryRepositoryImpl(
            generationDao = get(),
            styleRepository = get()
        )
    }

    factory<NavigationStrategy<HistoryNavigationAction>> {
        SharedNavigationStrategy(get())
    }

    viewModel { HistoryViewModel(get(), get()) }
}
