package com.rld.justlisten.ui.settingsscreen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope
import com.rld.justlisten.viewmodel.screens.settings.SettingsState

enum class SheetMode {
    SleepTimer,
    Equalizer
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetSettings(
    sheetMode: SheetMode,
    settings: SettingsState,
    updateSettings: (SettingsState) -> Unit,
    scaffoldState: BottomSheetScaffoldState,
    coroutineScope: CoroutineScope
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        when (sheetMode) {
            SheetMode.SleepTimer -> {
                TimerSetup(coroutineScope, scaffoldState)
            }
            SheetMode.Equalizer -> {
                EqualizerEditorSheet(
                    settings = settings,
                    updateSettings = updateSettings,
                    scaffoldState = scaffoldState,
                    coroutineScope = coroutineScope
                )
            }
        }
    }
}
