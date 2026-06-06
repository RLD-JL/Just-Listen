package com.rld.justlisten.ui.settingsscreen

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rld.justlisten.ui.settingsscreen.components.BottomSheetSettings
import com.rld.justlisten.viewmodel.screens.settings.SettingsState
import com.rld.justlisten.datalayer.repositories.SessionState
import com.rld.justlisten.datalayer.repositories.SyncState
import com.rld.justlisten.ui.utils.SleepTimerService
import com.rld.justlisten.ui.utils.showToast
import org.koin.compose.koinInject
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.rld.justlisten.datalayer.models.PlayListModel
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

enum class SettingsSheetType {
    SLEEP_TIMER,
    LIBRARY_CONTENT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: SettingsState,
    updateSettings: (SettingsState) -> Unit,
    onNavigateToCustomTheme: () -> Unit,
    onLogin: (String) -> Unit = {},
    onLogout: () -> Unit = {},
    onRetrySync: () -> Unit = {},
    onClearSync: () -> Unit = {},
    onFetchLibraryContent: () -> Unit = {}
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    val coroutineScope = rememberCoroutineScope()
    val sleepTimerService: SleepTimerService = koinInject()

    var currentSheetType by remember { mutableStateOf(SettingsSheetType.SLEEP_TIMER) }
    var selectedTabIndex by remember { mutableStateOf(0) }

    // Ticking countdown logic
    var remainingTimeMs by remember { mutableStateOf(0L) }
    LaunchedEffect(Unit) {
        while (true) {
            remainingTimeMs = sleepTimerService.getRemainingTimeMs()
            kotlinx.coroutines.delay(1000L)
        }
    }

    val countdownText = remember(remainingTimeMs) {
        com.rld.justlisten.util.formatCountdown(remainingTimeMs)
    }

    BottomSheetScaffold(
        sheetContent = {
            if (settings.syncState is SyncState.SyncFailed) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CloudQueue,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Sync Failed",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = (settings.syncState as SyncState.SyncFailed).errorMessage,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = {
                                onRetrySync()
                                coroutineScope.launch {
                                    scaffoldState.bottomSheetState.partialExpand()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = CircleShape
                        ) {
                            Text("Retry Sync")
                        }
                        OutlinedButton(
                            onClick = {
                                onClearSync()
                                coroutineScope.launch {
                                    scaffoldState.bottomSheetState.partialExpand()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = CircleShape
                        ) {
                            Text("Cancel / Clear")
                        }
                    }
                }
            } else {
                when (currentSheetType) {
                    SettingsSheetType.SLEEP_TIMER -> {
                        BottomSheetSettings(scaffoldState, coroutineScope)
                    }
                    SettingsSheetType.LIBRARY_CONTENT -> {
                        CloudLibrarySheet(
                            settings = settings,
                            selectedTabIndex = selectedTabIndex,
                            onTabSelected = { selectedTabIndex = it }
                        )
                    }
                }
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

            // Audius Account Section
            SettingsSectionHeader(title = "Audius Account")
            
            SettingsCard {
                when (val session = settings.sessionState) {
                    is SessionState.Guest -> {
                        SettingsClickableRow(
                            icon = Icons.Rounded.Person,
                            title = "Connect Audius",
                            subtitle = "Sync favorites and playlists to the cloud",
                            onClick = {
                                onLogin("justlisten://oauth/callback")
                            }
                        )
                    }
                    is SessionState.Authenticated -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val avatarUrl = session.userProfile.profilePicture?.image150
                            val painter = coil3.compose.rememberAsyncImagePainter(avatarUrl)
                            
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (avatarUrl != null) {
                                    androidx.compose.foundation.Image(
                                        painter = painter,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = session.userProfile.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "@${session.userProfile.handle}",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                            TextButton(onClick = onLogout) {
                                Text("Log Out", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                // Sync Status Bar Row (Only visible when logged in)
                if (settings.sessionState is SessionState.Authenticated) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), thickness = 0.5.dp, modifier = Modifier.padding(horizontal = 16.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (settings.syncState is SyncState.SyncFailed) {
                                    coroutineScope.launch {
                                        scaffoldState.bottomSheetState.expand()
                                    }
                                } else {
                                    currentSheetType = SettingsSheetType.LIBRARY_CONTENT
                                    onFetchLibraryContent()
                                    coroutineScope.launch {
                                        scaffoldState.bottomSheetState.expand()
                                    }
                                }
                            }
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val (syncIcon, syncTitle, syncColor) = when (val sync = settings.syncState) {
                            is SyncState.Synced -> Triple(Icons.Rounded.CloudDone, "Library Synced", MaterialTheme.colorScheme.primary)
                            is SyncState.Syncing -> Triple(Icons.Rounded.Sync, "Syncing ${sync.pendingCount} items...", MaterialTheme.colorScheme.secondary)
                            is SyncState.SyncFailed -> Triple(Icons.Rounded.CloudQueue, "Sync Failed (Tap to resolve)", MaterialTheme.colorScheme.error)
                        }

                        Icon(
                            imageVector = syncIcon,
                            contentDescription = null,
                            tint = syncColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = syncTitle,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
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
                                customSurface = settings.customSurface,
                                isFirstLaunch = settings.isFirstLaunch,
                                sessionState = settings.sessionState,
                                syncState = settings.syncState,
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
                                customSurface = settings.customSurface,
                                isFirstLaunch = settings.isFirstLaunch,
                                sessionState = settings.sessionState,
                                syncState = settings.syncState,
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
                        currentSheetType = SettingsSheetType.SLEEP_TIMER
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
                                    sleepTimerService.extendTimer(15)
                                    showToast("Timer extended by 15 minutes")
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
                                    sleepTimerService.cancelTimer()
                                    showToast("Sleeper has been canceled")
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

@Composable
fun CloudLibrarySheet(
    settings: SettingsState,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 350.dp, max = 600.dp)
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Synced Cloud Library",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                if (selectedTabIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = MaterialTheme.colorScheme.primary,
                        height = 3.dp
                    )
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
        ) {
            val tabs = listOf("Tracks", "Playlists", "Liked Playlists")
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { onTabSelected(index) },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (settings.isLibraryLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
        } else if (settings.libraryError != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = settings.libraryError,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
            }
        } else {
            val items = when (selectedTabIndex) {
                0 -> settings.favoriteTracks
                1 -> settings.userPlaylists
                2 -> settings.favoritePlaylists
                else -> emptyList()
            }

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = when (selectedTabIndex) {
                            0 -> "No favorite tracks found"
                            1 -> "No created playlists found"
                            else -> "No liked playlists found"
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items) { item ->
                        CloudLibraryItemRow(item, isPlaylist = selectedTabIndex > 0)
                    }
                }
            }
        }
    }
}

@Composable
fun CloudLibraryItemRow(item: PlayListModel, isPlaylist: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val artworkUrl = item.songImgList.songImageURL150px
            val painter = coil3.compose.rememberAsyncImagePainter(
                model = artworkUrl.ifBlank { null }
            )
            
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                if (artworkUrl.isNotBlank()) {
                    androidx.compose.foundation.Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = if (isPlaylist) Icons.Rounded.LibraryMusic else Icons.Rounded.MusicNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                val displayName = if (isPlaylist && item.playlistTitle.isNotBlank()) item.playlistTitle else item.title
                Text(
                    text = displayName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                
                val subtitleText = if (isPlaylist) {
                    if (item.user.username.isNotBlank()) "by ${item.user.username}" else "Playlist"
                } else {
                    item.user.username.ifBlank { "Unknown Artist" }
                }
                
                Text(
                    text = subtitleText,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    maxLines = 1
                )
            }
        }
    }
}

