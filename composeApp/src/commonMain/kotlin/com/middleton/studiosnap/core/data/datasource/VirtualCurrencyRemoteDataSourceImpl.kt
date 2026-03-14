package com.middleton.studiosnap.core.data.datasource

import com.middleton.studiosnap.core.domain.exception.NotAuthenticatedException
import com.middleton.studiosnap.core.domain.service.AuthService
import dev.gitlive.firebase.functions.FirebaseFunctionsException
import kotlinx.datetime.Clock

class VirtualCurrencyRemoteDataSourceImpl(
    private val authService: AuthService,
    private val cloudFunctions: CloudFunctionDataSource
) : VirtualCurrencyRemoteDataSource {

    private suspend fun getCustomerId(): String {
        val currentUser = authService.getCurrentUser()
            ?: throw NotAuthenticatedException()
        return currentUser.id
    }

    override suspend fun fetchUserCredits(): Result<Int> {
        return try {
            val customerId = getCustomerId()
            val balance = cloudFunctions.fetchUserCredits(customerId)
            Result.success(balance)
        } catch (e: NotAuthenticatedException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(Exception("Error fetching credits: ${e.message}", e))
        }
    }

    override suspend fun deductCredits(amount: Int, reason: String): Result<Int> {
        return try {
            val customerId = getCustomerId()
            val idempotencyKey = "$customerId-${Clock.System.now().toEpochMilliseconds()}-$amount"
            val newBalance = cloudFunctions.deductCredits(customerId, amount, idempotencyKey)
            Result.success(newBalance)
        } catch (e: NotAuthenticatedException) {
            Result.failure(e)
        } catch (e: FirebaseFunctionsException) {
            if (isInsufficientCredits(e)) {
                Result.failure(Exception("Insufficient credits"))
            } else {
                Result.failure(Exception("Error deducting credits: ${e.message}", e))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Error deducting credits: ${e.message}", e))
        }
    }

    override suspend fun addCredits(amount: Int, reason: String): Result<Int> {
        return try {
            val customerId = getCustomerId()
            val idempotencyKey = "$customerId-refund-${Clock.System.now().toEpochMilliseconds()}-$amount"
            val newBalance = cloudFunctions.addCredits(customerId, amount, idempotencyKey)
            Result.success(newBalance)
        } catch (e: NotAuthenticatedException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(Exception("Error refunding credits: ${e.message}", e))
        }
    }

    private fun isInsufficientCredits(e: FirebaseFunctionsException): Boolean {
        // Cloud function throws HttpsError("failed-precondition", "Insufficient credits")
        // GitLive wraps this as FirebaseFunctionsException with code FAILED_PRECONDITION
        val code = e.code.name
        return code == "FAILED_PRECONDITION" || code == "failed-precondition"
    }
}