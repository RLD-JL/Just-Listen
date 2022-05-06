package com.rld.justlisten.android.ui.bottombars.playbar.components

import android.media.session.PlaybackState
import android.support.v4.media.session.PlaybackStateCompat
import android.support.v4.media.session.PlaybackStateCompat.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.rld.justlisten.android.R
import com.rld.justlisten.android.exoplayer.MusicService
import com.rld.justlisten.android.exoplayer.MusicServiceConnection
import com.rld.justlisten.android.ui.extensions.ModifiedSlider
import com.rld.justlisten.android.ui.utils.offsetX
import com.rld.justlisten.android.ui.utils.offsetY
import com.rld.justlisten.android.ui.utils.widthSize
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.collect

@InternalCoroutinesApi
@Composable
fun PlayBarActionsMaximized(
    bottomPadding: Dp,
    currentFraction: Float,
    musicServiceConnection: MusicServiceConnection,
    title: String,
    onSkipNextPressed: () -> Unit,
    maxWidth: Float,
) {
    val interactionSource = remember { MutableInteractionSource() }


    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> {
                    musicServiceConnection.transportControls.seekTo(musicServiceConnection.songDuration.value)
                    musicServiceConnection.sliderClicked.value = false
                    musicServiceConnection.updateSong()
                }
                is PressInteraction.Release -> {}
                is PressInteraction.Cancel -> {}
                is DragInteraction.Start -> {}
                is DragInteraction.Stop -> {
                    musicServiceConnection.transportControls.seekTo(musicServiceConnection.songDuration.value)
                    musicServiceConnection.sliderClicked.value = false
                    musicServiceConnection.updateSong()
                }
                is DragInteraction.Cancel -> {}
            }
        }
    }


    if (currentFraction == 1f) {
        var sliderPosition by remember { mutableStateOf(0f) }
        sliderPosition =
            musicServiceConnection.songDuration.value / MusicService.curSongDuration.toFloat()
        Column(
            Modifier
                .fillMaxSize()
                .padding(bottom = bottomPadding + 5.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Text(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                text = title,
                textAlign = TextAlign.Center
            )
            ModifiedSlider(
                interactionSource = interactionSource,
                modifier = Modifier
                    .offset(x = offsetX(currentFraction, maxWidth).dp)
                    .width(widthSize(currentFraction, maxWidth).dp),
                value = sliderPosition, onValueChange = {
                    musicServiceConnection.sliderClicked.value = true
                    musicServiceConnection.songDuration.value =
                        (it * MusicService.curSongDuration).toLong()
                })

            Row(
                Modifier.height(IntrinsicSize.Max)
            ) {
                if (musicServiceConnection.shuffleMode == SHUFFLE_MODE_NONE) {
                    Icon(
                        modifier = Modifier
                            .size(40.dp)
                            .weight(0.2f)
                            .clickable {
                                musicServiceConnection.transportControls.setShuffleMode(
                                    SHUFFLE_MODE_ALL
                                )
                            },
                        painter = painterResource(id = R.drawable.exo_styled_controls_shuffle_off),
                        contentDescription = null,
                    )
                } else {
                    Icon(
                        modifier = Modifier
                            .size(40.dp)
                            .weight(0.2f)
                            .clickable {
                                musicServiceConnection.transportControls.setShuffleMode(
                                    SHUFFLE_MODE_NONE
                                )
                            },
                        painter = painterResource(id = R.drawable.exo_styled_controls_shuffle_on),
                        contentDescription = null,
                    )
                }
                Icon(
                    modifier = Modifier
                        .size(40.dp)
                        .weight(0.2f)
                        .clickable {
                            musicServiceConnection.transportControls.skipToPrevious()
                        },
                    painter = painterResource(id = R.drawable.exo_ic_skip_previous),
                    contentDescription = null,
                )
                if (musicServiceConnection.playbackState.value?.state != PlaybackState.STATE_PLAYING &&
                    musicServiceConnection.playbackState.value?.state != PlaybackState.STATE_BUFFERING
                ) {
                    OutlinedButton(
                        shape = CircleShape,
                        border = BorderStroke(1.dp, MaterialTheme.colors.onSurface),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.onSurface),
                        modifier = Modifier
                            .size(40.dp)
                            .weight(0.2f),
                        onClick = { musicServiceConnection.transportControls.play() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.exo_icon_play),
                            contentDescription = null
                        )
                    }
                } else {
                    OutlinedButton(
                        shape = CircleShape,
                        border = BorderStroke(1.dp, MaterialTheme.colors.onSurface),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colors.onSurface),
                        modifier = Modifier
                            .size(40.dp)
                            .weight(0.2f),
                        onClick = { musicServiceConnection.transportControls.pause() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.exo_icon_pause),
                            contentDescription = null
                        )
                    }
                }

                Icon(
                    painter = painterResource(id = R.drawable.exo_ic_skip_next),
                    modifier = Modifier
                        .size(40.dp)
                        .clickable(onClick = onSkipNextPressed)
                        .weight(0.2f),
                    contentDescription = null,
                )
                when (musicServiceConnection.repeatMode) {
                    REPEAT_MODE_NONE -> Icon(
                        modifier = Modifier
                            .size(40.dp)
                            .weight(0.2f)
                            .clickable {
                                musicServiceConnection.transportControls.setRepeatMode(
                                    REPEAT_MODE_ONE
                                )
                            },
                        painter = painterResource(id = R.drawable.exo_controls_repeat_off),
                        contentDescription = null,
                    )
                    REPEAT_MODE_ONE -> Icon(
                        modifier = Modifier
                            .size(40.dp)
                            .weight(0.2f)
                            .clickable {
                                musicServiceConnection.transportControls.setRepeatMode(
                                    REPEAT_MODE_ALL
                                )
                            },
                        painter = painterResource(id = R.drawable.exo_controls_repeat_one),
                        contentDescription = null,
                    )
                    REPEAT_MODE_ALL -> Icon(
                        modifier = Modifier
                            .size(40.dp)
                            .weight(0.2f)
                            .clickable {
                                musicServiceConnection.transportControls.setRepeatMode(
                                    REPEAT_MODE_NONE
                                )
                            },
                        painter = painterResource(id = R.drawable.exo_controls_repeat_all),
                        contentDescription = null,
                    )
                }

            }
        }
    }
}


