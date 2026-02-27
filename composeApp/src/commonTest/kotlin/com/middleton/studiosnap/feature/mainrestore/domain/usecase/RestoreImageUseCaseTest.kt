package com.middleton.studiosnap.feature.mainrestore.domain.usecase

import com.middleton.studiosnap.core.domain.model.UserCredits
import com.middleton.studiosnap.core.domain.service.CreditDeductor
import com.middleton.studiosnap.core.domain.service.FakeErrorReporter
import com.middleton.studiosnap.core.domain.service.ImageStorage
import com.middleton.studiosnap.feature.mainrestore.domain.repository.RestorationRepository
import com.middleton.studiosnap.feature.mainrestore.domain.repository.RestorationStatusResponse
import com.middleton.studiosnap.feature.mainrestore.domain.model.RestorationResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okio.FileSystem
import okio.Path.Companion.toPath
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RestoreImageUseCaseTest {

    @Test
    fun `successful restoration produces correct state flow`() = runTest {
        val useCase = createUseCase()

        val states = useCase(
            imageBytes = TEST_IMAGE_BYTES
        ).toList()

        val stateTypes = states.map { it::class }
        val preparingIndex = stateTypes.indexOfFirst { it == RestoreImageUseCase.RestoreImageState.Preparing::class }
        val uploadingIndex = stateTypes.indexOfFirst { it == RestoreImageUseCase.RestoreImageState.Uploading::class }
        val processingIndex = stateTypes.indexOfFirst { it == RestoreImageUseCase.RestoreImageState.Processing::class }
        val successIndex = stateTypes.indexOfFirst { it == RestoreImageUseCase.RestoreImageState.Success::class }

        assertTrue(preparingIndex >= 0, "Should have Preparing state")
        assertTrue(uploadingIndex > preparingIndex, "Uploading should come after Preparing")
        assertTrue(processingIndex > uploadingIndex, "Processing should come after Uploading")
        assertTrue(successIndex > processingIndex, "Success should come after Processing")
    }

    @Test
    fun `successful paid restoration deducts credits`() = runTest {
        val fakeDeductor = FakeCreditDeductor()
        val useCase = createUseCase(creditDeductor = fakeDeductor)

        useCase(
            imageBytes = TEST_IMAGE_BYTES,
            isFreeRestoration = false
        ).toList()

        assertEquals(1, fakeDeductor.timesDeducted, "Credits should be deducted once on success")
    }

    @Test
    fun `successful free restoration does not deduct credits`() = runTest {
        val fakeDeductor = FakeCreditDeductor()
        val useCase = createUseCase(creditDeductor = fakeDeductor)

        useCase(
            imageBytes = TEST_IMAGE_BYTES,
            isFreeRestoration = true
        ).toList()

        assertEquals(0, fakeDeductor.timesDeducted, "Credits should not be deducted for free restoration")
    }

    @Test
    fun `free restoration result is watermarked with zero credits used`() = runTest {
        val useCase = createUseCase()

        val states = useCase(
            imageBytes = TEST_IMAGE_BYTES,
            isFreeRestoration = true
        ).toList()

        val successState = states.last() as RestoreImageUseCase.RestoreImageState.Success
        assertTrue(successState.result.isWatermarked)
        assertEquals(0, successState.result.creditsUsed)
    }

    @Test
    fun `paid restoration result is not watermarked with 1 credit used`() = runTest {
        val useCase = createUseCase()

        val states = useCase(
            imageBytes = TEST_IMAGE_BYTES,
            isFreeRestoration = false
        ).toList()

        val successState = states.last() as RestoreImageUseCase.RestoreImageState.Success
        assertFalse(successState.result.isWatermarked)
        assertEquals(1, successState.result.creditsUsed)
    }

    @Test
    fun `restoration failure produces Error state`() = runTest {
        val fakeRepo = FakeRestorationRepository(restorationShouldFail = true)
        val useCase = createUseCase(restorationRepository = fakeRepo)

        val states = useCase(
            imageBytes = TEST_IMAGE_BYTES
        ).toList()

        assertTrue(
            states.last() is RestoreImageUseCase.RestoreImageState.Error,
            "Expected Error state when restoration fails"
        )
    }

    @Test
    fun `credits are refunded when restoration fails`() = runTest {
        val fakeRepo = FakeRestorationRepository(restorationShouldFail = true)
        val fakeDeductor = FakeCreditDeductor()
        val useCase = createUseCase(
            restorationRepository = fakeRepo,
            creditDeductor = fakeDeductor
        )

        useCase(
            imageBytes = TEST_IMAGE_BYTES,
            isFreeRestoration = false
        ).toList()

        assertEquals(1, fakeDeductor.timesDeducted, "Credits should be deducted upfront")
        assertEquals(1, fakeDeductor.timesRefunded, "Credits should be refunded when restoration fails")
    }

    @Test
    fun `credits are not refunded on free restoration failure`() = runTest {
        val fakeRepo = FakeRestorationRepository(restorationShouldFail = true)
        val fakeDeductor = FakeCreditDeductor()
        val useCase = createUseCase(
            restorationRepository = fakeRepo,
            creditDeductor = fakeDeductor
        )

        useCase(
            imageBytes = TEST_IMAGE_BYTES,
            isFreeRestoration = true
        ).toList()

        assertEquals(0, fakeDeductor.timesDeducted, "Credits should not be deducted for free restoration")
        assertEquals(0, fakeDeductor.timesRefunded, "Credits should not be refunded for free restoration")
    }

    @Test
    fun `downloading state includes progress`() = runTest {
        val useCase = createUseCase()

        val states = useCase(
            imageBytes = TEST_IMAGE_BYTES
        ).toList()

        val downloadingStates = states.filterIsInstance<RestoreImageUseCase.RestoreImageState.Downloading>()
        assertTrue(downloadingStates.isNotEmpty(), "Should have at least one Downloading state")
    }

    @Test
    fun `free restoration result path is the clean restored image`() = runTest {
        val useCase = createUseCase()

        val states = useCase(
            imageBytes = TEST_IMAGE_BYTES,
            isFreeRestoration = true
        ).toList()

        val successState = states.last() as RestoreImageUseCase.RestoreImageState.Success
        assertFalse(
            successState.result.restoredImagePath.contains("_watermarked"),
            "Free restoration should use clean image path (watermark applied on share)"
        )
    }

    // --- buildPrompt tests ---

    @Test
    fun `buildPrompt with empty user prompt returns base prompt only`() {
        val result = RestoreImageUseCase.buildPrompt("")

        assertFalse(result.contains("Additional user instructions"))
        assertTrue(result.contains("Restore this old photograph"))
    }

    @Test
    fun `buildPrompt with blank user prompt returns base prompt only`() {
        val result = RestoreImageUseCase.buildPrompt("   ")

        assertFalse(result.contains("Additional user instructions"))
    }

    @Test
    fun `buildPrompt with user prompt appends instructions`() {
        val result = RestoreImageUseCase.buildPrompt("The dress is blue")

        assertTrue(result.contains("Restore this old photograph"))
        assertTrue(result.contains("Additional user instructions: The dress is blue"))
    }

    // --- Prompt forwarding tests ---

    @Test
    fun `restoration without user prompt sends base prompt to repository`() = runTest {
        val fakeRepo = FakeRestorationRepository()
        val useCase = createUseCase(restorationRepository = fakeRepo)

        useCase(imageBytes = TEST_IMAGE_BYTES).toList()

        val receivedPrompt = fakeRepo.lastReceivedPrompt
        assertTrue(receivedPrompt != null)
        assertTrue(receivedPrompt.contains("Restore this old photograph"))
        assertFalse(receivedPrompt.contains("Additional user instructions"))
    }

    @Test
    fun `restoration with user prompt sends combined prompt to repository`() = runTest {
        val fakeRepo = FakeRestorationRepository()
        val useCase = createUseCase(restorationRepository = fakeRepo)

        useCase(
            imageBytes = TEST_IMAGE_BYTES,
            userPrompt = "Make the car red"
        ).toList()

        val receivedPrompt = fakeRepo.lastReceivedPrompt
        assertTrue(receivedPrompt != null)
        assertTrue(receivedPrompt.contains("Restore this old photograph"))
        assertTrue(receivedPrompt.contains("Additional user instructions: Make the car red"))
    }

    companion object {
        private val TEST_IMAGE_BYTES = ByteArray(100)
    }

    // --- Helpers ---

    private fun createUseCase(
        restorationRepository: RestorationRepository = FakeRestorationRepository(),
        creditDeductor: CreditDeductor = FakeCreditDeductor(),
        imageStorage: FakeImageStorage = FakeImageStorage(),
        errorReporter: com.middleton.studiosnap.core.domain.service.ErrorReporter = FakeErrorReporter()
    ): RestoreImageUseCase {
        return RestoreImageUseCase(
            restorationRepository = restorationRepository,
            creditDeductor = creditDeductor,
            imageStorage = imageStorage,
            errorReporter = errorReporter
        )
    }
}

// --- Fakes ---

class FakeImageStorage : ImageStorage {
    var deletedFiles = mutableListOf<String>()
        private set

    override suspend fun saveImage(bytes: ByteArray, fileName: String): String {
        val tempDir = System.getProperty("java.io.tmpdir")
        return "$tempDir/$fileName"
    }

    override suspend fun deleteFile(filePath: String): Boolean {
        deletedFiles.add(filePath)
        return true
    }

    override suspend fun readFile(filePath: String): ByteArray? = null

    override suspend fun readFileHeader(filePath: String, maxBytes: Int): ByteArray? = null
}

class FakeRestorationRepository(
    private val restorationShouldFail: Boolean = false
) : RestorationRepository {
    override var currentResult: RestorationResult? = null

    var lastReceivedPrompt: String? = null
        private set

    override suspend fun createRestoration(
        imageBase64DataUri: String,
        prompt: String
    ): Result<String> {
        lastReceivedPrompt = prompt
        return if (restorationShouldFail) {
            Result.failure(Exception("Restoration API failed"))
        } else {
            Result.success("fake-prediction-id")
        }
    }

    override suspend fun getRestorationStatus(predictionId: String): Result<RestorationStatusResponse> {
        return Result.success(
            RestorationStatusResponse(
                status = "succeeded",
                outputUrl = "https://fake.replicate.com/result.png"
            )
        )
    }

    override suspend fun createUpscale(imageUrl: String, scaleFactor: Int): Result<String> {
        return if (restorationShouldFail) {
            Result.failure(Exception("Upscale API failed"))
        } else {
            Result.success("fake-upscale-prediction-id")
        }
    }

    override suspend fun downloadRestoredImage(url: String): Result<ByteArray> {
        return Result.success(ByteArray(100))
    }

    override suspend fun downloadRestoredImageToFile(url: String): Result<String> {
        return Result.success(writeFakeFile("restored"))
    }

    override suspend fun downloadRestoredImageToFileWithProgress(
        url: String,
        onProgress: suspend (Float) -> Unit
    ): Result<String> {
        onProgress(0.5f)
        onProgress(1.0f)
        return Result.success(writeFakeFile("restored_progress"))
    }

    private fun writeFakeFile(name: String): String {
        val tempDir = System.getProperty("java.io.tmpdir")
        val filePath = "$tempDir/image_clone_test_${name}_${System.currentTimeMillis()}.png".toPath()
        FileSystem.SYSTEM.write(filePath) { write(ByteArray(100)) }
        return filePath.toString()
    }

    override fun setResult(result: RestorationResult) {
        currentResult = result
    }

    override fun clearResult() {
        currentResult = null
    }

    override suspend fun getImageDimensions(filePath: String): Pair<Int, Int>? = Pair(1024, 768)
}

class FakeCreditDeductor : CreditDeductor {
    var timesDeducted = 0
        private set
    var timesRefunded = 0
        private set

    override suspend fun deductCredits(amount: Int, reason: String): Result<UserCredits> {
        timesDeducted++
        return Result.success(UserCredits(tokenCount = 10 - amount))
    }

    override suspend fun refundCredits(amount: Int, reason: String): Result<UserCredits> {
        timesRefunded++
        return Result.success(UserCredits(tokenCount = 10 + amount))
    }
}

// FakeErrorReporter moved to commonTest/core/domain/service/FakeErrorReporter.kt
