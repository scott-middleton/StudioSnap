package com.middleton.studiosnap.core.data.util

/**
 * Platform-specific function to read raw bytes from a URI string.
 * Handles content:// URIs on Android (via ContentResolver) and
 * file paths on iOS.
 */
expect fun readBytesFromUri(uri: String): ByteArray?
