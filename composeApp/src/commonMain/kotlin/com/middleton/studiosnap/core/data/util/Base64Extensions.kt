package com.middleton.studiosnap.core.data.util

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@OptIn(ExperimentalEncodingApi::class)
fun ByteArray.toBase64DataUri(): String {
    val base64String = Base64.encode(this)
    return "data:image/jpeg;base64,$base64String"
}
