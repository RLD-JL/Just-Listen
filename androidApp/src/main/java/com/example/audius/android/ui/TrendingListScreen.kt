package com.example.audius.android.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.SnackbarDefaults.backgroundColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester.Companion.createRefs
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.audius.android.R
import com.example.audius.viewmodel.screens.trending.TrendingListItem
import com.example.audius.viewmodel.screens.trending.TrendingListState
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util

@Composable
fun TrendingListScreen(
    trendingListState: TrendingListState,
    onLastItemClick: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (trendingListState.isLoading) {
            Toast.makeText(LocalContext.current, "loading", Toast.LENGTH_SHORT).show()
        } else {
            if (trendingListState.trendingListItems.isEmpty()) {
                EmptyList()
            } else {
                LazyColumn {
                    items(items = trendingListState.trendingListItems, itemContent = { item ->
                        TrendingListRow(
                            data = item,
                            onLastItemClick = { onLastItemClick(item.id) }

                        )
                    })
                }
                if (trendingListState.playMusic) {
                    if (trendingListState.songId != "") {
                        VideoPlayer(
                            trendingListState.songId,
                            Modifier.align(Alignment.BottomCenter), playMusic = true
                        )
                        //PlayerBottomBar(Modifier.align(Alignment.BottomCenter))
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyList() {
    Text(
        text = "empty list",
        style = MaterialTheme.typography.body1,
        modifier = Modifier
            .padding(top = 30.dp)
            .fillMaxWidth(),
        textAlign = TextAlign.Center,
        fontSize = 18.sp
    )
}

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


@Composable
fun VideoPlayer(songId: String, modifier: Modifier, playMusic: Boolean) {
    val sampleVideo: String =
        "https://discoveryprovider.audius2.prod-us-west-2.staked.cloud/v1/tracks/${songId}/stream?app_name=EXAMPLEAPP"
    // This is the official way to access current context from Composable functions
    val context = LocalContext.current
    val playMusicMutable by remember {
        mutableStateOf(playMusic)
    }
    // Do not recreate the player everytime this Composable commits
    val exoPlayer = remember {
        SimpleExoPlayer.Builder(context).build()
    }

    // We only want to react to changes in sourceUrl.
    // This effect will be executed at each commit phase if
    // [sourceUrl] has changed.
    LaunchedEffect(sampleVideo) {
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(
            context,
            Util.getUserAgent(context, context.packageName)
        )

        val source = ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(sampleVideo))

        exoPlayer.setMediaSource(source)
        exoPlayer.prepare()
    }
    if (playMusicMutable)
        exoPlayer.play()

    PlayerBottomBar(modifier = modifier)

    /* Gateway to traditional Android Views
        AndroidView(
            {
                PlayerView(context).apply {
                    player = exoPlayer
                }
            },
            modifier = Modifier
                .requiredHeightIn(min = 80.dp, max = 120.dp)
                .fillMaxWidth()
        )

     */
}


@Composable
fun PlayerBottomBar(modifier: Modifier) {
    val bottomBarHeight = 57.dp
    Row(
        modifier = modifier
            .padding(bottom = bottomBarHeight)
            .fillMaxWidth()
            .background(color = backgroundColor)
            .clickable {  },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = R.drawable.adele21),
            modifier = Modifier.size(65.dp),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        Text(
            text = "Someone Like you by Adele",
            style = typography.h6.copy(fontSize = 14.sp),
            modifier = Modifier
                .padding(8.dp)
                .weight(1f),
        )
        Icon(
            imageVector = Icons.Default.FavoriteBorder, modifier = Modifier.padding(8.dp),
            contentDescription = null
        )
        Icon(
            imageVector = Icons.Default.PlayArrow, modifier = Modifier.padding(8.dp),
            contentDescription = null
        )
    }
}
