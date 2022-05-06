package com.rld.justlisten.android.ui.utils

import com.rld.justlisten.android.ui.theme.ColorPallet

fun getColorPallet(pallet: String): ColorPallet {
    return when(pallet) {
        "Dark" -> ColorPallet.Dark
        "Green" -> ColorPallet.Green
        "Purple" -> ColorPallet.Purple
        "Blue" -> ColorPallet.Blue
        "Orange" -> ColorPallet.Orange
        else -> ColorPallet.Dark
    }
}