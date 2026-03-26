package com.middleton.studiosnap.feature.paywall.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.middleton.studiosnap.core.domain.model.UiText
import com.middleton.studiosnap.core.domain.service.AnalyticsEvents
import com.middleton.studiosnap.core.domain.service.AnalyticsParams
import com.middleton.studiosnap.core.domain.service.AnalyticsService
import com.middleton.studiosnap.core.domain.repository.PurchaseRepository
import com.middleton.studiosnap.core.domain.service.AuthService
import com.middleton.studiosnap.core.domain.service.CreditManager
import com.middleton.studiosnap.core.domain.repository.UserPreferencesRepository
import com.middleton.studiosnap.core.presentation.navigation.NavigationStrategy
import com.middleton.studiosnap.feature.auth.domain.usecase.SignInUseCase
import com.middleton.studiosnap.feature.paywall.presentation.action.PaywallUiAction
import com.middleton.studiosnap.feature.paywall.presentation.navigation.PaywallNavigationAction
import com.middleton.studiosnap.feature.paywall.presentation.ui_state.PaywallUiState
import com.middleton.studiosnap.feature.paywall.presentation.ui_state.TokenPack
import studiosnap.composeapp.generated.resources.Res
import studiosnap.composeapp.generated.resources.paywall_error_load_offerings
import studiosnap.composeapp.generated.resources.paywall_error_purchase_failed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Credit Store ViewModel — handles purchasing credit packs.
 * Reused "Paywall" naming from Restorer AI for consistency with existing infrastructure.
 */
class PaywallViewModel(
    private val purchaseRepository: PurchaseRepository,
    private val creditManager: CreditManager,
    private val navigationStrategy: NavigationStrategy<PaywallNavigationAction>,
    private val authService: AuthService,
    private val signInUseCase: SignInUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    companion object {
        // Maps RevenueCat product IDs → credit grants
        private val CREDIT_GRANTS = mapOf(
            "studiosnap_pack_starter" to 5,
            "studiosnap_pack_standard" to 20,
            "studiosnap_pack_pro" to 50,
            "studiosnap_pack_studio" to 120
        )
    }

    private val _uiState = MutableStateFlow(PaywallUiState())
    val uiState: StateFlow<PaywallUiState> = _uiState.asStateFlow()

    init {
        loadOfferings()
        observeSignInState()
        loadTrialState()
        analyticsService.logEvent(AnalyticsEvents.CREDIT_STORE_VIEWED)
    }

    fun handleAction(action: PaywallUiAction) {
        when (action) {
            is PaywallUiAction.NavigationAction -> navigationStrategy.navigate(action.navigationAction)
            is PaywallUiAction.SelectPack -> {
                analyticsService.logEvent(
                    AnalyticsEvents.PACK_SELECTED,
                    mapOf(
                        AnalyticsParams.PACK_NAME to action.pack.storeProduct.id,
                        AnalyticsParams.PACK_CREDITS to action.pack.grantedCredits.toString()
                    )
                )
                _uiState.update { it.copy(selectedPack = action.pack) }
            }
            is PaywallUiAction.ConfirmPurchase -> handleConfirmPurchase()
            is PaywallUiAction.DismissError -> _uiState.update { it.copy(error = null) }
            is PaywallUiAction.SignIn -> handleSignIn()
            is PaywallUiAction.OnSignInResult -> handleSignInResult(action.success)
            is PaywallUiAction.DismissSignInSuccess -> _uiState.update { it.copy(signInSuccess = false) }
        }
    }

    private fun loadOfferings() {
        viewModelScope.launch {
            val currentCredits = creditManager.credits.value?.amount ?: 0

            val offeringsResult = purchaseRepository.getAvailableTokenPacks()
            offeringsResult.onFailure {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentCredits = currentCredits,
                        error = UiText.StringResource(Res.string.paywall_error_load_offerings)
                    )
                }
                return@launch
            }

            val sortedPacks = offeringsResult.getOrThrow()
                .mapNotNull { product ->
                    CREDIT_GRANTS[product.id]?.let { grants -> TokenPack(product, grants) }
                }
                .sortedBy { it.grantedCredits }

            // Assign badges: "Most Popular" = 2nd smallest pack (Standard 20 credits),
            // "Best Value" = largest pack. Indices assume packs sorted by grantedCredits ascending.
            // If pack lineup changes, review these assignments.
            val packs = sortedPacks.mapIndexed { index, pack ->
                pack.copy(
                    isMostPopular = sortedPacks.size >= 2 && index == 1,
                    isBestValue = sortedPacks.size >= 2 && index == sortedPacks.lastIndex
                )
            }

            val preSelected = packs.firstOrNull { it.isMostPopular }

            _uiState.update {
                it.copy(
                    tokenPacks = packs,
                    selectedPack = preSelected,
                    currentCredits = currentCredits,
                    isLoading = false
                )
            }
        }
    }

    private fun handleConfirmPurchase() {
        val pack = _uiState.value.selectedPack ?: return

        if (!authService.isSignedIn.value) {
            _uiState.update { it.copy(showSignIn = true, isSignInForPurchase = true, isSigningIn = true) }
            return
        }

        executePurchase(pack)
    }

    private fun handleSignIn() {
        _uiState.update { it.copy(showSignIn = true, isSignInForPurchase = false, isSigningIn = true) }
    }

    private fun handleSignInResult(success: Boolean) {
        _uiState.update { it.copy(showSignIn = false, isSigningIn = false) }

        if (success) {
            analyticsService.logEvent(AnalyticsEvents.SIGN_IN_COMPLETED)
            viewModelScope.launch {
                signInUseCase.execute()
                creditManager.refreshCredits()
                val currentCredits = creditManager.credits.value?.amount ?: 0

                if (currentCredits > 0) {
                    userPreferencesRepository.setHasPurchasedCredits()
                }
                _uiState.update { it.copy(currentCredits = currentCredits) }

                val isForPurchase = _uiState.value.isSignInForPurchase
                _uiState.update { it.copy(isSignInForPurchase = false) }

                if (isForPurchase) {
                    val pack = _uiState.value.selectedPack
                    if (pack != null) {
                        executePurchase(pack)
                    }
                } else {
                    _uiState.update { it.copy(signInSuccess = true) }
                }
            }
        }
    }

    private fun executePurchase(pack: TokenPack) {
        _uiState.update { it.copy(isPurchasing = true) }
        analyticsService.logEvent(
            AnalyticsEvents.PURCHASE_STARTED,
            mapOf(
                AnalyticsParams.PACK_NAME to pack.storeProduct.id,
                AnalyticsParams.PACK_CREDITS to pack.grantedCredits.toString()
            )
        )

        viewModelScope.launch {
            purchaseRepository.purchaseTokenPack(pack.storeProduct).fold(
                onSuccess = {
                    analyticsService.logEvent(
                        AnalyticsEvents.PURCHASE_COMPLETED,
                        mapOf(
                            AnalyticsParams.PACK_NAME to pack.storeProduct.id,
                            AnalyticsParams.PACK_CREDITS to pack.grantedCredits.toString()
                        )
                    )
                    creditManager.refreshCredits()
                    userPreferencesRepository.setHasPurchasedCredits()
                    navigationStrategy.navigate(PaywallNavigationAction.PurchaseComplete)
                },
                onFailure = { error ->
                    analyticsService.logEvent(
                        AnalyticsEvents.PURCHASE_FAILED,
                        mapOf(AnalyticsParams.ERROR_TYPE to (error.message ?: "unknown"))
                    )
                    _uiState.update {
                        it.copy(
                            isPurchasing = false,
                            error = UiText.StringResource(Res.string.paywall_error_purchase_failed)
                        )
                    }
                }
            )
        }
    }

    /**
     * Determines if user has exhausted their free trial (1 free full-res download).
     * When true, PaywallScreen shows the trial-ended headline instead of the default.
     */
    private fun loadTrialState() {
        viewModelScope.launch {
            val freeDownloadsUsed = userPreferencesRepository.getFreeDownloadsUsed()
            val hasPurchased = userPreferencesRepository.hasPurchasedCredits()
            // Post-trial = used free download but hasn't purchased yet
            _uiState.update { it.copy(isPostTrial = freeDownloadsUsed >= 1 && !hasPurchased) }
        }
    }

    private fun observeSignInState() {
        authService.isSignedIn.onEach { signedIn ->
            _uiState.update { it.copy(isSignedIn = signedIn) }
        }.launchIn(viewModelScope)
    }
}
