package com.middleton.studiosnap.core.data.repository

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.ContextCompat
import com.middleton.studiosnap.core.domain.repository.GalleryRepository
import com.middleton.studiosnap.core.presentation.imagepicker.AndroidContextHolder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.net.toUri
import java.io.File
import java.io.FileInputStream

class AndroidGalleryRepository : GalleryRepository {

    override suspend fun saveImage(filePath: String, displayName: String): Result<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val context = AndroidContextHolder.context
                    ?: throw Exception("Android context not available")

                // API 28 and below requires WRITE_EXTERNAL_STORAGE
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    val hasPermission = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.WRITE_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                    if (!hasPermission) {
                        throw GalleryPermissionException("Storage permission required to save images")
                    }
                }

                val sourceFile = File(filePath)
                if (!sourceFile.exists()) {
                    throw Exception("Source file not found: $filePath")
                }

                val contentValues = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.jpg")
                    put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(
                            MediaStore.Images.Media.RELATIVE_PATH,
                            Environment.DIRECTORY_PICTURES + "/StudioSnap"
                        )
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?: throw Exception("Failed to create MediaStore entry")

                resolver.openOutputStream(uri)?.use { outputStream ->
                    FileInputStream(sourceFile).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                } ?: throw Exception("Failed to open output stream")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }

                uri.toString()
            }
        }

    override suspend fun deleteImage(galleryUri: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                val context = AndroidContextHolder.context
                    ?: throw Exception("Android context not available")

                val uri = galleryUri.toUri()
                val deleted = context.contentResolver.delete(uri, null, null)
                if (deleted == 0) {
                    // Image may already be deleted — not an error
                    println("GalleryRepository: No rows deleted for $galleryUri (may already be removed)")
                }
            }
        }

    override suspend fun imageExists(galleryUri: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val context = AndroidContextHolder.context ?: return@withContext false
                val uri = galleryUri.toUri()
                context.contentResolver.query(uri, arrayOf(MediaStore.Images.Media._ID), null, null, null)
                    ?.use { cursor -> cursor.count > 0 }
                    ?: false
            } catch (_: Exception) {
                false
            }
        }
}

class GalleryPermissionException(message: String) : Exception(message)
