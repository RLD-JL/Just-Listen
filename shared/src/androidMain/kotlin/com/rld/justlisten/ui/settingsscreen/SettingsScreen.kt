package com.rld.justlisten.ui.settingsscreen

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.rld.justlisten.ui.settingsscreen.components.BottomSheetSettings
import com.rld.justlisten.viewmodel.screens.settings.SettingsState
import com.rld.justlisten.workers.SleepWorker
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: SettingsState,
    updateSettings: (SettingsState) -> Unit,
    onNavigateToCustomTheme: () -> Unit
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val workManager = WorkManager.getInstance(context)

    // Load sleep timer persistently
    val sharedPrefs = remember { context.getSharedPreferences("sleep_timer_prefs", Context.MODE_PRIVATE) }
    val activeTimerEndTime = remember { mutableStateOf(sharedPrefs.getLong("sleep_timer_end_time_ms", 0L)) }

    // Ticking countdown logic
    var remainingTimeMs by remember { mutableStateOf(0L) }
    LaunchedEffect(activeTimerEndTime.value) {
        while (activeTimerEndTime.value > System.currentTimeMillis()) {
            remainingTimeMs = activeTimerEndTime.value - System.currentTimeMillis()
            kotlinx.coroutines.delay(1000L)
        }
        remainingTimeMs = 0L
    }

    val countdownText = remember(remainingTimeMs) {
        com.rld.justlisten.util.formatCountdown(remainingTimeMs)
    }

    BottomSheetScaffold(
        sheetContent = {
            BottomSheetSettings(workManager, scaffoldState, coroutineScope) { _, _ ->
                activeTimerEndTime.value = sharedPrefs.getLong("sleep_timer_end_time_ms", 0L)
            }
        },
        sheetPeekHeight = 0.dp,
        scaffoldState = scaffoldState,
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        val scrollState = rememberScrollState()
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Premium compact Header with harmonious theme gradient
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                                )
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Settings",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "Active Palette: ${settings.palletColor}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.75f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Rounded.Settings,
                        contentDescription = null,
                        modifier = Modifier.size(44.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }

            // Appearance & Themes Section
            SettingsSectionHeader(title = "Appearance & Styling")
            
            SettingsCard {
                SettingsClickableRow(
                    icon = Icons.Rounded.Palette,
                    title = "Theme Customizer",
                    subtitle = "Adjust custom colors and system themes",
                    onClick = onNavigateToCustomTheme
                )
                
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                
                SettingsSwitchRow(
                    icon = Icons.Rounded.DarkMode,
                    title = "Dark Theme",
                    checked = settings.isDarkThemeOn,
                    onCheckedChange = {
                        updateSettings(
                            SettingsState(
                                isDarkThemeOn = it,
                                hasDonationNavigationOn = settings.hasDonationNavigationOn,
                                palletColor = settings.palletColor,
                                customPrimary = settings.customPrimary,
                                customSecondary = settings.customSecondary,
                                customBackground = settings.customBackground,
                                customSurface = settings.customSurface
                            )
                        )
                    }
                )
            }

            // support options section
            SettingsSectionHeader(title = "Support & Navigation")
            
            SettingsCard {
                SettingsSwitchRow(
                    icon = Icons.Rounded.Favorite,
                    title = "Support Tab in Navigation",
                    checked = settings.hasDonationNavigationOn,
                    onCheckedChange = {
                        updateSettings(
                            SettingsState(
                                hasDonationNavigationOn = it,
                                isDarkThemeOn = settings.isDarkThemeOn,
                                palletColor = settings.palletColor,
                                customPrimary = settings.customPrimary,
                                customSecondary = settings.customSecondary,
                                customBackground = settings.customBackground,
                                customSurface = settings.customSurface
                            )
                        )
                    }
                )
            }

            // sleep utilities section
            SettingsSectionHeader(title = "Sleep Utilities")
            
            SettingsCard {
                SettingsClickableRow(
                    icon = Icons.Rounded.Snooze,
                    title = "Sleep Timer",
                    subtitle = "Automatically close the application after timeout",
                    onClick = {
                        coroutineScope.launch {
                            scaffoldState.bottomSheetState.expand()
                        }
                    }
                )
            }

            // Animated Active Sleep Timer Card
            AnimatedVisibility(
                visible = remainingTimeMs > 0,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Sleeper active: closes in $countdownText",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                fontSize = 15.sp
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    val newEndTime = activeTimerEndTime.value + 15 * 60 * 1000L
                                    activeTimerEndTime.value = newEndTime
                                    
                                    sharedPrefs.edit()
                                        .putLong("sleep_timer_end_time_ms", newEndTime)
                                        .apply()

                                    // Reschedule WorkManager worker
                                    val currentMinsLeft = (newEndTime - System.currentTimeMillis()) / (60 * 1000)
                                    val myWorkRequest = OneTimeWorkRequestBuilder<SleepWorker>()
                                        .setInitialDelay(maxOf(1, currentMinsLeft), TimeUnit.MINUTES)
                                        .build()
                                    workManager.beginUniqueWork(
                                        "SleepWorker",
                                        ExistingWorkPolicy.REPLACE,
                                        myWorkRequest
                                    ).enqueue()

                                    Toast.makeText(context, "Timer extended by 15 minutes", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                ),
                                shape = CircleShape
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("15 Mins", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    workManager.cancelUniqueWork("SleepWorker")
                                    
                                    sharedPrefs.edit()
                                        .putLong("sleep_timer_end_time_ms", 0L)
                                        .apply()
                                    
                                    activeTimerEndTime.value = 0L
                                    Toast.makeText(context, "Sleeper has been canceled", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                shape = CircleShape,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Cancel", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Version Footer info
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp).alpha(0.5f),
                    tint = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "App Version 1.0.9-a",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        ),
        content = content
    )
}

@Composable
fun SettingsClickableRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
        Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

@Composable
fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        )
    }
}
