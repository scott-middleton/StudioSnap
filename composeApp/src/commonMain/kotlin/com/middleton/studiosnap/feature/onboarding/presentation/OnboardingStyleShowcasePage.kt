package com.middleton.studiosnap.feature.onboarding.presentation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.middleton.studiosnap.core.presentation.theme.AppColors
import com.middleton.studiosnap.core.presentation.theme.LocalExtendedColorScheme
import com.middleton.studiosnap.core.presentation.theme.studioSnapTextStyles
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.next_button
import studiosnap.composeapp.generated.resources.onboarding_showcase_headline
import studiosnap.composeapp.generated.resources.onboarding_showcase_subheadline
import studiosnap.composeapp.generated.resources.onboarding_style_clean_white
import studiosnap.composeapp.generated.resources.onboarding_style_concrete_minimal
import studiosnap.composeapp.generated.resources.onboarding_style_marble_luxe
import studiosnap.composeapp.generated.resources.onboarding_style_paper_craft
import studiosnap.composeapp.generated.resources.onboarding_style_rustic_wood
import studiosnap.composeapp.generated.resources.onboarding_style_warm_linen
import studiosnap.composeapp.generated.resources.style_clean_white
import studiosnap.composeapp.generated.resources.style_concrete_minimal
import studiosnap.composeapp.generated.resources.style_marble_luxe
import studiosnap.composeapp.generated.resources.style_paper_craft
import studiosnap.composeapp.generated.resources.style_rustic_wood
import studiosnap.composeapp.generated.resources.style_warm_linen

private val CardShape = RoundedCornerShape(16.dp)
private val ButtonShape = RoundedCornerShape(18.dp)

@Composable
fun OnboardingStyleShowcasePage(
    onNext: () -> Unit
) {
    val extendedColors = LocalExtendedColorScheme.current

    data class StyleItem(
        val image: DrawableResource,
        val label: String
    )

    val styles = listOf(
        StyleItem(
            image = Res.drawable.onboarding_style_clean_white,
            label = stringResource(Res.string.style_clean_white)
        ),
        StyleItem(
            image = Res.drawable.onboarding_style_warm_linen,
            label = stringResource(Res.string.style_warm_linen)
        ),
        StyleItem(
            image = Res.drawable.onboarding_style_marble_luxe,
            label = stringResource(Res.string.style_marble_luxe)
        ),
        StyleItem(
            image = Res.drawable.onboarding_style_rustic_wood,
            label = stringResource(Res.string.style_rustic_wood)
        ),
        StyleItem(
            image = Res.drawable.onboarding_style_paper_craft,
            label = stringResource(Res.string.style_paper_craft)
        ),
        StyleItem(
            image = Res.drawable.onboarding_style_concrete_minimal,
            label = stringResource(Res.string.style_concrete_minimal)
        )
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.systemBars)
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(Res.string.onboarding_showcase_headline),
                style = studioSnapTextStyles().onboardingHeadline,
                color = AppColors.Ink,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.onboarding_showcase_subheadline),
                style = studioSnapTextStyles().onboardingSubheadline,
                color = AppColors.Ink.copy(alpha = 0.5f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                userScrollEnabled = false
            ) {
                itemsIndexed(styles) { index, style ->
                    AnimatedStyleCard(
                        image = style.image,
                        label = style.label,
                        index = index
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 42.dp)
                .fillMaxWidth(0.85f)
                .height(50.dp),
            shape = ButtonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            ),
            contentPadding = PaddingValues(),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = stringResource(Res.string.next_button),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.2).sp,
                color = AppColors.PrimaryGreen
            )
        }
    }
}

@Composable
private fun AnimatedStyleCard(
    image: DrawableResource,
    label: String,
    index: Int
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(index * 150L)
        isVisible = true
    }

    val scale by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "card_scale_$index"
    )

    val opacity by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "card_opacity_$index"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(opacity),
        shape = CardShape,
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column {
            Image(
                painter = painterResource(image),
                contentDescription = label,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f),
                contentScale = ContentScale.Crop
            )
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.Ink,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            )
        }
    }
}
