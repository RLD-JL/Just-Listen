package com.rld.justlisten.media.exoplayer

import com.google.android.exoplayer2.ExoPlayer
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface MusicServiceEntryPoint {
    fun musicSource(): MusicSource
    fun exoPlayer(): ExoPlayer
}


