package com.rld.justlisten.ui.settingsscreen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetSettings(
    scaffoldState: BottomSheetScaffoldState,
    coroutineScope: CoroutineScope
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        TimerSetup(coroutineScope, scaffoldState)
    }
}
