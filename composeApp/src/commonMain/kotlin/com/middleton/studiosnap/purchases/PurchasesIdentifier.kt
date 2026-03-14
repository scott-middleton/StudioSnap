package com.middleton.studiosnap.purchases

import kotlin.concurrent.Volatile
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

interface PurchasesIdentifier {
    suspend fun identifyUser(userId: String): Result<Unit>
    fun clearIdentity()
}

class PurchasesIdentifierImpl : PurchasesIdentifier {
    @Volatile
    private var identifiedUserId: String? = null

    override suspend fun identifyUser(userId: String): Result<Unit> {
        if (identifiedUserId == userId) return Result.success(Unit)

        return suspendCancellableCoroutine { continuation ->
            PurchasesManager.logIn(
                appUserId = userId,
                onSuccess = {
                    identifiedUserId = userId
                    continuation.resume(Result.success(Unit))
                },
                onError = { error -> continuation.resume(Result.failure(error)) }
            )
        }
    }

    override fun clearIdentity() {
        identifiedUserId = null
        PurchasesManager.logOut()
    }
}
