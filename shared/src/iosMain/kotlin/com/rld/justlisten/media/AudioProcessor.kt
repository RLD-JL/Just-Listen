package com.rld.justlisten.media

import kotlinx.cinterop.*
import kotlin.math.pow
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun computeCoefficients(statePtr: CPointer<FloatVar>) {
    val sampleRate = statePtr[71]
    if (sampleRate <= 0f) return
    val centerFreqs = floatArrayOf(60f, 230f, 910f, 4000f, 14000f)
    val Qs = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f, 1.0f)
    for (i in 0 until 5) {
        val bandOffset = 1 + i * 14
        val gainDb = statePtr[bandOffset + 0]
        val A = 10.0.pow(gainDb.toDouble() / 40.0).toFloat()
        val w0 = (2.0f * PI.toFloat() * centerFreqs[i] / sampleRate)
        val cosW0 = cos(w0)
        val sinW0 = sin(w0)
        val alpha = sinW0 / (2.0f * Qs[i])

        val b0 = 1.0f + alpha * A
        val b1 = -2.0f * cosW0
        val b2 = 1.0f - alpha * A
        val a0 = 1.0f + alpha / A
        val a1 = -2.0f * cosW0
        val a2 = 1.0f - alpha / A

        statePtr[bandOffset + 1] = b0 / a0
        statePtr[bandOffset + 2] = b1 / a0
        statePtr[bandOffset + 3] = b2 / a0
        statePtr[bandOffset + 4] = a1 / a0
        statePtr[bandOffset + 5] = a2 / a0
    }
    statePtr[72] = 0.0f
}

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
fun processAudioSamples(statePtr: CPointer<FloatVar>, mData: CPointer<FloatVar>, framesToProcess: Int, channels: Int, chIndex: Int = 0) {
    val isEqEnabled = statePtr[0] > 0.5f
    val isNormEnabled = statePtr[73] > 0.5f

    if (!isEqEnabled && !isNormEnabled) return

    if (channels == 2) {
        for (f in 0 until framesToProcess) {
            var sampleL = mData[f * 2]
            var sampleR = mData[f * 2 + 1]

            if (isEqEnabled) {
                for (i in 0 until 5) {
                    val bandOffset = 1 + i * 14
                    val b0 = statePtr[bandOffset + 1]
                    val b1 = statePtr[bandOffset + 2]
                    val b2 = statePtr[bandOffset + 3]
                    val a1 = statePtr[bandOffset + 4]
                    val a2 = statePtr[bandOffset + 5]

                    val x1_l = statePtr[bandOffset + 6]
                    val x2_l = statePtr[bandOffset + 7]
                    val y1_l = statePtr[bandOffset + 8]
                    val y2_l = statePtr[bandOffset + 9]

                    val outL = b0 * sampleL + b1 * x1_l + b2 * x2_l - a1 * y1_l - a2 * y2_l
                    statePtr[bandOffset + 7] = x1_l
                    statePtr[bandOffset + 6] = sampleL
                    statePtr[bandOffset + 9] = y1_l
                    statePtr[bandOffset + 8] = outL
                    sampleL = outL

                    val x1_r = statePtr[bandOffset + 10]
                    val x2_r = statePtr[bandOffset + 11]
                    val y1_r = statePtr[bandOffset + 12]
                    val y2_r = statePtr[bandOffset + 13]

                    val outR = b0 * sampleR + b1 * x1_r + b2 * x2_r - a1 * y1_r - a2 * y2_r
                    statePtr[bandOffset + 11] = x1_r
                    statePtr[bandOffset + 10] = sampleR
                    statePtr[bandOffset + 13] = y1_r
                    statePtr[bandOffset + 12] = outR
                    sampleR = outR
                }
            }

            if (isNormEnabled) {
                val limitValue = 0.5f // -6dB limit
                val absL = if (sampleL < 0f) -sampleL else sampleL
                val absR = if (sampleR < 0f) -sampleR else sampleR
                val maxVal = if (absL > absR) absL else absR
                if (maxVal > limitValue) {
                    val scale = limitValue / maxVal
                    sampleL *= scale
                    sampleR *= scale
                }
            }

            mData[f * 2] = sampleL
            mData[f * 2 + 1] = sampleR
        }
    } else {
        for (f in 0 until framesToProcess) {
            var sample = mData[f]

            if (isEqEnabled) {
                for (i in 0 until 5) {
                    val bandOffset = 1 + i * 14
                    val b0 = statePtr[bandOffset + 1]
                    val b1 = statePtr[bandOffset + 2]
                    val b2 = statePtr[bandOffset + 3]
                    val a1 = statePtr[bandOffset + 4]
                    val a2 = statePtr[bandOffset + 5]

                    val x1Idx = if (chIndex == 0) 6 else 10
                    val x2Idx = if (chIndex == 0) 7 else 11
                    val y1Idx = if (chIndex == 0) 8 else 12
                    val y2Idx = if (chIndex == 0) 9 else 13

                    val x1 = statePtr[bandOffset + x1Idx]
                    val x2 = statePtr[bandOffset + x2Idx]
                    val y1 = statePtr[bandOffset + y1Idx]
                    val y2 = statePtr[bandOffset + y2Idx]

                    val out = b0 * sample + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2
                    statePtr[bandOffset + x2Idx] = x1
                    statePtr[bandOffset + x1Idx] = sample
                    statePtr[bandOffset + y2Idx] = y1
                    statePtr[bandOffset + y1Idx] = out
                    sample = out
                }
            }

            if (isNormEnabled) {
                val limitValue = 0.5f
                val absVal = if (sample < 0f) -sample else sample
                if (absVal > limitValue) {
                    sample *= (limitValue / absVal)
                }
            }

            mData[f] = sample
        }
    }
}
