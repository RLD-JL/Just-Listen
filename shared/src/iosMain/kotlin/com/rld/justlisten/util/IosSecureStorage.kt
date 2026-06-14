@file:OptIn(
    kotlinx.cinterop.ExperimentalForeignApi::class,
    kotlinx.cinterop.BetaInteropApi::class
)
package com.rld.justlisten.util

import platform.Foundation.NSData
import platform.Foundation.create
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.dataUsingEncoding
import platform.Foundation.NSString
import platform.Foundation.CFBridgingRetain
import platform.Foundation.CFBridgingRelease
import platform.Security.*
import kotlinx.cinterop.*
import platform.CoreFoundation.*

class IosSecureStorage : SecureStorage {

    private val serviceName = "com.rld.justlisten.secure"

    override fun saveToken(key: String, value: String) {
        val nsValue = NSString.create(string = value)
        val nsKey = NSString.create(string = key)
        val nsService = NSString.create(string = serviceName)
        
        val valueData = nsValue.dataUsingEncoding(NSUTF8StringEncoding) ?: return
        
        memScoped {
            val cfKey = CFBridgingRetain(nsKey)
            val cfService = CFBridgingRetain(nsService)
            val cfData = CFBridgingRetain(valueData)
            
            try {
                // Delete existing first
                val query = CFDictionaryCreateMutable(null, 3, null, null)
                CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
                CFDictionaryAddValue(query, kSecAttrService, cfService)
                CFDictionaryAddValue(query, kSecAttrAccount, cfKey)
                SecItemDelete(query)

                // Add the new item
                val attributes = CFDictionaryCreateMutable(null, 5, null, null)
                CFDictionaryAddValue(attributes, kSecClass, kSecClassGenericPassword)
                CFDictionaryAddValue(attributes, kSecAttrService, cfService)
                CFDictionaryAddValue(attributes, kSecAttrAccount, cfKey)
                CFDictionaryAddValue(attributes, kSecValueData, cfData)
                CFDictionaryAddValue(attributes, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlock)
                
                val status = SecItemAdd(attributes, null)
                if (status != errSecSuccess) {
                    co.touchlab.kermit.Logger.e { "IosSecureStorage saveToken error status: $status" }
                }
            } finally {
                CFRelease(cfKey)
                CFRelease(cfService)
                CFRelease(cfData)
            }
        }
    }

    override fun getToken(key: String): String? {
        val nsKey = NSString.create(string = key)
        val nsService = NSString.create(string = serviceName)
        
        return memScoped {
            val cfKey = CFBridgingRetain(nsKey)
            val cfService = CFBridgingRetain(nsService)
            
            try {
                val query = CFDictionaryCreateMutable(null, 5, null, null)
                CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
                CFDictionaryAddValue(query, kSecAttrService, cfService)
                CFDictionaryAddValue(query, kSecAttrAccount, cfKey)
                CFDictionaryAddValue(query, kSecReturnData, kCFBooleanTrue)
                CFDictionaryAddValue(query, kSecMatchLimit, kSecMatchLimitOne)

                val resultRef = alloc<CFTypeRefVar>()
                val status = SecItemCopyMatching(query, resultRef.ptr)
                
                if (status == errSecSuccess) {
                    val retainedData = resultRef.value
                    if (retainedData != null) {
                        val nsData = CFBridgingRelease(retainedData) as? NSData
                        if (nsData != null) {
                            NSString.create(data = nsData, encoding = NSUTF8StringEncoding)?.toString()
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                } else {
                    if (status != errSecItemNotFound) {
                        co.touchlab.kermit.Logger.e { "IosSecureStorage getToken error status: $status" }
                    }
                    null
                }
            } finally {
                CFRelease(cfKey)
                CFRelease(cfService)
            }
        }
    }

    override fun clear() {
        val nsService = NSString.create(string = serviceName)
        memScoped {
            val cfService = CFBridgingRetain(nsService)
            try {
                val query = CFDictionaryCreateMutable(null, 2, null, null)
                CFDictionaryAddValue(query, kSecClass, kSecClassGenericPassword)
                CFDictionaryAddValue(query, kSecAttrService, cfService)
                SecItemDelete(query)
            } finally {
                CFRelease(cfService)
            }
        }
    }
}
