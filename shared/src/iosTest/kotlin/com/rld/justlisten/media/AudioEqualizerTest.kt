package com.rld.justlisten.media

import kotlinx.cinterop.*
import kotlin.math.sin
import kotlin.math.PI
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertEquals

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class AudioEqualizerTest {

    private fun generateSineWave(frequency: Float, sampleRate: Float, numSamples: Int, amplitude: Float = 0.2f): FloatArray {
        val signal = FloatArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toFloat() / sampleRate
            signal[i] = amplitude * sin(2.0f * PI.toFloat() * frequency * t)
        }
        return signal
    }

    private fun calculateRMS(samples: FloatArray): Float {
        var sum = 0.0f
        for (s in samples) {
            sum += s * s
        }
        return sqrt(sum / samples.size)
    }

    @Test
    fun testBypassWhenDisabled() {
        val sampleRate = 44100f
        val numSamples = 1000
        val centerFreq = 910f // Band 2 (index 2) is 910Hz
        
        val inputSignal = generateSineWave(centerFreq, sampleRate, numSamples)
        
        memScoped {
            val state = nativeHeap.allocArray<FloatVar>(74)
            // Initialize storage
            state[0] = 0.0f  // isEqEnabled = false
            state[71] = sampleRate
            state[72] = 1.0f // recalculate coefficients
            state[73] = 0.0f // isNormEnabled = false
            
            // Set 12dB boost on band 2
            val bandOffset = 1 + 2 * 14
            state[bandOffset + 0] = 12.0f
            
            computeCoefficients(state)
            
            val buffer = nativeHeap.allocArray<FloatVar>(numSamples * 2)
            // Fill stereo buffer
            for (i in 0 until numSamples) {
                buffer[i * 2] = inputSignal[i]
                buffer[i * 2 + 1] = inputSignal[i]
            }
            
            processAudioSamples(state, buffer, numSamples, channels = 2)
            
            // Verify output is exactly identical to input because EQ is disabled
            for (i in 0 until numSamples) {
                assertEquals(inputSignal[i], buffer[i * 2], 1e-5f)
                assertEquals(inputSignal[i], buffer[i * 2 + 1], 1e-5f)
            }
            
            nativeHeap.free(buffer)
            nativeHeap.free(state)
        }
    }

    @Test
    fun testEqBoostAndCut() {
        val sampleRate = 44100f
        val numSamples = 1000
        val centerFreq = 910f // Band 2 is 910Hz
        
        val inputSignal = generateSineWave(centerFreq, sampleRate, numSamples)
        val inputRMS = calculateRMS(inputSignal)

        // 1. Test Boost
        memScoped {
            val state = nativeHeap.allocArray<FloatVar>(74)
            state[0] = 1.0f  // isEqEnabled = true
            state[71] = sampleRate
            state[72] = 1.0f // recalculate coefficients
            state[73] = 0.0f // isNormEnabled = false
            
            // Boost Band 2 by +12dB
            val bandOffset = 1 + 2 * 14
            state[bandOffset + 0] = 12.0f
            
            computeCoefficients(state)
            
            val buffer = nativeHeap.allocArray<FloatVar>(numSamples)
            // Fill mono buffer
            for (i in 0 until numSamples) {
                buffer[i] = inputSignal[i]
            }
            
            processAudioSamples(state, buffer, numSamples, channels = 1)
            
            val outputSamples = FloatArray(numSamples)
            for (i in 0 until numSamples) {
                outputSamples[i] = buffer[i]
            }
            
            val outputRMS = calculateRMS(outputSamples)
            
            // RMS of output must be strictly greater than input RMS
            assertTrue(outputRMS > inputRMS, "Boost of +12dB should increase output RMS. Input: $inputRMS, Output: $outputRMS")
            
            nativeHeap.free(buffer)
            nativeHeap.free(state)
        }

        // 2. Test Cut
        memScoped {
            val state = nativeHeap.allocArray<FloatVar>(74)
            state[0] = 1.0f  // isEqEnabled = true
            state[71] = sampleRate
            state[72] = 1.0f // recalculate coefficients
            state[73] = 0.0f // isNormEnabled = false
            
            // Cut Band 2 by -12dB
            val bandOffset = 1 + 2 * 14
            state[bandOffset + 0] = -12.0f
            
            computeCoefficients(state)
            
            val buffer = nativeHeap.allocArray<FloatVar>(numSamples)
            // Fill mono buffer
            for (i in 0 until numSamples) {
                buffer[i] = inputSignal[i]
            }
            
            processAudioSamples(state, buffer, numSamples, channels = 1)
            
            val outputSamples = FloatArray(numSamples)
            for (i in 0 until numSamples) {
                outputSamples[i] = buffer[i]
            }
            
            val outputRMS = calculateRMS(outputSamples)
            
            // RMS of output must be strictly less than input RMS
            assertTrue(outputRMS < inputRMS, "Cut of -12dB should decrease output RMS. Input: $inputRMS, Output: $outputRMS")
            
            nativeHeap.free(buffer)
            nativeHeap.free(state)
        }
    }

    @Test
    fun testVolumeNormalization() {
        val sampleRate = 44100f
        val numSamples = 500
        
        // Large input signal exceeding the 0.5f threshold
        val inputSignal = generateSineWave(100f, sampleRate, numSamples, amplitude = 0.9f)
        
        memScoped {
            val state = nativeHeap.allocArray<FloatVar>(74)
            state[0] = 0.0f  // isEqEnabled = false
            state[71] = sampleRate
            state[72] = 1.0f
            state[73] = 1.0f // isNormEnabled = true
            
            computeCoefficients(state)
            
            val buffer = nativeHeap.allocArray<FloatVar>(numSamples)
            for (i in 0 until numSamples) {
                buffer[i] = inputSignal[i]
            }
            
            processAudioSamples(state, buffer, numSamples, channels = 1)
            
            // Verify no sample exceeds 0.5f in magnitude
            for (i in 0 until numSamples) {
                val absVal = if (buffer[i] < 0f) -buffer[i] else buffer[i]
                assertTrue(absVal <= 0.5f, "Sample $i magnitude exceeds 0.5f limit: $absVal")
            }
            
            nativeHeap.free(buffer)
            nativeHeap.free(state)
        }
    }
}
