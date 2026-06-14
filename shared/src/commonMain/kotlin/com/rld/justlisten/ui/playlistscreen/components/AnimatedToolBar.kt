package com.rld.justlisten.ui.playlistscreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rld.justlisten.ui.theme.modifiers.horizontalGradientBackground
import com.rld.justlisten.ui.utils.getGreetingText

@Composable
fun AnimatedToolBar(
    onSearchClicked: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .horizontalGradientBackground(
                listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.background)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {

        val text = getGreetingText()
        Header(text = text)
        Icon(
            modifier = Modifier.clickable(onClick = onSearchClicked),
            imageVector = Icons.Default.Search,
            contentDescription = null
        )
    }
}
