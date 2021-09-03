package com.example.audius.android.exoplayer

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import com.example.audius.Navigation
import com.example.audius.StateManager
import com.example.audius.android.exoplayer.State.*
import com.example.audius.datalayer.datacalls.getTrendingList
import com.example.audius.datalayer.datacalls.getTrendingListData
import com.example.audius.datalayer.webservices.ApiClient
import com.example.audius.viewmodel.screens.ScreenInitSettings
import com.example.audius.viewmodel.screens.trending.TrendingListParams
import com.example.audius.viewmodel.screens.trending.TrendingListState
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


class MusicSource @Inject constructor(
    stateManager: StateManager
) {

    private val navigation: Navigation = Navigation(stateManager = stateManager)

    private val onReadyListener = mutableListOf<(Boolean) -> Unit> ()

    var songs = emptyList<MediaMetadataCompat>()

    suspend fun fetchMediaData() = withContext(Dispatchers.Main){
       state = STATE_INITIALIZING

     val allSongs = navigation.dataRepository.getTrendingListData()
        songs = allSongs.map { song ->
            Builder()
                .putString(METADATA_KEY_ARTIST, song.title)
                .putString(METADATA_KEY_MEDIA_ID, song.id)
                .putString(METADATA_KEY_TITLE, song.title)
                .putString(METADATA_KEY_DISPLAY_TITLE, song.title)
                .putString(METADATA_KEY_DISPLAY_ICON_URI, song.songIconList.songImageURL1000px)
                .putString(METADATA_KEY_MEDIA_URI, setSongUrl(song.id))
                .putString(METADATA_KEY_ALBUM_ART_URI, song.songIconList.songImageURL480px)
                .putString(METADATA_KEY_DISPLAY_SUBTITLE, song.title)
                .putString(METADATA_KEY_DISPLAY_DESCRIPTION, song.title).build()
        }
    state = STATE_INITIALIZED
    }

    fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        songs.forEach {song ->
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(song.getString(METADATA_KEY_MEDIA_URI)))
            concatenatingMediaSource.addMediaSource(mediaSource)
        }
        return concatenatingMediaSource
    }

    fun asMediaItems() = songs.map { song ->
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(song.description.title)
            .setSubtitle(song.description.subtitle)
            .setMediaId(song.description.mediaId)
            .setIconUri(song.description.iconUri)
            .build()
        MediaBrowserCompat.MediaItem(desc, FLAG_PLAYABLE)
    }.toMutableList()

    private var state: State = STATE_CREATED
        set(value) {
            if (value == STATE_INITIALIZED || value == STATE_ERROR) {
                synchronized(onReadyListener) {
                    field = value
                    onReadyListener.forEach{listener->
                        listener(state == STATE_INITIALIZED)
                    }
                }
            } else {
                field = value
            }
        }

    fun whenReady(action: (Boolean) -> Unit): Boolean {
        return if (state == STATE_CREATED || state == STATE_INITIALIZING) {
            onReadyListener +=action
            false
        } else {
            action(state == STATE_INITIALIZED)
            true
        }
    }

    private fun setSongUrl(songId: String): String {
       return "https://discoveryprovider.audius2.prod-us-west-2.staked.cloud/v1/tracks/${songId}/stream?app_name=EXAMPLEAPP"
    }
}


enum class State {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}