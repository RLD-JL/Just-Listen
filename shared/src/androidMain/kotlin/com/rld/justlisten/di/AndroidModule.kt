package com.rld.justlisten.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.rld.justlisten.LocalDb
import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.viewmodel.addplaylist.AddPlaylistViewModel
import com.rld.justlisten.viewmodel.donation.DonationViewModel
import com.rld.justlisten.viewmodel.library.LibraryViewModel
import com.rld.justlisten.viewmodel.playlist.PlaylistViewModel
import com.rld.justlisten.viewmodel.playlistdetail.PlaylistDetailViewModel
import com.rld.justlisten.viewmodel.search.SearchViewModel
import com.rld.justlisten.viewmodel.settings.SettingsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

fun androidModule() = module {
    single<SqlDriver> {
        AndroidSqliteDriver(LocalDb.Schema, androidContext(), "Local.db")
    }

    single { Repository(get()) }

    viewModel { LibraryViewModel(get()) }
    viewModel { PlaylistViewModel(get()) }
    viewModel { SearchViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { AddPlaylistViewModel(get()) }
    viewModel { PlaylistDetailViewModel(get()) }
    viewModel { DonationViewModel() }
}
