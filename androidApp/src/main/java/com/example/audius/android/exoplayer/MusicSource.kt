package com.example.audius.android.exoplayer

import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.METADATA_KEY_MEDIA_URI
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import com.example.audius.android.exoplayer.State.*
import com.example.audius.android.exoplayer.library.extension.*
import com.example.audius.datalayer.utils.Constants.BASEURL
import com.example.audius.viewmodel.interfaces.Item
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource

class MusicSource {

    private val onReadyListener = mutableListOf<(Boolean) -> Unit>()

    var songs = emptyList<MediaMetadataCompat>()

    var playlist: List<Item> = emptyList()

    fun fetchMediaData() {
        state = STATE_INITIALIZING

        songs = playlist.map { song ->
            MediaMetadataCompat.Builder().from(song).build()
        }

        state = STATE_INITIALIZED
    }

    private fun MediaMetadataCompat.Builder.from(song: Item): MediaMetadataCompat.Builder {
        artist = song.title
        id = song.id
        title = song.title
        displayIconUri = song.songIconList.songImageURL480px
        mediaUri = setSongUrl(song.id)
        albumArtUri = song.songIconList.songImageURL480px
        displaySubtitle = song.title
        displayDescription = song.title
        genre = "true"
        duration = 120

        return this
    }

    fun asMediaItems() = songs.map { song ->
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(song.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(song.description.title)
            .setSubtitle(song.description.subtitle)
            .setMediaId(song.description.mediaId)
            .setIconUri(song.description.iconUri)
            .setExtras(bundleOf(Pair("key1","true")))
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