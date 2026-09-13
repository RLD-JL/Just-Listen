package com.rld.justlisten.viewmodel.player

import androidx.lifecycle.ViewModelStore
import com.rld.justlisten.datalayer.models.PlayListModel
import com.rld.justlisten.datalayer.models.SongIconList
import com.rld.justlisten.datalayer.models.UserModel
import com.rld.justlisten.datalayer.repositories.PlaylistRepository
import com.rld.justlisten.navigation.Route
import com.rld.justlisten.viewmodel.playlistdetail.PlaylistDetailViewModel
import com.rld.justlisten.viewmodel.screens.playlist.PlayListEnum
import com.rld.justlisten.viewmodel.screens.playlist.PlaylistItem
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class CachedPlaylistLoadingTest {
    @Test
    fun cachedRowsAppearBeforeRefreshAndRefreshPreservesFavoriteToggle() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        val gate = CompletableDeferred<Unit>()
        var requests = 0
        val repository = object : PlaylistRepository by FakePlaylistRepository() {
            override suspend fun getPlaylist(index: Int, playListEnum: PlayListEnum,
                playlistId: String, songsList: List<String>, queryPlaylist: String) =
                (1..12).map { PlaylistItem(PlayListModel(id = "$it", title = "Cached"), isFavorite = true) }
            override suspend fun fetchTrackDetails(trackId: String): PlayListModel {
                requests++
                gate.await()
                return PlayListModel(id = trackId, title = "Refreshed")
            }
        }
        try {
            val vm = PlaylistDetailViewModel(repository, FakeFavoritesRepository(), FakeLibraryRepository(), FakeAuthRepository())
            store.put("test", vm)
            vm.load(Route.PlaylistDetail("favorites", "", "Favorites", "", "FAVORITE"))
            runCurrent()
            assertFalse(vm.playlistDetailState.value.isLoading)
            assertEquals(12, vm.playlistDetailState.value.songPlaylist.size)
            assertEquals(5, requests)
            vm.onFavoritePressed("1", "Cached", UserModel(), SongIconList(), false)
            gate.complete(Unit)
            advanceUntilIdle()
            val row = vm.playlistDetailState.value.songPlaylist.first()
            assertEquals("Refreshed", row.title)
            assertFalse(row.isFavorite)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }
}
