package com.example.justlisten.viewmodel

import com.example.justlisten.AppState
import com.example.justlisten.Navigation
import com.example.justlisten.StateManager
import com.example.justlisten.datalayer.Repository
import kotlinx.coroutines.flow.StateFlow

class JustListenViewModel (repo: Repository) {

    companion object Factory {
        // factory methods are defined in the platform-specific shared code (androidMain and iosMain)
    }

    val state = StateManager(repo)
    val repository = repo

    val stateFlow: StateFlow<AppState>
        get() = stateManager.mutableStateFlow

    private val stateManager by lazy { StateManager(repo) }
    val navigation by lazy { com.example.justlisten.Navigation(stateManager) }

}