package com.middleton.studiosnap.core.domain.service

interface ImageStorage {
    suspend fun saveImage(bytes: ByteArray, fileName: String): String
    suspend fun deleteFile(filePath: String): Boolean
    suspend fun readFile(filePath: String): ByteArray?

    /**
     * Reads up to [maxBytes] from the start of a file. Useful for reading
     * image headers without loading the full file into memory.
     */
    suspend fun readFileHeader(filePath: String, maxBytes: Int): ByteArray?
}
