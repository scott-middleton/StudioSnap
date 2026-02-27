package com.middleton.studiosnap.core.data.datasource

import com.middleton.studiosnap.composeapp.BuildKonfig
import com.middleton.studiosnap.core.data.cache.ImageCacheManager
import com.middleton.studiosnap.core.data.model.KontextPredictionRequest
import com.middleton.studiosnap.core.data.model.ReplicatePredictionResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.prepareGet
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import kotlinx.datetime.Clock
import kotlinx.io.readByteArray

class KontextRemoteDataSourceImpl(
    private val httpClient: HttpClient,
    private val imageCacheManager: ImageCacheManager
) : KontextRemoteDataSource {

    private val baseUrl = "https://api.replicate.com/v1"
    private val apiToken = BuildKonfig.REPLICATE_API_TOKEN

    companion object {
        private const val BUFFER_SIZE = 8192L
    }

    override suspend fun createPrediction(
        request: KontextPredictionRequest
    ): Result<ReplicatePredictionResponse> = runCatching {
        val response = httpClient.post("$baseUrl/predictions") {
            header("Authorization", "Token $apiToken")
            contentType(ContentType.Application.Json)
            setBody(request)
        }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw ReplicateApiException(response.status.value, errorBody)
        }

        response.body()
    }

    override suspend fun getPrediction(
        predictionId: String
    ): Result<ReplicatePredictionResponse> = runCatching {
        val response = httpClient.get("$baseUrl/predictions/$predictionId") {
            header("Authorization", "Token $apiToken")
        }

        if (!response.status.isSuccess()) {
            val errorBody = response.bodyAsText()
            throw ReplicateApiException(response.status.value, errorBody)
        }

        response.body()
    }

    override suspend fun downloadImage(url: String): Result<ByteArray> = runCatching {
        val response = httpClient.get(url)
        response.body()
    }

    override suspend fun downloadImageToFile(url: String): Result<String> = runCatching {
        val timestamp = Clock.System.now().epochSeconds
        val fileName = "generated_$timestamp.jpg"

        httpClient.prepareGet(url).execute { response ->
            val channel: ByteReadChannel = response.body()

            imageCacheManager.saveImageStreamToCache(fileName) { sink ->
                while (!channel.isClosedForRead) {
                    val packet = channel.readRemaining(BUFFER_SIZE)
                    while (!packet.exhausted()) {
                        val bytes = packet.readByteArray()
                        sink.write(bytes)
                    }
                }
            }
        }
    }
}

class ReplicateApiException(
    val statusCode: Int,
    val responseBody: String
) : Exception("Replicate API error $statusCode: $responseBody") {

    val isRateLimited: Boolean get() = statusCode == 429

    val retryAfterSeconds: Int?
        get() {
            if (!isRateLimited) return null
            val regex = """"retry_after"\s*:\s*(\d+)""".toRegex()
            return regex.find(responseBody)?.groupValues?.get(1)?.toIntOrNull()
        }
}
