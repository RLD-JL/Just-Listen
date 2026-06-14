package com.rld.justlisten.util

import androidx.compose.runtime.Composable

@Composable
expect fun rememberImagePicker(onImagePicked: (String) -> Unit): ImagePickerLauncher

interface ImagePickerLauncher {
    fun launch()
}
