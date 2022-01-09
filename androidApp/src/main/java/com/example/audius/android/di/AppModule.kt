package com.example.audius.android.di

import android.content.Context
import coil.ImageLoader
import com.example.audius.android.exoplayer.MusicServiceConnection
import com.example.audius.android.exoplayer.MusicSource
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
    fun provideMusicServiceConnection(
        @ApplicationContext context: Context,
        musicSource: MusicSource
    ) = MusicServiceConnection(musicSource, context)

    @Singleton
    @Provides
    fun provideMusicSource(
    ) = MusicSource()

    @Singleton
    @Provides
    fun providesImageLoader(
        @ApplicationContext context: Context
    ) = ImageLoader.Builder(context)
        .availableMemoryPercentage(0.25)
        .crossfade(true)
        .build()

}