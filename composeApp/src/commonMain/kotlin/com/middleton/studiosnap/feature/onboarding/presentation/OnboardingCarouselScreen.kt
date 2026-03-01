package com.middleton.studiosnap.feature.onboarding.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.middleton.studiosnap.core.presentation.theme.LocalExtendedColorScheme
import com.middleton.studiosnap.core.presentation.navigation.NavigationHandler
import com.middleton.studiosnap.feature.onboarding.presentation.action.OnboardingUiAction
import com.middleton.studiosnap.feature.onboarding.presentation.viewmodel.OnboardingViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

/**
 * Stub — will be fully reimplemented for StudioSnap onboarding
 * (product photo style demos instead of restoration demos).
 */
@Composable
fun OnboardingCarouselScreen() {
    val extendedColors = LocalExtendedColorScheme.current
    val viewModel: OnboardingViewModel = koinViewModel()
    val navigationHandler: NavigationHandler = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val navigationEvent by viewModel.navigationEvent.collectAsState()

    val pagerState = rememberPagerState(
        initialPage = uiState.currentPage,
        pageCount = { OnboardingViewModel.TOTAL_PAGES }
    )

    // Handle navigation events (Get Started → Home)
    LaunchedEffect(navigationEvent) {
        navigationEvent?.let { action ->
            navigationHandler.handleNavigation(action.navigationCommand)
            viewModel.handleAction(OnboardingUiAction.OnNavigationHandled)
        }
    }

    // Sync swipe → ViewModel
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                if (page != uiState.currentPage) {
                    viewModel.handleAction(OnboardingUiAction.NavigateToPage(page))
                }
            }
    }

    // Sync ViewModel → pager (button presses)
    LaunchedEffect(uiState.currentPage) {
        if (pagerState.currentPage != uiState.currentPage) {
            pagerState.animateScrollToPage(uiState.currentPage)
        }
    }

    val backgroundBrush = remember(extendedColors) {
        Brush.verticalGradient(
            colors = listOf(
                extendedColors.restore.color,
                extendedColors.processing.color
            )
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            // Page layout: [0] Hero → [1..N-2] Demo pages → [N-1] Value/CTA
            // Requires TOTAL_PAGES >= 3 for at least one demo page.
            check(OnboardingViewModel.TOTAL_PAGES >= 3) { "Onboarding requires at least 3 pages" }
            when (page) {
                0 -> OnboardingHeroPage(
                    onNext = { viewModel.handleAction(OnboardingUiAction.NextPage) }
                )
                OnboardingViewModel.TOTAL_PAGES - 1 -> OnboardingValuePage(
                    onGetStarted = { viewModel.handleAction(OnboardingUiAction.GetStarted) }
                )
                else -> OnboardingDemoPage()
            }
        }
    }
}
