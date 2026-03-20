package com.middleton.studiosnap.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GenerationDao {

    @Query("SELECT * FROM generations ORDER BY createdAt DESC")
    fun getAll(): Flow<List<GenerationEntity>>

    @Query("SELECT * FROM generations WHERE id = :id")
    suspend fun getById(id: String): GenerationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: GenerationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<GenerationEntity>)

    @Query("UPDATE generations SET isPurchased = 1, fullResLocalUri = :fullResLocalUri WHERE id = :id")
    suspend fun markAsPurchased(id: String, fullResLocalUri: String)

    @Query("DELETE FROM generations WHERE id = :id")
    suspend fun delete(id: String)

    @Query("SELECT COUNT(*) FROM generations WHERE isPurchased = 1")
    suspend fun getPurchasedCount(): Int

    /**
     * Returns one row per session, ordered newest first.
     * COALESCE(NULLIF(batchId,''), id) groups legacy rows (batchId='') by their own id,
     * so each pre-v3 row appears as its own single-image session.
     */
    @Query("""
        SELECT COALESCE(NULLIF(batchId,''), id) AS sessionId,
               COUNT(*) AS imageCount,
               sessionLabel,
               styleName,
               MAX(createdAt) AS latestCreatedAt
        FROM generations
        GROUP BY sessionId
        ORDER BY latestCreatedAt DESC
    """)
    fun getSessions(): Flow<List<SessionSummaryEntity>>

    /** Returns up to [limit] preview URIs for a session, for thumbnail strips. */
    @Query("""
        SELECT previewUri FROM generations
        WHERE COALESCE(NULLIF(batchId,''), id) = :sessionId
        ORDER BY createdAt ASC
        LIMIT :limit
    """)
    suspend fun getPreviewUrisBySessionId(sessionId: String, limit: Int): List<String>

    /** Returns all rows belonging to a batch, ordered by creation time. */
    @Query("SELECT * FROM generations WHERE batchId = :batchId ORDER BY createdAt ASC")
    fun getByBatchId(batchId: String): Flow<List<GenerationEntity>>

    @Query("UPDATE generations SET sessionLabel = :label WHERE COALESCE(NULLIF(batchId,''), id) = :sessionId")
    suspend fun updateSessionLabel(sessionId: String, label: String)

    @Query("DELETE FROM generations WHERE COALESCE(NULLIF(batchId,''), id) = :sessionId")
    suspend fun deleteSession(sessionId: String)
}
