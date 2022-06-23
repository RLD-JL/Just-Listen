package com.rld.justlisten.android.ui.playlistscreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rld.justlisten.android.ui.theme.modifiers.horizontalGradientBackground
import java.util.*

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
                listOf(MaterialTheme.colors.background, MaterialTheme.colors.background)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {

        val rightNow = Calendar.getInstance()

        val text = when (rightNow.get(Calendar.HOUR_OF_DAY)) {
            in 0..5 -> "Chilling"
            in 5..11 -> "Good Morning"
            in 12..17 -> "Hey there"
            in 17..23 -> "Good Evening"
            else -> "Hello"
        }
        Header(text = text)
        Icon(
            modifier = Modifier.clickable(onClick = onSearchClicked),
            imageVector = Icons.Default.Search,
            contentDescription = null
        )
    }
}