package com.rld.justlisten.android.ui.playlistdetailscreen.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rld.justlisten.viewmodel.screens.playlistdetail.PlaylistDetailState

@Composable
fun BoxTopSection(scrollState: MutableState<Float>, playlistDetailState: PlaylistDetailState, playlistPainter: Painter) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {

        //animate as scroll value increase but not fast so divide by random number 50
        val dynamicValue =
            if (250.dp - Dp(-scrollState.value / 50f) < 10.dp) 10.dp //prevent going 0 cause crash
            else 250.dp - Dp(-scrollState.value / 20f)
        val animateImageSize = animateDpAsState(dynamicValue).value
        Image(
            painter = playlistPainter,
            contentDescription = null,
            modifier = Modifier
                .size(animateImageSize)
                .padding(8.dp)
        )
        Text(
            text = playlistDetailState.playlistName,
            style = typography.subtitle2.copy(fontWeight = FontWeight.ExtraBold),
            maxLines = 1,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(8.dp),
        )
        Text(
            text = "FOLLOWING",
            style = typography.h6.copy(fontSize = 12.sp),
            modifier = Modifier
                .padding(4.dp)
                .border(
                    border = BorderStroke(2.dp, MaterialTheme.colors.primaryVariant),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(vertical = 4.dp, horizontal = 24.dp)
        )
        Text(
            text = "Created by ${playlistDetailState.playListCreatedBy}",
            maxLines = 1,
            textAlign = TextAlign.Center,
            style = typography.subtitle2,
            modifier = Modifier.padding(4.dp)
        )
    }
}
