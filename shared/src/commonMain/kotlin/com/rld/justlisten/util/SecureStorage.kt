package com.rld.justlisten.util

interface SecureStorage {
    fun saveToken(key: String, value: String)
    fun getToken(key: String): String?
    fun clear()
}
