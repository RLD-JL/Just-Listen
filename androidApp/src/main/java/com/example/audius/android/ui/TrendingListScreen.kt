package com.example.audius.android.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audius.viewmodel.screens.trending.TrendingListItem
import com.example.audius.viewmodel.screens.trending.TrendingListState

@Composable
fun TrendingListScreen(
    trendingListState: TrendingListState,
    onLastItemClick: (String) -> Unit
) {
    if (trendingListState.isLoading) {
        Toast.makeText(LocalContext.current, "laoding", Toast.LENGTH_SHORT).show()
    } else {
        if (trendingListState.trendingListItems.isEmpty()) {
            Text(
                text = "empty list",
                style = MaterialTheme.typography.body1,
                modifier = Modifier
                    .padding(top = 30.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontSize = 18.sp
            )
        } else {
            LazyColumn() {
                items(items = trendingListState.trendingListItems, itemContent = {
                    item -> TrendingListRow(
                    data = item,
                    onLastItemClick = { onLastItemClick (item.id)}

                    )
                })
            }
        }
    }
}

@Composable
fun TrendingListRow(data: TrendingListItem, onLastItemClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onLastItemClick).height(50.dp).padding(start=10.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
            Text(text = data.title, style = MaterialTheme.typography.body1, fontWeight = FontWeight.Bold)
        }
        Column(modifier = Modifier.width(70.dp), horizontalAlignment = Alignment.End) {
            Text(text = data.id, style = MaterialTheme.typography.body1)
        }
    }
    Divider()
}
