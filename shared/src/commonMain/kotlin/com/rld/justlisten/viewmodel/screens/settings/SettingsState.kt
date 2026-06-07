package com.rld.justlisten.viewmodel.screens.settings

import com.rld.justlisten.ScreenState
import com.rld.justlisten.datalayer.repositories.SessionState
import com.rld.justlisten.datalayer.repositories.SyncState

import com.rld.justlisten.datalayer.models.PlayListModel

data class SettingsState(
    val isLoading: Boolean = false,
    val hasDonationNavigationOn: Boolean = true,
    val isDarkThemeOn: Boolean = true,
    val palletColor: String = "Pink",
    val customPrimary: String? = null,
    val customSecondary: String? = null,
    val customBackground: String? = null,
    val customSurface: String? = null,
    val isFirstLaunch: Boolean = true,
    val sessionState: SessionState = SessionState.Guest,
    val syncState: SyncState = SyncState.Synced,
    val favoriteTracks: List<PlayListModel> = emptyList(),
    val favoritePlaylists: List<PlayListModel> = emptyList(),
    val userPlaylists: List<PlayListModel> = emptyList(),
    val isLibraryLoading: Boolean = false,
    val libraryError: String? = null,
    val isOngoingStreamEnabled: Boolean = true,
) : ScreenState