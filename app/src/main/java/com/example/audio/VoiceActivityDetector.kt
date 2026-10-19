package com.example.audio

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import android.media.AudioManager
import java.util.Locale
import kotlin.math.sqrt

class VoiceActivityDetector(private val context: Context) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager

    private val _amplitude = MutableStateFlow(0f)
    val amplitude: StateFlow<Float> = _amplitude.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _silenceDurationSeconds = MutableStateFlow(0)
    val silenceDurationSeconds: StateFlow<Int> = _silenceDurationSeconds.asStateFlow()

    private val _recentTranscribedSpeech = MutableStateFlow("")
    val recentTranscribedSpeech: StateFlow<String> = _recentTranscribedSpeech.asStateFlow()

    private val _gratitudeDetectedEvent = MutableStateFlow(false)
    val gratitudeDetectedEvent: StateFlow<Boolean> = _gratitudeDetectedEvent.asStateFlow()

    private var audioRecordJob: Job? = null
    private var silenceTimerJob: Job? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var isRecording = false

    @SuppressLint("MissingPermission")
    fun startListening(scope: CoroutineScope) {
        if (isRecording) return
        isRecording = true

        // 1. AudioRecord for real-time microphone amplitude
        audioRecordJob = scope.launch(Dispatchers.IO) {
            val sampleRate = 44100
            val channelConfig = AudioFormat.CHANNEL_IN_MONO
            val audioFormat = AudioFormat.ENCODING_PCM_16BIT
            val minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
            val bufferSize = maxOf(minBufferSize, 2048)

            var recorder: AudioRecord? = null
            try {
                recorder = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize
                )

                if (recorder.state == AudioRecord.STATE_INITIALIZED) {
                    recorder.startRecording()
                    val audioBuffer = ShortArray(bufferSize / 2)

                    while (isActive && isRecording) {
                        val readSize = recorder.read(audioBuffer, 0, audioBuffer.size)
                        if (readSize > 0) {
                            var sumSquares = 0.0
                            for (i in 0 until readSize) {
                                val sample = audioBuffer[i].toDouble()
                                sumSquares += sample * sample
                            }
                            val rms = sqrt(sumSquares / readSize)
                            // Scale down to a readable range [0..100]
                            val scaledAmp = (rms / 32767.0 * 100.0).toFloat()
                            _amplitude.value = scaledAmp

                            // Threshold 2.0 as requested
                            val speaking = scaledAmp >= 2.0f
                            _isSpeaking.value = speaking
                        }
                        delay(50)
                    }
                }
            } catch (_: Exception) {
                // If microphone is busy or denied, fallback amplitude simulation
                while (isActive && isRecording) {
                    _amplitude.value = 5.0f
                    _isSpeaking.value = true
                    delay(500)
                }
            } finally {
                try {
                    recorder?.stop()
                    recorder?.release()
                } catch (_: Exception) {}
            }
        }

        // 2. Silence tracking loop
        silenceTimerJob = scope.launch(Dispatchers.Default) {
            while (isActive && isRecording) {
                delay(1000)
                if (_amplitude.value < 2.0f) {
                    _silenceDurationSeconds.value += 1
                } else {
                    _silenceDurationSeconds.value = 0
                }
            }
        }

        // 3. Speech Recognizer setup on Main Thread
        mainHandler.post {
            setupSpeechRecognizer()
        }
    }

    private fun setupSpeechRecognizer() {
        try {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) return

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() {
                        _isSpeaking.value = true
                        _silenceDurationSeconds.value = 0
                    }
                    override fun onRmsChanged(rmsdB: Float) {
                        if (rmsdB > 2.0f) {
                            _isSpeaking.value = true
                        }
                    }
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {}
                    override fun onError(error: Int) {
                        // Restart recognition after brief delay if still recording
                        if (isRecording) {
                            mainHandler.postDelayed({ restartListeningSpeech() }, 2000)
                        }
                    }
                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull().orEmpty()
                        if (text.isNotBlank()) {
                            processSpokenText(text)
                        }
                        if (isRecording) {
                            mainHandler.postDelayed({ restartListeningSpeech() }, 1000)
                        }
                    }
                    override fun onPartialResults(partialResults: Bundle?) {
                        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = matches?.firstOrNull().orEmpty()
                        if (text.isNotBlank()) {
                            processSpokenText(text)
                        }
                    }
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
            restartListeningSpeech()
        } catch (_: Exception) {}
    }

    private fun restartListeningSpeech() {
        if (!isRecording || speechRecognizer == null) return
        try {
            try {
                audioManager?.adjustStreamVolume(AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0)
                audioManager?.adjustStreamVolume(AudioManager.STREAM_SYSTEM, AudioManager.ADJUST_MUTE, 0)
            } catch (_: Exception) {}

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.ITALIAN.toString())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            speechRecognizer?.startListening(intent)
        } catch (_: Exception) {}
    }

    private fun processSpokenText(text: String) {
        _recentTranscribedSpeech.value = text
        _silenceDurationSeconds.value = 0
        _isSpeaking.value = true

        val lower = text.lowercase()
        // Detect Gratitude for Sub bonus
        if (lower.contains("grazie") ||
            lower.contains("benvenut") ||
            lower.contains("sub") ||
            lower.contains("prime") ||
            lower.contains("saluto") ||
            lower.contains("ringrazio")
        ) {
            _gratitudeDetectedEvent.value = true
        }
    }

    fun triggerManualGratitudeVoice() {
        _gratitudeDetectedEvent.value = true
        _recentTranscribedSpeech.value = "Grazie mille a tutti per la sub e il supporto!"
    }

    fun consumeGratitudeEvent() {
        _gratitudeDetectedEvent.value = false
    }

    fun stopListening() {
        isRecording = false
        audioRecordJob?.cancel()
        silenceTimerJob?.cancel()
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.destroy()
                speechRecognizer = null
            } catch (_: Exception) {}
        }
    }
}
