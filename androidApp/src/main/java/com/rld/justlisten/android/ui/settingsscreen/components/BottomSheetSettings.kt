package com.rld.justlisten.android.ui.settingsscreen.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineScope

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BottomSheetSettings(
    workManager: WorkManager,
    scaffoldState: BottomSheetScaffoldState,
    coroutineScope: CoroutineScope,
    onConfirmClicked: (String, String) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .height(175.dp)
            .fillMaxWidth()
    ) {
        val maxHeight = this.maxHeight

        Canvas(modifier = Modifier.fillMaxWidth()) {
            val width = size.width
            val height = 25.dp
            val newSize = Size(width, height.toPx())
            drawRoundRect(
                color = Color.LightGray.copy(alpha = 0.40f),
                size = newSize,
                style = Fill,
                topLeft = Offset(0f, (maxHeight.toPx() / 2) + ((height - 8.dp) / 2).toPx()),
                cornerRadius = CornerRadius(
                    x = 5.dp.toPx(),
                    y = 10.dp.toPx()
                )
            )
        }
        TimerSetup(workManager, onConfirmClicked, coroutineScope, scaffoldState)
    }
}