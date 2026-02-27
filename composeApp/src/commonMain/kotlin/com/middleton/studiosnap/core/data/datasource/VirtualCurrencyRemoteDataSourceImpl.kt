package com.middleton.studiosnap.core.data.datasource

import com.middleton.studiosnap.composeapp.BuildKonfig
import com.middleton.studiosnap.core.data.http.HttpClientProvider
import com.middleton.studiosnap.core.data.model.RevenueCatVirtualCurrenciesDto
import com.middleton.studiosnap.core.data.model.VirtualCurrencyTransactionRequest
import com.middleton.studiosnap.core.domain.exception.NotAuthenticatedException
import com.middleton.studiosnap.core.domain.service.AuthService
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.datetime.Clock

class VirtualCurrencyRemoteDataSourceImpl(
    private val authService: AuthService
) : VirtualCurrencyRemoteDataSource {

    private val httpClient = HttpClientProvider.create()
    private val baseUrl = "https://api.revenuecat.com/v2"

    private val secretKey = BuildKonfig.REVENUE_CAT_SECRET_KEY
    private val projectId = BuildKonfig.REVENUE_CAT_PROJECT_ID

    private suspend fun getCustomerId(): String {
        val currentUser = authService.getCurrentUser()
            ?: throw NotAuthenticatedException()
        return currentUser.id
    }

    override suspend fun fetchUserCredits(): Result<Int> {
        return try {
            val customerId = getCustomerId()

            val response = httpClient.get(
                "$baseUrl/projects/$projectId/customers/$customerId/virtual_currencies"
            ) {
                header(HttpHeaders.Authorization, "Bearer $secretKey")
            }

            when {
                response.status.isSuccess() -> {
                    val dto = response.body<RevenueCatVirtualCurrenciesDto>()
                    val tokenBalance = dto.items.find { it.currencyCode == CURRENCY_CODE }?.balance ?: 0
                    Result.success(tokenBalance)
                }
                response.status.value == HTTP_NOT_FOUND -> {
                    Result.success(0) // New user, no purchases yet
                }
                else -> {
                    Result.failure(Exception("Failed to fetch credits: ${response.status}"))
                }
            }
        } catch (e: NotAuthenticatedException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(Exception("Error fetching credits: ${e.message}", e))
        }
    }

    override suspend fun deductCredits(amount: Int, reason: String): Result<Int> {
        return try {
            val customerId = getCustomerId()
            val url = "$baseUrl/projects/$projectId/customers/$customerId/virtual_currencies/transactions"
            val requestBody = VirtualCurrencyTransactionRequest(
                adjustments = mapOf(CURRENCY_CODE to -amount)
            )

            val response = httpClient.post(url) {
                header(HttpHeaders.Authorization, "Bearer $secretKey")
                header("Idempotency-Key", "$customerId-${Clock.System.now().toEpochMilliseconds()}-$amount")
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            when {
                response.status.value == HTTP_UNPROCESSABLE_ENTITY -> {
                    Result.failure(Exception("Insufficient credits"))
                }
                response.status.isSuccess() -> {
                    val dto = response.body<RevenueCatVirtualCurrenciesDto>()
                    val newBalance = dto.items.find { it.currencyCode == CURRENCY_CODE }?.balance ?: 0
                    Result.success(newBalance)
                }
                else -> {
                    Result.failure(Exception("Failed to deduct credits: ${response.status}"))
                }
            }
        } catch (e: NotAuthenticatedException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(Exception("Error deducting credits: ${e.message}", e))
        }
    }

    override suspend fun addCredits(amount: Int, reason: String): Result<Int> {
        return try {
            val customerId = getCustomerId()
            val url = "$baseUrl/projects/$projectId/customers/$customerId/virtual_currencies/transactions"
            val requestBody = VirtualCurrencyTransactionRequest(
                adjustments = mapOf(CURRENCY_CODE to amount)
            )

            val response = httpClient.post(url) {
                header(HttpHeaders.Authorization, "Bearer $secretKey")
                header("Idempotency-Key", "$customerId-refund-${Clock.System.now().toEpochMilliseconds()}-$amount")
                contentType(ContentType.Application.Json)
                setBody(requestBody)
            }

            when {
                response.status.isSuccess() -> {
                    val dto = response.body<RevenueCatVirtualCurrenciesDto>()
                    val newBalance = dto.items.find { it.currencyCode == CURRENCY_CODE }?.balance ?: 0
                    Result.success(newBalance)
                }
                else -> {
                    Result.failure(Exception("Failed to refund credits: ${response.status}"))
                }
            }
        } catch (e: NotAuthenticatedException) {
            Result.failure(e)
        } catch (e: Exception) {
            Result.failure(Exception("Error refunding credits: ${e.message}", e))
        }
    }

    companion object {
        private const val CURRENCY_CODE = "credits"
        private const val HTTP_NOT_FOUND = 404
        private const val HTTP_UNPROCESSABLE_ENTITY = 422
    }
}
