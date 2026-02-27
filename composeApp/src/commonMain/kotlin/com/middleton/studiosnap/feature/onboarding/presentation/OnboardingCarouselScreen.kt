package com.middleton.studiosnap.feature.onboarding.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.middleton.studiosnap.core.presentation.theme.LocalExtendedColorScheme
import com.middleton.studiosnap.feature.onboarding.presentation.action.OnboardingUiAction
import com.middleton.studiosnap.feature.onboarding.presentation.viewmodel.OnboardingViewModel
import com.middleton.studiosnap.feature.onboarding.presentation.viewmodel.OnboardingViewModel.Companion.TOTAL_PAGES
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OnboardingCarouselScreen() {
    val extendedColors = LocalExtendedColorScheme.current

    val viewModel: OnboardingViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    val pagerState = rememberPagerState(
        initialPage = uiState.currentPage,
        pageCount = { OnboardingViewModel.TOTAL_PAGES }
    )

    LaunchedEffect(uiState.currentPage) {
        if (pagerState.currentPage != uiState.currentPage) {
            pagerState.animateScrollToPage(uiState.currentPage)
        }
    }

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
            val isPageSettled = !pagerState.isScrollInProgress &&
                pagerState.currentPage == page

            when (page) {
                0 -> OnboardingHeroPage(
                    onNext = {
                        viewModel.handleAction(OnboardingUiAction.NextPage)
                    }
                )
                1 -> OnboardingDemoPage(
                    demoExamples = uiState.demoSet1,
                    isPageSettled = isPageSettled,
                    onNext = {
                        viewModel.handleAction(OnboardingUiAction.NextPage)
                    }
                )
                2 -> OnboardingDemoPage(
                    demoExamples = uiState.demoSet2,
                    isPageSettled = isPageSettled,
                    onNext = {
                        viewModel.handleAction(OnboardingUiAction.NextPage)
                    }
                )
                3 -> OnboardingDemoPage(
                    demoExamples = uiState.demoSet3,
                    isPageSettled = isPageSettled,
                    onNext = {
                        viewModel.handleAction(OnboardingUiAction.NextPage)
                    }
                )
                4 -> OnboardingValuePage(
                    animationTrigger = uiState.valuePageAnimationTrigger,
                    onGetStarted = {
                        viewModel.handleAction(OnboardingUiAction.GetStarted)
                    }
                )
            }
        }
        
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
                        .background(Color.White)
                )
            }
        }
    }
}

