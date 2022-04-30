package com.example.justlisten.viewmodel.screens

import com.example.justlisten.ScreenIdentifier
import com.example.justlisten.ScreenState
import com.example.justlisten.StateManager

class ScreenInitSettings (
    val title : String,
    val initState : (ScreenIdentifier) -> ScreenState,
    val callOnInit : suspend (StateManager) -> Unit,
    val reinitOnEachNavigation : Boolean = false,
    /* use cases for reinitOnEachNavigation = true:
        By default, if the screen is already in the backstack, it doesn't get reinitialized if it becomes active again.
        However if you want to refresh it each time it becomes active, you might want to reinitialize it again.
        In order to achieve this behaviour, just set the flag "reinitOnEachNavigation" to true for such screen. */
    val callOnInitAlsoAfterBackground : Boolean = false,
    /* use cases for callOnInitAlsoAfterBackground = true:
        By default, the "callOnInit" function is not called again when the app comes back from the background.
        However in use cases such as "polling", you might want to call "callOnInit" again.
        In order to achieve this behaviour, you can set the flag "callOnInitAlsoAfterBackground" to true. */
)