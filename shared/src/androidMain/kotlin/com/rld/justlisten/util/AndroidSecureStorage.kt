package com.rld.justlisten.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class AndroidSecureStorage(private val context: Context) : SecureStorage {

    private val sharedPreferences = context.getSharedPreferences("secure_prefs", Context.MODE_PRIVATE)
    private val keyAlias = "JustListenSecureKey"
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    init {
        if (!keyStore.containsAlias(keyAlias)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                "AndroidKeyStore"
            )
            keyGenerator.init(
                KeyGenParameterSpec.Builder(
                    keyAlias,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .build()
            )
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey {
        return (keyStore.getEntry(keyAlias, null) as KeyStore.SecretKeyEntry).secretKey
    }

    override fun saveToken(key: String, value: String) {
        try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            val encryptedBytes = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
            val iv = cipher.iv

            val base64Encrypted = Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
            val base64Iv = Base64.encodeToString(iv, Base64.DEFAULT)

            sharedPreferences.edit()
                .putString(key, base64Encrypted)
                .putString("${key}_iv", base64Iv)
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getToken(key: String): String? {
        try {
            val base64Encrypted = sharedPreferences.getString(key, null) ?: return null
            val base64Iv = sharedPreferences.getString("${key}_iv", null) ?: return null

            val encryptedBytes = Base64.decode(base64Encrypted, Base64.DEFAULT)
            val iv = Base64.decode(base64Iv, Base64.DEFAULT)

            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            return String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    override fun clear() {
        sharedPreferences.edit().clear().apply()
    }
}
