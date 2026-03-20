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
     *
     * thumbnailUris is a comma-separated list of up to 4 preview URIs (oldest first),
     * embedded here to avoid an N+1 query per session.
     */
    @Query("""
        SELECT COALESCE(NULLIF(batchId,''), id) AS sessionId,
               COUNT(*) AS imageCount,
               sessionLabel,
               styleName,
               MAX(createdAt) AS latestCreatedAt,
               (SELECT GROUP_CONCAT(p.previewUri, ',')
                FROM (SELECT previewUri FROM generations g2
                      WHERE COALESCE(NULLIF(g2.batchId,''), g2.id) = COALESCE(NULLIF(generations.batchId,''), generations.id)
                      ORDER BY g2.createdAt ASC LIMIT 4) p) AS thumbnailUris
        FROM generations
        GROUP BY sessionId
        ORDER BY latestCreatedAt DESC
    """)
    fun getSessions(): Flow<List<SessionSummaryEntity>>

    /**
     * Returns all rows belonging to a session, ordered by creation time.
     * Uses the same COALESCE key as getSessions() so legacy rows (batchId='')
     * are correctly matched by their own id.
     */
    @Query("SELECT * FROM generations WHERE COALESCE(NULLIF(batchId,''), id) = :sessionId ORDER BY createdAt ASC")
    fun getBySessionId(sessionId: String): Flow<List<GenerationEntity>>

    @Query("UPDATE generations SET sessionLabel = :label WHERE COALESCE(NULLIF(batchId,''), id) = :sessionId")
    suspend fun updateSessionLabel(sessionId: String, label: String)

    @Query("DELETE FROM generations WHERE COALESCE(NULLIF(batchId,''), id) = :sessionId")
    suspend fun deleteSession(sessionId: String)
}
