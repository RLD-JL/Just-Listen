package com.rld.justlisten.viewmodel

import com.rld.justlisten.AppState
import com.rld.justlisten.Navigation
import com.rld.justlisten.StateManager
import com.rld.justlisten.datalayer.Repository
import kotlinx.coroutines.flow.StateFlow

class JustListenViewModel(repo: Repository) {

    companion object Factory {
        // factory methods are defined in the platform-specific shared code (androidMain and iosMain)
    }

    val state = StateManager(repo)
    val repository = repo

    private val stateManager by lazy { StateManager(repo) }

    val stateFlow: StateFlow<AppState>
        get() = stateManager.mutableStateFlow

    val navigation by lazy { Navigation(stateManager) }

}