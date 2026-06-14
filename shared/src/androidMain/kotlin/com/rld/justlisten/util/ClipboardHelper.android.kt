package com.rld.justlisten.util

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.toClipEntry

actual fun clipEntryOf(text: String): ClipEntry {
    return ClipData.newPlainText(null, text).toClipEntry()
}
