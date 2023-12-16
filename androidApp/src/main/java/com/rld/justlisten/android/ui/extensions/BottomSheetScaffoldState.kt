package com.rld.justlisten.android.ui.extensions

import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import kotlin.math.absoluteValue

/**
 * Align fraction states into single value
 *
 *  1.0f - Expanded
 *  0.0f - Collapsed
 */

@ExperimentalMaterialApi
val BottomSheetScaffoldState.fraction: Float
    get() {
        val fraction = bottomSheetState.progress.absoluteValue
        val currentValue = bottomSheetState.currentValue
        val targetValue = bottomSheetState.targetValue

        return when {
            currentValue == BottomSheetValue.Collapsed && (targetValue != BottomSheetValue.Expanded)&& fraction == 1f-> 0f
            targetValue == BottomSheetValue.Collapsed && fraction == 1f -> 0f
            currentValue == BottomSheetValue.Expanded && fraction != 1f -> 1f - fraction
            else -> fraction
        }
    }