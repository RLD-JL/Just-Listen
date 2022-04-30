package com.rld.justlisten.android.exoplayer

import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.rld.justlisten.android.exoplayer.State.*
import com.rld.justlisten.android.exoplayer.library.extension.*
import com.rld.justlisten.datalayer.utils.Constants.BASEURL
import com.rld.justlisten.viewmodel.interfaces.Item

class MusicSource {

    private val onReadyListener = mutableListOf<(Boolean) -> Unit>()

    var songs: List<MediaMetadataCompat> = emptyList()

    var playlist: List<Item> = emptyList()

    fun fetchMediaData() {
        state = STATE_INITIALIZING

        songs = updateCatalog(playlist)

        state = STATE_INITIALIZED
    }

    private fun updateCatalog(playlist: List<Item>): List<MediaMetadataCompat> {
        val mediaMetadataCompat = playlist.map { song ->
            MediaMetadataCompat.Builder().from(song).build()
        }.toList()
        mediaMetadataCompat.forEach {
            it.description.extras?.putAll(it.bundle)
        }
        return mediaMetadataCompat
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
        isFavorite = song.isFavorite.toString()
        duration = 120

        downloadStatus = MediaDescriptionCompat.STATUS_NOT_DOWNLOADED

        return this
    }

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