package com.middleton.studiosnap.feature.auth.presentation.di

import com.middleton.studiosnap.feature.auth.domain.usecase.SignInUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val authModule = module {
    factoryOf(::SignInUseCase)
}
