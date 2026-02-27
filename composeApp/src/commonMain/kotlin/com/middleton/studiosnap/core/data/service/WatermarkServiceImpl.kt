package com.middleton.studiosnap.core.data.service

import com.middleton.studiosnap.core.domain.service.WatermarkService
import com.middleton.studiosnap.core.data.util.applyWatermark

class WatermarkServiceImpl : WatermarkService {
    override suspend fun apply(inputPath: String, outputPath: String): Result<Unit> {
        return applyWatermark(inputPath, outputPath)
    }
}
