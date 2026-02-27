package com.middleton.studiosnap.core.presentation.imagepicker

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * Converts the image at the given URI to a ByteArray.
 * Platform-specific implementation required.
 */
expect suspend fun ImagePickerResult.toByteArray(): ByteArray?

/**
 * Converts ByteArray to Base64 data URI for API transmission.
 */
@OptIn(ExperimentalEncodingApi::class)
fun ByteArray.toBase64DataUri(): String {
    val base64String = Base64.encode(this)
    return "data:image/jpeg;base64,$base64String"
}