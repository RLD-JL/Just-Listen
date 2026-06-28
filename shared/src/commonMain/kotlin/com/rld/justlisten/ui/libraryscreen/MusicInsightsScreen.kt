package com.rld.justlisten.ui.libraryscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.rld.justlisten.datalayer.repositories.LibraryRepository
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.media.PlaybackStatus
import com.rld.justlisten.ui.components.MusicLoadingSpinner
import com.rld.justlisten.ui.utils.playMusicFromId
import com.rld.justlisten.viewmodel.screens.library.LibraryState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicInsightsScreen(
    libraryState: LibraryState,
    musicPlayer: MusicPlayer,
    libraryRepository: LibraryRepository,
    onBackPressed: () -> Unit,
    onLoadMoreMostPlayed: (Int) -> Unit
) {
    val context = LocalPlatformContext.current
    val totalPlays = libraryState.totalPlays
    val isNewUser = totalPlays == 0
    val topArtistName = libraryState.topArtistName
    val topArtistPlays = libraryState.topArtistPlays

    // Observe player playback state
    val playbackState by musicPlayer.playbackState.collectAsState()
    val isPlayingAny = playbackState.status == PlaybackStatus.PLAYING
    val currentMediaId = playbackState.currentMedia?.id

    // Capture initial order of tracks to prevent rearranging in real-time
    val initialTrackIds = remember(libraryState.isLoading) {
        if (libraryState.isLoading) emptyList() else libraryState.mostPlayedSongs.map { it.id }
    }
    val displayedTracks = remember(libraryState.mostPlayedSongs, initialTrackIds) {
        if (initialTrackIds.isEmpty()) {
            libraryState.mostPlayedSongs
        } else {
            initialTrackIds.mapNotNull { id ->
                libraryState.mostPlayedSongs.find { it.id == id }
            }
        }
    }
    // Get exact hours listened from state
    val hoursListened = libraryState.hoursPlayed

    Scaffold(
        topBar = {
            TopAppBar(
                windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
                title = {
                    Column {
                        Text(
                            text = "Music Insights",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Calculated completely locally and privately",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (isNewUser) {
            // --- ONBOARDING / EMPTY STATE SCREEN ---
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF6B11CB), Color(0xFF2575FC))
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text(
                        text = "No Statistics Yet!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "We collect your play history completely locally and privately on your device. Start listening to songs from the feed to calculate your Listening Hours, Top Artists, and playable Top Tracks!",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Button(
                        onClick = onBackPressed,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Explore Trending Music 🎵",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            // --- FULL DEDICATED STATISTICS SCREEN ---
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // 1. Core Stats Row Grid
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Stat A: Hours Listened
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$hoursListened hrs",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF4CAF50)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Hours Listened",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Stat B: Total Plays
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$totalPlays",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF2575FC)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Total Plays",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Stat C: Unique Tracks
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(100.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${libraryState.uniquePlays}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF6B11CB)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Unique Played",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                }

                // 2. Top Artist Visual Banner
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFF6B11CB), Color(0xFFFF416C))
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "YOUR TOP ARTIST",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White.copy(alpha = 0.8f)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (topArtistPlays >= 25) "Super Fan Tier 🌟" else "Rising Listener",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = topArtistName,
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "With a total of $topArtistPlays plays • ${libraryState.topArtistHours} hrs in your history!",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }
                    }
                }

                // 3. Section Title
                item {
                    Text(
                        text = "Your Top Played Tracks",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // 4. Playable list of top tracks
                itemsIndexed(displayedTracks) { index, track ->
                    val isPlayingThis = isPlayingAny && currentMediaId == track.id
                    
                    LaunchedEffect(index) {
                        if (index >= displayedTracks.lastIndex - 3 && !libraryState.lastMostPlayedIndexReached) {
                            onLoadMoreMostPlayed(displayedTracks.size)
                        }
                    }
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                playMusicFromId(
                                    musicPlayer = musicPlayer,
                                    playlist = displayedTracks,
                                    songId = track.id,
                                    repository = libraryRepository,
                                    playlistId = "MusicInsights"
                                )
                            },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPlayingThis) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Ranking Number
                            Text(
                                text = "#${index + 1}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPlayingThis) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .width(32.dp)
                                    .padding(start = 4.dp)
                            )

                            // Track Cover Image with overlay equalizer
                            val painter = rememberAsyncImagePainter(
                                model = remember(track.songIconList.songImageURL150px, context) {
                                    ImageRequest.Builder(context)
                                        .data(track.songIconList.songImageURL150px)
                                        .build()
                                }
                            )

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                Image(
                                    painter = painter,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (isPlayingThis) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Black.copy(alpha = 0.5f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        MusicLoadingSpinner(
                                            size = 18.dp,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Title & Artist
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = track.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPlayingThis) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = track.user,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    maxLines = 1
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // Dynamic playing badge & counter with listening duration
                            val trackHours = remember(track.durationPlayedSec) {
                                ((track.durationPlayedSec / 3600.0) * 10).toInt() / 10.0
                            }
                            val badgeText = remember(track.songCounter, trackHours, track.durationPlayedSec) {
                                if (trackHours > 0.0) {
                                    "${track.songCounter} plays • $trackHours hrs"
                                } else {
                                    val trackMins = ((track.durationPlayedSec / 60.0) * 10).toInt() / 10.0
                                    if (trackMins > 0.0) {
                                        "${track.songCounter} plays • $trackMins mins"
                                    } else {
                                        "${track.songCounter} plays • <1 min"
                                    }
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isPlayingThis) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = badgeText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPlayingThis) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

