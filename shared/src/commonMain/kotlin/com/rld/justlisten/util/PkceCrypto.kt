package com.rld.justlisten.util

interface PkceCrypto {
    fun generateCodeVerifier(): String
    fun generateCodeChallenge(verifier: String): String
}
