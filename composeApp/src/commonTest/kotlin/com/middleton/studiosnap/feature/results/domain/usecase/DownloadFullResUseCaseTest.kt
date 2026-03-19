package com.middleton.studiosnap.feature.results.domain.usecase

import com.middleton.studiosnap.core.domain.model.UiText
import com.middleton.studiosnap.core.domain.model.UserCredits
import com.middleton.studiosnap.core.domain.service.CreditDeductor
import com.middleton.studiosnap.core.domain.service.ErrorReporter
import com.middleton.studiosnap.feature.home.domain.model.GenerationResult
import com.middleton.studiosnap.feature.home.domain.model.ProductPhoto
import com.middleton.studiosnap.feature.home.domain.model.Style
import com.middleton.studiosnap.feature.home.domain.model.StyleCategory
import com.middleton.studiosnap.feature.home.domain.repository.GenerationRepository
import com.middleton.studiosnap.feature.home.domain.model.ExportFormat
import com.middleton.studiosnap.feature.home.domain.model.GenerationQuality
import com.middleton.studiosnap.feature.history.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class DownloadFullResUseCaseTest {

    private val testStyle = Style(
        id = "test", displayName = UiText.DynamicString("Test"), categories = setOf(StyleCategory.ALL),
        thumbnail = null, kontextPrompt = "test"
    )

    @Test
    fun `successful download deducts credit and marks as purchased`() = runTest {
        val history = FakeHistoryRepository()
        val useCase = DownloadFullResUseCase(
            generationRepository = FakeGenerationRepository(downloadResult = Result.success("/path/full.jpg")),
            historyRepository = history,
            creditDeductor = FakeCreditDeductor(shouldSucceed = true),
            errorReporter = FakeErrorReporter()
        )

        val result = useCase("gen_1")
        assertTrue(result.isSuccess)
        assertTrue(history.purchasedIds.contains("gen_1"))
    }

    @Test
    fun `insufficient credits returns failure`() = runTest {
        val useCase = DownloadFullResUseCase(
            generationRepository = FakeGenerationRepository(downloadResult = Result.success("/path/full.jpg")),
            historyRepository = FakeHistoryRepository(),
            creditDeductor = FakeCreditDeductor(shouldSucceed = false),
            errorReporter = FakeErrorReporter()
        )

        val result = useCase("gen_1")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is InsufficientCreditsException)
    }

    @Test
    fun `download failure refunds credit`() = runTest {
        val deductor = FakeCreditDeductor(shouldSucceed = true)
        val useCase = DownloadFullResUseCase(
            generationRepository = FakeGenerationRepository(
                downloadResult = Result.failure(Exception("Network error"))
            ),
            historyRepository = FakeHistoryRepository(),
            creditDeductor = deductor,
            errorReporter = FakeErrorReporter()
        )

        val result = useCase("gen_1")
        assertTrue(result.isFailure)
        assertTrue(deductor.refundCalled)
    }

    // --- Fakes ---

    private class FakeGenerationRepository(
        private val downloadResult: Result<String>
    ) : GenerationRepository {
        override suspend fun generateImage(
            photo: ProductPhoto, style: Style, shadow: Boolean,
            reflection: Boolean, exportFormat: ExportFormat, quality: GenerationQuality
        ) = Result.success(
            GenerationResult.Success(
                generationId = "gen_1", inputPhoto = photo,
                previewUri = "preview.jpg", style = style, createdAt = 0L
            )
        )
        override suspend fun downloadFullRes(generationId: String) = downloadResult
    }

    private class FakeHistoryRepository : HistoryRepository {
        val purchasedIds = mutableListOf<String>()

        override fun getAll(): Flow<List<GenerationResult.Success>> = flowOf(emptyList())
        override suspend fun save(result: GenerationResult.Success) {}
        override suspend fun saveAll(results: List<GenerationResult.Success>) {}
        override suspend fun getById(id: String): GenerationResult.Success? = null
        override suspend fun delete(id: String) {}
        override suspend fun markAsPurchased(id: String, fullResLocalUri: String) {
            purchasedIds.add(id)
        }
    }

    private class FakeCreditDeductor(private val shouldSucceed: Boolean) : CreditDeductor {
        var refundCalled = false

        override suspend fun deductCredits(amount: Int, reason: String): Result<UserCredits> {
            return if (shouldSucceed) Result.success(UserCredits(10)) else Result.failure(Exception("No credits"))
        }

        override suspend fun refundCredits(amount: Int, reason: String): Result<UserCredits> {
            refundCalled = true
            return Result.success(UserCredits(11))
        }
    }

    private class FakeErrorReporter : ErrorReporter {
        override fun recordException(exception: Throwable) {}
    }
}
