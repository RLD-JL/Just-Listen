package com.example.audius.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.tooling.preview.Preview
import com.example.audius.Navigation
import com.example.audius.android.ui.theme.OnePane

@Composable
@Preview
fun Navigation.Router() {
    val screenUIisStateHolder = rememberSaveableStateHolder()

    OnePane(screenUIisStateHolder)

    screenStatesToRemove.forEach{
        screenUIisStateHolder.removeState(it.URI)
    }
}