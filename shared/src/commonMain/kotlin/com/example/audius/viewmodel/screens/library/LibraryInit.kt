package com.example.audius.viewmodel.screens.library

import com.example.audius.Navigation
import com.example.audius.ScreenParams
import com.example.audius.viewmodel.screens.ScreenInitSettings
import kotlinx.serialization.Serializable

@Serializable
data class LibraryParams(val string: String) : ScreenParams

fun Navigation.initLibrary(params: LibraryParams) = ScreenInitSettings(
    title = "Library" + params.string,
    initState = { LibraryState(isLoading = true) },
    callOnInit = {

    },
    reinitOnEachNavigation = true
)