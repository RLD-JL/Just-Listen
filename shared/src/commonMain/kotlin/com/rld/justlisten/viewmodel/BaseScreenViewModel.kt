package com.rld.justlisten.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.rld.justlisten.navigation.Route
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow

/**
 * Sealed interface for navigation events
 */
sealed interface NavigationEvent {
    data class NavigateTo(val route: Route) : NavigationEvent
    data object PopBackStack : NavigationEvent
}

/**
 * Base class for all screen ViewModels.
 * Handles common functionality like navigation and loading states.
 */
abstract class BaseScreenViewModel : ViewModel() {

    protected val _navigationEvents = Channel<NavigationEvent>(Channel.BUFFERED)
    val navigationEvents: Flow<NavigationEvent> = _navigationEvents.receiveAsFlow()

    /**
     * Emit a navigation event to navigate to a route
     */
    protected fun navigate(route: Route) {
        viewModelScope.launch {
            _navigationEvents.send(NavigationEvent.NavigateTo(route))
        }
    }

    /**
     * Emit a back navigation event
     */
    protected fun popBackStack() {
        viewModelScope.launch {
            _navigationEvents.send(NavigationEvent.PopBackStack)
        }
    }
}

/**
 * Sealed class for common UI states
 */
sealed class UiState {
    data object Loading : UiState()
    data class Success(val data: Any) : UiState()
    data class Error(val message: String) : UiState()
}

/**
 * Extension function for easier state updates
 */
inline fun <T> kotlinx.coroutines.flow.MutableStateFlow<T>.update(transform: (T) -> T) {
    value = transform(value)
}

