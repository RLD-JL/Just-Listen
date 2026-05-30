package com.rld.justlisten.di

import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.datalayer.webservices.ApiClient
import com.rld.justlisten.datalayer.webservices.DiscoveryNodeService
import com.rld.justlisten.viewmodel.addplaylist.AddPlaylistViewModel
import com.rld.justlisten.viewmodel.donation.DonationViewModel
import com.rld.justlisten.viewmodel.library.LibraryViewModel
import com.rld.justlisten.viewmodel.playlist.PlaylistViewModel
import com.rld.justlisten.viewmodel.playlistdetail.PlaylistDetailViewModel
import com.rld.justlisten.viewmodel.search.SearchViewModel
import com.rld.justlisten.viewmodel.settings.SettingsViewModel
import org.koin.dsl.module

fun appModule() = module {
    // Single instances
    single { DiscoveryNodeService() }
    single { ApiClient(get()) }
    single { Repository(get(), get()) }
    
    // ViewModels
    factory { LibraryViewModel(get()) }
    factory { PlaylistViewModel(get()) }
    factory { SearchViewModel(get()) }
    factory { PlaylistDetailViewModel(get()) }
    factory { AddPlaylistViewModel(get()) }
    single { SettingsViewModel(get()) }
    factory { DonationViewModel() }
}
