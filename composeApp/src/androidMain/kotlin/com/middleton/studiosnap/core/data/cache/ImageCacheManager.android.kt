package com.middleton.studiosnap.core.data.cache

import com.middleton.studiosnap.core.presentation.imagepicker.AndroidContextHolder
import okio.Path
import okio.Path.Companion.toPath

actual fun getSystemTempDirectory(): Path {
    val context = AndroidContextHolder.context
    return if (context != null) {
        context.cacheDir.absolutePath.toPath()
    } else {
        // Fallback for JVM unit tests where Android context is unavailable
        (System.getProperty("java.io.tmpdir") ?: "/tmp").toPath()
    }
}

actual fun getPersistentBaseDirectory(): Path {
    val context = AndroidContextHolder.context
    return if (context != null) {
        context.filesDir.absolutePath.toPath()
    } else {
        (System.getProperty("java.io.tmpdir") ?: "/tmp").toPath()
    }
}
