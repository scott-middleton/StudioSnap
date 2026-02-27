package com.middleton.studiosnap.purchases

import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.configure
import com.revenuecat.purchases.kmp.models.StoreProduct

object PurchasesManager {
    fun configure() {
        Purchases.logLevel = LogLevel.DEBUG
        Purchases.configure(
            apiKey = getPlatformApiKey(),
        )
    }

    fun getStoreProducts(
        onError: (Exception) -> Unit = {},
        onSuccess: (List<StoreProduct>) -> Unit
    ) {
        Purchases.sharedInstance.getOfferings(
            onError = { error -> onError(Exception(error.message)) },
            onSuccess = { offerings ->
                val currentOffering = offerings.current
                if (currentOffering != null) {
                    val allStoreProduct: List<StoreProduct> =
                        currentOffering.availablePackages.map { it.storeProduct }
                    onSuccess(allStoreProduct)
                } else {
                    onSuccess(emptyList())
                }
            }
        )
    }

    fun logIn(
        appUserId: String,
        onError: (Exception) -> Unit = {},
        onSuccess: () -> Unit
    ) {
        Purchases.sharedInstance.logIn(
            newAppUserID = appUserId,
            onError = { error -> onError(Exception(error.message)) },
            onSuccess = { _, _ -> onSuccess() }
        )
    }

    fun purchase(
        storeProduct: StoreProduct,
        onError: (Exception) -> Unit = {},
        onSuccess: () -> Unit
    ) {
        Purchases.sharedInstance.purchase(
            storeProduct = storeProduct,
            onError = { error, _ ->
                onError(Exception(error.message))
            },
            onSuccess = { _, customerInfo ->
                onSuccess()
            }
        )
    }
}