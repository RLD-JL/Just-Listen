package com.rld.justlisten.android.ui.settingsscreen.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.chrisbanes.snapper.ExperimentalSnapperApi
import dev.chrisbanes.snapper.rememberSnapperFlingBehavior

@Composable
fun TimerSetup(
    maxWidth: Dp
) {
    val hours =
        (0..12).map { number -> if (number < 10) "0$number" else number.toString() }.toList()
    val minutes =
        (0..59).map { number -> if (number < 10) "0$number" else number.toString() }.toList()
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        val spaceBetween = 50.dp
        val width = maxWidth / 2 - spaceBetween
        CircularList(
            hours,
            modifier = Modifier.width(width),
            isEndless = true,
            alignment = Alignment.End
        )
        CircularList(
            minutes,
            modifier = Modifier.width(width),
            isEndless = true,
            alignment = Alignment.Start
        )
    }
}

@OptIn(ExperimentalSnapperApi::class)
@Composable
fun CircularList(
    items: List<String>,
    modifier: Modifier = Modifier,
    isEndless: Boolean = false,
    alignment: Alignment.Horizontal
) {
    val lazyListState = rememberLazyListState(if (isEndless) Int.MAX_VALUE / 2 else 0)
    val contentPadding = PaddingValues(2.dp)

    LazyColumn(
        state = lazyListState,
        modifier = modifier,
        horizontalAlignment = alignment,
        flingBehavior = rememberSnapperFlingBehavior(
            lazyListState = lazyListState,
            endContentPadding = contentPadding.calculateBottomPadding(),
        ),
    ) {
        items(
            count = if (isEndless) Int.MAX_VALUE else items.size,
            itemContent = {
                val index = it % items.size
                Text(text = items[index], modifier = Modifier.padding(1.dp))
            }
        )
    }
}