package com.middleton.studiosnap.core.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class KontextInput(
    val prompt: String,
    @SerialName("input_image")
    val inputImage: String,
    @SerialName("output_format")
    val outputFormat: String = "jpg",
    @SerialName("aspect_ratio")
    val aspectRatio: String = "match_input_image",
    val guidance: Float = 3.5f,
    @SerialName("num_inference_steps")
    val numInferenceSteps: Int = 30
)

@Serializable
data class KontextPredictionRequest(
    val version: String,
    val input: KontextInput
)

@Serializable
data class ReplicatePredictionResponse(
    val id: String,
    val status: String,
    val output: JsonElement? = null,
    val error: String? = null,
    @SerialName("created_at")
    val createdAt: String? = null,
    @SerialName("started_at")
    val startedAt: String? = null,
    @SerialName("completed_at")
    val completedAt: String? = null
)
