package com.rld.justlisten.android.di

import android.content.Context
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.RenderersFactory
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer
import com.google.android.exoplayer2.database.DatabaseProvider
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ServiceScoped
import java.io.File


@Module
@InstallIn(ServiceComponent::class)
object ServiceModule {


    @Provides
    @ServiceScoped
    fun provideExoPlayer(
        @ApplicationContext context: Context,
        audioAttributes: AudioAttributes,
        renderersFactory: RenderersFactory,
        cacheDataSourceFactory: CacheDataSource.Factory
    ) = ExoPlayer.Builder(context, renderersFactory)
        .setMediaSourceFactory(DefaultMediaSourceFactory(cacheDataSourceFactory)).build().apply {
        setAudioAttributes(audioAttributes, true)
        setHandleAudioBecomingNoisy(true)
    }

    @Provides
    @ServiceScoped
    fun provideAudioAttributes() = AudioAttributes.Builder()
        .setContentType(C.CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    @Provides
    @ServiceScoped
    fun provideRenderFactory(@ApplicationContext context: Context) =
        RenderersFactory { handler, _, audioListener, _, _ ->
            arrayOf(
                MediaCodecAudioRenderer(
                    context, MediaCodecSelector.DEFAULT, handler, audioListener
                )
            )
        }

    @Provides
    @ServiceScoped
    fun provideCacheFactory(@ApplicationContext context: Context): CacheDataSource.Factory {

        val httpDataSourceFactory: HttpDataSource.Factory =
            DefaultHttpDataSource.Factory()

        val evictor = LeastRecentlyUsedCacheEvictor((50 * 1024 * 1024).toLong())
        val databaseProvider: DatabaseProvider = StandaloneDatabaseProvider(context)

        val simpleCache = SimpleCache(File(context.cacheDir, "media"), evictor, databaseProvider)

        return CacheDataSource.Factory().setCache(simpleCache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
    }
}