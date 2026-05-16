package com.rld.justlisten.ui

import androidx.compose.runtime.staticCompositionLocalOf
import com.rld.justlisten.media.MusicPlayer

val LocalMusicPlayer = staticCompositionLocalOf<MusicPlayer> {
    error("MusicPlayer not provided. Inject via MainActivity / platform entry point.")
}
