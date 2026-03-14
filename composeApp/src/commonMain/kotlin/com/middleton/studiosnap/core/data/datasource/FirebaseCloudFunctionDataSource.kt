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

    suspend fun fetchUserCredits(customerId: String): Int

    suspend fun deductCredits(
        customerId: String,
        amount: Int,
        idempotencyKey: String
    ): Int

    suspend fun addCredits(
        customerId: String,
        amount: Int,
        idempotencyKey: String
    ): Int
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

    override suspend fun fetchUserCredits(customerId: String): Int {
        val callable = functions.httpsCallable("fetchUserCredits")
        val result = callable.invoke(
            mapOf("customerId" to customerId)
        )
        val data = result.dataAsMap("fetchUserCredits")
        return (data["balance"] as? Number)?.toInt() ?: 0
    }

    override suspend fun deductCredits(
        customerId: String,
        amount: Int,
        idempotencyKey: String
    ): Int {
        val callable = functions.httpsCallable("deductCredits")
        val result = callable.invoke(
            mapOf(
                "customerId" to customerId,
                "amount" to amount,
                "idempotencyKey" to idempotencyKey
            )
        )
        val data = result.dataAsMap("deductCredits")
        return (data["balance"] as? Number)?.toInt() ?: 0
    }

    override suspend fun addCredits(
        customerId: String,
        amount: Int,
        idempotencyKey: String
    ): Int {
        val callable = functions.httpsCallable("addCredits")
        val result = callable.invoke(
            mapOf(
                "customerId" to customerId,
                "amount" to amount,
                "idempotencyKey" to idempotencyKey
            )
        )
        val data = result.dataAsMap("addCredits")
        return (data["balance"] as? Number)?.toInt() ?: 0
    }
}

internal expect fun dev.gitlive.firebase.functions.HttpsCallableResult.dataAsMap(
    functionName: String
): Map<String, Any?>