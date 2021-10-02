package com.example.audius.android.ui.trendinglistscreen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.audius.viewmodel.screens.playlist.TrendingListItem

@Composable
fun TrendingListRow(data: TrendingListItem, onLastItemClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onLastItemClick)
            .height(50.dp)
            .padding(start = 10.dp), verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp)
        ) {
            Text(
                text = data.title,
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.Bold
            )
        }
        Column(modifier = Modifier.width(70.dp), horizontalAlignment = Alignment.End) {
            Text(text = data.id, style = MaterialTheme.typography.body1)
        }
    }
    Divider()
}
