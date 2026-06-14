package com.rld.justlisten.ui.utils

import android.os.Build

actual val hasDynamicThemeSupport: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
