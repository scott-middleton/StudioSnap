package com.middleton.studiosnap.feature.onboarding.presentation

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.middleton.studiosnap.core.presentation.navigation.NavigationHandler
import com.middleton.studiosnap.core.presentation.theme.AppColors
import com.middleton.studiosnap.core.presentation.theme.extendedColorScheme
import com.middleton.studiosnap.feature.onboarding.presentation.action.OnboardingUiAction
import com.middleton.studiosnap.feature.onboarding.presentation.viewmodel.OnboardingViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.onboarding_jewelry_after
import studiosnap.composeapp.generated.resources.onboarding_jewelry_before

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
                if (page == OnboardingViewModel.TOTAL_PAGES - 1) {
                    viewModel.handleAction(OnboardingUiAction.TriggerValuePageAnimation)
                }
            }
    }

    // Sync ViewModel -> pager (button presses)
    LaunchedEffect(uiState.currentPage) {
        if (pagerState.currentPage != uiState.currentPage) {
            pagerState.animateScrollToPage(uiState.currentPage)
        }
    }

    val density = LocalDensity.current
    val glowRadius = with(density) { 150.dp.toPx() }
    val glowOffsetY = with(density) { -60.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Soft green glow at top
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AppColors.PrimaryGreen.copy(alpha = 0.09f),
                        Color.Transparent
                    ),
                    center = Offset(size.width / 2f, glowOffsetY),
                    radius = glowRadius
                ),
                center = Offset(size.width / 2f, glowOffsetY),
                radius = glowRadius
            )
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> OnboardingBeforeAfterPage(
                    isPageSettled = !pagerState.isScrollInProgress && pagerState.currentPage == page,
                    beforeImage = Res.drawable.onboarding_jewelry_before,
                    afterImage = Res.drawable.onboarding_jewelry_after,
                    keyIndex = 0,
                    onNext = { viewModel.handleAction(OnboardingUiAction.NextPage) }
                )
                1 -> OnboardingBeforeAfterPage(
                    isPageSettled = !pagerState.isScrollInProgress && pagerState.currentPage == page,
                    keyIndex = 1,
                    onNext = { viewModel.handleAction(OnboardingUiAction.NextPage) }
                )
                2 -> OnboardingStyleShowcasePage(
                    onNext = { viewModel.handleAction(OnboardingUiAction.NextPage) }
                )
                3 -> OnboardingValuePage(
                    animationTrigger = uiState.valuePageAnimationTrigger,
                    onGetStarted = { viewModel.handleAction(OnboardingUiAction.GetStarted) }
                )
            }
        }

        // Page indicator dots (pill shape)
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(OnboardingViewModel.TOTAL_PAGES) { index ->
                val isSelected = pagerState.currentPage == index
                val dotWidth by animateDpAsState(
                    targetValue = if (isSelected) 20.dp else 6.dp,
                    animationSpec = tween(300),
                    label = "dot_width_$index"
                )
                val dotScale by animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.85f,
                    animationSpec = tween(300),
                    label = "dot_scale_$index"
                )

                Box(
                    modifier = Modifier
                        .height(6.dp)
                        .width(dotWidth)
                        .scale(dotScale)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                            if (isSelected) AppColors.PrimaryGreen
                            else extendedColorScheme().ink10
                        )
                )
            }
        }
    }
}
