package com.rld.justlisten.viewmodel.library

import com.rld.justlisten.database.addplaylistscreen.AddPlaylist
import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.datalayer.repositories.FavoritesRepository
import com.rld.justlisten.datalayer.repositories.LibraryRepository
import com.rld.justlisten.navigation.Route
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val fakeFavoritesRepo = FakeFavoritesRepository()
    private val fakeLibraryRepo = FakeLibraryRepository()

    private lateinit var viewModel: LibraryViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = LibraryViewModel(fakeLibraryRepo, fakeFavoritesRepo)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testPlaylistCreatedClicked_WhenPlaylistDoesNotExist() = runTest(testDispatcher) {
        val collectJob = launch { viewModel.libraryState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        // 1. Trigger clicked for a non-existent playlist
        val playlistName = "New Party Mix"
        val playlistDesc = "Party music"
        val songs = listOf("song1", "song2")

        viewModel.onPlaylistCreatedClicked(playlistName, playlistDesc, songs)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify it was saved to repository
        assertTrue(fakeLibraryRepo.savePlaylistCalledWith.contains(playlistName))
        val playlists = fakeLibraryRepo.getAddPlaylist()
        assertTrue(playlists.any { it.playlistName == playlistName })

        collectJob.cancel()
    }

    @Test
    fun testPlaylistCreatedClicked_WhenPlaylistAlreadyExists() = runTest(testDispatcher) {
        val collectJob = launch { viewModel.libraryState.collect {} }
        testDispatcher.scheduler.advanceUntilIdle()

        // 1. Pre-populate the playlist in the repo
        val playlistName = "Existing Chill Mix"
        val playlistDesc = "Relaxing tunes"
        val songs = listOf("chill1", "chill2")
        
        fakeLibraryRepo.savePlaylist(playlistName, playlistDesc)
        // Simulate adding songs to it
        fakeLibraryRepo.updatePlaylistSongs(playlistName, playlistDesc, songs)
        
        // Refresh the view model data
        testDispatcher.scheduler.advanceUntilIdle()
        
        // Reset the call tracker
        fakeLibraryRepo.savePlaylistCalledWith.clear()

        // 2. Trigger onPlaylistCreatedClicked for the existing playlist
        viewModel.onPlaylistCreatedClicked(playlistName, playlistDesc, songs)
        testDispatcher.scheduler.advanceUntilIdle()

        // Verify that savePlaylist was NOT called (to prevent overwriting/wiping)
        assertFalse(fakeLibraryRepo.savePlaylistCalledWith.contains(playlistName))

        collectJob.cancel()
    }

    // ── FAKES IMPLEMENTATION ───────────────────────────────────────────

    class FakeFavoritesRepository : FavoritesRepository {
        override fun saveSongToFavorites(id: String, title: String, user: UserModel, songImgList: SongIconList, playlistName: String, isFavorite: Boolean) {}
        override fun getFavoritePlaylist(): List<PlayListModel> = emptyList()
        override fun getFavoritePlaylistWithId(id: String): String? = null
        override fun getFavoritePlaylistFlow(): Flow<List<PlayListModel>> = flowOf(emptyList())
    }

    class FakeLibraryRepository : LibraryRepository {
        private val playlists = mutableListOf<AddPlaylist>()
        private val playlistsFlow = MutableStateFlow<List<AddPlaylist>>(emptyList())
        val savePlaylistCalledWith = mutableListOf<String>()

        private fun updateFlow() {
            playlistsFlow.value = playlists.toList()
        }

        override fun savePlaylist(playlistName: String, playlistDescription: String?) {
            savePlaylistCalledWith.add(playlistName)
            if (playlists.none { it.playlistName == playlistName }) {
                playlists.add(AddPlaylist(playlistName, playlistDescription ?: "", songsList = emptyList()))
                updateFlow()
            }
        }

        override fun getAddPlaylist(): List<AddPlaylist> = playlists

        override fun getAddPlaylistFlow(): Flow<List<AddPlaylist>> = playlistsFlow

        override fun updatePlaylistSongs(playlistName: String, playlistDescription: String?, songList: List<String>) {
            val index = playlists.indexOfFirst { it.playlistName == playlistName }
            if (index >= 0) {
                playlists[index] = AddPlaylist(playlistName, playlistDescription ?: "", songsList = songList)
            }
            updateFlow()
        }

        override fun deletePlaylist(playlistName: String) {
            playlists.removeAll { it.playlistName == playlistName }
            updateFlow()
        }

        override fun updatePlaylistName(oldName: String, newName: String) {
            val index = playlists.indexOfFirst { it.playlistName == oldName }
            if (index >= 0) {
                playlists[index] = playlists[index].copy(playlistName = newName)
            }
            updateFlow()
        }

        override fun saveSongToRecent(id: String, title: String, user: UserModel, songImgList: SongIconList, playlistName: String) {}
        override fun saveSongToMostPlayed(id: String, title: String, user: UserModel, songImgList: SongIconList, playlistName: String) {}
        override fun getMostPlayedSongs(numberOfLines: Long): List<PlayListModel> = emptyList()
        override fun getRecentSongs(numberOfLines: Long): List<PlayListModel> = emptyList()
        override fun getTimeCapsuleSongs(limit: Long): List<PlayListModel> = emptyList()

        override fun insertPlayLog(songId: String, timestamp: Long, durationPlayedSec: Long, completed: Boolean) {}
        override fun getTotalPlays(): Long = 0L
        override fun getUniquePlays(): Long = 0L
        override fun getTotalDurationPlayed(): Long = 0L
        override fun getDurationPlayedForSong(songId: String): Long = 0L
        override fun getDurationPlayedForArtist(user: UserModel): Long = 0L
        override fun getMostPlayedSongsFromHistory(limit: Long, offset: Long): List<PlayListModel> = emptyList()
        override fun getTopArtistFromHistory(): Triple<UserModel, Long, Long>? = null
        override fun getPlayHistoryFlow(): Flow<Unit> = flowOf(Unit)
    }
}
