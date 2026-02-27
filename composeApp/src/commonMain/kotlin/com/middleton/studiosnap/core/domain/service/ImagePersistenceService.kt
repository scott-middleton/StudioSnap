package com.middleton.studiosnap.core.domain.service

/**
 * Abstraction over persistent and cached image file storage.
 * Used by use cases that need to move images between temp cache and persistent storage.
 */
interface ImagePersistenceService {
    /** Reads an image from the temp cache. Returns null if the file does not exist. */
    suspend fun readImageFromCache(filePath: String): ByteArray?

    /** Saves image bytes to persistent storage (survives OS cache purges). Returns the stored path. */
    suspend fun saveImageToPersistent(bytes: ByteArray, fileName: String): String

    /** Deletes a file at the given path. Returns true if deleted, false otherwise. */
    suspend fun deleteFile(filePath: String): Boolean
}
