package com.rld.justlisten.media.exoplayer

import androidx.media3.common.MediaItem
import com.rld.justlisten.media.exoplayer.State.*
import com.rld.justlisten.media.toMediaItem
import com.rld.justlisten.viewmodel.interfaces.Item

class MusicSource {

    private val onReadyListener = mutableListOf<(Boolean) -> Unit>()

    var songs: List<MediaItem> = emptyList()

    var playlist: List<Item> = emptyList()

    fun fetchMediaData() {
        state = STATE_INITIALIZING

        songs = updateCatalog(playlist)

        state = STATE_INITIALIZED
    }

    private fun updateCatalog(playlist: List<Item>): List<MediaItem> {
        return playlist.map { song ->
            song.toMediaItem()
        }
    }

    private var state: State = STATE_CREATED
        set(value) {
            if (value == STATE_INITIALIZED || value == STATE_ERROR) {
                synchronized(onReadyListener) {
                    field = value
                    onReadyListener.forEach { listener ->
                        listener(state == STATE_INITIALIZED)
                    }
                    onReadyListener.clear()
                }
            } else {
                field = value
            }
        }

    fun whenReady(performAction: (Boolean) -> Unit): Boolean {
        synchronized(onReadyListener) {
            return if (state == STATE_CREATED || state == STATE_INITIALIZING) {
                onReadyListener += performAction
                false
            } else {
                performAction(state != STATE_ERROR)
                true
            }
        }
    }
}

enum class State {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}
