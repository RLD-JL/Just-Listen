package com.rld.justlisten.util

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private class AndroidShareLauncher(
    private val launchShareSheet: (String, String) -> Unit,
) : ShareLauncher {
    override fun share(text: String, title: String) {
        launchShareSheet(text, title)
    }
}

@Composable
actual fun rememberShareLauncher(): ShareLauncher {
    val context = LocalContext.current
    return remember(context) {
        AndroidShareLauncher { text, title ->
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, title)
                putExtra(Intent.EXTRA_TEXT, text)
            }
            context.startActivity(Intent.createChooser(shareIntent, title))
        }
    }
}
