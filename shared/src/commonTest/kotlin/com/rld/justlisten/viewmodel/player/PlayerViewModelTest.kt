package com.rld.justlisten.viewmodel.player

import com.rld.justlisten.database.addplaylistscreen.AddPlaylist
import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.datalayer.repositories.FavoritesRepository
import com.rld.justlisten.datalayer.repositories.LibraryRepository
import com.rld.justlisten.datalayer.repositories.PlaylistRepository
import com.rld.justlisten.datalayer.repositories.AuthRepository
import com.rld.justlisten.datalayer.repositories.SessionState
import com.rld.justlisten.viewmodel.screens.playlist.PlayListEnum
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem
import com.rld.justlisten.viewmodel.screens.search.TrackItem
import com.rld.justlisten.media.MediaMetadata
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.media.PlaybackState
import com.rld.justlisten.media.PlaybackStatus
import com.rld.justlisten.media.RepeatMode
import com.rld.justlisten.ui.actions.PlayerAction
import com.rld.justlisten.datalayer.repositories.FeedRepository
import com.rld.justlisten.datalayer.repositories.SettingsRepository
import com.rld.justlisten.database.settingsscreen.SettingsInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.rld.justlisten.media.PlayHistoryTracker
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeFavoritesRepo = FakeFavoritesRepository()
    private val fakeLibraryRepo = FakeLibraryRepository()
    private val fakeMusicPlayer = FakeMusicPlayer()
    private val fakePlaylistRepo = FakePlaylistRepository()
    private val fakeAuthRepo = FakeAuthRepository()
    private val fakeFeedRepo = FakeFeedRepository()
    private val fakeSettingsRepo = FakeSettingsRepository()

    private lateinit var viewModel: PlayerViewModel
    private lateinit var playHistoryTracker: PlayHistoryTracker

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        playHistoryTracker = PlayHistoryTracker(fakeLibraryRepo, fakeMusicPlayer, kotlinx.coroutines.CoroutineScope(testDispatcher))
        playHistoryTracker.release() // Cancel background delay loops to prevent advanceUntilIdle hangs
        viewModel = PlayerViewModel(
            favoritesRepository = fakeFavoritesRepo,
            libraryRepository = fakeLibraryRepo,
            playlistRepository = fakePlaylistRepo,
            musicPlayer = fakeMusicPlayer,
            authRepository = fakeAuthRepo,
            feedRepository = fakeFeedRepo,
            settingsRepository = fakeSettingsRepo
        )
    }

    @AfterTest
    fun tearDown() {
        playHistoryTracker.release()
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() = runTest(testDispatcher) {
        val collectJob = launch { viewModel.playerUiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.playerUiState.value
        assertEquals(emptyList(), uiState.addPlaylistList)
        assertEquals(PlaybackStatus.IDLE, uiState.playbackState?.status)

        collectJob.cancel()
    }

    @Test
    fun testLoadPlaylists() = runTest(testDispatcher) {
        val collectJob = launch { viewModel.playerUiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        val dummyPlaylist = AddPlaylist(
            playlistName = "My Playlist",
            playlistDescription = "Test desc",
            songsList = emptyList(),
            isRemote = false,
            isPrivate = false,
            playlistId = null
        )
        fakeLibraryRepo.addPlaylist(dummyPlaylist)

        viewModel.onAction(PlayerAction.LoadPlaylists)
        testDispatcher.scheduler.advanceUntilIdle()

        val uiState = viewModel.playerUiState.value
        assertEquals(1, uiState.addPlaylistList.size)
        assertEquals("My Playlist", uiState.addPlaylistList[0].playlistName)

        collectJob.cancel()
    }

    @Test
    fun testToggleFavorite() = runTest(testDispatcher) {
        val user = UserModel("Artist")
        val icon = SongIconList("img", "img", "img")
        val action = PlayerAction.ToggleFavorite(
            songId = "song123",
            title = "Starlight",
            user = user,
            songIcon = icon,
            isFavorite = true
        )

        viewModel.onAction(action)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeFavoritesRepo.hasFavorite("song123"))
        assertTrue(fakeMusicPlayer.refreshMetadataCalled)
    }

    @Test
    fun testCreatePlaylist() = runTest(testDispatcher) {
        val action = PlayerAction.CreatePlaylist("Gym Mix", "Workout music")

        viewModel.onAction(action)
        testDispatcher.scheduler.advanceUntilIdle()

        val playlists = fakeLibraryRepo.getAddPlaylist()
        assertEquals(1, playlists.size)
        assertEquals("Gym Mix", playlists[0].playlistName)
    }

    @Test
    fun testSkipNext() = runTest(testDispatcher) {
        viewModel.onAction(PlayerAction.SkipNext)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeMusicPlayer.skipToNextCalled)
    }

    @Test
    fun testSkipPrevious() = runTest(testDispatcher) {
        viewModel.onAction(PlayerAction.SkipPrevious)
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(fakeMusicPlayer.skipToPreviousCalled)
    }

    @Test
    fun testAddSongToPlaylist() = runTest(testDispatcher) {
        val collectJob = launch { viewModel.playerUiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        // 1. Create a playlist in the repo first
        val dummyPlaylist = AddPlaylist(
            playlistName = "Running Mix",
            playlistDescription = "Workout tracks",
            songsList = emptyList(),
            isRemote = false,
            isPrivate = false,
            playlistId = null
        )
        fakeLibraryRepo.addPlaylist(dummyPlaylist)
        viewModel.onAction(PlayerAction.LoadPlaylists)
        testDispatcher.scheduler.advanceUntilIdle()

        // 2. Add a song to the playlist
        val action = PlayerAction.AddSongToPlaylist(
            playlistTitle = "Running Mix",
            playlistDescription = "Workout tracks",
            songs = listOf("song123")
        )
        viewModel.onAction(action)
        testDispatcher.scheduler.advanceUntilIdle()

        // 3. Verify that the UI state contains the updated playlist with the song!
        val playlists = viewModel.playerUiState.value.addPlaylistList
        assertEquals(1, playlists.size)
        assertEquals("Running Mix", playlists[0].playlistName)
        assertEquals(listOf("song123"), playlists[0].songsList)

        collectJob.cancel()
    }

    @Test
    fun testToggleRepostGuest() = runTest(testDispatcher) {
        val collectJob = launch { viewModel.playerUiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        fakeAuthRepo.setSessionState(SessionState.Guest)

        viewModel.onAction(PlayerAction.ToggleRepost("song123", isRepost = true))
        testDispatcher.scheduler.advanceUntilIdle()

        assertTrue(viewModel.playerUiState.value.showConnectPrompt)
        assertFalse(fakePlaylistRepo.isTrackReposted("song123"))

        collectJob.cancel()
    }

    @Test
    fun testToggleRepostAuthenticated() = runTest(testDispatcher) {
        val collectJob = launch { viewModel.playerUiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        val dummyProfile = com.rld.justlisten.datalayer.webservices.apis.authcalls.MeResponse(
            userId = "user123",
            name = "Test User",
            handle = "testuser",
            verified = false
        )
        fakeAuthRepo.setSessionState(SessionState.Authenticated(dummyProfile))

        viewModel.onAction(PlayerAction.ToggleRepost("song123", isRepost = true))
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(viewModel.playerUiState.value.showConnectPrompt)
        assertTrue(fakePlaylistRepo.isTrackReposted("song123"))
        assertTrue(fakeMusicPlayer.refreshMetadataCalled)

        // Now unrepost
        fakeMusicPlayer.refreshMetadataCalled = false
        viewModel.onAction(PlayerAction.ToggleRepost("song123", isRepost = false))
        testDispatcher.scheduler.advanceUntilIdle()

        assertFalse(fakePlaylistRepo.isTrackReposted("song123"))
        assertTrue(fakeMusicPlayer.refreshMetadataCalled)

        collectJob.cancel()
    }

    @Test
    fun testToggleAutoplay() = runTest(testDispatcher) {
        val collectJob = launch { viewModel.playerUiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        // Toggle OFF
        viewModel.onAction(PlayerAction.ToggleAutoplay(false))
        testDispatcher.scheduler.advanceUntilIdle()

        var retries = 100
        while (viewModel.playerUiState.value.isAutoplayEnabled && retries > 0) {
            delay(10)
            testDispatcher.scheduler.runCurrent()
            retries--
        }

        assertFalse(viewModel.playerUiState.value.isAutoplayEnabled)
        assertFalse(fakeSettingsRepo.getSettingsInfo().isOngoingStreamEnabled)

        // Toggle ON
        viewModel.onAction(PlayerAction.ToggleAutoplay(true))
        testDispatcher.scheduler.advanceUntilIdle()

        retries = 100
        while (!viewModel.playerUiState.value.isAutoplayEnabled && retries > 0) {
            delay(10)
            testDispatcher.scheduler.runCurrent()
            retries--
        }

        assertTrue(viewModel.playerUiState.value.isAutoplayEnabled)
        assertTrue(fakeSettingsRepo.getSettingsInfo().isOngoingStreamEnabled)

        collectJob.cancel()
    }

    @Test
    fun testAutoplayTriggersAtQueueEnd() = runTest(testDispatcher) {
        val collectJob = launch { viewModel.playerUiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        // 1. Enable autoplay
        viewModel.onAction(PlayerAction.ToggleAutoplay(true))
        testDispatcher.scheduler.advanceUntilIdle()

        var retries = 100
        while (!viewModel.playerUiState.value.isAutoplayEnabled && retries > 0) {
            delay(10)
            testDispatcher.scheduler.runCurrent()
            retries--
        }

        // 2. Set up recommendations in repository
        val mockData = com.rld.justlisten.datalayer.models.PlayListModel(
            id = "rec123",
            title = "Recommended Song",
            user = UserModel("Rec Artist"),
            songImgList = SongIconList("img", "img", "img")
        )
        val mockTrack = TrackItem(mockData, isFavorite = false, isReposted = false)
        fakePlaylistRepo.mockTracks = listOf(mockTrack)

        // 3. Set up queue with 1 song and start playing it
        val activeSong = MediaMetadata("song1", "Song 1", "Artist 1", 1000L)
        fakeMusicPlayer.setCurrentPlaylist(listOf(activeSong))
        fakeMusicPlayer.playMedia("song1")
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify recommendations got fetched
        assertEquals(1, viewModel.playerUiState.value.recommendedSongs.size)
        assertEquals("rec123", viewModel.playerUiState.value.recommendedSongs[0].id)

        // 4. Simulate queue finishing (STOPPED status while song1 was active)
        fakeMusicPlayer.stop()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify recommended song got added to queue and started playing
        assertEquals("rec123", fakeMusicPlayer.playMediaCalledWithId)
        assertTrue(fakeMusicPlayer.addedTracksToQueue.any { it.id == "rec123" })

        collectJob.cancel()
    }

    @Test
    fun testPlayRecommendedTrackAction() = runTest(testDispatcher) {
        val collectJob = launch { viewModel.playerUiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        // 1. Set up recommendations in repository
        val mockData = com.rld.justlisten.datalayer.models.PlayListModel(
            id = "rec123",
            title = "Recommended Song",
            user = UserModel("Rec Artist"),
            songImgList = SongIconList("img", "img", "img")
        )
        val mockTrack = TrackItem(mockData, isFavorite = false, isReposted = false)
        fakePlaylistRepo.mockTracks = listOf(mockTrack)

        // Set active track to load recommendations
        val activeSong = MediaMetadata("song1", "Song 1", "Artist 1", 1000L)
        fakeMusicPlayer.setCurrentPlaylist(listOf(activeSong))
        fakeMusicPlayer.playMedia("song1")
        testDispatcher.scheduler.advanceUntilIdle()

        // 2. Play recommended track
        viewModel.onAction(PlayerAction.PlayRecommendedTrack("rec123"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify it was queued and played
        assertEquals("rec123", fakeMusicPlayer.playMediaCalledWithId)
        assertTrue(fakeMusicPlayer.currentPlaylist.value.any { it.id == "rec123" })

        collectJob.cancel()
    }

    @Test
    fun testAutoplayPaginationAndReset() = runTest(testDispatcher) {
        val collectJob = launch { viewModel.playerUiState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        // 1. Enable autoplay
        viewModel.onAction(PlayerAction.ToggleAutoplay(true))
        testDispatcher.scheduler.advanceUntilIdle()

        var retries = 100
        while (!viewModel.playerUiState.value.isAutoplayEnabled && retries > 0) {
            delay(10)
            testDispatcher.scheduler.runCurrent()
            retries--
        }

        // 2. Mock 50 trending tracks for pagination
        val mockDataList = (1..50).map { i ->
            TrackItem(
                PlayListModel(
                    id = "rec_$i",
                    title = "Recommended Song $i",
                    user = UserModel("Rec Artist $i"),
                    songImgList = SongIconList("img", "img", "img")
                ),
                isFavorite = false,
                isReposted = false
            )
        }
        fakePlaylistRepo.mockTracks = mockDataList

        // Set active track to load recommendations
        val activeSong = MediaMetadata("song1", "Song 1", "Artist 1", 1000L)
        fakeMusicPlayer.setCurrentPlaylist(listOf(activeSong))
        fakeMusicPlayer.playMedia("song1")
        testDispatcher.scheduler.advanceUntilIdle()

        // Initially recommendations are loaded from offset 0
        var recommended = viewModel.playerUiState.value.recommendedSongs
        assertEquals(10, recommended.size)
        assertEquals("rec_1", recommended[0].id)

        // 3. Play recommended track "rec_1"
        viewModel.onAction(PlayerAction.PlayRecommendedTrack("rec_1"))
        testDispatcher.scheduler.advanceUntilIdle()

        // Now offset should have incremented by 10, so next recommendations should start from rec_11
        recommended = viewModel.playerUiState.value.recommendedSongs
        assertEquals(10, recommended.size)
        assertEquals("rec_11", recommended[0].id)

        // 4. Simulate queue finishing (play the last song in the playlist first, then stop)
        fakeMusicPlayer.playMedia("rec_10")
        testDispatcher.scheduler.advanceUntilIdle()
        fakeMusicPlayer.stop()
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify next autoplay song started playing ("rec_11")
        assertEquals("rec_11", fakeMusicPlayer.playMediaCalledWithId)
        
        // Offset should have incremented by another 10 (offset = 20), so recommendations start from rec_21
        recommended = viewModel.playerUiState.value.recommendedSongs
        assertEquals(10, recommended.size)
        assertEquals("rec_21", recommended[0].id)

        // 5. Play a manual track ("manualSong")
        val manualSong = MediaMetadata("manualSong", "Manual", "Artist", 1000L)
        fakeMusicPlayer.setCurrentPlaylist(listOf(manualSong))
        fakeMusicPlayer.playMedia("manualSong")
        testDispatcher.scheduler.advanceUntilIdle()

        // Offset should reset to 0, so recommendations should start from rec_1 again!
        recommended = viewModel.playerUiState.value.recommendedSongs
        assertEquals(10, recommended.size)
        assertEquals("rec_1", recommended[0].id)

        collectJob.cancel()
    }
}

// ── FAKES IMPLEMENTATION ───────────────────────────────────────────

class FakeFavoritesRepository : FavoritesRepository {
    private val favoritesMap = mutableMapOf<String, PlayListModel>()

    fun hasFavorite(id: String): Boolean = favoritesMap.containsKey(id)

    override suspend fun saveSongToFavorites(
        id: String,
        title: String,
        user: UserModel,
        songImgList: SongIconList,
        playlistName: String,
        isFavorite: Boolean
    ) {
        if (isFavorite) {
            favoritesMap[id] = PlayListModel(
                id = id,
                title = title,
                playlistTitle = playlistName,
                songImgList = songImgList,
                user = user
            )
        } else {
            favoritesMap.remove(id)
        }
    }

    override suspend fun getFavoritePlaylist(): List<PlayListModel> = favoritesMap.values.toList()

    override suspend fun getFavoritePlaylistWithId(id: String): String? = favoritesMap[id]?.id

    override fun getFavoritePlaylistFlow(): Flow<List<PlayListModel>> = flowOf(favoritesMap.values.toList())
}

class FakeLibraryRepository : LibraryRepository {
    private val playlists = mutableListOf<AddPlaylist>()
    private val playlistsFlow = MutableStateFlow<List<AddPlaylist>>(emptyList())

    private fun updateFlow() {
        playlistsFlow.value = playlists.toList()
    }

    fun addPlaylist(playlist: AddPlaylist) {
        playlists.add(playlist)
        updateFlow()
    }

    override suspend fun savePlaylist(playlistName: String, playlistDescription: String?, isRemote: Boolean, isPrivate: Boolean, playlistId: String?) {
        playlists.add(AddPlaylist(playlistName, playlistDescription ?: "", songsList = emptyList(), isRemote = isRemote, isPrivate = isPrivate, playlistId = playlistId))
        updateFlow()
    }

    override suspend fun getAddPlaylist(): List<AddPlaylist> = playlists

    override fun getAddPlaylistFlow(): Flow<List<AddPlaylist>> = playlistsFlow

    override suspend fun updatePlaylistSongs(playlistName: String, playlistDescription: String?, songList: List<String>, isRemote: Boolean, isPrivate: Boolean, playlistId: String?) {
        val index = playlists.indexOfFirst { it.playlistName == playlistName }
        if (index >= 0) {
            playlists[index] = AddPlaylist(playlistName, playlistDescription ?: "", songsList = songList, isRemote = isRemote, isPrivate = isPrivate, playlistId = playlistId)
        }
        updateFlow()
    }

    override suspend fun deletePlaylist(playlistName: String) {
        playlists.removeAll { it.playlistName == playlistName }
        updateFlow()
    }

    override suspend fun updatePlaylistName(oldName: String, newName: String) {
        val index = playlists.indexOfFirst { it.playlistName == oldName }
        if (index >= 0) {
            playlists[index] = playlists[index].copy(playlistName = newName)
        }
        updateFlow()
    }


    // Unused by PlayerViewModel in tests
    override suspend fun saveSongToRecent(id: String, title: String, user: UserModel, songImgList: SongIconList, playlistName: String) {}
    override suspend fun saveSongToMostPlayed(id: String, title: String, user: UserModel, songImgList: SongIconList, playlistName: String) {}
    override suspend fun getMostPlayedSongs(numberOfLines: Long): List<PlayListModel> = emptyList()
    override suspend fun getRecentSongs(numberOfLines: Long): List<PlayListModel> = emptyList()
    override suspend fun getTimeCapsuleSongs(limit: Long): List<PlayListModel> = emptyList()

    override suspend fun insertPlayLog(songId: String, timestamp: Long, durationPlayedSec: Long, completed: Boolean) {}
    override suspend fun getTotalPlays(): Long = 0L
    override suspend fun getUniquePlays(): Long = 0L
    override suspend fun getTotalDurationPlayed(): Long = 0L
    override suspend fun getDurationPlayedForSong(songId: String): Long = 0L
    override suspend fun getDurationPlayedForArtist(user: UserModel): Long = 0L
    override suspend fun getMostPlayedSongsFromHistory(limit: Long, offset: Long): List<PlayListModel> = emptyList()
    override suspend fun getTopArtistFromHistory(): Triple<UserModel, Long, Long>? = null
    override fun getPlayHistoryFlow(): Flow<Unit> = flowOf(Unit)
}

class FakeMusicPlayer : MusicPlayer {
    override var currentlyPlayingPlaylistId: String? = null
    override fun release() {}
    var skipToNextCalled = false
    var skipToPreviousCalled = false
    var refreshMetadataCalled = false
    var playMediaCalledWithId: String? = null
    val addedTracksToQueue = mutableListOf<com.rld.justlisten.viewmodel.interfaces.Item>()

    private val _playbackState = MutableStateFlow(
        PlaybackState(
            status = PlaybackStatus.IDLE,
            currentPosition = 0L,
            currentMedia = null
        )
    )
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    private val _currentPlaylist = MutableStateFlow(emptyList<MediaMetadata>())
    override val currentPlaylist: StateFlow<List<MediaMetadata>> = _currentPlaylist.asStateFlow()

    override val isConnected: StateFlow<Boolean> = MutableStateFlow(true).asStateFlow()
    override val networkError: StateFlow<Boolean> = MutableStateFlow(false).asStateFlow()

    override fun play() {}
    override fun pause() {}
    override fun stop() {
        setPlaybackStatus(PlaybackStatus.STOPPED)
    }
    override fun seekTo(position: Long) {}
    override fun setShuffleModeEnabled(enabled: Boolean) {}
    override fun setRepeatMode(repeatMode: RepeatMode) {}
    override fun playMedia(mediaId: String) {
        playMediaCalledWithId = mediaId
        val song = _currentPlaylist.value.find { it.id == mediaId }
        _playbackState.value = _playbackState.value.copy(
            status = PlaybackStatus.PLAYING,
            currentMedia = song
        )
    }
    override fun updatePlaylist(list: List<com.rld.justlisten.viewmodel.interfaces.Item>) {
        _currentPlaylist.value = list.map {
            MediaMetadata(
                id = it.id,
                title = it.title,
                artist = it.user,
                duration = 0L,
                artworkUrl = it.songIconList.songImageURL480px,
                lowResArtworkUrl = it.songIconList.songImageURL150px
            )
        }
    }
    override fun removeTrack(index: Int) {}
    override fun moveTrack(fromIndex: Int, toIndex: Int) {}
    override fun addTracksToQueue(tracks: List<com.rld.justlisten.viewmodel.interfaces.Item>) {
        addedTracksToQueue.addAll(tracks)
        val newMeta = tracks.map {
            MediaMetadata(
                id = it.id,
                title = it.title,
                artist = it.user,
                duration = 0L,
                artworkUrl = it.songIconList.songImageURL480px,
                lowResArtworkUrl = it.songIconList.songImageURL150px
            )
        }
        _currentPlaylist.value = _currentPlaylist.value + newMeta
    }

    override fun skipToNext() {
        skipToNextCalled = true
    }

    override fun skipToPrevious() {
        skipToPreviousCalled = true
    }

    override fun refreshMetadata() {
        refreshMetadataCalled = true
    }

    override fun updateTrackMetadata(
        songId: String,
        repostCount: Int,
        favoriteCount: Int,
        commentCount: Int,
        playCount: Int,
        artistId: String
    ) {}

    fun setPlaybackStatus(status: PlaybackStatus) {
        _playbackState.value = _playbackState.value.copy(status = status)
    }

    fun setPlaybackMedia(media: MediaMetadata?) {
        _playbackState.value = _playbackState.value.copy(currentMedia = media)
    }

    fun setCurrentPlaylist(list: List<MediaMetadata>) {
        _currentPlaylist.value = list
    }
}

class FakePlaylistRepository : PlaylistRepository {
    private val _repostedTrackIds = MutableStateFlow<Set<String>>(emptySet())
    override val repostedTrackIdsFlow = _repostedTrackIds.asStateFlow()

    private val _repostedPlaylistIds = MutableStateFlow<Set<String>>(emptySet())
    override val repostedPlaylistIdsFlow = _repostedPlaylistIds.asStateFlow()

    var mockTracks: List<TrackItem> = emptyList()

    override fun isTrackReposted(id: String): Boolean = _repostedTrackIds.value.contains(id)
    override fun setTrackReposted(id: String, reposted: Boolean) {
        _repostedTrackIds.value = if (reposted) _repostedTrackIds.value + id else _repostedTrackIds.value - id
    }
    override fun isPlaylistReposted(id: String): Boolean = _repostedPlaylistIds.value.contains(id)
    override fun setPlaylistReposted(id: String, reposted: Boolean) {
        _repostedPlaylistIds.value = if (reposted) _repostedPlaylistIds.value + id else _repostedPlaylistIds.value - id
    }

    override suspend fun repostTrack(trackId: String): Boolean {
        setTrackReposted(trackId, true)
        return true
    }
    override suspend fun unrepostTrack(trackId: String): Boolean {
        setTrackReposted(trackId, false)
        return true
    }
    override suspend fun repostPlaylist(playlistId: String): Boolean {
        setPlaylistReposted(playlistId, true)
        return true
    }
    override suspend fun unrepostPlaylist(playlistId: String): Boolean {
        setPlaylistReposted(playlistId, false)
        return true
    }

    override suspend fun getPlaylist(
        index: Int,
        playListEnum: PlayListEnum,
        playlistId: String,
        songsList: List<String>,
        queryPlaylist: String
    ): List<PlaylistItem> = emptyList()

    override suspend fun getTracks(limit: Int, category: String, timeRange: String): List<TrackItem> = mockTracks
    
    override fun getSongWithId(songId: String): PlayListModel? {
        return PlayListModel(id = songId)
    }

    override suspend fun fetchTrackDetails(trackId: String): PlayListModel? = null
}

class FakeAuthRepository : AuthRepository {
    private val _sessionState = MutableStateFlow<SessionState>(SessionState.Guest)
    override val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    fun setSessionState(state: SessionState) {
        _sessionState.value = state
    }

    override fun getAuthUrl(redirectUri: String): String = ""
    override suspend fun loginWithCode(code: String, redirectUri: String): Boolean = true
    override suspend fun refreshSession(): Boolean = true
    override fun logout() {}
    override fun getCustomName(userId: String): String? = null
    override fun getCustomBio(userId: String): String? = null
    override fun getCustomProfilePic(userId: String): String? = null
    override fun getCustomCoverPhoto(userId: String): String? = null
    override fun getCustomLocation(userId: String): String? = null
    override fun getCustomXHandle(userId: String): String? = null
    override fun getCustomInstagramHandle(userId: String): String? = null
    override fun getCustomTikTokHandle(userId: String): String? = null
    override fun getCustomWebsite(userId: String): String? = null
    override fun getCustomFanClubFlair(userId: String): String? = null
    override fun updateUserProfile(userId: String, name: String, bio: String?, profilePicUrl: String?, coverPhotoUrl: String?, location: String?, xHandle: String?, instagramHandle: String?, tiktokHandle: String?, website: String?, fanClubFlair: String?) {}
}

class FakeFeedRepository : FeedRepository {
    override suspend fun getUserFeed(
        userId: String,
        limit: Int,
        offset: Int,
        filter: String,
        tracksOnly: Boolean?
    ): List<PlaylistItem> = emptyList()
}

class FakeSettingsRepository : SettingsRepository {
    private var info = SettingsInfo(
        id = 1L,
        hasNavigationDonationOn = true,
        isDarkThemeOn = true,
        palletColor = "Pink",
        customPrimary = null,
        customSecondary = null,
        customBackground = null,
        customSurface = null,
        isFirstLaunch = true,
        isOngoingStreamEnabled = false,
        isEqEnabled = false,
        eqPreset = "Flat",
        eqBands = ""
    )
    override fun saveSettingsInfo(
        hasNavigationDonationOn: Boolean,
        isDarkThemeOn: Boolean,
        palletColor: String,
        customPrimary: String?,
        customSecondary: String?,
        customBackground: String?,
        customSurface: String?,
        isFirstLaunch: Boolean,
        isOngoingStreamEnabled: Boolean,
        isEqEnabled: Boolean,
        eqPreset: String,
        eqBands: String
    ) {
        info = SettingsInfo(
            id = 1L,
            hasNavigationDonationOn = hasNavigationDonationOn,
            isDarkThemeOn = isDarkThemeOn,
            palletColor = palletColor,
            customPrimary = customPrimary,
            customSecondary = customSecondary,
            customBackground = customBackground,
            customSurface = customSurface,
            isFirstLaunch = isFirstLaunch,
            isOngoingStreamEnabled = isOngoingStreamEnabled,
            isEqEnabled = isEqEnabled,
            eqPreset = eqPreset,
            eqBands = eqBands
        )
    }
    override var isCrossfadeEnabled: Boolean = false
    override var crossfadeDurationSeconds: Double = 5.0
    override var crossfadeStyle: String = "Radio Segue"
    override var isVolumeNormalizationEnabled: Boolean = false
    override fun getSettingsInfo(): SettingsInfo = info
}
