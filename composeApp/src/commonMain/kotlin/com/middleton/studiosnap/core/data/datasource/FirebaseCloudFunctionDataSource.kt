package com.middleton.studiosnap.core.data.datasource

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.functions.functions

interface CloudFunctionDataSource {
    suspend fun createModelPrediction(
        owner: String,
        name: String,
        input: Map<String, Any?>
    ): Map<String, Any?>

    suspend fun createVersionPrediction(
        version: String,
        input: Map<String, Any?>
    ): Map<String, Any?>

    suspend fun getPrediction(predictionId: String): Map<String, Any?>

    suspend fun fetchUserCredits(): Int

    suspend fun deductGenerationCredit(idempotencyKey: String): Int

    suspend fun refundGenerationCredit(): Int

    suspend fun checkFreeGenerationUsed(): Boolean

    suspend fun claimFreeGeneration(): Boolean
}

class FirebaseCloudFunctionDataSource : CloudFunctionDataSource {

    private val functions = Firebase.functions

    override suspend fun createModelPrediction(
        owner: String,
        name: String,
        input: Map<String, Any?>
    ): Map<String, Any?> {
        val callable = functions.httpsCallable("createModelPrediction")
        val result = callable.invoke(
            mapOf(
                "owner" to owner,
                "name" to name,
                "input" to input
            )
        )
        return result.dataAsMap("createModelPrediction")
    }

    override suspend fun createVersionPrediction(
        version: String,
        input: Map<String, Any?>
    ): Map<String, Any?> {
        val callable = functions.httpsCallable("createVersionPrediction")
        val result = callable.invoke(
            mapOf(
                "version" to version,
                "input" to input
            )
        )
        return result.dataAsMap("createVersionPrediction")
    }

    override suspend fun getPrediction(predictionId: String): Map<String, Any?> {
        val callable = functions.httpsCallable("getPrediction")
        val result = callable.invoke(
            mapOf("predictionId" to predictionId)
        )
        return result.dataAsMap("getPrediction")
    }

    override suspend fun fetchUserCredits(): Int {
        val callable = functions.httpsCallable("fetchUserCredits")
        val result = callable.invoke(emptyMap<String, Any>())
        val data = result.dataAsMap("fetchUserCredits")
        return (data["balance"] as? Number)?.toInt() ?: 0
    }

    override suspend fun deductGenerationCredit(idempotencyKey: String): Int {
        val callable = functions.httpsCallable("deductGenerationCredit")
        val result = callable.invoke(mapOf("idempotencyKey" to idempotencyKey))
        val data = result.dataAsMap("deductGenerationCredit")
        return (data["balance"] as? Number)?.toInt() ?: 0
    }

    override suspend fun refundGenerationCredit(): Int {
        val callable = functions.httpsCallable("refundGenerationCredit")
        val result = callable.invoke(emptyMap<String, Any>())
        val data = result.dataAsMap("refundGenerationCredit")
        return (data["balance"] as? Number)?.toInt() ?: 0
    }

    override suspend fun checkFreeGenerationUsed(): Boolean {
        val callable = functions.httpsCallable("checkFreeGenerationUsed")
        val result = callable.invoke(emptyMap<String, Any>())
        val data = result.dataAsMap("checkFreeGenerationUsed")
        return data["used"] as? Boolean ?: true
    }

    override suspend fun claimFreeGeneration(): Boolean {
        val callable = functions.httpsCallable("claimFreeGeneration")
        val result = callable.invoke(emptyMap<String, Any>())
        val data = result.dataAsMap("claimFreeGeneration")
        return data["claimed"] as? Boolean ?: false
    }
}

internal expect fun dev.gitlive.firebase.functions.HttpsCallableResult.dataAsMap(
    functionName: String
): Map<String, Any?>