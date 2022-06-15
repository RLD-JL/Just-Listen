package com.rld.justlisten.android.ui.searchscreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.rld.justlisten.android.R

@Composable
fun ItemRowSearch(itemSearched: String, onPreviousSearchedPressed: (String) -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .clickable(onClick = { onPreviousSearchedPressed(itemSearched) })
    ) {
        Icon(imageVector = Icons.Default.Search, contentDescription = null)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(modifier = Modifier.padding(start = 5.dp), text = itemSearched)
            Icon(
                painter = painterResource(id = R.drawable.ic_baseline_north_west_24),
                contentDescription = null
            )
        }
    }
}