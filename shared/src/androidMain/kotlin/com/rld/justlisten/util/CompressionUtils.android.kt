package com.rld.justlisten.util

import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

actual fun gzipCompress(bytes: ByteArray): ByteArray {
    val bos = ByteArrayOutputStream()
    GZIPOutputStream(bos).use { it.write(bytes) }
    return bos.toByteArray()
}

actual fun gzipDecompress(bytes: ByteArray): ByteArray {
    val bis = ByteArrayInputStream(bytes)
    return GZIPInputStream(bis).use { it.readBytes() }
}

actual fun base64UrlEncode(bytes: ByteArray): String {
    return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
}

actual fun base64UrlDecode(str: String): ByteArray {
    return Base64.decode(str, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
}
