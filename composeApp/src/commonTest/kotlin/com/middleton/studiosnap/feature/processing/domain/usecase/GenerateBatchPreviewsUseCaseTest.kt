package com.middleton.studiosnap.feature.processing.domain.usecase

import com.middleton.studiosnap.core.domain.model.UiText
import com.middleton.studiosnap.core.domain.model.UserCredits
import com.middleton.studiosnap.core.domain.service.CreditDeductor
import com.middleton.studiosnap.core.domain.service.FakeErrorReporter
import com.middleton.studiosnap.feature.history.domain.model.HistorySession
import com.middleton.studiosnap.feature.history.domain.repository.HistoryRepository
import com.middleton.studiosnap.feature.home.domain.model.ExportFormat
import com.middleton.studiosnap.feature.home.domain.model.GenerationConfig
import com.middleton.studiosnap.feature.home.domain.model.GenerationQuality
import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
import com.middleton.studiosnap.feature.home.domain.model.ProductPhoto
import com.middleton.studiosnap.feature.home.domain.model.Style
import com.middleton.studiosnap.feature.home.domain.model.StyleCategory
import com.middleton.studiosnap.feature.home.domain.repository.GenerationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GenerateBatchPreviewsUseCaseTest {

    private val testStyle = Style(
        id = "clean_white",
        displayName = UiText.DynamicString("Clean White"),
        categories = setOf(StyleCategory.ALL),
        thumbnail = null,
        kontextPrompt = "White bg"
    )

    private val photo1 = ProductPhoto(id = "photo_1", localUri = "content://photo1")
    private val photo2 = ProductPhoto(id = "photo_2", localUri = "content://photo2")

    private val config = GenerationConfig(
        photos = listOf(photo1, photo2),
        style = testStyle,
        resolvedPrompt = testStyle.kontextPrompt,
        shadow = false,
        reflection = false,
        exportFormat = ExportFormat.DEFAULT,
        quality = GenerationQuality.STANDARD,
        batchId = "batch-1"
    )

    @Test
    fun `refund uses the same key as the failed photo's deduction`() = runTest {
        val singlePhotoConfig = config.copy(photos = listOf(photo1))
        val creditDeductor = FakeCreditDeductor()
        val repo = FakeGenerationRepository(failOnIndex = 0)
        val useCase = GenerateBatchPreviewsUseCase(
            GeneratePreviewUseCase(repo, FakeHistoryRepository(), FakeErrorReporter()),
            creditDeductor,
            FakeErrorReporter()
        )

        useCase(singlePhotoConfig).collectAll()

        assertEquals(1, creditDeductor.deductKeys.size)
        assertEquals(creditDeductor.deductKeys[0], creditDeductor.refundKeys.single())
    }

    @Test
    fun `each failing photo in a multi-photo batch refunds its own distinct deduction key`() = runTest {
        val creditDeductor = FakeCreditDeductor()
        val repo = FakeGenerationRepository(failIndices = setOf(0, 1))
        val useCase = GenerateBatchPreviewsUseCase(
            GeneratePreviewUseCase(repo, FakeHistoryRepository(), FakeErrorReporter()),
            creditDeductor,
            FakeErrorReporter()
        )

        useCase(config).collectAll()

        assertEquals(2, creditDeductor.deductKeys.size)
        assertEquals(2, creditDeductor.refundKeys.size)
        assertEquals(creditDeductor.deductKeys.toSet(), creditDeductor.refundKeys.toSet())
        assertEquals(2, creditDeductor.deductKeys.toSet().size, "deduction keys must be distinct per photo")
        // Each refund is paired with the deduction made immediately before it — never a later photo's key.
        assertEquals(creditDeductor.deductKeys[0], creditDeductor.refundKeys[0])
        assertEquals(creditDeductor.deductKeys[1], creditDeductor.refundKeys[1])
    }

    @Test
    fun `refundedCredits not incremented and failure reported when refund call fails`() = runTest {
        val creditDeductor = FakeCreditDeductor(refundShouldFail = true)
        val repo = FakeGenerationRepository(failOnIndex = 0)
        val errorReporter = FakeErrorReporter()
        val useCase = GenerateBatchPreviewsUseCase(
            GeneratePreviewUseCase(repo, FakeHistoryRepository(), FakeErrorReporter()),
            creditDeductor,
            errorReporter
        )

        val progress = useCase(config).collectAll()

        assertEquals(0, progress.last().refundedCredits)
        assertEquals(1, errorReporter.recordedExceptions.size)
    }

    @Test
    fun `resume skips already-processed photos and does not re-deduct them`() = runTest {
        val creditDeductor = FakeCreditDeductor()
        val repo = FakeGenerationRepository()
        val useCase = GenerateBatchPreviewsUseCase(
            GeneratePreviewUseCase(repo, FakeHistoryRepository(), FakeErrorReporter()),
            creditDeductor,
            FakeErrorReporter()
        )
        val priorResult = GenerationResult.Success(
            generationId = "gen_prior",
            inputPhoto = photo1,
            previewUri = "preview_prior.jpg",
            style = testStyle,
            createdAt = 0L
        )
        val resumeState = BatchResumeState(results = listOf(priorResult))

        val progress = useCase(config, resumeState = resumeState).collectAll()

        // Only photo2 (the unfinished one) is deducted/generated — photo1 is not re-run.
        assertEquals(1, creditDeductor.deductKeys.size)
        assertEquals(1, repo.generateCallCount)
        assertEquals(2, progress.last().results.size)
        assertEquals(1, progress.last().currentIndex)
        assertTrue(progress.last().isComplete)
    }

    @Test
    fun `resume preserves refundedCredits carried over from the prior attempt`() = runTest {
        val creditDeductor = FakeCreditDeductor()
        val repo = FakeGenerationRepository()
        val useCase = GenerateBatchPreviewsUseCase(
            GeneratePreviewUseCase(repo, FakeHistoryRepository(), FakeErrorReporter()),
            creditDeductor,
            FakeErrorReporter()
        )
        val priorFailure = GenerationResult.Failure(
            inputPhoto = photo1,
            error = com.middleton.studiosnap.feature.home.domain.model.GenerationError.NETWORK
        )
        val resumeState = BatchResumeState(results = listOf(priorFailure), refundedCredits = 1)

        val progress = useCase(config, resumeState = resumeState).collectAll()

        assertEquals(1, progress.last().refundedCredits)
    }

    @Test
    fun `resume with a fully-processed resumeState emits a terminal progress instead of nothing`() = runTest {
        val creditDeductor = FakeCreditDeductor()
        val repo = FakeGenerationRepository()
        val useCase = GenerateBatchPreviewsUseCase(
            GeneratePreviewUseCase(repo, FakeHistoryRepository(), FakeErrorReporter()),
            creditDeductor,
            FakeErrorReporter()
        )
        val bothDone = listOf(
            GenerationResult.Success(
                generationId = "gen_1", inputPhoto = photo1, previewUri = "p1.jpg",
                style = testStyle, createdAt = 0L
            ),
            GenerationResult.Success(
                generationId = "gen_2", inputPhoto = photo2, previewUri = "p2.jpg",
                style = testStyle, createdAt = 0L
            )
        )
        val resumeState = BatchResumeState(results = bothDone)

        val progress = useCase(config, resumeState = resumeState).collectAll()

        // No further deductions/generations — everything was already done.
        assertEquals(0, creditDeductor.deductKeys.size)
        assertEquals(0, repo.generateCallCount)
        assertEquals(1, progress.size, "must emit exactly one terminal progress, not zero")
        assertTrue(progress.single().isComplete)
        assertEquals(bothDone, progress.single().results)
    }

    @Test
    fun `deduction failure aborts the flow`() = runTest {
        val creditDeductor = FakeCreditDeductor(deductShouldFail = true)
        val repo = FakeGenerationRepository()
        val useCase = GenerateBatchPreviewsUseCase(
            GeneratePreviewUseCase(repo, FakeHistoryRepository(), FakeErrorReporter()),
            creditDeductor,
            FakeErrorReporter()
        )

        var threw = false
        try {
            useCase(config).collectAll()
        } catch (e: Exception) {
            threw = true
        }

        assertTrue(threw)
        assertEquals(0, repo.generateCallCount)
    }

    private suspend fun Flow<BatchProgress>.collectAll(): List<BatchProgress> {
        val results = mutableListOf<BatchProgress>()
        collect { results.add(it) }
        return results
    }

    private class FakeCreditDeductor(
        private val deductShouldFail: Boolean = false,
        private val refundShouldFail: Boolean = false
    ) : CreditDeductor {
        val deductKeys = mutableListOf<String>()
        val refundKeys = mutableListOf<String>()

        override suspend fun deductGenerationCredit(idempotencyKey: String): Result<UserCredits> {
            if (deductShouldFail) return Result.failure(Exception("Insufficient credits"))
            deductKeys.add(idempotencyKey)
            return Result.success(UserCredits(10))
        }

        override suspend fun refundGenerationCredit(idempotencyKey: String): Result<UserCredits> {
            if (refundShouldFail) return Result.failure(Exception("Refund failed"))
            refundKeys.add(idempotencyKey)
            return Result.success(UserCredits(11))
        }
    }

    private class FakeGenerationRepository(
        failOnIndex: Int = -1,
        private val failIndices: Set<Int> = if (failOnIndex >= 0) setOf(failOnIndex) else emptySet()
    ) : GenerationRepository {
        var generateCallCount = 0
            private set

        override suspend fun generateImage(
            photo: ProductPhoto,
            prompt: String,
            style: Style,
            exportFormat: ExportFormat,
            quality: GenerationQuality,
            deductionKey: String?,
            onProgress: (suspend (Float) -> Unit)?
        ): Result<GenerationResult.Success> {
            val index = generateCallCount
            generateCallCount++
            if (index in failIndices) {
                return Result.failure(RuntimeException("API failure"))
            }
            return Result.success(
                GenerationResult.Success(
                    generationId = "gen_$index",
                    inputPhoto = photo,
                    previewUri = "preview_$index.jpg",
                    style = style,
                    createdAt = 1000L * index
                )
            )
        }

        override suspend fun downloadFullRes(generationId: String) =
            Result.success("/path/full_$generationId.jpg")
    }

    private class FakeHistoryRepository : HistoryRepository {
        override fun getAll(): Flow<List<GenerationResult.Success>> = flowOf(emptyList())
        override fun getSessions(): Flow<List<HistorySession>> = flowOf(emptyList())
        override fun getBySessionId(sessionId: String): Flow<List<GenerationResult.Success>> = flowOf(emptyList())
        override suspend fun save(result: GenerationResult.Success) {}
        override suspend fun saveAll(results: List<GenerationResult.Success>) {}
        override suspend fun getById(id: String): GenerationResult.Success? = null
        override suspend fun delete(id: String) {}
        override suspend fun markAsPurchased(id: String, fullResLocalUri: String) {}
        override suspend fun updateSessionLabel(sessionId: String, label: String) {}
        override suspend fun deleteSession(sessionId: String) {}
    }
}
