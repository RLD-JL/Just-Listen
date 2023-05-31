package com.rld.justlisten.android.ui

import androidx.activity.compose.BackHandler
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.util.fastForEach
import coil.annotation.ExperimentalCoilApi
import com.rld.justlisten.Navigation
import com.rld.justlisten.android.exoplayer.MusicServiceConnection

@ExperimentalCoilApi
@ExperimentalMaterialApi
@Composable
fun Navigation.Router(
    musicServiceConnection: MusicServiceConnection, settingsUpdated: () -> Unit,
    hasNavigationFundOn: Boolean,
    updateStatusBarColor: (Int, Boolean) -> Unit
) {
    val screenUIisStateHolder = rememberSaveableStateHolder()

    OnePane(
        screenUIisStateHolder,
        musicServiceConnection,
        settingsUpdated = settingsUpdated,
        hasNavigationFundOn,
        updateStatusBarColor = updateStatusBarColor
    )

    screenStatesToRemove.fastForEach {
        screenUIisStateHolder.removeState(it.URI)
    }

    if (!only1ScreenInBackstack) {
        BackHandler { // catching the back button to update the DKMPViewModel
            exitScreen()
        }
    }
}