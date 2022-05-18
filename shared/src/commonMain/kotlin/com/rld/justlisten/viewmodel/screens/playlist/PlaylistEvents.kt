package com.rld.justlisten.viewmodel.screens.playlist

import com.rld.justlisten.datalayer.datacalls.playlist.getPlaylist
import com.rld.justlisten.datalayer.datacalls.playlist.getTracks
import com.rld.justlisten.datalayer.utils.Constants.list
import com.rld.justlisten.viewmodel.Events
import com.rld.justlisten.viewmodel.screens.playlist.PlayListEnum.*
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlin.random.Random

fun Events.fetchPlaylist(index: Int, playlistEnum: PlayListEnum, queryPlaylist: String = "Rock") =
    screenCoroutine {
        stateManager.updateScreen(PlaylistState::class) {
            when (playlistEnum) {
                TOP_PLAYLIST -> {
                    val playlist = dataRepository.getPlaylist(
                        index,
                        playlistEnum
                    )
                    if (playlist.size == it.playlistItems.size) {
                        it.copy(lastFetchPlaylist = true)
                    } else {
                        it.copy(
                            playlistItems = playlist
                        )
                    }
                }
                REMIX -> {
                    val remixPlaylist = dataRepository.getPlaylist(
                        index,
                        playlistEnum,
                        queryPlaylist = queryPlaylist
                    )
                    if (remixPlaylist.size == it.remixPlaylist.size) {
                        it.copy(lastFetchRemix = true)
                    } else {
                        it.copy(remixPlaylist = remixPlaylist)
                    }
                }
                HOT -> {
                    val hotPlaylist = dataRepository.getPlaylist(
                        index,
                        playlistEnum,
                        queryPlaylist = queryPlaylist
                    )
                    if (hotPlaylist.size == it.hotPlaylist.size) {
                        it.copy(lastFetchHot = true)
                    } else {
                        it.copy(hotPlaylist = hotPlaylist)
                    }
                }
                CURRENT_PLAYLIST -> TODO()
                CREATED_BY_USER -> TODO()
                FAVORITE -> it.copy(currentPlaylist = dataRepository.getPlaylist(0, playlistEnum))
            }
        }
    }

fun Events.playMusicFromPlaylist(playlistId: String) {
    currentPlaylistId = playlistId
}

fun Events.refreshScreen() = screenCoroutine {
    stateManager.updateScreen(PlaylistState::class) {
        it.copy(isLoading = true)
    }
    stateManager.updateScreen(PlaylistState::class) {
        val playlist: Deferred<List<PlaylistItem>>
        val remix: Deferred<List<PlaylistItem>>
        val hot: Deferred<List<PlaylistItem>>
        var queryIndex = Random.nextInt(0, list.size)
        val queryIndex2 = Random.nextInt(0, list.size)
        if (queryIndex == queryIndex2) {
            if (queryIndex > 0)
                queryIndex -= 1
            else queryIndex += 1
        }
        coroutineScope {
            playlist = async { dataRepository.getPlaylist(index = 20, TOP_PLAYLIST) }
            remix = async {
                dataRepository.getPlaylist(
                    index = 20,
                    REMIX,
                    queryPlaylist = list[queryIndex]
                )
            }
            hot = async {
                dataRepository.getPlaylist(
                    index = 20,
                    HOT,
                    queryPlaylist = list[queryIndex2]
                )
            }
        }
        it.copy(
            remixPlaylist = remix.await().shuffled(),
            isLoading = false,
            queryIndex = queryIndex,
            queryIndex2 = queryIndex2,
            playlistItems = playlist.await().shuffled(),
            hotPlaylist = hot.await().shuffled()
        )
    }
}

fun Events.getNewTracks(category: TracksCategory, timeRange: TimeRange) = screenCoroutine {
    stateManager.updateScreen(PlaylistState::class) {
        it.copy(tracksLoading = true)
    }
    stateManager.updateScreen(PlaylistState::class) {
        val time = when(timeRange) {
            TimeRange.ALLTIME -> "allTime"
            TimeRange.MONTH -> TimeRange.MONTH.value.lowercase()
            TimeRange.WEEK -> TimeRange.WEEK.value.lowercase()
        }
        val newTracks = dataRepository.getTracks(16, category.value, time)
        it.copy(tracksList = newTracks, tracksLoading = false)
    }
}

