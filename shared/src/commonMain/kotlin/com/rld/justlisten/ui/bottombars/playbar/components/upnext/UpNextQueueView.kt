package com.rld.justlisten.ui.bottombars.playbar.components.upnext

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rld.justlisten.media.MediaMetadata
import com.rld.justlisten.media.PlaybackStatus
import com.rld.justlisten.ui.LocalMusicPlayer
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.viewmodel.player.PlayerUiState
import com.rld.justlisten.ui.actions.PlayerAction
import androidx.compose.ui.Alignment

@Stable
class UpNextDragState(
    private val localPlaylist: MutableState<List<MediaMetadata>>,
    private val musicPlayer: MusicPlayer
) {
    var draggingId by mutableStateOf<String?>(null)
        private set
    var draggingStartIndex by mutableStateOf(-1)
        private set
    var currentDragIndex by mutableStateOf(-1)
        private set
    var isPendingPlayerUpdate by mutableStateOf(false)

    val playlistSize: Int
        get() = localPlaylist.value.size

    fun startDrag(id: String, index: Int) {
        draggingId = id
        draggingStartIndex = index
        currentDragIndex = index
        isPendingPlayerUpdate = false
    }
    
    fun updateDragIndex(index: Int) {
        currentDragIndex = index
    }
    
    fun endDrag(startIndex: Int, endIndex: Int) {
        if (startIndex != endIndex) {
            val mutable = localPlaylist.value.toMutableList()
            val item = mutable.removeAt(startIndex)
            mutable.add(endIndex, item)
            localPlaylist.value = mutable
            musicPlayer.moveTrack(startIndex, endIndex)
            isPendingPlayerUpdate = true
        }
        reset()
    }
    
    fun cancelDrag() {
        isPendingPlayerUpdate = false
        reset()
    }
    
    private fun reset() {
        draggingId = null
        draggingStartIndex = -1
        currentDragIndex = -1
    }
}

@Composable
fun rememberUpNextDragState(
    localPlaylist: MutableState<List<MediaMetadata>>,
    musicPlayer: MusicPlayer
): UpNextDragState {
    return remember(localPlaylist, musicPlayer) {
        UpNextDragState(localPlaylist, musicPlayer)
    }
}

@Composable
fun UpNextQueueView(
    playlist: List<MediaMetadata>,
    uiState: PlayerUiState,
    onAction: (PlayerAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val musicPlayer = LocalMusicPlayer.current
    val playbackState by musicPlayer.playbackState.collectAsState()
    val currentMedia = playbackState.currentMedia

    // We maintain a local list to prevent glitchy drag-and-drop due to state flow delays
    val localPlaylistState = remember { mutableStateOf(playlist) }

    // Drag state hoisted to the parent so other items can read it without localPlaylist mutations
    val dragState = rememberUpNextDragState(localPlaylistState, musicPlayer)

    LaunchedEffect(playlist) {
        if (dragState.draggingId == null) {
            if (dragState.isPendingPlayerUpdate) {
                val localIds = localPlaylistState.value.map { it.id }
                val newIds = playlist.map { it.id }
                if (localIds.toSet() != newIds.toSet()) {
                    dragState.isPendingPlayerUpdate = false
                    localPlaylistState.value = playlist
                } else if (localIds == newIds) {
                    dragState.isPendingPlayerUpdate = false
                }
            } else {
                localPlaylistState.value = playlist
            }
        }
    }

    val localPlaylist = localPlaylistState.value
    val currentIndex = localPlaylist.indexOfFirst { it.id == currentMedia?.id }

    val pastSongs = remember(localPlaylist, currentIndex) {
        localPlaylist.take(maxOf(0, currentIndex))
    }
    val currentSong = remember(localPlaylist, currentIndex) {
        localPlaylist.getOrNull(currentIndex)
    }
    val nextSongs = remember(localPlaylist, currentIndex) {
        if (currentIndex >= 0 && currentIndex < localPlaylist.size - 1)
            localPlaylist.drop(currentIndex + 1)
        else
            emptyList()
    }

    val lazyListState = rememberLazyListState()
    val isPlaying = playbackState.status == PlaybackStatus.PLAYING

    LazyColumn(
        state = lazyListState,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (pastSongs.isNotEmpty()) {
            item {
                Text(
                    text = "PAST SONGS",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
            itemsIndexed(pastSongs, key = { index, song -> "past_${song.id}_$index" }) { relativeIndex, song ->
                TrackCard(
                    song = song,
                    actualIndex = relativeIndex,
                    currentIndex = currentIndex,
                    canDrag = true,
                    isPlaying = isPlaying,
                    dragState = dragState
                )
            }
            item {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.1f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }

        if (currentSong != null) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "CURRENTLY PLAYING",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
                TrackCard(
                    song = currentSong,
                    actualIndex = currentIndex,
                    currentIndex = currentIndex,
                    canDrag = false,
                    isPlaying = isPlaying,
                    dragState = dragState
                )
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.1f),
                    thickness = 1.dp,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }

        if (nextSongs.isNotEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "UP NEXT",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
            itemsIndexed(nextSongs, key = { index, song -> "next_${song.id}_$index" }) { relativeIndex, song ->
                val actualIndex = (if (currentIndex >= 0) currentIndex + 1 else 0) + relativeIndex
                TrackCard(
                    song = song,
                    actualIndex = actualIndex,
                    currentIndex = currentIndex,
                    canDrag = true,
                    isPlaying = isPlaying,
                    dragState = dragState
                )
            }
        }

        // ── Autoplay Toggle Section ──
        item {
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.1f),
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Autoplay",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Play similar songs when your queue ends",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp
                    )
                }
                androidx.compose.material3.Switch(
                    checked = uiState.isAutoplayEnabled,
                    onCheckedChange = { enabled ->
                        onAction(PlayerAction.ToggleAutoplay(enabled))
                    },
                    colors = androidx.compose.material3.SwitchDefaults.colors(
                        checkedThumbColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                        checkedTrackColor = androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                )
            }
        }

        // ── Autoplay Recommended Songs ──
        if (uiState.isAutoplayEnabled && uiState.recommendedSongs.isNotEmpty()) {
            item {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "RECOMMENDED SONGS",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
            itemsIndexed(uiState.recommendedSongs, key = { index, song -> "rec_${song.id}_$index" }) { index, song ->
                val mediaMetadata = MediaMetadata(
                    id = song.id,
                    title = song.title,
                    artist = song.user,
                    duration = 0L,
                    artworkUrl = song.songIconList.songImageURL480px,
                    lowResArtworkUrl = song.songIconList.songImageURL150px,
                    isFavorite = song.isFavorite,
                    isReposted = song.isReposted,
                    repostCount = song.repostCount,
                    favoriteCount = song.favoriteCount,
                    commentCount = song.commentCount,
                    playCount = song.playCount,
                    artistId = song.userId
                )
                TrackCard(
                    song = mediaMetadata,
                    actualIndex = index,
                    currentIndex = -1,
                    canDrag = false,
                    isPlaying = false,
                    dragState = dragState,
                    onTrackClicked = {
                        onAction(PlayerAction.PlayRecommendedTrack(song.id))
                    }
                )
            }
        }
    }
}
