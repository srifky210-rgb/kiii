package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.sin

enum class ToneWave {
    SINE, TRIANGLE, SQUARE, SAWTOOTH
}

class AudioCoachManager(context: Context) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Volatile
    var isSoundEnabled: Boolean = true

    private var tts: TextToSpeech? = null
    @Volatile
    private var isTtsReady = false

    init {
        try {
            tts = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val indonesian = Locale("id", "ID")
                    val res = tts?.setLanguage(indonesian)
                    if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                        tts?.language = Locale.getDefault()
                    }
                    tts?.setSpeechRate(0.95f)
                    isTtsReady = true
                }
            }
        } catch (e: Exception) {
            Log.w("AudioCoachManager", "TTS init failed: ${e.message}")
        }
    }

    fun speak(text: String) {
        if (!isSoundEnabled || !isTtsReady || text.isBlank()) return
        try {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "pulserun_${System.currentTimeMillis()}")
        } catch (e: Exception) {
            Log.w("AudioCoachManager", "TTS speak failed: ${e.message}")
        }
    }

    fun stopSpeaking() {
        try {
            tts?.stop()
        } catch (_: Exception) {
        }
    }

    fun playBeat(isAccent: Boolean) {
        playTone(
            freqHz = if (isAccent) 230.0 else 150.0,
            durationSec = 0.10,
            wave = ToneWave.TRIANGLE,
            volume = 0.25f
        )
    }

    fun playCountdownBeep() {
        playTone(freqHz = 880.0, durationSec = 0.10, wave = ToneWave.SQUARE, volume = 0.08f)
    }

    fun sfxGood() {
        playTone(660.0, 0.15, ToneWave.SINE, 0.20f, delayMs = 0L)
        playTone(880.0, 0.25, ToneWave.SINE, 0.20f, delayMs = 150L)
    }

    fun sfxBad() {
        playTone(150.0, 0.35, ToneWave.SAWTOOTH, 0.12f, delayMs = 0L)
    }

    fun sfxFanfare() {
        val notes = listOf(523.25, 659.25, 783.99, 1046.50)
        notes.forEachIndexed { index, freq ->
            playTone(freq, 0.30, ToneWave.TRIANGLE, 0.20f, delayMs = index * 150L)
        }
    }

    fun vibrateShort() {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(45L, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(45L)
                }
            }
        } catch (_: Exception) {
        }
    }

    fun playTone(
        freqHz: Double,
        durationSec: Double,
        wave: ToneWave = ToneWave.SINE,
        volume: Float = 0.15f,
        delayMs: Long = 0L
    ) {
        if (!isSoundEnabled) return
        scope.launch {
            if (delayMs > 0) delay(delayMs)
            if (!isSoundEnabled) return@launch
            try {
                val sampleRate = 22050
                val numSamples = (durationSec * sampleRate).toInt().coerceAtLeast(1)
                val samples = ShortArray(numSamples)
                for (i in 0 until numSamples) {
                    val t = i.toDouble() / sampleRate
                    val phase = (t * freqHz) % 1.0
                    val raw = when (wave) {
                        ToneWave.SINE -> sin(2.0 * PI * phase)
                        ToneWave.TRIANGLE -> 2.0 * abs(2.0 * phase - 1.0) - 1.0
                        ToneWave.SQUARE -> if (phase < 0.5) 1.0 else -1.0
                        ToneWave.SAWTOOTH -> 2.0 * phase - 1.0
                    }
                    val envelope = exp(-3.5 * (i.toDouble() / numSamples))
                    val scaled = (raw * envelope * volume * Short.MAX_VALUE).toInt()
                        .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                    samples[i] = scaled.toShort()
                }
                val bufferBytes = numSamples * 2
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .setBufferSizeInBytes(bufferBytes)
                    .build()

                audioTrack.write(samples, 0, numSamples)
                audioTrack.play()
                delay((durationSec * 1000).toLong() + 60L)
                audioTrack.stop()
                audioTrack.release()
            } catch (e: Exception) {
                Log.w("AudioCoachManager", "Tone playback skipped: ${e.message}")
            }
        }
    }

    fun release() {
        stopSpeaking()
        try {
            tts?.shutdown()
        } catch (_: Exception) {
        }
        scope.cancel()
    }
}
