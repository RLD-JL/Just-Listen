package com.rld.justlisten.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import com.rld.justlisten.viewmodel.BaseScreenViewModel
import com.rld.justlisten.viewmodel.NavigationEvent

@Composable
fun CollectNavigationEvents(
    viewModel: BaseScreenViewModel,
    navController: NavHostController,
) {
    LaunchedEffect(viewModel, navController) {
        viewModel.navigationEvents.collect { event ->
            when (event) {
                is NavigationEvent.NavigateTo -> navController.navigate(event.route)
                NavigationEvent.PopBackStack -> navController.popBackStack()
            }
        }
    }
}
