package com.middleton.studiosnap.feature.processing.presentation.di

import com.middleton.studiosnap.core.presentation.navigation.NavigationStrategy
import com.middleton.studiosnap.core.presentation.navigation.SharedNavigationStrategy
import com.middleton.studiosnap.feature.processing.data.GenerationResultsHolderImpl
import com.middleton.studiosnap.feature.processing.domain.usecase.GenerateBatchPreviewsUseCase
import com.middleton.studiosnap.feature.processing.domain.usecase.GeneratePreviewUseCase
import com.middleton.studiosnap.feature.processing.domain.usecase.GenerationResultsHolder
import com.middleton.studiosnap.feature.processing.presentation.navigation.ProcessingNavigationAction
import com.middleton.studiosnap.feature.processing.presentation.viewmodel.ProcessingViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val processingModule = module {

    single<GenerationResultsHolder> { GenerationResultsHolderImpl() }

    factory {
        GeneratePreviewUseCase(
            generationRepository = get(),
            historyRepository = get(),
            errorReporter = get()
        )
    }

    factory {
        GenerateBatchPreviewsUseCase(
            generatePreviewUseCase = get()
        )
    }

    factory<NavigationStrategy<ProcessingNavigationAction>> {
        SharedNavigationStrategy(get())
    }

    viewModelOf(::ProcessingViewModel)
}
