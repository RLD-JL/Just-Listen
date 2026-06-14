package com.rld.justlisten.di

import androidx.annotation.OptIn
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.rld.justlisten.LocalDb
import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.media.AndroidMusicPlayer
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.media.exoplayer.MusicServiceConnection
import com.rld.justlisten.media.exoplayer.MusicSource
import com.rld.justlisten.media.exoplayer.MusicPreloader
import com.rld.justlisten.ui.utils.SleepTimerService
import com.rld.justlisten.ui.utils.AndroidSleepTimerService
import com.rld.justlisten.util.SecureStorage
import com.rld.justlisten.util.AndroidSecureStorage
import com.rld.justlisten.util.PkceCrypto
import com.rld.justlisten.util.AndroidPkceCrypto
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import com.rld.justlisten.datalayer.DatabaseSchemaHelper
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.database.StandaloneDatabaseProvider
import java.io.File

@OptIn(UnstableApi::class)
fun androidModule(apiKey: String = "") = module {

    single<SleepTimerService> { AndroidSleepTimerService(androidContext()) }

    single<SecureStorage> { AndroidSecureStorage(androidContext()) }

    single<PkceCrypto> { AndroidPkceCrypto() }

    single { ApiClient(apiKey = apiKey, secureStorage = get()) }

    single<SqlDriver> {
        AndroidSqliteDriver(DatabaseSchemaHelper.SafeSchema, androidContext(), "Local.db")
    }
    single {
        LocalDb(
            get(),
            Repository.addPlaylistAdapter,
            Repository.libraryAdapter,
            Repository.playlistDetailAdapter,
            Repository.syncQueueAdapter
        )
    }
    single { MusicSource() }
    single {
        AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()
    }
    single {
        val evictor = LeastRecentlyUsedCacheEvictor(150 * 1024 * 1024L)
        val databaseProvider = StandaloneDatabaseProvider(androidContext())
        SimpleCache(File(androidContext().cacheDir, "media"), evictor, databaseProvider)
    }
    single {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
        CacheDataSource.Factory()
            .setCache(get<SimpleCache>())
            .setUpstreamDataSourceFactory(httpDataSourceFactory)
    }
    single { MusicPreloader(get()) }
    single { MusicServiceConnection(get(), get(), androidContext()) }
    single<MusicPlayer> { AndroidMusicPlayer(get(), get(), get()) }
}