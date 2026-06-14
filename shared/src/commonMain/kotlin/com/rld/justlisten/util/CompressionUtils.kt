package com.rld.justlisten.util

expect fun gzipCompress(bytes: ByteArray): ByteArray
expect fun gzipDecompress(bytes: ByteArray): ByteArray
expect fun base64UrlEncode(bytes: ByteArray): String
expect fun base64UrlDecode(str: String): ByteArray
