package com.example.justlisten.viewmodel

import com.example.justlisten.ScreenIdentifier
import com.example.justlisten.ScreenState
import com.example.justlisten.StateManager

class StateProvider(val stateManager: StateManager) {

    inline fun <reified T: ScreenState> get(screenIdentifier: ScreenIdentifier) : T {
        return stateManager.screenStatesMap[screenIdentifier.URI] as T
    }

    // reified functions cannot be exported to iOS, so we use this function returning the "ScreenState" interface type
    // on Swift, we then need to cast it to the specific state class
    fun getToCast(screenIdentifier: ScreenIdentifier) : ScreenState? {
        return stateManager.screenStatesMap[screenIdentifier.URI]
    }
}