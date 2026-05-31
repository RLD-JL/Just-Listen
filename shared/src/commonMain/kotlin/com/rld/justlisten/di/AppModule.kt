package com.rld.justlisten.di

import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.viewmodel.addplaylist.AddPlaylistViewModel
import com.rld.justlisten.viewmodel.donation.DonationViewModel
import com.rld.justlisten.viewmodel.library.LibraryViewModel
import com.rld.justlisten.viewmodel.playlist.PlaylistViewModel
import com.rld.justlisten.viewmodel.playlistdetail.PlaylistDetailViewModel
import com.rld.justlisten.viewmodel.search.SearchViewModel
import com.rld.justlisten.viewmodel.settings.SettingsViewModel
import org.koin.dsl.module
import org.koin.core.module.dsl.viewModel

fun appModule() = module {
    // Single instances
    single { Repository(get(), get()) }
    
    // ViewModels
    viewModel { LibraryViewModel(get()) }
    viewModel { PlaylistViewModel(get()) }
    viewModel { SearchViewModel(get()) }
    viewModel { PlaylistDetailViewModel(get()) }
    viewModel { AddPlaylistViewModel(get()) }
    single { SettingsViewModel(get()) }
    viewModel { DonationViewModel() }
}
