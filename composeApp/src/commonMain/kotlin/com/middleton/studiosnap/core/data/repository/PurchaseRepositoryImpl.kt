package com.middleton.studiosnap.core.data.repository

import com.middleton.studiosnap.core.data.datasource.VirtualCurrencyRemoteDataSource
import com.middleton.studiosnap.core.data.mapper.toUserCredits
import com.middleton.studiosnap.core.domain.model.UserCredits
import com.middleton.studiosnap.core.domain.repository.PurchaseRepository
import com.middleton.studiosnap.purchases.PurchasesManager
import com.revenuecat.purchases.kmp.models.StoreProduct
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

class PurchaseRepositoryImpl(
    private val creditDataSource: VirtualCurrencyRemoteDataSource
) : PurchaseRepository {

    override suspend fun purchaseTokenPack(storeProduct: StoreProduct): Result<UserCredits> {
        // RevenueCat requires purchase() to be called on the main thread
        val purchaseResult = withContext(Dispatchers.Main) {
            suspendCancellableCoroutine<Result<Unit>> { continuation ->
                PurchasesManager.purchase(
                    storeProduct = storeProduct,
                    onSuccess = {
                        continuation.resume(Result.success(Unit))
                    },
                    onError = { error ->
                        continuation.resume(Result.failure(error))
                    }
                )
            }
        }
        // RevenueCat auto-adds credits on purchase, fetch updated balance
        return purchaseResult.fold(
            onSuccess = { fetchUpdatedCredits() },
            onFailure = { Result.failure(it) }
        )
    }

    override suspend fun getAvailableTokenPacks(): Result<List<StoreProduct>> {
        // RevenueCat requires getOfferings() to be called on the main thread
        return withContext(Dispatchers.Main) {
            suspendCancellableCoroutine { continuation ->
                PurchasesManager.getStoreProducts(
                    onSuccess = { products ->
                        continuation.resume(Result.success(products))
                    },
                    onError = { error ->
                        continuation.resume(Result.failure(error))
                    }
                )
            }
        }
    }

    override suspend fun restorePurchases(): Result<UserCredits> {
        return fetchUpdatedCredits()
    }

    private suspend fun fetchUpdatedCredits(): Result<UserCredits> {
        return creditDataSource.fetchUserCredits()
            .map { balance -> balance.toUserCredits() }
    }
}
