package com.rld.justlisten.ui.libraryscreen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.ui.libraryscreen.components.FavoritePlaylist
import com.rld.justlisten.ui.libraryscreen.components.MostPlayedSongs
import com.rld.justlisten.ui.libraryscreen.components.PlaylistView
import com.rld.justlisten.ui.libraryscreen.components.RowListOfRecentActivity
import com.rld.justlisten.ui.libraryscreen.components.MyPlaylistRowItem
import com.rld.justlisten.ui.playlistscreen.components.Header
import com.rld.justlisten.ui.utils.playMusicFromId
import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.viewmodel.screens.library.LibraryState
import com.rld.justlisten.viewmodel.screens.search.TrackItem

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.History
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

import com.rld.justlisten.ui.actions.LibraryScreenAction

@Composable
fun LibraryScreen(
    musicPlayer: MusicPlayer,
    libraryState: LibraryState,
    onAction: (LibraryScreenAction) -> Unit
) {
    Box(modifier = Modifier
        .fillMaxSize()
        .padding(5.dp)) {
        val scrollState = rememberScrollState()
        Column(Modifier.verticalScroll(scrollState)) {
            Header(text = "Last Played")
            RowListOfRecentActivity(
                libraryState,
                onPlaylistClicked = { id, songIcon, user, playlistTitle, isFavorite ->
                    run {
                        val playlistModel = PlayListModel(
                            id,
                            playlistTitle,
                            playlistTitle,
                            SongIconList(songIcon, songIcon, songIcon),
                            UserModel(user),
                            false
                        )
                        val item = TrackItem(playlistModel, isFavorite)
                        playMusicFromId(musicPlayer, listOf(item), id)
                    }
                },
                lasItemReached = { onAction(LibraryScreenAction.LastItemReached(it)) },
                lastIndexReached = libraryState.lastIndexReached
            )
            HorizontalDivider(thickness = 1.dp)
            PlaylistView { onAction(LibraryScreenAction.PlayListViewClicked) }
            
            if (libraryState.playlistsCreated.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Header(text = "My Playlists")
                LazyRow(modifier = Modifier.fillMaxWidth()) {
                    items(libraryState.playlistsCreated) { playlist ->
                         MyPlaylistRowItem(
                             playlist, 
                             onPlaylistClicked = { title, desc, songs ->
                                 onAction(LibraryScreenAction.PlaylistCreatedClicked(title, desc, songs))
                             },
                             onDeleteClicked = { onAction(LibraryScreenAction.DeletePlaylistClicked(it)) }
                         )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            FavoritePlaylist(libraryState, onPlaylistPressed = { id, icon, title, createdBy ->
                onAction(LibraryScreenAction.FavoritePlaylistPressed(id, icon, title, createdBy))
            })
            Spacer(modifier = Modifier.height(10.dp))
            MostPlayedSongs(libraryState, onPlaylistPressed = { id, icon, title, createdBy ->
                onAction(LibraryScreenAction.MostPlayedPlaylistPressed(id, icon, title, createdBy))
            })
            Spacer(modifier = Modifier.height(10.dp))
            PremiumLibraryCards(
                libraryState = libraryState,
                onTimeCapsuleClicked = { onAction(LibraryScreenAction.TimeCapsulePressed) },
                onExploreClicked = { onAction(LibraryScreenAction.ExploreMusicPressed) }
            )
        }
    }
}

@Composable
fun PremiumLibraryCards(
    libraryState: LibraryState,
    onTimeCapsuleClicked: () -> Unit,
    onExploreClicked: () -> Unit
) {
    val context = LocalContext.current
    var showInsightsDialog by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 15.dp, bottom = 25.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val isNewUser = libraryState.totalPlays == 0 && libraryState.timeCapsuleSongs.isEmpty()
        
        // --- CARD 1: MUSIC INSIGHTS ---
        Card(
            modifier = Modifier
                .weight(1f)
                .height(180.dp)
                .clickable { showInsightsDialog = true },
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFF6B11CB), Color(0xFF2575FC))
                        )
                    )
                    .padding(14.dp)
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
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrendingUp,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    Column {
                        Text(
                            text = "Music Insights",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (isNewUser) {
                            Text(
                                text = "Unlock insights as you play your first songs!",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        } else {
                            Text(
                                text = "Total Plays: ${libraryState.totalPlays}\nTop Artist: ${if (libraryState.topArtistName.length > 12) libraryState.topArtistName.take(12) + "..." else libraryState.topArtistName}",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }

        // --- CARD 2: TIME CAPSULE ---
        Card(
            modifier = Modifier
                .weight(1f)
                .height(180.dp)
                .clickable {
                    if (isNewUser) {
                        Toast.makeText(context, "Favorite songs by clicking the ❤️ icon to build your capsule!", Toast.LENGTH_LONG).show()
                    } else {
                        onTimeCapsuleClicked()
                    }
                },
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFFF416C), Color(0xFFFF4B2B))
                        )
                    )
                    .padding(14.dp)
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
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color.White.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    
                    Column {
                        Text(
                            text = "Time Capsule",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        if (isNewUser) {
                            Text(
                                text = "Tap the ❤️ icon on tracks to build your future throwback mix!",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 11.sp,
                                lineHeight = 14.sp
                            )
                        } else {
                            Text(
                                text = "${libraryState.timeCapsuleSongs.size} personal throwback tracks waiting...",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // --- MUSIC INSIGHTS POPUP DIALOG ---
    if (showInsightsDialog) {
        val isNewUser = libraryState.totalPlays == 0
        AlertDialog(
            onDismissRequest = { showInsightsDialog = false },
            title = {
                Text(
                    text = if (isNewUser) "Unlock Insights" else "Your Listening Insights 📊",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (isNewUser) {
                        Text(
                            text = "We collect your play history completely locally and private to your device. Play some tracks from the trending feed to calculate your Top Artists and listening counters!",
                            fontSize = 14.sp,
                            lineHeight = 18.sp
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Total Played Tracks", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(text = "${libraryState.totalPlays}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Your Top Artist", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(text = libraryState.topArtistName, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Top Artist Plays", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(text = "${libraryState.topArtistPlays} plays", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "⭐ You are a certified super fan! Keep listening to grow your statistics locally.",
                                fontStyle = FontStyle.Italic,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showInsightsDialog = false
                        if (isNewUser) {
                            onExploreClicked()
                        }
                    }
                ) {
                    Text(text = if (isNewUser) "Explore Music 🎵" else "Cool!")
                }
            },
            dismissButton = {
                if (isNewUser) {
                    TextButton(onClick = { showInsightsDialog = false }) {
                        Text(text = "Not now")
                    }
                }
            }
        )
    }
}

