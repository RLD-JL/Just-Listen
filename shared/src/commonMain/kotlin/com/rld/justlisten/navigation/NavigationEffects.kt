package com.rld.justlisten.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import com.rld.justlisten.viewmodel.BaseScreenViewModel
import com.rld.justlisten.viewmodel.NavigationEvent

import androidx.navigation.NavGraph.Companion.findStartDestination

@Composable
fun CollectNavigationEvents(
    viewModel: BaseScreenViewModel,
    navController: NavHostController,
) {
    val iosCallbacks = LocalIosNavigationCallbacks.current
    LaunchedEffect(viewModel, navController, iosCallbacks) {
        viewModel.navigationEvents.collect { event ->
            if (iosCallbacks != null) {
                when (event) {
                    is NavigationEvent.NavigateTo -> iosCallbacks.onNavigate(event.route)
                    NavigationEvent.PopBackStack -> iosCallbacks.onPopBackStack()
                }
            } else {
                when (event) {
                    is NavigationEvent.NavigateTo -> {
                        val route = event.route
                        if (route.navigationLevel == NavigationLevel.LEVEL_1) {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        } else {
                            navController.navigate(route)
                        }
                    }
                    NavigationEvent.PopBackStack -> navController.popBackStack()
                }
            }
        }
    }
}
