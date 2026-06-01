package com.rld.justlisten.di

import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.viewmodel.addplaylist.AddPlaylistViewModel
import com.rld.justlisten.viewmodel.donation.DonationViewModel
import com.rld.justlisten.viewmodel.library.LibraryViewModel
import com.rld.justlisten.viewmodel.playlist.PlaylistViewModel
import com.rld.justlisten.viewmodel.playlistdetail.PlaylistDetailViewModel
import com.rld.justlisten.viewmodel.search.SearchViewModel
import com.rld.justlisten.viewmodel.settings.SettingsViewModel
import com.rld.justlisten.viewmodel.player.PlayerViewModel
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel

import com.rld.justlisten.datalayer.repositories.*

fun appModule() = module {
    // Single instances
    single { Repository(get(), get()) }
    single<FavoritesRepository> { FavoritesRepositoryImpl(get()) }
    single<LibraryRepository> { LibraryRepositoryImpl(get()) }
    single<PlaylistRepository> { PlaylistRepositoryImpl(get(), get()) }
    single<SearchRepository> { SearchRepositoryImpl(get(), get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    
    // ViewModels
    viewModel { LibraryViewModel(get(), get()) }
    viewModel { PlaylistViewModel(get(), get()) }
    viewModel { SearchViewModel(get(), get()) }
    viewModel { PlaylistDetailViewModel(get(), get(), get()) }
    viewModel { AddPlaylistViewModel(get()) }
    single { SettingsViewModel(get()) }
    viewModel { DonationViewModel() }
    viewModel { PlayerViewModel(get(), get(), get()) }
}
