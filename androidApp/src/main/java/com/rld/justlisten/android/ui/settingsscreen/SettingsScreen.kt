package com.rld.justlisten.android.ui.settingsscreen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.work.WorkManager
import com.rld.justlisten.android.ui.settingsscreen.components.TimerSetup
import com.rld.justlisten.android.ui.theme.ColorPallet
import com.rld.justlisten.android.ui.utils.getColorPallet
import com.rld.justlisten.viewmodel.screens.settings.SettingsState
import kotlinx.coroutines.CoroutineScope
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
                .verticalScroll(nestedScroll)
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
                                  },
                        modifier = Modifier.clip(CircleShape)
                    ) {
                        Text("Cancel sleeper")
                    }
                }
            }
        }
    }

}

@Composable
fun SettingsContent(
    settings: SettingsState,
    updateSettings: (SettingsState) -> Unit,
    sleepTimerClicked: () -> Unit,
) {

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically

        ) {
            Text(
                text = "Night Mode",
                style = MaterialTheme.typography.h6.copy(fontSize = 14.sp)
            )
            Switch(
                checked = settings.isDarkThemeOn,
                modifier = Modifier.padding(8.dp),
                onCheckedChange = {
                    updateSettings(
                        SettingsState(
                            isDarkThemeOn = !settings.isDarkThemeOn,
                            hasDonationNavigationOn = settings.hasDonationNavigationOn,
                            palletColor = settings.palletColor
                        )
                    )
                }
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Has Bottom Donation Navigation",
                style = MaterialTheme.typography.h6.copy(fontSize = 14.sp)
            )
            Switch(
                checked = settings.hasDonationNavigationOn,
                modifier = Modifier.padding(8.dp),
                onCheckedChange = {
                    updateSettings(
                        SettingsState(
                            hasDonationNavigationOn = !settings.hasDonationNavigationOn,
                            isDarkThemeOn = settings.isDarkThemeOn,
                            palletColor = settings.palletColor
                        )
                    )
                }
            )
        }

        val palletOptions = listOf(
            ColorPallet.Dark,
            ColorPallet.Green,
            ColorPallet.Pink,
            ColorPallet.Purple,
            ColorPallet.Orange,
            ColorPallet.Blue
        )

        val (selectedOption, onOptionSelected) = remember {
            mutableStateOf(palletOptions.first {
                it == getColorPallet(
                    settings.palletColor
                )
            })
        }
        palletOptions.fastForEach { pallet ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(selected = (pallet == selectedOption),
                        onClick = {
                            onOptionSelected(pallet)
                            updateSettings(
                                SettingsState(
                                    isDarkThemeOn = settings.isDarkThemeOn,
                                    hasDonationNavigationOn = settings.hasDonationNavigationOn,
                                    palletColor = pallet.name
                                )
                            )
                        }),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (pallet == selectedOption),
                    onClick = {
                        onOptionSelected(pallet)
                        updateSettings(
                            SettingsState(
                                isDarkThemeOn = settings.isDarkThemeOn,
                                hasDonationNavigationOn = settings.hasDonationNavigationOn,
                                palletColor = pallet.name
                            )
                        )
                    })
                Text(pallet.name, modifier = Modifier.padding(start = 8.dp))
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(onClick = sleepTimerClicked) {
                Text("Set sleep timer")
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheetSettings(
    workManager: WorkManager,
    scaffoldState: BottomSheetScaffoldState,
    coroutineScope: CoroutineScope,
    onConfirmClicked: (String, String) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .height(175.dp)
            .fillMaxWidth()
    ) {
        val maxHeight = this.maxHeight

        Canvas(modifier = Modifier.fillMaxWidth()) {
            val width = size.width
            val height = 25.dp
            val newSize = Size(width, height.toPx())
            drawRoundRect(
                color = Color.LightGray.copy(alpha = 0.40f),
                size = newSize,
                style = Fill,
                topLeft = Offset(0f, (maxHeight.toPx() / 2) + ((height - 8.dp) / 2).toPx()),
                cornerRadius = CornerRadius(
                    x = 5.dp.toPx(),
                    y = 10.dp.toPx()
                )
            )
        }
        TimerSetup(workManager, onConfirmClicked, coroutineScope, scaffoldState)
    }
}