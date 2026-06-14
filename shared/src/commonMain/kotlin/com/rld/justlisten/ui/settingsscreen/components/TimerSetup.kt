package com.rld.justlisten.ui.settingsscreen.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MusicOff
import androidx.compose.material.icons.rounded.VolumeDown
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rld.justlisten.ui.LocalMusicPlayer
import com.rld.justlisten.media.PlaybackStatus
import com.rld.justlisten.ui.utils.SleepTimerService
import com.rld.justlisten.ui.utils.showToast
import org.koin.compose.koinInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerSetup(
    coroutineScope: CoroutineScope,
    scaffoldState: BottomSheetScaffoldState
) {
    val sleepTimerService: SleepTimerService = koinInject()

    // State bindings
    val selectedMinutes = rememberSaveable { mutableStateOf(30) }
    val maxMinutes = rememberSaveable { mutableStateOf(120) }
    val fadeOutEnabled = rememberSaveable { 
        mutableStateOf(true) 
    }
    val showCustomDialog = remember { mutableStateOf(false) }

    // Predefined capsules
    val presets = listOf("5", "10", "15", "30", "45", "60", "90", "120", "Custom")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 24.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Premium Interactive Dial Picker
        DialPicker(
            value = selectedMinutes.value,
            maxMinutes = maxMinutes.value,
            onValueChange = { newValue ->
                selectedMinutes.value = newValue
            },
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // 2. Predefined Time Capsule Selectors
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "QUICK DURATIONS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(presets) { preset ->
                    val isCustom = preset == "Custom"
                    // Custom is active if the current value is custom (i.e. not in standard list) or if custom was explicitly applied
                    val isPresetActive = if (isCustom) {
                        selectedMinutes.value !in listOf(5, 10, 15, 30, 45, 60, 90, 120)
                    } else {
                        selectedMinutes.value == preset.toInt()
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isPresetActive) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isPresetActive) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                if (isCustom) {
                                    showCustomDialog.value = true
                                } else {
                                    maxMinutes.value = 120 // Reset max scale when standard preset is tapped
                                    selectedMinutes.value = preset.toInt()
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isCustom) preset else "$preset min",
                            color = if (isPresetActive) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // 3. Premium "Fade out before stop" Switch Row
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { fadeOutEnabled.value = !fadeOutEnabled.value }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MusicOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Fade out before stop",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Gradually lower volume before app stops",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Switch(
                    modifier = Modifier.scale(0.85f),
                    checked = fadeOutEnabled.value,
                    onCheckedChange = { fadeOutEnabled.value = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                )
            }
        }

        // 4. Action Buttons (Confirm & Cancel)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = {
                    val minutesVal = selectedMinutes.value
                    if (minutesVal <= 0) {
                        showToast("Please select a duration")
                        return@Button
                    }

                    sleepTimerService.setTimer(minutesVal, fadeOutEnabled.value)

                    coroutineScope.launch { scaffoldState.bottomSheetState.partialExpand() }
                    showToast("Sleeper active: closes in $minutesVal minutes")
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Confirm", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            OutlinedButton(
                onClick = {
                    coroutineScope.launch { scaffoldState.bottomSheetState.partialExpand() }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
            ) {
                Text("Cancel", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }

    // 5. Sleek Custom input Dialog
    if (showCustomDialog.value) {
        var customInputText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showCustomDialog.value = false },
            title = {
                Text(
                    text = "Custom Sleep Duration",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Enter sleep time in minutes:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    OutlinedTextField(
                        value = customInputText,
                        onValueChange = { newValue ->
                            if (newValue.all { it.isDigit() }) {
                                customInputText = newValue
                            }
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        placeholder = { Text("e.g. 180 for 3 hours") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val inputMins = customInputText.toIntOrNull() ?: 0
                        if (inputMins > 0) {
                            maxMinutes.value = com.rld.justlisten.util.getNormalizedMaxMinutes(inputMins)
                            selectedMinutes.value = inputMins
                        }
                        showCustomDialog.value = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Apply", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomDialog.value = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}
