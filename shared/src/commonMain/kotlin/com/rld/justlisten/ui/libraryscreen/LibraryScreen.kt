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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import com.rld.justlisten.ui.utils.showToast
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip

import com.rld.justlisten.ui.actions.LibraryScreenAction
import com.rld.justlisten.ui.addplaylistscreen.components.AddPlaylistDialog
import com.rld.justlisten.ui.libraryscreen.components.RowListOfRecentActivity

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
                lastIndexReached = libraryState.lastIndexReached,
                onArtistClicked = { id, name -> onAction(LibraryScreenAction.ArtistClicked(id, name)) }
            )
            
            HorizontalDivider(thickness = 1.dp, modifier = Modifier.padding(vertical = 12.dp))
            
            Header(text = "My Collection")
            
            // Grid layout of four premium cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // CARD 1: Favorite Playlist
                    val favoriteSongIcon = if (libraryState.favoritePlaylistItems.isNotEmpty()) {
                        libraryState.favoritePlaylistItems[0].songIconList.songImageURL480px
                    } else ""
                    
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(95.dp)
                            .clickable {
                                onAction(LibraryScreenAction.FavoritePlaylistPressed("Favorite", favoriteSongIcon, "Favorite", "You"))
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFE91E63).copy(alpha = 0.4f), Color(0xFFE91E63).copy(alpha = 0.05f))
                            )
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(Color(0xFFE91E63).copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = Color(0xFFE91E63),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Favorites",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${libraryState.favoritePlaylistItems.size} tracks",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // CARD 2: Most Played
                    val mostPlayedSongIcon = if (libraryState.mostPlayedSongs.isNotEmpty()) {
                        libraryState.mostPlayedSongs[0].songIconList.songImageURL480px
                    } else ""
                    
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(95.dp)
                            .clickable {
                                onAction(LibraryScreenAction.MostPlayedPlaylistPressed("Most Played", mostPlayedSongIcon, "Most Played", "You"))
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFFFF9800).copy(alpha = 0.4f), Color(0xFFFF9800).copy(alpha = 0.05f))
                            )
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(Color(0xFFFF9800).copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                    contentDescription = null,
                                    tint = Color(0xFFFF9800),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Most Played",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${libraryState.mostPlayedSongs.size} tracks",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // CARD 3: All Playlists View
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(95.dp)
                            .clickable {
                                onAction(LibraryScreenAction.PlayListViewClicked)
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        ),
                        border = BorderStroke(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF9C27B0).copy(alpha = 0.4f), Color(0xFF9C27B0).copy(alpha = 0.05f))
                            )
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(Color(0xFF9C27B0).copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LibraryMusic,
                                    contentDescription = null,
                                    tint = Color(0xFF9C27B0),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Playlists",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${libraryState.playlistsCreated.size} custom",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // CARD 4: Create Playlist (Custom Dash-Border Glowing Card)
                    val openCreatePlaylistDialog = remember { mutableStateOf(false) }
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(95.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f))
                            .drawBehind {
                                val stroke = Stroke(
                                    width = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                                )
                                drawRoundRect(
                                    brush = Brush.linearGradient(
                                        colors = listOf(Color(0xFF9C27B0), Color(0xFF00BCD4))
                                    ),
                                    style = stroke,
                                    cornerRadius = CornerRadius(16.dp.toPx())
                                )
                            }
                            .clickable {
                                openCreatePlaylistDialog.value = true
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(Color(0xFF9C27B0).copy(alpha = 0.15f), Color(0xFF00BCD4).copy(alpha = 0.15f))
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = Color(0xFF00BCD4),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Create Playlist",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "New Collection",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // Dialog to instantly create a playlist
                    val isUserLoggedIn = libraryState.sessionState is com.rld.justlisten.datalayer.repositories.SessionState.Authenticated
                    AddPlaylistDialog(
                        openDialog = openCreatePlaylistDialog,
                        isUserLoggedIn = isUserLoggedIn,
                        onAddPlaylistClickedFull = { title, desc, isRemote, isPrivate ->
                            onAction(LibraryScreenAction.PlaylistCreatedClicked(title, desc, emptyList(), isRemote, isPrivate))
                        }
                    )
                }
            }

            val localPlaylists = libraryState.playlistsCreated.filter { !it.isRemote }
            val remotePlaylists = libraryState.playlistsCreated.filter { it.isRemote }

            // Display "My Local Playlists" row if they have local custom playlists
            if (localPlaylists.isNotEmpty()) {
                Spacer(modifier = Modifier.height(15.dp))
                Header(text = "My Local Playlists")
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(localPlaylists) { playlist ->
                         MyPlaylistRowItem(
                             playlist, 
                             onPlaylistClicked = { title, desc, songs ->
                                  onAction(LibraryScreenAction.PlaylistCreatedClicked(title, desc, songs, isRemote = false))
                              }
                          )
                    }
                }
            }

            // Display "My Audius Playlists" row if they have remote custom playlists
            if (remotePlaylists.isNotEmpty()) {
                Spacer(modifier = Modifier.height(15.dp))
                Header(text = "My Audius Playlists")
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(remotePlaylists) { playlist ->
                         MyPlaylistRowItem(
                             playlist, 
                             onPlaylistClicked = { title, desc, songs ->
                                  onAction(LibraryScreenAction.PlaylistCreatedClicked(title, desc, songs, isRemote = true, isPrivate = playlist.isPrivate))
                              }
                          )
                    }
                }
            }

            Spacer(modifier = Modifier.height(15.dp))
            PremiumLibraryCards(
                libraryState = libraryState,
                onTimeCapsuleClicked = { onAction(LibraryScreenAction.TimeCapsulePressed) },
                onMusicInsightsClicked = { onAction(LibraryScreenAction.MusicInsightsPressed) }
            )
        }
    }
}


@Composable
fun PremiumLibraryCards(
    libraryState: LibraryState,
    onTimeCapsuleClicked: () -> Unit,
    onMusicInsightsClicked: () -> Unit
) {
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
                .clickable { onMusicInsightsClicked() },
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
                                imageVector = Icons.AutoMirrored.Filled.TrendingUp,
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
                        showToast("Favorite songs by clicking the ❤️ icon to build your capsule!")
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
}

