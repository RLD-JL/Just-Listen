package com.rld.justlisten.ui.utils

expect val hasDynamicThemeSupport: Boolean
expect val isIos: Boolean
expect val appVersion: String
expect val supportsLiquidGlassNavigation: Boolean
expect fun isLiquidGlassNavigationEnabled(): Boolean
expect fun setLiquidGlassNavigationEnabled(enabled: Boolean)
