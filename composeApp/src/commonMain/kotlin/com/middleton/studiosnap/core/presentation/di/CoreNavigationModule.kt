package com.middleton.studiosnap.core.presentation.di

import com.middleton.studiosnap.core.presentation.navigation.NavControllerNavigationHandler
import com.middleton.studiosnap.core.presentation.navigation.NavigationHandler
import com.middleton.studiosnap.core.presentation.navigation.SharedNavigationManager
import org.koin.dsl.module

val coreNavigationModule = module {
    single { NavControllerNavigationHandler() }
    single<NavigationHandler> { get<NavControllerNavigationHandler>() }
    single { SharedNavigationManager(get()) }
}
