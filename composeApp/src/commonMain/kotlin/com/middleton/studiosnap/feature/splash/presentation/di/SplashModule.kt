package com.middleton.studiosnap.feature.splash.presentation.di

import com.middleton.studiosnap.feature.splash.presentation.viewmodel.SplashViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val splashModule = module {
    viewModelOf(::SplashViewModel)
}