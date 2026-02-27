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

    @Query("SELECT * FROM generations WHERE isPurchased = 1 ORDER BY createdAt DESC")
    fun getPurchasedOnly(): Flow<List<GenerationEntity>>

    @Query("SELECT * FROM generations WHERE isPurchased = 0 ORDER BY createdAt DESC")
    fun getPreviewsOnly(): Flow<List<GenerationEntity>>

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
}
