package com.middleton.studiosnap.feature.onboarding.presentation.di

import com.middleton.studiosnap.core.presentation.navigation.NavigationStrategy
import com.middleton.studiosnap.core.presentation.navigation.SharedNavigationStrategy
import com.middleton.studiosnap.feature.onboarding.data.repository.OnboardingDemoRepositoryImpl
import com.middleton.studiosnap.feature.onboarding.domain.repository.OnboardingDemoRepository
import com.middleton.studiosnap.feature.onboarding.presentation.navigation.OnboardingNavigationAction
import com.middleton.studiosnap.feature.onboarding.presentation.viewmodel.OnboardingViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val onboardingModule = module {

    factory<OnboardingDemoRepository> { OnboardingDemoRepositoryImpl() }

    factory<NavigationStrategy<OnboardingNavigationAction>> {
        SharedNavigationStrategy(get())
    }

    viewModelOf(::OnboardingViewModel)
}