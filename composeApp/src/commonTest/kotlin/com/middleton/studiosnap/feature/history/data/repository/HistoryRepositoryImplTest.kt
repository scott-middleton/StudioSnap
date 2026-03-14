package com.middleton.studiosnap.feature.history.data.repository

import com.middleton.studiosnap.core.data.database.GenerationDao
import com.middleton.studiosnap.core.data.database.GenerationEntity
import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
import com.middleton.studiosnap.feature.home.domain.model.ProductPhoto
import com.middleton.studiosnap.feature.home.domain.model.Style
import com.middleton.studiosnap.feature.home.domain.model.StyleCategory
import com.middleton.studiosnap.feature.home.domain.repository.StyleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class HistoryRepositoryImplTest {

    private val fakeDao = FakeGenerationDao()
    private val fakeStyleRepository = FakeStyleRepository()
    private val sut = HistoryRepositoryImpl(fakeDao, fakeStyleRepository)

    // region save & getById

    @Test
    fun `save persists result and getById retrieves it`() = runTest {
        sut.save(testResult("gen-1"))

        val retrieved = sut.getById("gen-1")
        assertNotNull(retrieved)
        assertEquals("gen-1", retrieved.generationId)
    }

    @Test
    fun `getById returns null for non-existent id`() = runTest {
        assertNull(sut.getById("non-existent"))
    }

    @Test
    fun `saveAll persists multiple results`() = runTest {
        sut.saveAll(listOf(testResult("gen-1"), testResult("gen-2")))

        val all = sut.getAll().first()
        assertEquals(2, all.size)
    }

    // endregion

    // region getAll, getPurchasedOnly, getPreviewsOnly

    @Test
    fun `getAll returns all generations`() = runTest {
        sut.save(testResult("gen-1"))
        sut.save(testResult("gen-2"))

        val all = sut.getAll().first()
        assertEquals(2, all.size)
    }

    @Test
    fun `getPurchasedOnly returns only purchased generations`() = runTest {
        sut.save(testResult("gen-1"))
        sut.save(testResult("gen-2", fullResUri = "/path/to/full.jpg"))

        val purchased = sut.getPurchasedOnly().first()
        assertEquals(1, purchased.size)
        assertEquals("gen-2", purchased[0].generationId)
    }

    @Test
    fun `getPreviewsOnly returns only non-purchased generations`() = runTest {
        sut.save(testResult("gen-1"))
        sut.save(testResult("gen-2", fullResUri = "/path/to/full.jpg"))

        val previews = sut.getPreviewsOnly().first()
        assertEquals(1, previews.size)
        assertEquals("gen-1", previews[0].generationId)
    }

    // endregion

    // region delete

    @Test
    fun `delete removes generation`() = runTest {
        sut.save(testResult("gen-1"))
        sut.delete("gen-1")

        assertNull(sut.getById("gen-1"))
    }

    // endregion

    // region markAsPurchased

    @Test
    fun `markAsPurchased updates entity`() = runTest {
        sut.save(testResult("gen-1"))
        sut.markAsPurchased("gen-1", "/path/to/full.jpg")

        val entity = fakeDao.getById("gen-1")
        assertNotNull(entity)
        assertTrue(entity.isPurchased)
        assertEquals("/path/to/full.jpg", entity.fullResLocalUri)
    }

    // endregion

    // region style lookup fallback

    @Test
    fun `maps with placeholder style when style not in repository`() = runTest {
        val result = testResult("gen-1", styleId = "unknown_style")
        sut.save(result)

        val retrieved = sut.getById("gen-1")
        assertNotNull(retrieved)
        assertEquals("unknown_style", retrieved.style.id)
        assertEquals("", retrieved.style.kontextPrompt)
    }

    @Test
    fun `maps with real style when found in repository`() = runTest {
        sut.save(testResult("gen-1", styleId = "clean_white"))

        val retrieved = sut.getById("gen-1")
        assertNotNull(retrieved)
        assertEquals("clean_white", retrieved.style.id)
        assertEquals("test prompt", retrieved.style.kontextPrompt)
    }

    // endregion

    // region helpers

    private fun testResult(
        id: String,
        styleId: String = "clean_white",
        fullResUri: String? = null
    ) = GenerationResult.Success(
        generationId = id,
        inputPhoto = ProductPhoto(id = "photo-1", localUri = "/path/to/photo.jpg"),
        previewUri = "/path/to/preview.jpg",
        fullResUrl = "https://replicate.com/output.jpg",
        fullResUri = fullResUri,
        style = fakeStyleRepository.getStyleById(styleId) ?: Style(
            id = styleId,
            nameKey = styleId,
            categories = emptySet(),
            thumbnailResName = "",
            kontextPrompt = ""
        ),
        createdAt = 1000L,
        imageWidth = 1024,
        imageHeight = 1024
    )

    // endregion
}

// region fakes

private class FakeGenerationDao : GenerationDao {
    private val entities = MutableStateFlow<List<GenerationEntity>>(emptyList())

    override fun getAll(): Flow<List<GenerationEntity>> =
        entities.map { list -> list.sortedByDescending { it.createdAt } }

    override fun getPurchasedOnly(): Flow<List<GenerationEntity>> =
        entities.map { list -> list.filter { it.isPurchased }.sortedByDescending { it.createdAt } }

    override fun getPreviewsOnly(): Flow<List<GenerationEntity>> =
        entities.map { list -> list.filter { !it.isPurchased }.sortedByDescending { it.createdAt } }

    override suspend fun getById(id: String): GenerationEntity? =
        entities.value.find { it.id == id }

    override suspend fun insert(entity: GenerationEntity) {
        entities.value = entities.value.filter { it.id != entity.id } + entity
    }

    override suspend fun insertAll(entities: List<GenerationEntity>) {
        val ids = entities.map { it.id }.toSet()
        this.entities.value = this.entities.value.filter { it.id !in ids } + entities
    }

    override suspend fun markAsPurchased(id: String, fullResLocalUri: String) {
        entities.value = entities.value.map {
            if (it.id == id) it.copy(isPurchased = true, fullResLocalUri = fullResLocalUri) else it
        }
    }

    override suspend fun delete(id: String) {
        entities.value = entities.value.filter { it.id != id }
    }

    override suspend fun getPurchasedCount(): Int =
        entities.value.count { it.isPurchased }
}

private class FakeStyleRepository : StyleRepository {
    private val styles = listOf(
        Style(
            id = "clean_white",
            nameKey = "Clean White",
            categories = setOf(StyleCategory.ALL),
            thumbnailResName = "style_clean_white",
            kontextPrompt = "test prompt"
        )
    )

    override fun getAllStyles(): List<Style> = styles
    override fun getStylesByCategory(category: StyleCategory): List<Style> =
        styles.filter { category in it.categories }
    override fun getStyleById(id: String): Style? = styles.find { it.id == id }
}

// endregion
