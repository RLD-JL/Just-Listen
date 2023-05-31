package com.rld.justlisten.android.ui.settingsscreen

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.work.WorkManager
import com.rld.justlisten.android.ui.settingsscreen.components.BottomSheetSettings
import com.rld.justlisten.android.ui.settingsscreen.components.SettingsContent
import com.rld.justlisten.viewmodel.screens.settings.SettingsState
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingsScreen(
    settings: SettingsState,
    updateSettings: (SettingsState) -> Unit
) {
    val scaffoldState =
        rememberBottomSheetScaffoldState(bottomSheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed))
    val coroutineScope = rememberCoroutineScope()

    val hasTimerSetup = rememberSaveable {
        mutableStateOf(false)
    }
    val hourTime = rememberSaveable {
        mutableStateOf("")
    }
    val minuteTime = rememberSaveable {
        mutableStateOf("")
    }
    val context = LocalContext.current
    val workManager = WorkManager.getInstance(context)

    BottomSheetScaffold(
        sheetContent = {
            BottomSheetSettings(workManager, scaffoldState, coroutineScope) { hours, minute ->
                hasTimerSetup.value = true
                hourTime.value = hours
                minuteTime.value = minute
            }
        },
        sheetPeekHeight = 0.dp,
        scaffoldState = scaffoldState
    ) {
        val nestedScroll = rememberScrollState()
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(nestedScroll),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            SettingsContent(
                settings,
                updateSettings,
                sleepTimerClicked = { coroutineScope.launch { scaffoldState.bottomSheetState.expand() } },
            )

            if (hasTimerSetup.value) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Timer to close the app has been added: ${hourTime.value}:${minuteTime.value}"
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        onClick = {
                            workManager.cancelUniqueWork("SleepWorker")
                            hasTimerSetup.value = false
                            Toast.makeText(context, "Sleeper has been canceled", Toast.LENGTH_SHORT)
                                .show()
                        },
                        modifier = Modifier.clip(CircleShape)
                    ) {
                        Text("Cancel sleeper")

                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth().weight(1f, false),
            horizontalArrangement = Arrangement.Center)
            {
                Text(text ="App version:1.0.6-fix")
            }
        }
    }
}