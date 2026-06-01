package com.rld.justlisten.ui.settingsscreen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetSettings(
    workManager: WorkManager,
    scaffoldState: BottomSheetScaffoldState,
    coroutineScope: CoroutineScope,
    onConfirmClicked: (String, String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        TimerSetup(workManager, onConfirmClicked, coroutineScope, scaffoldState)
    }
}
