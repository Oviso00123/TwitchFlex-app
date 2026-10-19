package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.ToneGenerator
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.sin

enum class SoundEffect(val displayName: String, val emoji: String) {
    AIRHORN("Airhorn", "🎺"),
    SAD_TROMBONE("Tromba Triste", "🎷"),
    SAD_HORN("Tromba Triste", "🎷"),
    LAUGH("Risata", "😂"),
    HYPE_SIREN("Sirena Hype", "🚨"),
    VICTORY("GG Fanfara", "👑"),
    APPLAUSE("Applausi", "👏"),
    GG("GG Vittoria", "🏆")
}

class SoundboardManager(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    init {
        try {
            tts = TextToSpeech(context, this)
        } catch (_: Exception) {}
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.ITALIAN)
            isTtsReady = result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED
            tts?.setPitch(1.05f)
            tts?.setSpeechRate(1.0f)
        }
    }

    fun speakDonationTts(donorName: String, amountText: String, message: String) {
        if (!isTtsReady || tts == null) return
        val speechText = "$donorName dona $amountText: $message"
        try {
            tts?.speak(speechText, TextToSpeech.QUEUE_ADD, null, "donation_${System.currentTimeMillis()}")
        } catch (_: Exception) {}
    }

    /**
     * Speaks donation message with a customized robotic, metallic tone for streamer donation alerts.
     */
    fun speakRoboticDonationTts(donorName: String, amountText: String, message: String) {
        if (!isTtsReady || tts == null) return
        try {
            // Robotic lower pitch & steady cadence
            tts?.setPitch(0.72f)
            tts?.setSpeechRate(0.92f)
            val speechText = "Nuova donazione da $donorName: $amountText. $message"
            tts?.speak(speechText, TextToSpeech.QUEUE_ADD, null, "robotic_donation_${System.currentTimeMillis()}")
            // Reset to normal pitch after queueing
            tts?.setPitch(1.05f)
            tts?.setSpeechRate(1.0f)
        } catch (_: Exception) {}
    }

    fun play(effect: SoundEffect, scope: CoroutineScope? = null) {
        if (scope != null) {
            playSoundEffect(effect, scope)
        } else {
            CoroutineScope(Dispatchers.Default).launch {
                executeSound(effect)
            }
        }
    }

    fun playSoundEffect(effect: SoundEffect, scope: CoroutineScope) {
        scope.launch(Dispatchers.Default) {
            executeSound(effect)
        }
    }

    private fun executeSound(effect: SoundEffect) {
        when (effect) {
            SoundEffect.AIRHORN -> playAirhornTone()
            SoundEffect.SAD_TROMBONE, SoundEffect.SAD_HORN -> playSadTromboneTone()
            SoundEffect.LAUGH -> playLaughTone()
            SoundEffect.HYPE_SIREN -> playHypeSirenTone()
            SoundEffect.VICTORY, SoundEffect.GG, SoundEffect.APPLAUSE -> playVictoryFanfareTone()
        }
    }

    private fun playAirhornTone() {
        val sampleRate = 44100
        val freqList = listOf(466, 466, 466, 466, 466, 554, 466)
        val durationMs = 90

        freqList.forEach { freq ->
            playSineTone(freq, durationMs, sampleRate, 0.9f)
            Thread.sleep(15)
        }
    }

    private fun playSadTromboneTone() {
        val sampleRate = 44100
        val notes = listOf(392, 370, 349, 311)
        notes.forEachIndexed { i, freq ->
            val dur = if (i == notes.size - 1) 400 else 180
            playSineTone(freq, dur, sampleRate, 0.7f)
            Thread.sleep(30)
        }
    }

    private fun playLaughTone() {
        val sampleRate = 44100
        for (i in 0 until 5) {
            playSineTone(587, 60, sampleRate, 0.8f)
            playSineTone(659, 60, sampleRate, 0.8f)
            Thread.sleep(40)
        }
    }

    private fun playHypeSirenTone() {
        val sampleRate = 44100
        for (sweep in 0 until 2) {
            for (f in 400..900 step 50) {
                playSineTone(f, 20, sampleRate, 0.7f)
            }
            for (f in 900 downTo 400 step 50) {
                playSineTone(f, 20, sampleRate, 0.7f)
            }
        }
    }

    private fun playVictoryFanfareTone() {
        val sampleRate = 44100
        val notes = listOf(523, 659, 783, 1046)
        notes.forEachIndexed { i, freq ->
            val dur = if (i == notes.size - 1) 350 else 120
            playSineTone(freq, dur, sampleRate, 0.85f)
            Thread.sleep(20)
        }
    }

    private fun playSineTone(frequency: Int, durationMs: Int, sampleRate: Int, volume: Float) {
        val numSamples = (durationMs * sampleRate) / 1000
        val generatedSnd = ByteArray(2 * numSamples)
        var idx = 0

        for (i in 0 until numSamples) {
            val dVal = sin(2.0 * Math.PI * i / (sampleRate / frequency))
            val valShort = (dVal * 32767 * volume).toInt().coerceIn(-32768, 32767).toShort()
            generatedSnd[idx++] = (valShort.toInt() and 0x00ff).toByte()
            generatedSnd[idx++] = ((valShort.toInt() and 0xff00) ushr 8).toByte()
        }

        try {
            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(generatedSnd.size)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(generatedSnd, 0, generatedSnd.size)
            track.play()
            Thread.sleep(durationMs.toLong())
            track.stop()
            track.release()
        } catch (_: Exception) {}
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
        } catch (_: Exception) {}
    }
}
