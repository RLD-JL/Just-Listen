package com.rld.justlisten.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.rld.justlisten.LocalDb
import com.rld.justlisten.media.AndroidMusicPlayer
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.media.exoplayer.MusicServiceConnection
import com.rld.justlisten.media.exoplayer.MusicSource
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.RenderersFactory
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.audio.MediaCodecAudioRenderer
import com.google.android.exoplayer2.database.StandaloneDatabaseProvider
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import java.io.File

fun androidModule() = module {
    // Provide SqlDriver for Android
    single<SqlDriver> {
        AndroidSqliteDriver(LocalDb.Schema, androidContext(), "Local.db")
    }
    
    // Provide MusicSource
    single { MusicSource() }
    
    // Provide ExoPlayer dependencies
    single {
        AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
    }

    single {
        RenderersFactory { handler, _, audioListener, _, _ ->
            arrayOf(
                MediaCodecAudioRenderer(
                    androidContext(), MediaCodecSelector.DEFAULT, handler, audioListener
                )
            )
        }
    }

    single {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        val evictor = LeastRecentlyUsedCacheEvictor((50 * 1024 * 1024).toLong())
        val databaseProvider = StandaloneDatabaseProvider(androidContext())
        val simpleCache = SimpleCache(File(androidContext().cacheDir, "media"), evictor, databaseProvider)
        CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
    }

    single {
        ExoPlayer.Builder(androidContext(), get<RenderersFactory>())
            .setMediaSourceFactory(DefaultMediaSourceFactory(get<CacheDataSource.Factory>()))
            .build().apply {
                setAudioAttributes(get(), true)
                setHandleAudioBecomingNoisy(true)
            }
    }

    // Provide MusicServiceConnection
    single {
        MusicServiceConnection(get(), androidContext())
    }
    
    // Provide MusicPlayer implementation
    single<MusicPlayer> { 
        AndroidMusicPlayer(get(), get())
    }
}
