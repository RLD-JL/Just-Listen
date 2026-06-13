package com.rld.justlisten.util

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class AndroidSecureStorage(private val context: Context) : SecureStorage {
    
    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    
    private val sharedPreferences = EncryptedSharedPreferences.create(
        "secure_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

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
