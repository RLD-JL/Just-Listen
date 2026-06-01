package com.rld.justlisten.viewmodel.player

import com.rld.justlisten.database.addplaylistscreen.AddPlaylist
import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.datalayer.repositories.FavoritesRepository
import com.rld.justlisten.datalayer.repositories.LibraryRepository
import com.rld.justlisten.media.MediaMetadata
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.media.PlaybackState
import com.rld.justlisten.media.PlaybackStatus
import com.rld.justlisten.media.RepeatMode
import com.rld.justlisten.ui.actions.PlayerAction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
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

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeFavoritesRepo = FakeFavoritesRepository()
    private val fakeLibraryRepo = FakeLibraryRepository()
    private val fakeMusicPlayer = FakeMusicPlayer()

    private lateinit var viewModel: PlayerViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = PlayerViewModel(fakeFavoritesRepo, fakeLibraryRepo, fakeMusicPlayer)
    }

    @AfterTest
    fun tearDown() {
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
            songsList = emptyList()
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
}

// ── FAKES IMPLEMENTATION ───────────────────────────────────────────

class FakeFavoritesRepository : FavoritesRepository {
    private val favoritesMap = mutableMapOf<String, PlayListModel>()

    fun hasFavorite(id: String): Boolean = favoritesMap.containsKey(id)

    override fun saveSongToFavorites(
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

    override fun getFavoritePlaylist(): List<PlayListModel> = favoritesMap.values.toList()

    override fun getFavoritePlaylistWithId(id: String): String? = favoritesMap[id]?.id

    override fun getFavoritePlaylistFlow(): Flow<List<PlayListModel>> = flowOf(favoritesMap.values.toList())
}

class FakeLibraryRepository : LibraryRepository {
    private val playlists = mutableListOf<AddPlaylist>()

    fun addPlaylist(playlist: AddPlaylist) {
        playlists.add(playlist)
    }

    override fun savePlaylist(playlistName: String, playlistDescription: String?) {
        playlists.add(AddPlaylist(playlistName, playlistDescription ?: "", songsList = emptyList()))
    }

    override fun getAddPlaylist(): List<AddPlaylist> = playlists

    override fun updatePlaylistSongs(playlistName: String, playlistDescription: String?, songList: List<String>) {
        val index = playlists.indexOfFirst { it.playlistName == playlistName }
        if (index >= 0) {
            playlists[index] = AddPlaylist(playlistName, playlistDescription ?: "", songsList = songList)
        }
    }

    override fun deletePlaylist(playlistName: String) {
        playlists.removeAll { it.playlistName == playlistName }
    }

    // Unused by PlayerViewModel in tests
    override fun saveSongToRecent(id: String, title: String, user: UserModel, songImgList: SongIconList, playlistName: String) {}
    override fun saveSongToMostPlayed(id: String, title: String, user: UserModel, songImgList: SongIconList, playlistName: String) {}
    override fun getMostPlayedSongs(numberOfLines: Long): List<PlayListModel> = emptyList()
    override fun getRecentSongs(numberOfLines: Long): List<PlayListModel> = emptyList()
}

class FakeMusicPlayer : MusicPlayer {
    override var currentlyPlayingPlaylistId: String? = null
    var skipToNextCalled = false
    var refreshMetadataCalled = false

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
    override fun stop() {}
    override fun seekTo(position: Long) {}
    override fun setShuffleModeEnabled(enabled: Boolean) {}
    override fun setRepeatMode(repeatMode: RepeatMode) {}
    override fun playMedia(mediaId: String) {}
    override fun updatePlaylist(list: List<com.rld.justlisten.viewmodel.interfaces.Item>) {}
    override fun removeTrack(index: Int) {}
    override fun moveTrack(fromIndex: Int, toIndex: Int) {}

    override fun skipToNext() {
        skipToNextCalled = true
    }

    override fun skipToPrevious() {}

    override fun refreshMetadata() {
        refreshMetadataCalled = true
    }
}
