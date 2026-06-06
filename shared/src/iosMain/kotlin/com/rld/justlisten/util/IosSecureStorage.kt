package com.rld.justlisten.util

import platform.Foundation.NSUserDefaults

class IosSecureStorage : SecureStorage {

    private val defaults = NSUserDefaults.standardUserDefaults

    override fun saveToken(key: String, value: String) {
        defaults.setObject(value, key)
    }

    override fun getToken(key: String): String? {
        return defaults.stringForKey(key)
    }

    override fun clear() {
        defaults.removeObjectForKey("access_token")
        defaults.removeObjectForKey("refresh_token")
    }
}
