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
import com.rld.justlisten.util.SecureStorage
import com.rld.justlisten.util.IosSecureStorage
import com.rld.justlisten.util.PkceCrypto
import com.rld.justlisten.util.IosPkceCrypto
import com.rld.justlisten.datalayer.repositories.FavoritesRepository
import com.rld.justlisten.datalayer.repositories.SettingsRepository
import com.rld.justlisten.datalayer.DatabaseSchemaHelper
import org.koin.dsl.module
import org.koin.core.context.startKoin

/**
 * iOS-specific Koin module for providing iOS-only dependencies
 */
fun iosModule() = module {
    single<SqlDriver> {
        NativeSqliteDriver(DatabaseSchemaHelper.SafeSchema, "Local.db")
    }
    
    single<SecureStorage> {
        IosSecureStorage()
    }
    
    single<PkceCrypto> {
        IosPkceCrypto()
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
    
    single<SleepTimerService> {
        IosSleepTimerService()
    }
    
    // Provide MusicPlayer implementation for iOS
    single<MusicPlayer> {
        IOSMusicPlayer(get(), get(), get())
    }
}

// Swift bridge helper to start Koin on iOS
fun initKoin(apiKey: String = "") {
    startKoin {
        modules(
            iosModule(),
            appModule(),
            module { single { ApiClient(apiKey = apiKey, secureStorage = get()) } }
        )
    }
}

fun loginWithCode(code: String, redirectUri: String) {
    try {
        val settingsViewModel = org.koin.mp.KoinPlatform.getKoin().get<com.rld.justlisten.viewmodel.settings.SettingsViewModel>()
        settingsViewModel.loginWithCode(code, redirectUri)
    } catch (e: Exception) {
        // Log or handle error
    }
}

fun handleDeepLink(url: String) {
    try {
        com.rld.justlisten.util.DeepLinkRouter.handleDeepLink(url)
    } catch (e: Exception) {
        // Log or handle error
    }
}
