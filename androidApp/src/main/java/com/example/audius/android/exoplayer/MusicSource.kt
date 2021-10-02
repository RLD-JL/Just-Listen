package com.example.audius.android.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.example.audius.android.exoplayer.State.*
import com.example.audius.datalayer.utils.Constants.BASEURL
import com.example.audius.viewmodel.screens.playlist.*
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory

class MusicSource {

    private val onReadyListener = mutableListOf<(Boolean) -> Unit>()

    var songs = emptyList<MediaMetadataCompat>()

    var playlist: List<PlaylistItem> = emptyList()

    fun fetchMediaData() {
        state = STATE_INITIALIZING

        songs = playlist.map { song ->
            Builder()
                .putString(METADATA_KEY_ARTIST, song.title)
                .putString(METADATA_KEY_MEDIA_ID, song.id)
                .putString(METADATA_KEY_TITLE, song.title)
                .putString(METADATA_KEY_DISPLAY_TITLE, song.title)
                .putString(METADATA_KEY_DISPLAY_ICON_URI, song.songIconList.songImageURL1000px)
                .putString(METADATA_KEY_MEDIA_URI, setSongUrl(song.id))
                .putString(METADATA_KEY_ALBUM_ART_URI, song.songIconList.songImageURL480px)
                .putString(METADATA_KEY_DISPLAY_SUBTITLE, song.title)
                .putString(METADATA_KEY_DISPLAY_DESCRIPTION, song.title)
                .putString(METADATA_KEY_ALBUM, "playlistName").build()
        }
        state = STATE_INITIALIZED
    }

    fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        songs.forEach { song ->
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
                    onReadyListener.forEach { listener ->
                        listener(state == STATE_INITIALIZED)
                    }
                }
            } else {
                field = value
            }
        }

    fun whenReady(performAction: (Boolean) -> Unit): Boolean {
        return if (state == STATE_CREATED || state == STATE_INITIALIZING) {
            onReadyListener += performAction
            false
        } else {
            performAction(state != STATE_ERROR)
            true
        }
    }

    private fun setSongUrl(songId: String): String {
        return "${BASEURL}/v1/tracks/${songId}/stream?app_name=EXAMPLEAPP"
    }
}

enum class State {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}