package com.rld.justlisten.util

import platform.Foundation.*
import platform.CoreCrypto.*
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.usePinned
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.refTo

class IosPkceCrypto : PkceCrypto {
    
    override fun generateCodeVerifier(): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"
        return (1..64).map { allowedChars.random() }.joinToString("")
    }

    @OptIn(ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)
    override fun generateCodeChallenge(verifier: String): String {
        val nsString = NSString.create(string = verifier)
        val data = nsString.dataUsingEncoding(NSASCIIStringEncoding) ?: return verifier
        val digest = ByteArray(CC_SHA256_DIGEST_LENGTH)
        var base64String = ""
        digest.usePinned { pinned ->
            CC_SHA256(data.bytes, data.length.toUInt(), pinned.addressOf(0).reinterpret())
            val digestData = NSData.create(bytes = pinned.addressOf(0), length = CC_SHA256_DIGEST_LENGTH.toULong())
            base64String = digestData.base64EncodedStringWithOptions(0UL)
        }
        return base64String
            .replace("+", "-")
            .replace("/", "_")
            .replace("=", "")
            .trim()
    }
}
