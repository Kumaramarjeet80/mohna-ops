package com.mohna.ops.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * Real-time Sound Synthesis using AudioTrack
 * Generates exact tone frequencies directly from math sine functions
 * matching the HTML5 Web Audio API implementation in mgrrider.html.
 */
object SoundManager {

    private const val SAMPLE_RATE = 44100
    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * High-pitch success chime
     * Frequency sweeps smoothly from 587.33 Hz (D5) to 880 Hz (A5)
     */
    fun playSuccess() {
        scope.launch {
            try {
                val durationSec = 0.22
                val numSamples = (durationSec * SAMPLE_RATE).toInt()
                val buffer = ShortArray(numSamples)

                val startFreq = 587.33
                val endFreq = 880.0
                var currentPhase = 0.0

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / numSamples
                    val freq = startFreq + (endFreq - startFreq) * t
                    currentPhase += 2.0 * PI * freq / SAMPLE_RATE

                    // Exponential decay envelope
                    val envelope = exp(-3.2 * t)
                    val sample = (sin(currentPhase) * envelope * Short.MAX_VALUE * 0.45).toInt()
                    buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                playPcmBuffer(buffer)
            } catch (_: Exception) {}
        }
    }

    /**
     * Low-pitch error buzzer
     * Frequency sweeps downwards from 220 Hz (A3) to 110 Hz (A2)
     */
    fun playError() {
        scope.launch {
            try {
                val durationSec = 0.28
                val numSamples = (durationSec * SAMPLE_RATE).toInt()
                val buffer = ShortArray(numSamples)

                val startFreq = 220.0
                val endFreq = 110.0
                var currentPhase = 0.0

                for (i in 0 until numSamples) {
                    val t = i.toDouble() / numSamples
                    val freq = startFreq - (startFreq - endFreq) * t
                    currentPhase += 2.0 * PI * freq / SAMPLE_RATE

                    // Decay envelope with slight buzzing harmonic
                    val envelope = exp(-2.8 * t)
                    val wave = sin(currentPhase) + 0.3 * sin(currentPhase * 3)
                    val sample = (wave * envelope * Short.MAX_VALUE * 0.4).toInt()
                    buffer[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                playPcmBuffer(buffer)
            } catch (_: Exception) {}
        }
    }

    private fun playPcmBuffer(buffer: ShortArray) {
        val audioTrack = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(buffer.size * 2)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        audioTrack.write(buffer, 0, buffer.size)
        audioTrack.play()

        // Release after playback completes
        scope.launch {
            kotlinx.coroutines.delay(400)
            try {
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {}
        }
    }
}
