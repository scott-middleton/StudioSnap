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
import studiosnap.composeapp.generated.resources.onboarding_style_dark
import studiosnap.composeapp.generated.resources.onboarding_style_minimal
import studiosnap.composeapp.generated.resources.onboarding_style_outdoor
import studiosnap.composeapp.generated.resources.onboarding_style_warm

private val CardShape = RoundedCornerShape(12.dp)
private val ButtonShape = RoundedCornerShape(20.dp)

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
            image = Res.drawable.onboarding_style_warm,
            label = stringResource(Res.string.onboarding_style_warm)
        ),
        StyleItem(
            image = Res.drawable.onboarding_style_dark,
            label = stringResource(Res.string.onboarding_style_dark)
        ),
        StyleItem(
            image = Res.drawable.onboarding_style_outdoor,
            label = stringResource(Res.string.onboarding_style_outdoor)
        ),
        StyleItem(
            image = Res.drawable.onboarding_style_minimal,
            label = stringResource(Res.string.onboarding_style_minimal)
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
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = stringResource(Res.string.onboarding_showcase_headline),
                style = studioSnapTextStyles.onboardingHeadline,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(Res.string.onboarding_showcase_subheadline),
                style = studioSnapTextStyles.onboardingSubheadline,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
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

            Spacer(modifier = Modifier.weight(1f))
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(bottom = 42.dp)
                .fillMaxWidth(0.55f)
                .height(44.dp),
            shape = ButtonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.White
            ),
            contentPadding = PaddingValues(),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = stringResource(Res.string.next_button),
                style = studioSnapTextStyles.buttonText.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                ),
                color = extendedColors.restore.color
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
            containerColor = Color.Black.copy(alpha = 0.3f)
        )
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
                style = studioSnapTextStyles.captionText.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }
    }
}
