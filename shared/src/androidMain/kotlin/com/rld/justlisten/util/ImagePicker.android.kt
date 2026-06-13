package com.rld.justlisten.util

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.FileOutputStream

class AndroidImagePickerLauncher(
    private val launchPicker: () -> Unit
) : ImagePickerLauncher {
    override fun launch() {
        launchPicker()
    }
}

@Composable
actual fun rememberImagePicker(onImagePicked: (String) -> Unit): ImagePickerLauncher {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val cacheDir = context.cacheDir
                    val destFile = File(cacheDir, "picked_profile_${System.currentTimeMillis()}.jpg")
                    FileOutputStream(destFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                    onImagePicked(destFile.absolutePath)
                }
            } catch (e: Exception) {
                co.touchlab.kermit.Logger.e(e) { "Error saving picked image" }
            }
        }
    }

    return remember(launcher) {
        AndroidImagePickerLauncher {
            launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }
}
