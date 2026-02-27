package com.middleton.studiosnap.feature.results.presentation.di

import com.middleton.studiosnap.core.presentation.navigation.NavigationStrategy
import com.middleton.studiosnap.core.presentation.navigation.SharedNavigationStrategy
import com.middleton.studiosnap.feature.results.domain.usecase.DownloadFullResUseCase
import com.middleton.studiosnap.feature.results.presentation.navigation.ResultsNavigationAction
import com.middleton.studiosnap.feature.results.presentation.viewmodel.ResultsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val resultsModule = module {

    factory {
        DownloadFullResUseCase(
            generationRepository = get(),
            historyRepository = get(),
            creditDeductor = get(),
            errorReporter = get()
        )
    }

    factory<NavigationStrategy<ResultsNavigationAction>> {
        SharedNavigationStrategy(get())
    }

    viewModelOf(::ResultsViewModel)
}
