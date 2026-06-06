package com.rld.justlisten.util

import android.content.Context

class AndroidSecureStorage(private val context: Context) : SecureStorage {
    
    private val sharedPreferences = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)

    override fun saveToken(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    override fun getToken(key: String): String? {
        return sharedPreferences.getString(key, null)
    }

    override fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}
