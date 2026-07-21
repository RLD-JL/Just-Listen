@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package com.rld.justlisten.media

import kotlinx.cinterop.*
import kotlin.math.PI
import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import platform.AudioToolbox.kAUNBandEQParam_BypassBand
import platform.AudioToolbox.kAUNBandEQParam_Gain
import platform.CoreAudioTypes.*

class NativeAudioUnitEqProcessorTest {
    @Test
    fun nativeEqCanBeEnabledAndBoostedWhilePrepared() = memScoped {
        val frameCount = 16_384
        val processor = NativeAudioUnitEqProcessor()
        val processorRef = StableRef.create(processor)
        try {
            val format = alloc<AudioStreamBasicDescription>()
            format.mSampleRate = 48_000.0
            format.mFormatID = AUDIO_FORMAT_LINEAR_PCM
            format.mFormatFlags = AUDIO_FORMAT_FLAG_IS_FLOAT or 8u // packed
            format.mBytesPerPacket = 4u
            format.mFramesPerPacket = 1u
            format.mBytesPerFrame = 4u
            format.mChannelsPerFrame = 1u
            format.mBitsPerChannel = 32u
            format.mReserved = 0u

            processor.update(
                enabled = false,
                gains = listOf(0f, 0f, 0f, 0f, 0f),
                normalizeVolume = false,
            )
            assertTrue(processor.prepare(processorRef.asCPointer(), frameCount.toLong(), format.ptr))

            val samples = allocArray<FloatVar>(frameCount)
            for (index in 0 until frameCount) {
                samples[index] = (0.1 * sin(2.0 * PI * 60.0 * index / 48_000.0)).toFloat()
            }
            val bufferList = alloc<AudioBufferList>()
            bufferList.mNumberBuffers = 1u
            bufferList.mBuffers[0].mNumberChannels = 1u
            bufferList.mBuffers[0].mDataByteSize = (frameCount * Float.SIZE_BYTES).toUInt()
            bufferList.mBuffers[0].mData = samples

            assertEquals(0, processor.process(bufferList.ptr, frameCount.toLong()))
            val flatRms = rms(samples, frameCount, frameCount / 2)

            for (index in 0 until frameCount) {
                samples[index] = (0.1 * sin(2.0 * PI * 60.0 * index / 48_000.0)).toFloat()
            }
            processor.update(
                enabled = true,
                gains = listOf(12f, 0f, 0f, 0f, 0f),
                normalizeVolume = false,
            )
            assertEquals(12f, processor.parameterValue(kAUNBandEQParam_Gain))
            assertEquals(0f, processor.parameterValue(kAUNBandEQParam_BypassBand))
            assertEquals(0, processor.process(bufferList.ptr, frameCount.toLong()))
            val boostedRms = rms(samples, frameCount, frameCount / 2)
            assertTrue(
                boostedRms > flatRms * 2.0,
                "Expected live native EQ boost: flat=$flatRms boosted=$boostedRms",
            )
        } finally {
            processor.unprepare()
            processorRef.dispose()
        }
    }

    private fun rms(samples: CPointer<FloatVar>, size: Int, start: Int): Double {
        var sum = 0.0
        for (index in start until size) {
            val sample = samples[index].toDouble()
            sum += sample * sample
        }
        return kotlin.math.sqrt(sum / (size - start))
    }
}
