package com.middleton.studiosnap.core.presentation.di

import androidx.navigation.NavHostController
import com.middleton.studiosnap.core.presentation.navigation.NavigationHandler
import com.middleton.studiosnap.core.presentation.navigation.NavControllerNavigationHandler
import com.middleton.studiosnap.core.presentation.navigation.SharedNavigationManager
import org.koin.dsl.module

fun coreNavigationModule(navController: NavHostController) = module {
    single<NavHostController> { navController }
    single<NavigationHandler> { NavControllerNavigationHandler(get()) }
    single { SharedNavigationManager(get()) }
}