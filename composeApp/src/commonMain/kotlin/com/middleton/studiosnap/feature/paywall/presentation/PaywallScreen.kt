package com.middleton.studiosnap.feature.paywall.presentation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.middleton.studiosnap.core.presentation.components.GradientButton
import com.middleton.studiosnap.core.presentation.components.NativeSignInEffect
import com.middleton.studiosnap.core.presentation.theme.AppColors
import com.middleton.studiosnap.core.presentation.util.SystemBarsAppearance
import com.middleton.studiosnap.core.presentation.util.asString
import com.middleton.studiosnap.feature.paywall.presentation.action.PaywallUiAction
import com.middleton.studiosnap.feature.paywall.presentation.navigation.PaywallNavigationAction
import com.middleton.studiosnap.feature.paywall.presentation.ui_state.PaywallUiState
import com.middleton.studiosnap.feature.paywall.presentation.ui_state.TokenPack
import com.middleton.studiosnap.feature.paywall.presentation.viewmodel.PaywallViewModel
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.content_close
import studiosnap.composeapp.generated.resources.ic_diamond
import studiosnap.composeapp.generated.resources.paywall_already_have_account
import studiosnap.composeapp.generated.resources.paywall_best_value
import studiosnap.composeapp.generated.resources.paywall_continue
import studiosnap.composeapp.generated.resources.paywall_credits_remaining
import studiosnap.composeapp.generated.resources.paywall_headline_topup
import studiosnap.composeapp.generated.resources.paywall_headline_post_trial
import studiosnap.composeapp.generated.resources.paywall_most_popular
import studiosnap.composeapp.generated.resources.paywall_per_restoration
import studiosnap.composeapp.generated.resources.paywall_restorations
import studiosnap.composeapp.generated.resources.paywall_select_pack
import studiosnap.composeapp.generated.resources.paywall_sign_in_success
import studiosnap.composeapp.generated.resources.paywall_subtitle

private val CardShape = RoundedCornerShape(16.dp)
private val BadgeShape = RoundedCornerShape(8.dp)

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
fun PaywallScreenContent(
    uiState: PaywallUiState,
    onAction: (PaywallUiAction) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    val errorMessage = uiState.error?.asString()
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
            .background(MaterialTheme.colorScheme.background)
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
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
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
                        painter = painterResource(Res.drawable.ic_diamond),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = stringResource(Res.string.paywall_credits_remaining, uiState.currentCredits),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Headline — context-aware
            Text(
                text = stringResource(
                    if (uiState.isPostTrial) Res.string.paywall_headline_post_trial
                    else Res.string.paywall_headline_topup
                ),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 36.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle
            Text(
                text = stringResource(Res.string.paywall_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            // Pack cards
            if (uiState.isLoading && uiState.tokenPacks.isEmpty()) {
                CircularProgressIndicator(
                    color = AppColors.PrimaryGreen,
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
                            onSelect = { onAction(PaywallUiAction.SelectPack(pack)) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom CTA area
            BottomCtaSection(uiState = uiState, onAction = onAction)
        }

        // Snackbar
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

// ─── Pack Card ──────────────────────────────────────────────────────────────

@Composable
private fun PackCard(
    pack: TokenPack,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val selectionAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(200),
        label = "selection"
    )

    val cardBackground = MaterialTheme.colorScheme.surfaceVariant.copy(
        alpha = 0.3f + 0.2f * selectionAlpha
    )
    val borderColor = AppColors.PrimaryGreen.copy(alpha = selectionAlpha * 0.8f)
    val unselectedBorder = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShape)
                .background(cardBackground, CardShape)
                .then(
                    if (isSelected) Modifier.border(1.5.dp, borderColor, CardShape)
                    else Modifier.border(1.dp, unselectedBorder, CardShape)
                )
                .clickable { onSelect() }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Credits + per-photo price
            Column(modifier = Modifier.weight(1f)) {
                Row {
                    Text(
                        text = "${pack.grantedCredits}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.alignByBaseline()
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(Res.string.paywall_restorations),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.alignByBaseline()
                    )
                }

                val pricePerRestoration = calculatePerRestorationPrice(pack)
                if (pricePerRestoration != null) {
                    Text(
                        text = "$pricePerRestoration ${stringResource(Res.string.paywall_per_restoration)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Price
            Text(
                text = pack.storeProduct.price.formatted,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = if (isSelected) AppColors.PrimaryGreen else MaterialTheme.colorScheme.onSurface
            )
        }

        // Badge
        if (pack.isMostPopular || pack.isBestValue) {
            val badgeText = stringResource(
                if (pack.isMostPopular) Res.string.paywall_most_popular
                else Res.string.paywall_best_value
            )
            val badgeColor = if (pack.isMostPopular) AppColors.PrimaryGreen else AppColors.Warning

            Text(
                text = badgeText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                ),
                color = MaterialTheme.colorScheme.onPrimary,
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

// ─── Bottom CTA ─────────────────────────────────────────────────────────────

@Composable
private fun BottomCtaSection(
    uiState: PaywallUiState,
    onAction: (PaywallUiAction) -> Unit
) {
    val hasSelection = uiState.selectedPack != null
    val isBusy = uiState.isPurchasing || uiState.isSigningIn

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .windowInsetsPadding(WindowInsets.navigationBars),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isBusy) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = AppColors.PrimaryGreen,
                    strokeWidth = 2.5.dp
                )
            }
        } else {
            val buttonText = uiState.selectedPack?.let { pack ->
                stringResource(Res.string.paywall_continue, pack.storeProduct.price.formatted)
            } ?: stringResource(Res.string.paywall_select_pack)

            GradientButton(
                text = buttonText,
                onClick = { onAction(PaywallUiAction.ConfirmPurchase) },
                enabled = hasSelection
            )
        }

        if (!uiState.isSignedIn) {
            TextButton(
                onClick = { onAction(PaywallUiAction.SignIn) }
            ) {
                Text(
                    text = stringResource(Res.string.paywall_already_have_account),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textDecoration = TextDecoration.Underline
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

// ─── Helpers ────────────────────────────────────────────────────────────────

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
