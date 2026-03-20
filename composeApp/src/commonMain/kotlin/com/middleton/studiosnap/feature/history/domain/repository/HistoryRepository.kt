package com.middleton.studiosnap.feature.history.domain.repository

import com.middleton.studiosnap.feature.history.domain.model.HistorySession
import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
import kotlinx.coroutines.flow.Flow

/**
 * Manages locally stored generation history.
 * All data is device-local (Room DB). No cloud sync in v1.
 */
interface HistoryRepository {

    fun getAll(): Flow<List<GenerationResult.Success>>

    /** Emits one [HistorySession] per batch, newest first. */
    fun getSessions(): Flow<List<HistorySession>>

    /** All results belonging to a single batch, ordered by creation time. */
    fun getByBatchId(batchId: String): Flow<List<GenerationResult.Success>>

    suspend fun save(result: GenerationResult.Success)

    suspend fun saveAll(results: List<GenerationResult.Success>)

    suspend fun getById(id: String): GenerationResult.Success?

    suspend fun delete(id: String)

    suspend fun markAsPurchased(id: String, fullResLocalUri: String)

    /** Sets the user-defined label for a session. */
    suspend fun updateSessionLabel(sessionId: String, label: String)

    /** Deletes all rows belonging to a session. */
    suspend fun deleteSession(sessionId: String)
}
