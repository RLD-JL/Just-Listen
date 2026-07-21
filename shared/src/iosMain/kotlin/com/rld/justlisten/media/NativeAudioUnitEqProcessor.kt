@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package com.rld.justlisten.media

import kotlinx.cinterop.*
import platform.AVFAudio.AVAudioUnitEQ
import platform.AVFAudio.AVAudioUnitEQFilterParameters
import platform.AVFAudio.AVAudioUnitEQFilterTypeParametric
import platform.AudioToolbox.*
import platform.CoreAudioTypes.*
import platform.Foundation.NSLog

internal class NativeAudioUnitEqProcessor {
    private val equalizer = AVAudioUnitEQ(numberOfBands = 5u)
    private val centerFrequencies = floatArrayOf(60f, 230f, 910f, 4_000f, 14_000f)

    private var sourceBufferList: CPointer<AudioBufferList>? = null
    private var prepared = false
    private var equalizerEnabled = false
    private var bandGains = FloatArray(centerFrequencies.size)
    private var sampleRate = 48_000f
    private var renderedFrames = 0.0
    private var normalizationEnabled = false

    init {
        equalizer.bands.forEachIndexed { index, value ->
            val band = value as? AVAudioUnitEQFilterParameters ?: return@forEachIndexed
            band.filterType = AVAudioUnitEQFilterTypeParametric
            band.frequency = centerFrequencies.getOrElse(index) { 1_000f }
            band.bandwidth = 1.0f
            band.gain = 0.0f
            band.bypass = true
        }
        equalizer.globalGain = 0.0f
        // Keep the effect itself active. Individual bands implement EQ bypass so
        // a disabled EQ can later be enabled without rebuilding the audio unit.
        equalizer.bypass = false
    }

    fun update(enabled: Boolean, gains: List<Float>, normalizeVolume: Boolean) {
        equalizerEnabled = enabled
        bandGains = FloatArray(centerFrequencies.size) { index ->
            gains.getOrElse(index) { 0.0f }.coerceIn(-24.0f, 24.0f)
        }
        normalizationEnabled = normalizeVolume
        if (prepared) applyParameters()
    }

    fun prepare(
        stableRefPointer: COpaquePointer,
        maximumFrames: Long,
        processingFormat: CPointer<AudioStreamBasicDescription>,
    ): Boolean {
        unprepare()

        val audioUnit = equalizer.audioUnit
        val formatSize = sizeOf<AudioStreamBasicDescription>().toUInt()
        var status = AudioUnitSetProperty(
            audioUnit,
            kAudioUnitProperty_StreamFormat,
            kAudioUnitScope_Input,
            0u,
            processingFormat,
            formatSize,
        )
        if (status != 0) return logPrepareFailure("input format", status)

        status = AudioUnitSetProperty(
            audioUnit,
            kAudioUnitProperty_StreamFormat,
            kAudioUnitScope_Output,
            0u,
            processingFormat,
            formatSize,
        )
        if (status != 0) return logPrepareFailure("output format", status)

        memScoped {
            val maxFrames = alloc<UIntVar>()
            maxFrames.value = maximumFrames.coerceAtMost(UInt.MAX_VALUE.toLong()).toUInt()
            status = AudioUnitSetProperty(
                audioUnit,
                kAudioUnitProperty_MaximumFramesPerSlice,
                kAudioUnitScope_Global,
                0u,
                maxFrames.ptr,
                sizeOf<UIntVar>().toUInt(),
            )
            if (status != 0) return logPrepareFailure("maximum frames", status)

            val callback = alloc<AURenderCallbackStruct>()
            callback.inputProc = nativeEqInputCallback
            callback.inputProcRefCon = stableRefPointer
            status = AudioUnitSetProperty(
                audioUnit,
                kAudioUnitProperty_SetRenderCallback,
                kAudioUnitScope_Input,
                0u,
                callback.ptr,
                sizeOf<AURenderCallbackStruct>().toUInt(),
            )
            if (status != 0) return logPrepareFailure("render callback", status)
        }

        status = AudioUnitInitialize(audioUnit)
        if (status != 0) return logPrepareFailure("initialization", status)

        sampleRate = processingFormat.pointed.mSampleRate.toFloat()
        renderedFrames = 0.0
        prepared = true
        applyParameters()
        return true
    }

    fun unprepare() {
        if (prepared) {
            AudioUnitUninitialize(equalizer.audioUnit)
            prepared = false
        }
        sourceBufferList = null
    }

    fun process(bufferList: CPointer<AudioBufferList>, frameCount: Long): Int {
        if (!prepared) return 0

        sourceBufferList = bufferList
        val renderStatus = memScoped {
            val actionFlags = alloc<AudioUnitRenderActionFlagsVar>()
            actionFlags.value = 0u
            val timestamp = alloc<AudioTimeStamp>()
            timestamp.mSampleTime = renderedFrames
            timestamp.mHostTime = 0uL
            timestamp.mRateScalar = 0.0
            timestamp.mWordClockTime = 0uL
            timestamp.mSMPTETime.mSubframes = 0
            timestamp.mSMPTETime.mSubframeDivisor = 0
            timestamp.mSMPTETime.mCounter = 0u
            timestamp.mSMPTETime.mType = 0u
            timestamp.mSMPTETime.mFlags = 0u
            timestamp.mReserved = 0u
            timestamp.mFlags = kAudioTimeStampSampleTimeValid

            AudioUnitRender(
                equalizer.audioUnit,
                actionFlags.ptr,
                timestamp.ptr,
                0u,
                frameCount.coerceAtMost(UInt.MAX_VALUE.toLong()).toUInt(),
                bufferList,
            )
        }
        sourceBufferList = null

        if (renderStatus == 0) renderedFrames += frameCount
        if (renderStatus == 0 && normalizationEnabled) {
            limitAudio(bufferList, frameCount)
        }
        return renderStatus
    }

    fun provideInput(ioData: CPointer<AudioBufferList>?): Int {
        val source = sourceBufferList ?: return kAudioUnitErr_NoConnection
        val destination = ioData ?: return kAudioUnitErr_InvalidPropertyValue
        val bufferCount = minOf(
            source.pointed.mNumberBuffers.toInt(),
            destination.pointed.mNumberBuffers.toInt(),
        )

        for (index in 0 until bufferCount) {
            val sourceBuffer = source.pointed.mBuffers[index]
            val destinationBuffer = destination.pointed.mBuffers[index]
            destinationBuffer.mNumberChannels = sourceBuffer.mNumberChannels
            destinationBuffer.mDataByteSize = sourceBuffer.mDataByteSize
            destinationBuffer.mData = sourceBuffer.mData
        }
        return 0
    }

    internal fun parameterValue(parameter: UInt): Float? = memScoped {
        val value = alloc<FloatVar>()
        val status = AudioUnitGetParameter(
            equalizer.audioUnit,
            parameter,
            kAudioUnitScope_Global,
            0u,
            value.ptr,
        )
        if (status == 0) value.value else null
    }

    private fun limitAudio(bufferList: CPointer<AudioBufferList>, frameCount: Long) {
        val buffers = bufferList.pointed
        for (bufferIndex in 0 until buffers.mNumberBuffers.toInt()) {
            val buffer = buffers.mBuffers[bufferIndex]
            val channels = buffer.mNumberChannels.toInt()
            val samples = buffer.mData?.reinterpret<FloatVar>() ?: continue
            if (channels <= 0) continue
            val availableFrames = buffer.mDataByteSize.toInt() / (Float.SIZE_BYTES * channels)
            val frames = minOf(frameCount.toInt(), availableFrames)
            for (sampleIndex in 0 until frames * channels) {
                samples[sampleIndex] = samples[sampleIndex].coerceIn(-0.5f, 0.5f)
            }
        }
    }

    /**
     * AVAudioUnitEQ's Objective-C band properties do not propagate after its
     * underlying v2 Audio Unit has been initialized manually. These parameters
     * are the real-time-safe control surface used by the running audio unit.
     */
    private fun applyParameters() {
        val audioUnit = equalizer.audioUnit
        setParameter(audioUnit, kAUNBandEQParam_GlobalGain, 0.0f)
        centerFrequencies.indices.forEach { index ->
            val parameterOffset = index.toUInt()
            setParameter(
                audioUnit,
                kAUNBandEQParam_FilterType + parameterOffset,
                kAUNBandEQFilterType_Parametric.toFloat(),
            )
            setParameter(
                audioUnit,
                kAUNBandEQParam_Frequency + parameterOffset,
                centerFrequencies[index].coerceAtMost(sampleRate * 0.45f),
            )
            setParameter(
                audioUnit,
                kAUNBandEQParam_Gain + parameterOffset,
                bandGains[index],
            )
            setParameter(
                audioUnit,
                kAUNBandEQParam_Bandwidth + parameterOffset,
                1.0f,
            )
            setParameter(
                audioUnit,
                kAUNBandEQParam_BypassBand + parameterOffset,
                if (equalizerEnabled) 0.0f else 1.0f,
            )
        }
    }

    private fun setParameter(audioUnit: AudioUnit?, parameter: UInt, value: Float) {
        val status = AudioUnitSetParameter(
            audioUnit,
            parameter,
            kAudioUnitScope_Global,
            0u,
            value,
            0u,
        )
        if (status != 0) {
            NSLog("JustListen AVAudioUnitEQ parameter $parameter failed: OSStatus=$status")
        }
    }

    private fun logPrepareFailure(step: String, status: Int): Boolean {
        NSLog("JustListen AVAudioUnitEQ failed during $step: OSStatus=$status")
        return false
    }
}

private val nativeEqInputCallback = staticCFunction {
        refCon: COpaquePointer?,
        _: CPointer<AudioUnitRenderActionFlagsVar>?,
        _: CPointer<AudioTimeStamp>?,
        _: UInt,
        _: UInt,
        ioData: CPointer<AudioBufferList>?,
    ->
    refCon?.asStableRef<NativeAudioUnitEqProcessor>()?.get()?.provideInput(ioData)
        ?: kAudioUnitErr_NoConnection
}
