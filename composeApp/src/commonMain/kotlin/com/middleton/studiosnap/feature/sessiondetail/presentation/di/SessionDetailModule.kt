package com.middleton.studiosnap.feature.sessiondetail.presentation.di

import com.middleton.studiosnap.core.presentation.navigation.NavigationStrategy
import com.middleton.studiosnap.core.presentation.navigation.SharedNavigationStrategy
import com.middleton.studiosnap.feature.sessiondetail.presentation.navigation.SessionDetailNavigationAction
import com.middleton.studiosnap.feature.sessiondetail.presentation.viewmodel.SessionDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val sessionDetailModule = module {

    factory<NavigationStrategy<SessionDetailNavigationAction>> {
        SharedNavigationStrategy(get())
    }

    viewModel { params ->
        SessionDetailViewModel(
            sessionId = params.get(),
            historyRepository = get()
        )
    }
}
