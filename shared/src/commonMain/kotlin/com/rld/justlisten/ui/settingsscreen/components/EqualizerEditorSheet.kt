package com.rld.justlisten.ui.settingsscreen.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rld.justlisten.viewmodel.screens.settings.SettingsState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EqualizerEditorSheet(
    settings: SettingsState,
    updateSettings: (SettingsState) -> Unit,
    scaffoldState: BottomSheetScaffoldState,
    coroutineScope: CoroutineScope
) {
    val isExpanded = scaffoldState.bottomSheetState.currentValue == SheetValue.Expanded
    // Local state to allow saving on confirm rather than instant persist
    var tempIsEqEnabled by remember(settings.isEqEnabled, isExpanded) { mutableStateOf(settings.isEqEnabled) }
    var tempEqPreset by remember(settings.eqPreset, isExpanded) { mutableStateOf(settings.eqPreset) }
    var tempEqBands by remember(settings.eqBands, isExpanded) { mutableStateOf(settings.eqBands) }

    val presetsMap = mapOf(
        "Flat" to listOf(0f, 0f, 0f, 0f, 0f),
        "Bass Booster" to listOf(6f, 4f, 0f, 0f, 0f),
        "Rock" to listOf(4f, 2f, -2f, 2f, 5f),
        "Pop" to listOf(-2f, 1f, 4f, 2f, -2f),
        "Classical" to listOf(4f, 2f, 0f, 3f, 4f),
        "Vocal Booster" to listOf(-3f, 0f, 5f, 4f, -1f)
    )
    val presetNames = listOf("Flat", "Bass Booster", "Rock", "Pop", "Classical", "Vocal Booster")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 8.dp, bottom = 24.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Drag handle / Title Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Equalizer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Equalizer Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (tempIsEqEnabled) "Preset: $tempEqPreset" else "Equalizer is Disabled",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            Switch(
                checked = tempIsEqEnabled,
                onCheckedChange = { enabled -> tempIsEqEnabled = enabled },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            )
        }

        if (tempIsEqEnabled) {
            // Preset selector chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presetNames.forEach { presetName ->
                    val isSelected = tempEqPreset == presetName
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            tempEqPreset = presetName
                            presetsMap[presetName]?.let { bands ->
                                tempEqBands = bands
                            }
                        },
                        label = { Text(presetName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            // Visual Frequency Curve
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
            ) {
                val primaryColor = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary
                val surfaceColor = MaterialTheme.colorScheme.surface
                
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    val centerY = height / 2f
                    
                    // Draw 0dB reference dashed baseline
                    drawLine(
                        color = primaryColor.copy(alpha = 0.2f),
                        start = androidx.compose.ui.geometry.Offset(0f, centerY),
                        end = androidx.compose.ui.geometry.Offset(width, centerY),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )
                    
                    if (tempEqBands.size >= 5) {
                        val points = mutableListOf<androidx.compose.ui.geometry.Offset>()
                        for (i in 0 until 5) {
                            val x = width * (i.toFloat() / 4f)
                            val gain = tempEqBands[i]
                            val y = centerY - (gain / 15f) * centerY
                            points.add(androidx.compose.ui.geometry.Offset(x, y))
                        }
                        
                        // Draw filled path under the curve
                        val fillPath = Path().apply {
                            moveTo(0f, height)
                            lineTo(points[0].x, points[0].y)
                            for (i in 0 until 4) {
                                val p0 = points[i]
                                val p1 = points[i + 1]
                                val controlX = (p0.x + p1.x) / 2f
                                cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                            }
                            lineTo(width, height)
                            close()
                        }
                        
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.15f),
                                    Color.Transparent
                                )
                            )
                        )
                        
                        // Draw the glowing curve
                        val curvePath = Path().apply {
                            moveTo(points[0].x, points[0].y)
                            for (i in 0 until 4) {
                                val p0 = points[i]
                                val p1 = points[i + 1]
                                val controlX = (p0.x + p1.x) / 2f
                                cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                            }
                        }
                        
                        drawPath(
                            path = curvePath,
                            brush = Brush.horizontalGradient(
                                colors = listOf(primaryColor, secondaryColor)
                            ),
                            style = Stroke(
                                width = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )
                        
                        // Draw point markers
                        points.forEach { point ->
                            drawCircle(
                                color = primaryColor,
                                radius = 4.dp.toPx(),
                                center = point
                            )
                            drawCircle(
                                color = surfaceColor,
                                radius = 2.dp.toPx(),
                                center = point
                            )
                        }
                    }
                }
            }

            // 5 Band Sliders Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val frequencies = listOf("60Hz", "230Hz", "910Hz", "4kHz", "14kHz")
                
                for (i in 0 until 5) {
                    val bandValue = tempEqBands.getOrElse(i) { 0f }
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${if (bandValue >= 0) "+" else ""}${bandValue.toInt()}dB",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        // Rotated Slider container
                        Box(
                            modifier = Modifier
                                .height(110.dp)
                                .width(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Slider(
                                value = bandValue,
                                onValueChange = { newValue ->
                                    val updatedBands = tempEqBands.toMutableList()
                                    if (i in updatedBands.indices) {
                                        updatedBands[i] = (newValue * 2).toInt() / 2f
                                        tempEqBands = updatedBands
                                        tempEqPreset = "Custom"
                                    }
                                },
                                valueRange = -15f..15f,
                                modifier = Modifier
                                    .width(100.dp)
                                    .graphicsLayer {
                                        rotationZ = 270f
                                    }
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = frequencies[i],
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            // Disabled state placeholder
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Equalizer is currently disabled.\nToggle switch to adjust audio frequencies.",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    coroutineScope.launch {
                        scaffoldState.bottomSheetState.partialExpand()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text("Cancel", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }

            Button(
                onClick = {
                    updateSettings(
                        settings.copy(
                            isEqEnabled = tempIsEqEnabled,
                            eqPreset = tempEqPreset,
                            eqBands = tempEqBands
                        )
                    )
                    coroutineScope.launch {
                        scaffoldState.bottomSheetState.partialExpand()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Save & Apply", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
