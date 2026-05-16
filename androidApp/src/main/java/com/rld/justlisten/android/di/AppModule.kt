package com.rld.justlisten.android.di

import android.content.Context
import com.google.android.exoplayer2.ExoPlayer
import com.rld.justlisten.media.AndroidMusicPlayer
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.media.exoplayer.MusicServiceConnection
import com.rld.justlisten.media.exoplayer.MusicSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideMusicSource() = MusicSource()

    @Singleton
    @Provides
    fun provideMusicServiceConnection(
        @ApplicationContext context: Context,
        musicSource: MusicSource
    ) = MusicServiceConnection(musicSource, context)

    @Singleton
    @Provides
    fun provideMusicPlayer(
        musicServiceConnection: MusicServiceConnection
    ): MusicPlayer = AndroidMusicPlayer(musicServiceConnection)

    @Singleton
    @Provides
    fun provideExoPlayer(
        @ApplicationContext context: Context
    ): ExoPlayer {
        return ExoPlayer.Builder(context).build()
    }
}
