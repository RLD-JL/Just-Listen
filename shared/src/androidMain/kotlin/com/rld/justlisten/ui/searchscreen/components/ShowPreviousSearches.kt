package com.rld.justlisten.ui.searchscreen.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.util.fastForEach

@Composable
fun ShowPreviousSearches(listOfSearches: List<String>, onPreviousSearchedPressed: (String) ->Unit) {
    listOfSearches.fastForEach { itemSearched ->
        ItemRowSearch(itemSearched, onPreviousSearchedPressed)
    }
}
