package com.middleton.studiosnap.feature.paywall.presentation.di

import com.middleton.studiosnap.core.presentation.navigation.NavigationStrategy
import com.middleton.studiosnap.core.presentation.navigation.SharedNavigationStrategy
import com.middleton.studiosnap.feature.paywall.presentation.navigation.PaywallNavigationAction
import com.middleton.studiosnap.feature.paywall.presentation.viewmodel.PaywallViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val paywallModule = module {

    factory<NavigationStrategy<PaywallNavigationAction>> {
        SharedNavigationStrategy(get())
    }

    viewModelOf(::PaywallViewModel)
}
