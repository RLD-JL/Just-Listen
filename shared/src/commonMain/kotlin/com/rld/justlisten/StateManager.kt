package com.rld.justlisten

import com.rld.justlisten.datalayer.Repository
import com.rld.justlisten.viewmodel.JustListenViewModel
import com.rld.justlisten.viewmodel.screens.ScreenInitSettings
import com.rld.justlisten.viewmodel.screens.navigationSettings
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.reflect.KClass

interface ScreenState
interface ScreenParams

class StateManager(repo: Repository) {
    internal val mutableStateFlow = MutableStateFlow(AppState())

    val screenStatesMap: MutableMap<URI, ScreenState> =
        mutableMapOf() // map of screen states currently in memory
    private val screenScopesMap: MutableMap<URI, CoroutineScope> =
        mutableMapOf() // map of coroutine scopes associated to current screen states

    private val level1Backstack: MutableList<ScreenIdentifier> =
        mutableListOf() // list elements are only NavigationLevel1 screenIdentifiers
    private val verticalBackstacks: MutableMap<URI, MutableList<ScreenIdentifier>> =
        mutableMapOf() // the map keys is NavigationLevel1 screenIdentifier URI
    val verticalNavigationLevels: MutableMap<URI, MutableMap<Int, ScreenIdentifier>> =
        mutableMapOf() // the first map key is the NavigationLevel1 screenIdentifier URI, the second map key is the NavigationLevel numbers

    private val lastRemovedScreens = mutableListOf<ScreenIdentifier>()

    internal val dataRepository by lazy { repo }

    val currentScreenIdentifier: ScreenIdentifier
        get() = currentVerticalBackstack.lastOrNull() ?: level1Backstack.last()

    private val currentLevel1ScreenIdentifier: ScreenIdentifier
        get() = level1Backstack.last()

    val currentVerticalBackstack: MutableList<ScreenIdentifier>
        get() = verticalBackstacks[currentLevel1ScreenIdentifier.URI]!!

    val currentVerticalNavigationLevelsMap: MutableMap<Int, ScreenIdentifier>
        get() = verticalNavigationLevels[currentLevel1ScreenIdentifier.URI]!!

    val only1ScreenInBackstack: Boolean
        get() = level1Backstack.size + currentVerticalBackstack.size == 2


    // used by Compose apps
    fun getScreenStatesToRemove(): List<ScreenIdentifier> {
        val screenStatesToRemove = lastRemovedScreens.toList()
        lastRemovedScreens.clear() // clear list
        return screenStatesToRemove
    }

    // used by SwiftUI apps
    fun getLevel1ScreenIdentifiers(): List<ScreenIdentifier> {
        val screenIdentifiers = verticalNavigationLevels.values.map { it[1]!! }.toMutableList()
        screenIdentifiers.removeAll { !screenStatesMap.containsKey(it.URI) }  // remove all that don't have the state stored
        return screenIdentifiers
    }

    private fun isInTheStatesMap(screenIdentifier: ScreenIdentifier): Boolean {
        return screenStatesMap.containsKey(screenIdentifier.URI)
    }

    private fun isInAnyVerticalBackstack(screenIdentifier: ScreenIdentifier): Boolean {
        verticalBackstacks.forEach { verticalBackstack ->
            verticalBackstack.value.forEach {
                if (it.URI == screenIdentifier.URI) {
                    return true
                }
            }
        }
        return false
    }

    inline fun <reified T : ScreenState> updateScreen(
        stateClass: KClass<T>,
        update: (T) -> T,
    ) {
        //debugLogger.log("updateScreen: "+currentScreenIdentifier.URI)

        lateinit var screenIdentifier: ScreenIdentifier
        var screenState: T? = null
        for (i in currentVerticalNavigationLevelsMap.keys.sortedDescending()) {
            screenState = screenStatesMap[currentVerticalNavigationLevelsMap[i]?.URI] as? T
            if (screenState != null) {
                screenIdentifier = currentVerticalNavigationLevelsMap[i]!!
                break
            }
        }
        if (screenState != null) { // only perform screen state update if screen is currently visible
            screenStatesMap[screenIdentifier.URI] = update(screenState)
            triggerRecomposition()
        }
    }

    fun triggerRecomposition() {
        mutableStateFlow.value = AppState(mutableStateFlow.value.recompositionIndex + 1)
    }

    // ADD SCREEN FUNCTIONS

    fun addScreen(screenIdentifier: ScreenIdentifier, screenInitSettings: ScreenInitSettings, triggerRecomposition: Boolean = true) {
        //debugLogger.log("addScreen: "+screenIdentifier.URI)
        addScreenToBackstack(screenIdentifier)
        initScreenScope(screenIdentifier)
        if (!isInTheStatesMap(screenIdentifier) || screenInitSettings.reinitOnEachNavigation) {
            screenStatesMap[screenIdentifier.URI] = screenInitSettings.initState(screenIdentifier)
            screenStatesMap.keys.forEach { println("YOLO 3 $it") }
            if (triggerRecomposition) {
                triggerRecomposition() // FIRST UI RECOMPOSITION
                runInScreenScope(screenIdentifier) {
                    screenInitSettings.callOnInit(this) // SECOND UI RECOMPOSITION
                }
            }
        } else {
            triggerRecomposition() // JUST 1 UI RECOMPOSITION
        }
    }

    private fun addScreenToBackstack(screenIdentifier: ScreenIdentifier) {
        if (screenIdentifier.screen.navigationLevel == 1) {
            if (level1Backstack.size > 0) {
                val sameAsNewScreen =
                    screenIdentifier.screen == currentLevel1ScreenIdentifier.screen
                clearLevel1Screen(currentLevel1ScreenIdentifier, sameAsNewScreen)
            }
            setupNewLevel1Screen(screenIdentifier)
        } else {
            if (currentScreenIdentifier.URI == screenIdentifier.URI) {
                return
            }
            if (currentScreenIdentifier.screen == screenIdentifier.screen && !screenIdentifier.screen.stackableInstances) {
                val currentScreenId = currentScreenIdentifier
                currentVerticalNavigationLevelsMap.remove(currentScreenId.screen.navigationLevel)
                currentVerticalBackstack.remove(currentScreenId)
                if (!isInAnyVerticalBackstack(currentScreenId)) {
                    removeScreenStateAndScope(currentScreenId)
                }
            }
            if (currentVerticalBackstack.lastOrNull()?.URI != screenIdentifier.URI) {
                currentVerticalBackstack.add(screenIdentifier)
            }
        }
        currentVerticalNavigationLevelsMap[screenIdentifier.screen.navigationLevel] =
            screenIdentifier
    }

    // REMOVE SCREEN FUNCTIONS
    fun removeScreen(screenIdentifier: ScreenIdentifier) {
        if (screenIdentifier.screen.navigationLevel == 1) {
            level1Backstack.remove(screenIdentifier)
            removeScreenStateAndScope(screenIdentifier)
        } else {
            currentVerticalNavigationLevelsMap.remove(screenIdentifier.screen.navigationLevel)
            currentVerticalBackstack.removeAll { it.URI == screenIdentifier.URI }
            currentVerticalNavigationLevelsMap[currentScreenIdentifier.screen.navigationLevel] =
                currentScreenIdentifier // set new currentScreenIdentifier, after the removal
            if (!isInAnyVerticalBackstack(screenIdentifier)) {
                removeScreenStateAndScope(screenIdentifier)
            }
        }
    }

    private fun removeScreenStateAndScope(screenIdentifier: ScreenIdentifier) {
        screenScopesMap[screenIdentifier.URI]?.cancel() // cancel screen's coroutine scope
        screenScopesMap.remove(screenIdentifier.URI)
        screenStatesMap.remove(screenIdentifier.URI)
        lastRemovedScreens.add(screenIdentifier)
    }

    // LEVEL 1 NAVIGATION FUNCTIONS
    private fun clearLevel1Screen(screenIdentifier: ScreenIdentifier, sameAsNewScreen: Boolean) {
        // debugLogger.log("clear vertical backstack /"+screenIdentifier.URI)
        if (!screenIdentifier.level1VerticalBackstackEnabled()) {
            currentVerticalBackstack.forEach {
                if (it.screen.navigationLevel > 1) {
                    removeScreenStateAndScope(it)
                }
            }
            currentVerticalBackstack.removeAll { it.URI != screenIdentifier.URI }
            currentVerticalNavigationLevelsMap.keys.removeAll { it != 1 }
        }
        if (sameAsNewScreen && !screenIdentifier.screen.stackableInstances) {
            removeScreenStateAndScope(screenIdentifier)
            currentVerticalBackstack.clear()
            currentVerticalNavigationLevelsMap.clear()
            level1Backstack.remove(screenIdentifier)
        }
    }

    private fun setupNewLevel1Screen(screenIdentifier: ScreenIdentifier) {
        level1Backstack.removeAll { it.URI == screenIdentifier.URI }
        if (navigationSettings.alwaysQuitOnHomeScreen) {
            if (screenIdentifier.URI == navigationSettings.homeScreen.screenIdentifier.URI) {
                level1Backstack.clear() // remove all elements
            } else if (level1Backstack.size == 0) {
                addLevel1ScreenToBackstack(navigationSettings.homeScreen.screenIdentifier)
            }
        }
        addLevel1ScreenToBackstack(screenIdentifier)
    }

    private fun addLevel1ScreenToBackstack(screenIdentifier: ScreenIdentifier) {
        level1Backstack.add(screenIdentifier)
        if (verticalBackstacks[screenIdentifier.URI] == null) {
            verticalBackstacks[screenIdentifier.URI] = mutableListOf(screenIdentifier)
            verticalNavigationLevels[screenIdentifier.URI] = mutableMapOf(1 to screenIdentifier)
        }
    }


    // COROUTINE SCOPES FUNCTIONS

    private fun initScreenScope(screenIdentifier: ScreenIdentifier) {
        //debugLogger.log("initScreenScope()")
        screenScopesMap[screenIdentifier.URI]?.cancel()
        screenScopesMap[screenIdentifier.URI] = CoroutineScope(Job() + Dispatchers.Main)
    }

    fun reinitScreenScopes(): List<ScreenIdentifier> {
        //debugLogger.log("reinitScreenScopes()")
        currentVerticalNavigationLevelsMap.forEach {
            screenScopesMap[it.value.URI] = CoroutineScope(Job() + Dispatchers.Main)
        }
        return currentVerticalNavigationLevelsMap.values.toMutableList() // return list of screens whose scope has been reinitialized
    }

    // we run each event function on a Dispatchers.Main coroutine
    fun runInScreenScope(screenIdentifier: ScreenIdentifier? = null, block: suspend () -> Unit) {
        val URI = screenIdentifier?.URI ?: currentScreenIdentifier.URI
        val screenScope = screenScopesMap[URI]
        screenScope?.launch {
            block()
        }
    }

    fun cancelScreenScopes() {
        //debugLogger.log("cancelScreenScopes()")
        screenScopesMap.forEach {
            it.value.cancel() // cancel screen's coroutine scope
        }
    }

}


// APPSTATE DATA CLASS DEFINITION

data class AppState(
    val recompositionIndex: Int = 0,
) {
    fun getNavigation(model: JustListenViewModel): Navigation {
        return model.navigation
    }
}