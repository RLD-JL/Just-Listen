package com.rld.justlisten.di

import com.rld.justlisten.viewmodel.addplaylist.AddPlaylistViewModel
import com.rld.justlisten.viewmodel.library.LibraryViewModel
import com.rld.justlisten.viewmodel.playlist.PlaylistViewModel
import com.rld.justlisten.viewmodel.playlistdetail.PlaylistDetailViewModel
import com.rld.justlisten.viewmodel.search.SearchViewModel
import com.rld.justlisten.viewmodel.settings.SettingsViewModel
import com.rld.justlisten.viewmodel.player.PlayerViewModel
import com.rld.justlisten.viewmodel.seeall.SeeAllViewModel
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel

import com.rld.justlisten.media.PlayHistoryTracker
import com.rld.justlisten.datalayer.repositories.*

fun appModule() = module {
    // Single instances
    single<FavoritesRepository> { FavoritesRepositoryImpl(get(), get(), get()) }
    single<LibraryRepository> { LibraryRepositoryImpl(get()) }
    single<PlaylistRepository> { PlaylistRepositoryImpl(get(), get()) }
    single<SearchRepository> { SearchRepositoryImpl(get(), get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single<AuthRepository> { AuthRepositoryImpl(apiClient = get(), secureStorage = get(), pkceCrypto = get(), syncRepository = get()) }
    single<SyncRepository> { SyncRepositoryImpl(localDb = get(), apiClient = get()) }
    single { PlayHistoryTracker(get(), get()) }
    
    // ViewModels
    viewModel { LibraryViewModel(get(), get(), get(), get()) }
    viewModel { PlaylistViewModel(get(), get()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { PlaylistDetailViewModel(get(), get(), get()) }
    viewModel { AddPlaylistViewModel(get()) }
    viewModel { SeeAllViewModel(get(), get()) }
    single { SettingsViewModel(get(), get(), get(), get()) }
    viewModel { PlayerViewModel(get(), get(), get(), get()) }
}
