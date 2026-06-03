package com.rld.justlisten.ui.utils

import android.widget.Toast
import android.content.Context
import org.koin.mp.KoinPlatform

actual fun showToast(message: String) {
    try {
        val context = KoinPlatform.getKoin().getOrNull<Context>()
        if (context != null) {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        println("Android Toast error: ${e.message}")
    }
}
