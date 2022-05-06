package com.rld.justlisten.android.ui.settingsscreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import com.rld.justlisten.android.ui.theme.ColorPallet
import com.rld.justlisten.viewmodel.screens.settings.SettingsState

@Composable
fun SettingsScreen(
    settings: SettingsState,
    updateSettings: (SettingsState) -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically

            ) {
                Text(
                    text = "Dark Theme",
                    style = MaterialTheme.typography.h6.copy(fontSize = 14.sp)
                )
                Switch(
                    checked = settings.isDarkThemeOn,
                    modifier = Modifier.padding(8.dp),
                    onCheckedChange = {
                        updateSettings(
                            SettingsState(
                                isDarkThemeOn = !settings.isDarkThemeOn,
                                hasFundNavigationOn = settings.hasFundNavigationOn
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
                    text = "Has Bottom Fund Navigation",
                    style = MaterialTheme.typography.h6.copy(fontSize = 14.sp)
                )
                Switch(
                    checked = settings.hasFundNavigationOn,
                    modifier = Modifier.padding(8.dp),
                    onCheckedChange = {
                        updateSettings(
                            SettingsState(
                                hasFundNavigationOn = !settings.hasFundNavigationOn,
                                isDarkThemeOn = settings.isDarkThemeOn
                            )
                        )
                    }
                )
            }

            val palletOptions = listOf(
                ColorPallet.Dark,
                ColorPallet.Green,
                ColorPallet.Purple,
                ColorPallet.Orange,
                ColorPallet.Blue
            )
            val (selectedOption, onOptionSelected) = remember { mutableStateOf(palletOptions[0]) }
            palletOptions.fastForEach { pallet ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .selectable(selected = (pallet == selectedOption),
                            onClick = { onOptionSelected(pallet) }),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = (pallet == selectedOption),
                        onClick = { onOptionSelected(pallet) })
                    Text(pallet.name, modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

    }
}