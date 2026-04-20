package com.middleton.studiosnap.core.data.datasource

import com.middleton.studiosnap.core.domain.exception.NotAuthenticatedException
import com.middleton.studiosnap.core.domain.service.AuthService
import dev.gitlive.firebase.functions.FirebaseFunctionsException
import kotlinx.datetime.Clock

class VirtualCurrencyRemoteDataSourceImpl(
    private val authService: AuthService,
    private val cloudFunctions: CloudFunctionDataSource
) : VirtualCurrencyRemoteDataSource {

    private suspend fun requireAuthenticated() {
        authService.getCurrentUser() ?: throw NotAuthenticatedException()
    }

    override suspend fun fetchUserCredits(): Result<Int> {
        return try {
            requireAuthenticated()
            val balance = cloudFunctions.fetchUserCredits()
            Result.success(balance)
        } catch (e: NotAuthenticatedException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(Exception("Error fetching credits: ${e.message}", e))
        }
    }

    override suspend fun deductGenerationCredit(idempotencyKey: String): Result<Int> {
        return try {
            requireAuthenticated()
            val newBalance = cloudFunctions.deductGenerationCredit(idempotencyKey)
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

    override suspend fun refundGenerationCredit(): Result<Int> {
        return try {
            requireAuthenticated()
            val newBalance = cloudFunctions.refundGenerationCredit()
            Result.success(newBalance)
        } catch (e: NotAuthenticatedException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(Exception("Error refunding credits: ${e.message}", e))
        }
    }

    override suspend fun checkFreeGenerationUsed(): Result<Boolean> {
        return try {
            requireAuthenticated()
            val used = cloudFunctions.checkFreeGenerationUsed()
            Result.success(used)
        } catch (e: NotAuthenticatedException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(Exception("Error checking free generation: ${e.message}", e))
        }
    }

    override suspend fun claimFreeGeneration(): Result<Boolean> {
        return try {
            requireAuthenticated()
            val claimed = cloudFunctions.claimFreeGeneration()
            Result.success(claimed)
        } catch (e: NotAuthenticatedException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(Exception("Error claiming free generation: ${e.message}", e))
        }
    }

    private fun isInsufficientCredits(e: FirebaseFunctionsException): Boolean {
        val code = e.code.name
        return code == "FAILED_PRECONDITION" || code == "failed-precondition"
    }
}