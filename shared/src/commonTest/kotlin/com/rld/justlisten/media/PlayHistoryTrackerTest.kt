package com.rld.justlisten.media

import com.rld.justlisten.database.addplaylistscreen.AddPlaylist
import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.datalayer.repositories.LibraryRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class PlayHistoryTrackerTest {
    private val testDispatcher = StandardTestDispatcher()
    private val fakeLibraryRepo = FakeLibraryRepo()
    private val fakeMusicPlayer = FakeMusicPlayer()
    private lateinit var tracker: PlayHistoryTracker
    private lateinit var scope: CoroutineScope

    @BeforeTest
    fun setUp() {
        scope = CoroutineScope(SupervisorJob() + testDispatcher)
        tracker = PlayHistoryTracker(fakeLibraryRepo, fakeMusicPlayer, scope)
    }

    @AfterTest
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun testTrackPlayback_LogsCompletedAndIncrementalDuration() = runTest(testDispatcher) {
        val songId = "test_song_123"
        val media = MediaMetadata(
            id = songId,
            title = "Title",
            artist = "Artist",
            duration = 60000L // 60s total, threshold is 30s
        )

        // 1. Transition state to PLAYING at position 0
        fakeMusicPlayer.updateState(PlaybackStatus.PLAYING, 0L, media)
        testDispatcher.scheduler.advanceUntilIdle()

        // Wait 1.5 seconds (PlayHistoryTracker loops inside PLAYING state with 1-second delays)
        testDispatcher.scheduler.apply {
            advanceTimeBy(1500)
            runCurrent()
        }

        // 2. Pause playback (total duration accumulated is ~1 second)
        fakeMusicPlayer.updateState(PlaybackStatus.PAUSED, 1000L, media)
        testDispatcher.scheduler.advanceUntilIdle()

        // 3. Clear/stop session to flush duration logs
        fakeMusicPlayer.updateState(PlaybackStatus.IDLE, 0L, null)
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert no completed play since we only played ~1 second (less than 30s threshold)
        assertEquals(0, fakeLibraryRepo.completedPlays.size)
        // Assert duration is logged as 1 second
        assertEquals(1, fakeLibraryRepo.durationLogs.size)
        assertEquals(1L, fakeLibraryRepo.durationLogs[songId])

        // Cancel the tracker scope to stop the background delay loop and prevent test hang
        scope.cancel()
    }

    class FakeLibraryRepo : LibraryRepository {
        val completedPlays = mutableListOf<String>()
        val durationLogs = mutableMapOf<String, Long>()

        override fun insertPlayLog(songId: String, timestamp: Long, durationPlayedSec: Long, completed: Boolean) {
            if (completed) {
                completedPlays.add(songId)
            }
            if (durationPlayedSec > 0) {
                durationLogs[songId] = (durationLogs[songId] ?: 0L) + durationPlayedSec
            }
        }

        override fun saveSongToRecent(id: String, title: String, user: UserModel, songImgList: SongIconList, playlistName: String) {}
        override fun saveSongToMostPlayed(id: String, title: String, user: UserModel, songImgList: SongIconList, playlistName: String) {}
        override fun getMostPlayedSongs(numberOfLines: Long): List<PlayListModel> = emptyList()
        override fun getRecentSongs(numberOfLines: Long): List<PlayListModel> = emptyList()
        override fun getTimeCapsuleSongs(limit: Long): List<PlayListModel> = emptyList()

        override fun getTotalPlays(): Long = 0L
        override fun getUniquePlays(): Long = 0L
        override fun getTotalDurationPlayed(): Long = 0L
        override fun getDurationPlayedForSong(songId: String): Long = 0L
        override fun getDurationPlayedForArtist(user: UserModel): Long = 0L
        override fun getMostPlayedSongsFromHistory(limit: Long, offset: Long): List<PlayListModel> = emptyList()
        override fun getTopArtistFromHistory(): Triple<UserModel, Long, Long>? = null
        override fun getPlayHistoryFlow(): Flow<Unit> = flowOf(Unit)

        override fun savePlaylist(playlistName: String, playlistDescription: String?, isRemote: Boolean, isPrivate: Boolean, playlistId: String?) {}
        override fun getAddPlaylist(): List<AddPlaylist> = emptyList()
        override fun getAddPlaylistFlow(): Flow<List<AddPlaylist>> = flowOf(emptyList())
        override fun updatePlaylistSongs(playlistName: String, playlistDescription: String?, songList: List<String>, isRemote: Boolean, isPrivate: Boolean, playlistId: String?) {}
        override fun deletePlaylist(playlistName: String) {}
        override fun updatePlaylistName(oldName: String, newName: String) {}
    }

    class FakeMusicPlayer : MusicPlayer {
        override var currentlyPlayingPlaylistId: String? = null
        override fun release() {}
        
        private val _playbackState = MutableStateFlow(PlaybackState(PlaybackStatus.IDLE, 0L))
        override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()
        
        private val _currentPlaylist = MutableStateFlow<List<MediaMetadata>>(emptyList())
        override val currentPlaylist: StateFlow<List<MediaMetadata>> = _currentPlaylist.asStateFlow()

        override val isConnected: StateFlow<Boolean> = MutableStateFlow(true).asStateFlow()
        override val networkError: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()

        fun updateState(status: PlaybackStatus, position: Long, media: MediaMetadata?) {
            _playbackState.value = PlaybackState(
                status = status,
                currentPosition = position,
                currentMedia = media
            )
        }

        override fun play() {}
        override fun pause() {}
        override fun stop() {}
        override fun skipToNext() {}
        override fun skipToPrevious() {}
        override fun seekTo(position: Long) {}
        override fun setShuffleModeEnabled(enabled: Boolean) {}
        override fun setRepeatMode(repeatMode: RepeatMode) {}
        override fun playMedia(mediaId: String) {}
        override fun updatePlaylist(list: List<com.rld.justlisten.viewmodel.interfaces.Item>) {}
        override fun refreshMetadata() {}
        override fun updateTrackMetadata(
            songId: String,
            repostCount: Int,
            favoriteCount: Int,
            commentCount: Int,
            playCount: Int,
            artistId: String
        ) {}
        override fun removeTrack(index: Int) {}
        override fun moveTrack(fromIndex: Int, toIndex: Int) {}
        override fun addTracksToQueue(tracks: List<com.rld.justlisten.viewmodel.interfaces.Item>) {}
    }
}
