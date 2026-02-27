package com.middleton.studiosnap.core.data.cache

import com.middleton.studiosnap.core.data.util.convertToJpeg
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.use

/**
 * Manages temporary image file caching for displaying large images.
 * Saves ByteArrays to disk to avoid OOM when loading large images.
 */
class ImageCacheManager :
    com.middleton.studiosnap.core.domain.service.ImageStorage,
    com.middleton.studiosnap.core.domain.service.ImagePersistenceService {
    private val fileSystem = FileSystem.SYSTEM

    override suspend fun saveImage(bytes: ByteArray, fileName: String): String =
        saveImageToCache(bytes, fileName)

    suspend fun saveImageToCache(bytes: ByteArray, fileName: String): String = withContext(Dispatchers.Default) {
        val cacheDir = getCacheDirectory()
        val imagePath = (cacheDir.toString() + "/$fileName").toPath()

        // Write bytes to file
        fileSystem.write(imagePath) {
            write(bytes)
        }

        imagePath.toString()
    }

    /**
     * Saves an image to cache using a streaming approach to avoid loading entire image into memory.
     * The writeBlock receives a Sink to write data to.
     */
    suspend fun saveImageStreamToCache(
        fileName: String,
        writeBlock: suspend (okio.BufferedSink) -> Unit
    ): String = withContext(Dispatchers.Default) {
        val cacheDir = getCacheDirectory()
        val imagePath = (cacheDir.toString() + "/$fileName").toPath()

        fileSystem.sink(imagePath).buffer().use { sink ->
            writeBlock(sink)
        }

        imagePath.toString()
    }

    /**
     * Reads an image from the cache as a ByteArray.
     * @param filePath The full path to the cached image file
     * @return ByteArray of the image data, or null if file doesn't exist
     */
    override suspend fun readImageFromCache(filePath: String): ByteArray? = withContext(Dispatchers.Default) {
        try {
            val path = filePath.toPath()
            if (fileSystem.exists(path)) {
                fileSystem.read(path) {
                    readByteArray()
                }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Deletes a specific file from the cache.
     * @param filePath The full path to the file to delete
     * @return true if deleted successfully, false otherwise
     */
    override suspend fun readFile(filePath: String): ByteArray? = readImageFromCache(filePath)

    override suspend fun readFileHeader(filePath: String, maxBytes: Int): ByteArray? = withContext(Dispatchers.Default) {
        try {
            val path = filePath.toPath()
            if (fileSystem.exists(path)) {
                fileSystem.read(path) {
                    readByteArray(minOf(maxBytes.toLong(), fileSystem.metadata(path).size ?: maxBytes.toLong()))
                }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun deleteFile(filePath: String): Boolean = withContext(Dispatchers.Default) {
        try {
            val path = filePath.toPath()
            if (fileSystem.exists(path)) {
                fileSystem.delete(path)
                true
            } else {
                false
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Deletes multiple files from the cache.
     * @param filePaths List of full paths to delete
     */
    suspend fun deleteFiles(filePaths: List<String>) = withContext(Dispatchers.Default) {
        filePaths.forEach { filePath ->
            try {
                val path = filePath.toPath()
                if (fileSystem.exists(path)) {
                    fileSystem.delete(path)
                }
            } catch (_: Exception) {
                // Continue deleting other files even if one fails
            }
        }
    }

    /**
     * Converts a cached image file to JPEG format.
     * Reads the file, converts to JPEG, saves with .jpg extension, and deletes the original.
     * Returns the new file path, or the original path if conversion fails.
     */
    suspend fun convertCachedImageToJpeg(
        filePath: String,
        quality: Int = 85
    ): String = withContext(Dispatchers.Default) {
        try {
            val path = filePath.toPath()
            if (!fileSystem.exists(path)) return@withContext filePath

            val imageBytes = fileSystem.read(path) { readByteArray() }
            val jpegBytes = imageBytes.convertToJpeg(quality)
                ?: return@withContext filePath

            val jpegPath = filePath.replaceSuffix(".jpg")
            val jpegOkioPath = jpegPath.toPath()

            fileSystem.write(jpegOkioPath) { write(jpegBytes) }

            // Delete original if it's a different file
            if (jpegPath != filePath) {
                fileSystem.delete(path)
            }

            jpegPath
        } catch (_: Exception) {
            filePath
        }
    }

    override suspend fun saveImageToPersistent(bytes: ByteArray, fileName: String): String = withContext(Dispatchers.Default) {
        val persistentDir = getPersistentDirectory()
        val imagePath = (persistentDir.toString() + "/$fileName").toPath()

        fileSystem.write(imagePath) {
            write(bytes)
        }

        imagePath.toString()
    }

    suspend fun readImageFromPersistent(filePath: String): ByteArray? = withContext(Dispatchers.Default) {
        try {
            val path = filePath.toPath()
            if (fileSystem.exists(path)) {
                fileSystem.read(path) {
                    readByteArray()
                }
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    suspend fun clearCache() = withContext(Dispatchers.Default) {
        val cacheDir = getCacheDirectory()
        try {
            fileSystem.deleteRecursively(cacheDir)
            fileSystem.createDirectory(cacheDir)
        } catch (_: Exception) {
            // Ignore errors during cleanup
        }
    }

    private fun getCacheDirectory(): Path {
        val tempDir = getSystemTempDirectory()
        val cacheDir = (tempDir.toString() + "/image_clone_cache").toPath()

        if (!fileSystem.exists(cacheDir)) {
            fileSystem.createDirectory(cacheDir)
        }

        return cacheDir
    }

    private fun getPersistentDirectory(): Path {
        val persistentDir = (getPersistentBaseDirectory().toString() + "/image_clone_persistent").toPath()

        if (!fileSystem.exists(persistentDir)) {
            fileSystem.createDirectory(persistentDir)
        }

        return persistentDir
    }
}

expect fun getSystemTempDirectory(): Path

expect fun getPersistentBaseDirectory(): Path

private fun String.replaceSuffix(newExtension: String): String {
    val dotIndex = lastIndexOf('.')
    return if (dotIndex >= 0) substring(0, dotIndex) + newExtension else this + newExtension
}
