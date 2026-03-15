package com.middleton.studiosnap.feature.onboarding.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.middleton.studiosnap.core.presentation.navigation.NavigationHandler
import com.middleton.studiosnap.core.presentation.theme.AppColors
import com.middleton.studiosnap.feature.onboarding.presentation.action.OnboardingUiAction
import com.middleton.studiosnap.feature.onboarding.presentation.viewmodel.OnboardingViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OnboardingCarouselScreen() {
    val viewModel: OnboardingViewModel = koinViewModel()
    val navigationHandler: NavigationHandler = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val navigationEvent by viewModel.navigationEvent.collectAsState()

    val pagerState = rememberPagerState(
        initialPage = uiState.currentPage,
        pageCount = { OnboardingViewModel.TOTAL_PAGES }
    )

    // Handle navigation events (Get Started -> Home)
    LaunchedEffect(navigationEvent) {
        navigationEvent?.let { action ->
            navigationHandler.handleNavigation(action.navigationCommand)
            viewModel.handleAction(OnboardingUiAction.OnNavigationHandled)
        }
    }

    // Sync swipe -> ViewModel
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { page ->
                if (page != uiState.currentPage) {
                    viewModel.handleAction(OnboardingUiAction.NavigateToPage(page))
                }
            }
    }

    // Sync ViewModel -> pager (button presses)
    LaunchedEffect(uiState.currentPage) {
        if (pagerState.currentPage != uiState.currentPage) {
            pagerState.animateScrollToPage(uiState.currentPage)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.DarkBackground)
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> OnboardingHeroPage(
                    onNext = { viewModel.handleAction(OnboardingUiAction.NextPage) }
                )
                1 -> OnboardingBeforeAfterPage(
                    isPageSettled = !pagerState.isScrollInProgress && pagerState.currentPage == page,
                    onNext = { viewModel.handleAction(OnboardingUiAction.NextPage) }
                )
                2 -> OnboardingStyleShowcasePage(
                    onNext = { viewModel.handleAction(OnboardingUiAction.NextPage) }
                )
                3 -> OnboardingValuePage(
                    onGetStarted = { viewModel.handleAction(OnboardingUiAction.GetStarted) }
                )
            }
        }

        // Page indicator dots
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(OnboardingViewModel.TOTAL_PAGES) { index ->
                val isSelected = pagerState.currentPage == index
                val dotAlpha by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.3f,
                    animationSpec = tween(300),
                    label = "dot_alpha_$index"
                )
                val dotScale by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.7f,
                    animationSpec = tween(300),
                    label = "dot_scale_$index"
                )

                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(dotScale)
                        .alpha(dotAlpha)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) AppColors.PrimaryGreen 
                            else AppColors.DarkTextTertiary
                        )
                )
            }
        }
    }
}
