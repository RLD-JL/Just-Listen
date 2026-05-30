package com.rld.justlisten.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.rld.justlisten.LocalDb
import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.media.AndroidMusicPlayer
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.media.exoplayer.MusicServiceConnection
import com.rld.justlisten.media.exoplayer.MusicSource
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.database.StandaloneDatabaseProvider
import java.io.File

fun androidModule() = module {
    // Provide SqlDriver for Android
    single<SqlDriver> {
        AndroidSqliteDriver(LocalDb.Schema, androidContext(), "Local.db")
    }

    // Provide LocalDb
    single {
        LocalDb(
            get(),
            Repository.addPlaylistAdapter,
            Repository.libraryAdapter,
            Repository.playlistDetailAdapter
        )
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
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        val evictor = LeastRecentlyUsedCacheEvictor((50 * 1024 * 1024).toLong())
        val databaseProvider = StandaloneDatabaseProvider(androidContext())
        val simpleCache = SimpleCache(File(androidContext().cacheDir, "media"), evictor, databaseProvider)
        CacheDataSource.Factory()
            .setCache(simpleCache)
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
    }

    single {
        ExoPlayer.Builder(androidContext())
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
