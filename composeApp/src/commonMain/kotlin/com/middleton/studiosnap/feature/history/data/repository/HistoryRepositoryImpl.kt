package com.middleton.studiosnap.feature.history.data.repository

import com.middleton.studiosnap.core.data.database.GenerationDao
import com.middleton.studiosnap.core.data.database.toDomainModel
import com.middleton.studiosnap.core.data.database.toEntity
import com.middleton.studiosnap.feature.history.domain.repository.HistoryRepository
import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
import com.middleton.studiosnap.feature.home.domain.repository.StyleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HistoryRepositoryImpl(
    private val generationDao: GenerationDao,
    private val styleRepository: StyleRepository
) : HistoryRepository {

    override fun getAll(): Flow<List<GenerationResult.Success>> {
        return generationDao.getAll().map { entities ->
            entities.map { it.toDomainModel(styleRepository) }
        }
    }

    override suspend fun save(result: GenerationResult.Success) {
        generationDao.insert(result.toEntity())
    }

    override suspend fun saveAll(results: List<GenerationResult.Success>) {
        generationDao.insertAll(results.map { it.toEntity() })
    }

    override suspend fun getById(id: String): GenerationResult.Success? {
        return generationDao.getById(id)?.toDomainModel(styleRepository)
    }

    override suspend fun delete(id: String) {
        generationDao.delete(id)
    }

    override suspend fun markAsPurchased(id: String, fullResLocalUri: String) {
        generationDao.markAsPurchased(id, fullResLocalUri)
    }
}
