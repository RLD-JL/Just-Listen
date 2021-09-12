package com.example.audius.android.ui.playlistdetailscreen

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.palette.graphics.Palette
import com.example.audius.android.ui.playlistdetailscreen.components.BoxTopSection
import com.example.audius.android.ui.playlistdetailscreen.components.SongListScrollingSection
import com.example.audius.android.ui.playlistdetailscreen.components.TopSectionOverlay
import com.example.audius.android.ui.playlistscreen.components.graySurface
import com.example.audius.android.ui.theme.modifiers.horizontalGradientBackground
import com.example.audius.android.ui.theme.modifiers.verticalGradientBackground
import com.example.audius.viewmodel.screens.trending.PlaylistDetailState
import android.graphics.drawable.BitmapDrawable
import androidx.compose.material.IconButton
import androidx.compose.runtime.*
import coil.ImageLoader
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import coil.request.SuccessResult
import coil.transform.RoundedCornersTransformation
import com.example.audius.viewmodel.screens.trending.PlaylistItem
import kotlinx.coroutines.launch
import java.lang.Exception


fun spotifySurfaceGradient(isDark: Boolean) =
    if (isDark) listOf(graySurface, Color.Black) else listOf(Color.White, Color.LightGray)

@Composable
fun SpotifyDetailScreen(playlistDetailState: PlaylistDetailState, onBackButtonPressed:(Boolean) -> Unit)
{
    val context = LocalContext.current
    val scrollState = rememberScrollState(0)
    val surfaceGradient = spotifySurfaceGradient(isSystemInDarkTheme()).asReversed()
    val listColor: MutableState<Int> = remember {
        mutableStateOf(-10082496)
    }
    val imageLoader = ImageLoader(context)
    val request = ImageRequest.Builder(context)
        .transformations(RoundedCornersTransformation(12.dp.value))
        .data(playlistDetailState.playlistIcon)
        .build()
    val imagePainter = rememberImagePainter(
        request = request,
        imageLoader = imageLoader
    )

    LaunchedEffect(key1 = imagePainter) {
        launch {
            try {
                val result = (imageLoader.execute(request) as SuccessResult).drawable
                val bitmap = (result as BitmapDrawable).bitmap
                val vibrant = Palette.from(bitmap)
                    .generate().dominantSwatch?.rgb
                listColor.value = vibrant ?: -13082496
            } catch (exception: Exception) {

            }

        }
    }

    val dominantColors = listOf(Color(listColor.value), MaterialTheme.colors.surface)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalGradientBackground(dominantColors)
    ) {
        BoxTopSection(scrollState = scrollState, playlistDetailState = playlistDetailState, playlistPainter = imagePainter)
        TopSectionOverlay(scrollState = scrollState)
        BottomScrollableContent(playlist = playlistDetailState.songPlaylist,scrollState = scrollState, surfaceGradient = surfaceGradient)
        AnimatedToolBar(playlistDetailState, scrollState, surfaceGradient, onBackButtonPressed)
    }
}

@Composable
fun AnimatedToolBar(playlistDetailState: PlaylistDetailState, scrollState: ScrollState, surfaceGradient: List<Color>, onBackButtonPressed:(Boolean) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .horizontalGradientBackground(
                if (Dp(scrollState.value.toFloat()) < 1080.dp)
                    listOf(Color.Transparent, Color.Transparent) else surfaceGradient
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        IconButton(onClick = { onBackButtonPressed(true) }) {
            Icon(
                imageVector = Icons.Default.ArrowBack, tint = MaterialTheme.colors.onSurface,
                contentDescription = null,
                )
        }
        Text(
            text = playlistDetailState.playlistName,
            color = MaterialTheme.colors.onSurface,
            modifier = Modifier
                .padding(16.dp)
                .alpha(((scrollState.value + 0.001f) / 1000).coerceIn(0f, 1f))
        )
        Icon(
            imageVector = Icons.Default.MoreVert, tint = MaterialTheme.colors.onSurface,
            contentDescription = null
        )
    }
}

@Composable
fun BottomScrollableContent(playlist: List<PlaylistItem>, scrollState: ScrollState, surfaceGradient: List<Color>) {
    Column(modifier = Modifier.verticalScroll(state = scrollState)) {
        Spacer(modifier = Modifier.height(480.dp))
        Column(modifier = Modifier.horizontalGradientBackground(surfaceGradient)) {
            SongListScrollingSection(playlist = playlist)
        }
        Spacer(modifier = Modifier.height(50.dp))
    }
}
