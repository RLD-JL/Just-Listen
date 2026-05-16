package com.rld.justlisten.di

import com.rld.justlisten.media.IOSMusicPlayer
import com.rld.justlisten.media.MusicPlayer
import org.koin.dsl.module

/**
 * iOS-specific Koin module for providing iOS-only dependencies
 */
fun iosModule() = module {
    // Provide MusicPlayer implementation for iOS
    single<MusicPlayer> {
        IOSMusicPlayer()
    }
}

