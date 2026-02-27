package com.middleton.studiosnap.feature.history.domain.repository

import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
import kotlinx.coroutines.flow.Flow

/**
 * Manages locally stored generation history.
 * All data is device-local (Room DB). No cloud sync in v1.
 */
interface HistoryRepository {

    fun getAll(): Flow<List<GenerationResult.Success>>

    fun getPurchasedOnly(): Flow<List<GenerationResult.Success>>

    fun getPreviewsOnly(): Flow<List<GenerationResult.Success>>

    suspend fun save(result: GenerationResult.Success)

    suspend fun saveAll(results: List<GenerationResult.Success>)

    suspend fun getById(id: String): GenerationResult.Success?

    suspend fun delete(id: String)

    suspend fun markAsPurchased(id: String, fullResLocalUri: String)
}
