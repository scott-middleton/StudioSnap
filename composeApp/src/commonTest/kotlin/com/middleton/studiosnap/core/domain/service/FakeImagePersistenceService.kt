package com.middleton.studiosnap.core.domain.service

/**
 * Test fake for ImagePersistenceService.
 * Returns minimal JPEG bytes for any cache read by default.
 * Records paths saved to persistent storage for assertion.
 *
 * @param cacheReadReturnsNull When true, readImageFromCache returns null (simulates missing file).
 */
class FakeImagePersistenceService(
    private val cacheReadReturnsNull: Boolean = false
) : ImagePersistenceService {
    val persistentPaths = mutableListOf<String>()
    val deletedPaths = mutableListOf<String>()

    // Minimal valid JPEG header + footer
    private val fakeImageBytes = byteArrayOf(
        0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xD9.toByte()
    )

    override suspend fun readImageFromCache(filePath: String): ByteArray? =
        if (cacheReadReturnsNull) null else fakeImageBytes

    override suspend fun saveImageToPersistent(bytes: ByteArray, fileName: String): String {
        val path = "/persistent/$fileName"
        persistentPaths.add(path)
        return path
    }

    override suspend fun deleteFile(filePath: String): Boolean {
        deletedPaths.add(filePath)
        return true
    }
}
