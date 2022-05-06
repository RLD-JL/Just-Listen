package com.rld.justlisten.android.ui.settingsscreen

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rld.justlisten.viewmodel.screens.settings.SettingsState

@Composable
fun SettingsScreen(
    settings: SettingsState,
    updateSettings: (SettingsState) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
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
                horizontalArrangement = Arrangement.SpaceBetween
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
        }

    }
}