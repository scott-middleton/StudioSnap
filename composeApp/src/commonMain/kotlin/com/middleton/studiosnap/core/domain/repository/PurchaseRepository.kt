package com.middleton.studiosnap.core.domain.repository

import com.middleton.studiosnap.core.domain.model.UserCredits
import com.revenuecat.purchases.kmp.models.StoreProduct

/**
 * Repository for handling token pack purchases
 */
interface PurchaseRepository {
    /**
     * Purchase a token pack via RevenueCat
     *
     * Flow:
     * 1. RevenueCat processes payment
     * 2. RevenueCat AUTO-ADDS virtual currency (configured in dashboard)
     * 3. Fetch updated credits
     *
     * @param storeProduct The product to purchase (from RevenueCat offerings)
     * @return Updated UserCredits after purchase completes
     */
    suspend fun purchaseTokenPack(storeProduct: StoreProduct): Result<UserCredits>

    /**
     * Get available token packs from RevenueCat offerings
     * @return List of available products to purchase
     */
    suspend fun getAvailableTokenPacks(): Result<List<StoreProduct>>

    /**
     * Restore previous purchases
     * Useful when user reinstalls app or switches devices
     * @return Updated UserCredits after restore
     */
    suspend fun restorePurchases(): Result<UserCredits>
}
