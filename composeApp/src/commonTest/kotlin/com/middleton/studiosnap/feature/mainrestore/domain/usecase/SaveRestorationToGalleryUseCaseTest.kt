package com.middleton.studiosnap.feature.mainrestore.domain.usecase

import com.middleton.studiosnap.core.domain.repository.GalleryRepository
import com.middleton.studiosnap.core.domain.service.FakeImagePersistenceService
import com.middleton.studiosnap.feature.mainrestore.domain.model.PhotoRestoreOptions
import com.middleton.studiosnap.feature.mainrestore.domain.model.RecentRestoration
import com.middleton.studiosnap.feature.mainrestore.domain.model.RestorationResult
import com.middleton.studiosnap.feature.mainrestore.domain.repository.RecentRestorationsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SaveRestorationToGalleryUseCaseTest {

    private val fakeGalleryRepo = FakeSaveGalleryRepository()
    private val fakeRecentRepo = FakeSaveRecentRestorationsRepository()
    private val fakePersistenceService = FakeImagePersistenceService()

    private fun createUseCase() = SaveRestorationToGalleryUseCase(
        galleryRepository = fakeGalleryRepo,
        recentRestorationsRepository = fakeRecentRepo,
        imagePersistenceService = fakePersistenceService
    )

    // --- Watermarked (free trial) tests ---

    @Test
    fun `watermarked result copies image to persistent storage`() = runTest {
        val result = makeResult(isWatermarked = true)
        val useCase = createUseCase()

        val outcome = useCase(result)

        assertTrue(outcome.isSuccess)
        assertEquals(1, fakePersistenceService.persistentPaths.size,
            "Watermarked image should be saved to persistent storage")
    }

    @Test
    fun `watermarked result does not save to gallery`() = runTest {
        val result = makeResult(isWatermarked = true)
        val useCase = createUseCase()

        useCase(result)

        assertFalse(fakeGalleryRepo.saveImageCalled,
            "Gallery save should be skipped for watermarked results")
    }

    @Test
    fun `watermarked result stores persistent path in Room`() = runTest {
        val result = makeResult(isWatermarked = true)
        val useCase = createUseCase()

        useCase(result)

        val saved = fakeRecentRepo.savedRestorations.first()
        assertTrue(saved.restoredImagePath.startsWith("/persistent/"),
            "Room record should store the persistent path, not the cache path")
    }

    @Test
    fun `watermarked result returns failure when cache read fails`() = runTest {
        val failingPersistenceService = FakeImagePersistenceService(cacheReadReturnsNull = true)
        val useCase = SaveRestorationToGalleryUseCase(
            galleryRepository = fakeGalleryRepo,
            recentRestorationsRepository = fakeRecentRepo,
            imagePersistenceService = failingPersistenceService
        )

        val outcome = useCase(makeResult(isWatermarked = true))

        assertTrue(outcome.isFailure)
        assertTrue(fakeRecentRepo.savedRestorations.isEmpty(),
            "Nothing should be saved to Room when cache read fails")
    }

    // --- Paid result tests ---

    @Test
    fun `paid result saves to gallery and stores gallery URI in Room`() = runTest {
        val result = makeResult(isWatermarked = false)
        val useCase = createUseCase()

        val outcome = useCase(result)

        assertTrue(outcome.isSuccess)
        assertTrue(fakeGalleryRepo.saveImageCalled,
            "Paid result should be saved to the gallery")
        val saved = fakeRecentRepo.savedRestorations.first()
        assertEquals("gallery://uri", saved.restoredImagePath,
            "Room record should store the gallery URI for paid results")
    }

    @Test
    fun `paid result does not copy to persistent storage`() = runTest {
        val result = makeResult(isWatermarked = false)
        val useCase = createUseCase()

        useCase(result)

        assertTrue(fakePersistenceService.persistentPaths.isEmpty(),
            "Persistent storage should not be used for paid results")
    }

    // --- Helpers ---

    private fun makeResult(isWatermarked: Boolean) = RestorationResult(
        originalImagePath = "/cache/original.jpg",
        restoredImagePath = "/cache/restored.jpg",
        processingTimeSeconds = 3,
        restoreOptions = PhotoRestoreOptions(),
        creditsUsed = if (isWatermarked) 0 else 1,
        isWatermarked = isWatermarked
    )
}

private class FakeSaveGalleryRepository : GalleryRepository {
    var saveImageCalled = false

    override suspend fun saveImage(filePath: String, displayName: String): Result<String> {
        saveImageCalled = true
        return Result.success("gallery://uri")
    }

    override suspend fun deleteImage(galleryUri: String): Result<Unit> = Result.success(Unit)
    override suspend fun imageExists(galleryUri: String): Boolean = true
}

private class FakeSaveRecentRestorationsRepository : RecentRestorationsRepository {
    val savedRestorations = mutableListOf<RecentRestoration>()

    override fun observeRecentRestorations(limit: Int): Flow<List<RecentRestoration>> =
        flowOf(savedRestorations)

    override suspend fun getRecentRestorations(limit: Int): List<RecentRestoration> = savedRestorations

    override suspend fun saveRestoration(restoration: RecentRestoration): Result<Unit> {
        savedRestorations.add(restoration)
        return Result.success(Unit)
    }

    override suspend fun removeRestoration(restorationId: String): Result<Unit> {
        savedRestorations.removeAll { it.id == restorationId }
        return Result.success(Unit)
    }

    override suspend fun hideRestoration(restorationId: String): Result<Unit> = Result.success(Unit)

    override suspend fun clearAll(): Result<Unit> {
        savedRestorations.clear()
        return Result.success(Unit)
    }

    override suspend fun getRestoration(id: String): RecentRestoration? =
        savedRestorations.firstOrNull { it.id == id }

    override suspend fun getWatermarkedRestorations(): List<RecentRestoration> =
        savedRestorations.filter { it.isWatermarked }

    override suspend fun unlockAllWatermarked(): Result<Unit> = Result.success(Unit)

    override suspend fun updateRestoredImagePath(id: String, newPath: String): Result<Unit> {
        val index = savedRestorations.indexOfFirst { it.id == id }
        if (index >= 0) {
            savedRestorations[index] = savedRestorations[index].copy(restoredImagePath = newPath)
        }
        return Result.success(Unit)
    }
}
