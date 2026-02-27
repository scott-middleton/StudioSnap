package com.middleton.studiosnap.core.domain.service

interface WatermarkService {
    suspend fun apply(inputPath: String, outputPath: String): Result<Unit>
}
