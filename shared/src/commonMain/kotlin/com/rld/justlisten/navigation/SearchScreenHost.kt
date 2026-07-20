package com.rld.justlisten.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.navigation.NavHostController
import com.rld.justlisten.datalayer.repositories.LibraryRepository
import com.rld.justlisten.ui.LocalMusicPlayer
import com.rld.justlisten.ui.actions.SearchScreenAction
import com.rld.justlisten.ui.searchscreen.SearchScreen
import com.rld.justlisten.ui.utils.playMusicFromId
import com.rld.justlisten.viewmodel.search.SearchViewModel
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchScreenHost(navController: NavHostController) {
    val viewModel: SearchViewModel = koinViewModel()
    val musicPlayer = LocalMusicPlayer.current
    val repository: LibraryRepository = koinInject()
    val state by viewModel.searchState.collectAsState()

    CollectNavigationEvents(viewModel, navController)

    SearchScreen(
        searchScreenState = state,
        onAction = { action ->
            when (action) {
                is SearchScreenAction.BackPressed -> viewModel.popBack()
                is SearchScreenAction.SearchPressed -> viewModel.onSearchSubmitted(action.query)
                is SearchScreenAction.QueryChanged -> viewModel.onSearchQueryChanged(action.query)
                is SearchScreenAction.SeeAllClicked -> viewModel.onSeeAllClicked(action.type)
                SearchScreenAction.LoadMoreSeeAll -> viewModel.loadMoreSeeAllItems()
                is SearchScreenAction.SongPressed -> {
                    val tracksList = when {
                        state.searchResultTracks.any { it.id == action.songId } -> state.searchResultTracks
                        state.seeAllTracks.any { it.id == action.songId } -> state.seeAllTracks
                        else -> state.autocompleteTracks
                    }
                    playMusicFromId(musicPlayer, tracksList, action.songId, repository)
                }
                is SearchScreenAction.PlaylistPressed -> viewModel.onPlaylistPressed(
                    action.playlistId,
                    action.playlistIcon,
                    action.playlistTitle,
                    action.playlistCreatedBy
                )
                is SearchScreenAction.ArtistClicked -> viewModel.onArtistClicked(
                    action.artistId,
                    action.artistName
                )
            }
        }
    )
}
