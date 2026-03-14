package com.middleton.studiosnap.feature.home.presentation.di

import com.middleton.studiosnap.core.presentation.navigation.NavigationStrategy
import com.middleton.studiosnap.core.presentation.navigation.SharedNavigationStrategy
import com.middleton.studiosnap.feature.home.data.repository.GenerationConfigHolderImpl
import com.middleton.studiosnap.feature.home.data.repository.GenerationRepositoryImpl
import com.middleton.studiosnap.feature.home.data.repository.StyleRepositoryImpl
import com.middleton.studiosnap.feature.home.domain.repository.GenerationConfigHolder
import com.middleton.studiosnap.feature.home.domain.repository.GenerationRepository
import com.middleton.studiosnap.feature.home.domain.repository.StyleRepository
import com.middleton.studiosnap.feature.home.domain.usecase.BuildKontextPromptUseCase
import com.middleton.studiosnap.feature.home.presentation.navigation.HomeNavigationAction
import com.middleton.studiosnap.feature.home.presentation.viewmodel.HomeViewModel
import com.middleton.studiosnap.feature.home.presentation.viewmodel.StylePickerViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homeModule = module {

    single<StyleRepository> { StyleRepositoryImpl() }

    single<GenerationConfigHolder> { GenerationConfigHolderImpl() }

    single<GenerationRepository> {
        GenerationRepositoryImpl(
            kontextDataSource = get(),
            imageCacheManager = get(),
            buildKontextPromptUseCase = get(),
            generationDao = get()
        )
    }

    factory { BuildKontextPromptUseCase() }

    factory<NavigationStrategy<HomeNavigationAction>> {
        SharedNavigationStrategy(get())
    }

    viewModelOf(::HomeViewModel)
    viewModelOf(::StylePickerViewModel)
}
