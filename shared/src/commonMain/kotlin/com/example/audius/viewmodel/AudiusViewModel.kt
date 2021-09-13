package com.example.audius.viewmodel

import com.example.audius.AppState
import com.example.audius.Navigation
import com.example.audius.StateManager
import com.example.audius.datalayer.Repository
import kotlinx.coroutines.flow.StateFlow

class AudiusViewModel (repo: Repository) {

    companion object Factory {
        // factory methods are defined in the platform-specific shared code (androidMain and iosMain)
    }

    val state = StateManager(repo)
    val repository = repo

    val stateFlow: StateFlow<AppState>
        get() = stateManager.mutableStateFlow

    private val stateManager by lazy { StateManager(repo) }
    val navigation by lazy { Navigation(stateManager) }

}