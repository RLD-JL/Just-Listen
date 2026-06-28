package com.rld.justlisten.ui.utils

import platform.Foundation.NSBundle

actual val hasDynamicThemeSupport: Boolean = false
actual val isIos: Boolean = true
actual val appVersion: String 
    get() = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "1.0.0"
