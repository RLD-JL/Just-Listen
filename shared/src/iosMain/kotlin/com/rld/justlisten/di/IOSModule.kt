package com.rld.justlisten.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.rld.justlisten.LocalDb
import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.media.IOSMusicPlayer
import com.rld.justlisten.media.MusicPlayer
import com.rld.justlisten.ui.utils.SleepTimerService
import com.rld.justlisten.ui.utils.IosSleepTimerService
import org.koin.dsl.module
import org.koin.core.context.startKoin

/**
 * iOS-specific Koin module for providing iOS-only dependencies
 */
fun iosModule() = module {
    single<SqlDriver> {
        NativeSqliteDriver(LocalDb.Schema, "Local.db")
    }
    
    single {
        LocalDb(
            get(),
            Repository.addPlaylistAdapter,
            Repository.libraryAdapter,
            Repository.playlistDetailAdapter
        )
    }
    
    single<SleepTimerService> {
        IosSleepTimerService()
    }
    
    // Provide MusicPlayer implementation for iOS
    single<MusicPlayer> {
        IOSMusicPlayer(get())
    }
}

// Swift bridge helper to start Koin on iOS
fun initKoin(apiKey: String = "") {
    startKoin {
        modules(
            iosModule(),
            appModule(),
            module { single { ApiClient(apiKey) } }
        )
    }
}
