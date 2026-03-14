package com.middleton.studiosnap.feature.paywall.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex
import com.middleton.studiosnap.core.presentation.theme.AppColors
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.middleton.studiosnap.core.presentation.components.NativeSignInEffect
import com.middleton.studiosnap.core.presentation.theme.LocalExtendedColorScheme
import com.middleton.studiosnap.feature.paywall.presentation.action.PaywallUiAction
import com.middleton.studiosnap.feature.paywall.presentation.navigation.PaywallNavigationAction
import com.middleton.studiosnap.feature.paywall.presentation.ui_state.PaywallUiState
import com.middleton.studiosnap.feature.paywall.presentation.ui_state.TokenPack
import com.middleton.studiosnap.feature.paywall.presentation.viewmodel.PaywallViewModel
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.content_close
import studiosnap.composeapp.generated.resources.paywall_already_have_account
import studiosnap.composeapp.generated.resources.paywall_best_value
import studiosnap.composeapp.generated.resources.paywall_continue
import studiosnap.composeapp.generated.resources.paywall_headline_topup
import studiosnap.composeapp.generated.resources.paywall_headline_trial
import studiosnap.composeapp.generated.resources.paywall_most_popular
import studiosnap.composeapp.generated.resources.paywall_credits_remaining
import studiosnap.composeapp.generated.resources.paywall_per_restoration
import studiosnap.composeapp.generated.resources.paywall_restorations
import studiosnap.composeapp.generated.resources.paywall_select_pack
import studiosnap.composeapp.generated.resources.paywall_sign_in_success
import studiosnap.composeapp.generated.resources.paywall_subtitle
import org.jetbrains.compose.resources.stringResource
import com.middleton.studiosnap.core.presentation.util.SystemBarsAppearance
import org.koin.compose.viewmodel.koinViewModel

// Pre-allocated
private val BackgroundColor = Color(0xFF0A0A0F)
private val CardShape = RoundedCornerShape(16.dp)
private val BadgeShape = RoundedCornerShape(8.dp)
private val ButtonShape = RoundedCornerShape(28.dp)

@Composable
fun PaywallScreen() {
    val viewModel: PaywallViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    SystemBarsAppearance(lightIcons = true)

    NativeSignInEffect(
        showSignIn = uiState.showSignIn,
        onResult = { success -> viewModel.handleAction(PaywallUiAction.OnSignInResult(success)) }
    )

    PaywallScreenContent(uiState = uiState, onAction = viewModel::handleAction)
}

@Composable
private fun PaywallScreenContent(
    uiState: PaywallUiState,
    onAction: (PaywallUiAction) -> Unit
) {
    val extendedColors = LocalExtendedColorScheme.current
    val snackbarHostState = remember { SnackbarHostState() }

    val errorMessage = when (val error = uiState.error) {
        is com.middleton.studiosnap.core.domain.model.UiText.StringResource -> stringResource(error.resId)
        is com.middleton.studiosnap.core.domain.model.UiText.DynamicString -> error.value
        null -> null
    }

    val signInSuccessMessage = stringResource(Res.string.paywall_sign_in_success)

    LaunchedEffect(uiState.error) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            onAction(PaywallUiAction.DismissError)
        }
    }

    LaunchedEffect(uiState.signInSuccess) {
        if (uiState.signInSuccess) {
            snackbarHostState.showSnackbar(signInSuccessMessage)
            onAction(PaywallUiAction.DismissSignInSuccess)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Close button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp)
            ) {
                IconButton(
                    onClick = { onAction(PaywallUiAction.NavigationAction(PaywallNavigationAction.NavigateBack)) },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(Res.string.content_close),
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            // Current credits
            if (uiState.currentCredits > 0) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(Res.string.paywall_credits_remaining, uiState.currentCredits),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Headline — context-aware
            Text(
                text = stringResource(
                    if (uiState.isPostTrial) Res.string.paywall_headline_trial
                    else Res.string.paywall_headline_topup
                ),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 36.sp
                ),
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = stringResource(Res.string.paywall_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            // Pack cards
            if (uiState.isLoading && uiState.tokenPacks.isEmpty()) {
                CircularProgressIndicator(
                    color = AppColors.PrimaryBlue,
                    modifier = Modifier.size(32.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    uiState.tokenPacks.forEach { pack ->
                        PackCard(
                            pack = pack,
                            isSelected = pack == uiState.selectedPack,
                            accentColor = AppColors.PrimaryBlue,
                            onSelect = { onAction(PaywallUiAction.SelectPack(pack)) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom CTA area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val hasSelection = uiState.selectedPack != null

                val isBusy = uiState.isPurchasing || uiState.isSigningIn
                Button(
                    onClick = { onAction(PaywallUiAction.ConfirmPurchase) },
                    enabled = hasSelection && !isBusy,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = ButtonShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.PrimaryBlue,
                        disabledContainerColor = AppColors.PrimaryBlue.copy(alpha = 0.4f)
                    )
                ) {
                    if (isBusy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = if (hasSelection) {
                                stringResource(
                                    Res.string.paywall_continue,
                                    uiState.selectedPack!!.storeProduct.price.formatted
                                )
                            } else {
                                stringResource(Res.string.paywall_select_pack)
                            },
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color.White
                        )
                    }
                }

                if (!uiState.isSignedIn) {
                    TextButton(
                        onClick = { onAction(PaywallUiAction.SignIn) }
                    ) {
                        Text(
                            text = stringResource(Res.string.paywall_already_have_account),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.6f),
                            textDecoration = TextDecoration.Underline
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun PackCard(
    pack: TokenPack,
    isSelected: Boolean,
    accentColor: Color,
    onSelect: () -> Unit
) {
    val selectionAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(200),
        label = "selection"
    )

    val cardBackground = Color.White.copy(alpha = 0.06f + 0.04f * selectionAlpha)
    val borderColor = accentColor.copy(alpha = selectionAlpha * 0.8f)

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShape)
                .background(cardBackground, CardShape)
                .then(
                    if (isSelected) Modifier.border(1.5.dp, borderColor, CardShape)
                    else Modifier.border(1.dp, Color.White.copy(alpha = 0.08f), CardShape)
                )
                .clickable { onSelect() }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Credits + restorations
            Column(modifier = Modifier.weight(1f)) {
                Row {
                    Text(
                        text = "${pack.grantedCredits}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White,
                        modifier = Modifier.alignByBaseline()
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(Res.string.paywall_restorations),
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.alignByBaseline()
                    )
                }

                // Per-restoration price
                val pricePerRestoration = calculatePerRestorationPrice(pack)
                if (pricePerRestoration != null) {
                    Text(
                        text = "$pricePerRestoration ${stringResource(Res.string.paywall_per_restoration)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            // Price
            Text(
                text = pack.storeProduct.price.formatted,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (isSelected) accentColor else Color.White
            )
        }

        // Badge rendered after the card so it draws on top of the border
        if (pack.isMostPopular || pack.isBestValue) {
            val badgeText = stringResource(
                if (pack.isMostPopular) Res.string.paywall_most_popular
                else Res.string.paywall_best_value
            )
            val badgeColor = if (pack.isMostPopular) accentColor else Color(0xFFFF9800)

            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                ),
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .zIndex(1f)
                    .padding(end = 16.dp)
                    .graphicsLayer { translationY = -4.dp.toPx() }
                    .background(badgeColor, BadgeShape)
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            )
        }
    }
}

private fun calculatePerRestorationPrice(pack: TokenPack): String? {
    val priceAmountMicros = pack.storeProduct.price.amountMicros
    if (priceAmountMicros <= 0 || pack.restorations <= 0) return null
    val perRestoration = priceAmountMicros.toDouble() / pack.restorations / 1_000_000.0
    val formatted = perRestoration.asTwoDecimalString()
    val currencyCode = pack.storeProduct.price.currencyCode
    return when (currencyCode) {
        "GBP" -> "£$formatted"
        "USD" -> "$$formatted"
        "EUR" -> "€$formatted"
        else -> "$formatted $currencyCode"
    }
}

private fun Double.asTwoDecimalString(): String {
    val whole = toLong()
    val fraction = ((this - whole) * 100).toLong()
    return "$whole.${fraction.toString().padStart(2, '0')}"
}
