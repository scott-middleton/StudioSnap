package com.middleton.studiosnap.feature.paywall.presentation.action

import com.middleton.studiosnap.feature.paywall.presentation.navigation.PaywallNavigationAction
import com.middleton.studiosnap.feature.paywall.presentation.ui_state.TokenPack

sealed interface PaywallUiAction {
    data class NavigationAction(val navigationAction: PaywallNavigationAction) : PaywallUiAction
    data class SelectPack(val pack: TokenPack) : PaywallUiAction
    data object ConfirmPurchase : PaywallUiAction
    data object DismissError : PaywallUiAction
    data object SignIn : PaywallUiAction
    data class OnSignInResult(val success: Boolean) : PaywallUiAction
    data object DismissSignInSuccess : PaywallUiAction
}
