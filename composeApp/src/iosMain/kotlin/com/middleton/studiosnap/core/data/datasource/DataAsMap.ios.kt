package com.middleton.studiosnap.core.data.datasource

import dev.gitlive.firebase.functions.HttpsCallableResult
import dev.gitlive.firebase.functions.ios

internal actual fun HttpsCallableResult.dataAsMap(
    functionName: String
): Map<String, Any?> {
    val raw = this.ios.data()
    if (raw is Map<*, *>) {
        @Suppress("UNCHECKED_CAST")
        return raw as Map<String, Any?>
    }
    throw IllegalStateException(
        "Unexpected response type from $functionName: ${raw?.let { it::class.simpleName } ?: "null"}"
    )
}