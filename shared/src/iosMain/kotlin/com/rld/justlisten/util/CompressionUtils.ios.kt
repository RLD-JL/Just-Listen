package com.rld.justlisten.util

import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.BetaInteropApi

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun gzipCompress(bytes: ByteArray): ByteArray {
    if (bytes.isEmpty()) return ByteArray(0)
    val nsData = bytes.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
    }
    val compressed = nsData.compressedDataUsingAlgorithm(NSDataCompressionAlgorithmZlib, error = null) ?: return ByteArray(0)
    return compressed.toByteArray()
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun gzipDecompress(bytes: ByteArray): ByteArray {
    if (bytes.isEmpty()) return ByteArray(0)
    val nsData = bytes.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
    }
    val decompressed = nsData.decompressedDataUsingAlgorithm(NSDataCompressionAlgorithmZlib, error = null) ?: return ByteArray(0)
    return decompressed.toByteArray()
}

@OptIn(ExperimentalForeignApi::class)
fun NSData.toByteArray(): ByteArray {
    val size = this.length.toInt()
    val byteArray = ByteArray(size)
    if (size > 0) {
        byteArray.usePinned { pinned ->
            platform.posix.memcpy(pinned.addressOf(0), this.bytes, this.length)
        }
    }
    return byteArray
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun base64UrlEncode(bytes: ByteArray): String {
    if (bytes.isEmpty()) return ""
    @OptIn(ExperimentalForeignApi::class)
    val nsData = bytes.usePinned { pinned ->
        NSData.create(bytes = pinned.addressOf(0), length = bytes.size.toULong())
    }
    val base64String = nsData.base64EncodedStringWithOptions(0UL)
    return base64String
        .replace("+", "-")
        .replace("/", "_")
        .replace("=", "")
        .trim()
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
actual fun base64UrlDecode(str: String): ByteArray {
    if (str.isEmpty()) return ByteArray(0)
    var base64 = str.replace("-", "+").replace("_", "/")
    while (base64.length % 4 != 0) {
        base64 += "="
    }
    val nsData = NSData.create(base64EncodedString = base64, options = 0UL) ?: return ByteArray(0)
    return nsData.toByteArray()
}
