package com.middleton.studiosnap.core.data.datasource

import com.middleton.studiosnap.core.data.model.KontextPredictionRequest
import com.middleton.studiosnap.core.data.model.ReplicatePredictionResponse

/**
 * Handles Flux Kontext API calls via Replicate.
 */
interface KontextRemoteDataSource {

    suspend fun createPrediction(
        request: KontextPredictionRequest
    ): Result<ReplicatePredictionResponse>

    suspend fun getPrediction(predictionId: String): Result<ReplicatePredictionResponse>

    suspend fun downloadImage(url: String): Result<ByteArray>

    suspend fun downloadImageToFile(
        url: String,
        onProgress: (suspend (Float) -> Unit)? = null
    ): Result<String>
}
