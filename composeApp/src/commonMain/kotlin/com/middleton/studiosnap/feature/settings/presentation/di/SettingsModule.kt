package com.middleton.studiosnap.feature.settings.presentation.di

import com.middleton.studiosnap.core.presentation.navigation.NavigationStrategy
import com.middleton.studiosnap.core.presentation.navigation.SharedNavigationStrategy
import com.middleton.studiosnap.feature.settings.presentation.navigation.SettingsNavigationAction
import com.middleton.studiosnap.feature.settings.presentation.viewmodel.SettingsViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsModule = module {

    factory<NavigationStrategy<SettingsNavigationAction>> {
        SharedNavigationStrategy(get())
    }

    viewModelOf(::SettingsViewModel)
}
