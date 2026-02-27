package com.middleton.studiosnap.purchases

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

interface PurchasesIdentifier {
    suspend fun identifyUser(userId: String)
}

class PurchasesIdentifierImpl : PurchasesIdentifier {
    override suspend fun identifyUser(userId: String) {
        suspendCancellableCoroutine { continuation ->
            PurchasesManager.logIn(
                appUserId = userId,
                onSuccess = { continuation.resume(Unit) },
                onError = { continuation.resume(Unit) }
            )
        }
    }
}
