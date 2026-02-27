package com.middleton.studiosnap.feature.paywall.presentation.ui_state

import com.middleton.studiosnap.core.domain.model.UiText
import com.revenuecat.purchases.kmp.models.StoreProduct

data class PaywallUiState(
    val tokenPacks: List<TokenPack> = emptyList(),
    val selectedPack: TokenPack? = null,
    val currentCredits: Int = 0,
    val isLoading: Boolean = true,
    val isPurchasing: Boolean = false,
    val error: UiText? = null,
    val showSignIn: Boolean = false,
    val isSignInForPurchase: Boolean = false,
    val isSigningIn: Boolean = false,
    val isSignedIn: Boolean = false,
    val isPostTrial: Boolean = false,
    val signInSuccess: Boolean = false
)

data class TokenPack(
    val storeProduct: StoreProduct,
    val grantedCredits: Int,
    val restorations: Int = grantedCredits,
    val isMostPopular: Boolean = false,
    val isBestValue: Boolean = false
)
