package com.rld.justlisten.ui.utils

import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults
import platform.UIKit.UIDevice

private const val LIQUID_GLASS_NAVIGATION_KEY = "useLiquidGlassNavigation"

actual val hasDynamicThemeSupport: Boolean = false
actual val isIos: Boolean = true
actual val appVersion: String 
    get() = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString") as? String ?: "1.0.0"
actual val supportsLiquidGlassNavigation: Boolean
    get() {
        val components = UIDevice.currentDevice.systemVersion.split('.')
        val major = components.getOrNull(0)?.toIntOrNull() ?: 0
        val minor = components.getOrNull(1)?.toIntOrNull() ?: 0
        return major > 26 || (major == 26 && minor >= 1)
    }

actual fun isLiquidGlassNavigationEnabled(): Boolean {
    val defaults = NSUserDefaults.standardUserDefaults
    return if (defaults.objectForKey(LIQUID_GLASS_NAVIGATION_KEY) == null) {
        supportsLiquidGlassNavigation
    } else {
        defaults.boolForKey(LIQUID_GLASS_NAVIGATION_KEY)
    }
}

actual fun setLiquidGlassNavigationEnabled(enabled: Boolean) {
    NSUserDefaults.standardUserDefaults.setBool(enabled, LIQUID_GLASS_NAVIGATION_KEY)
}
