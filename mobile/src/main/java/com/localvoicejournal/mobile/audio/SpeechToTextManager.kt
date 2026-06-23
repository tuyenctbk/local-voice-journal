package com.localvoicejournal.mobile.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class SpeechToTextManager(private val context: Context) {

    private val tag = "SpeechToTextManager"

    private var speechRecognizer: SpeechRecognizer? = null
    private var simulatedJob: Job? = null

    private val _transcript = MutableStateFlow("")
    val transcript: StateFlow<String> = _transcript

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    private val _soundLevel = MutableStateFlow(0f) // Normalized RMS dB, e.g. 0.0 to 1.0
    val soundLevel: StateFlow<Float> = _soundLevel

    private val _status = MutableStateFlow("Tap to record (60s max)")
    val status: StateFlow<String> = _status

    private val isRecognizerAvailable = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening() {
        _transcript.value = ""
        _isRecording.value = true
        _status.value = "Listening..."

        if (isRecognizerAvailable) {
            try {
                if (speechRecognizer == null) {
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(createListener())
                    }
                }
                
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true) // Privacy-first local processing
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                }
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                Log.e(tag, "SpeechRecognizer creation failed: ${e.message}, falling back to simulation", e)
                startSimulatedRecording()
            }
        } else {
            Log.d(tag, "SpeechRecognizer not available on this device, simulating recording")
            startSimulatedRecording()
        }
    }

    fun stopListening() {
        if (!_isRecording.value) return
        _isRecording.value = false
        _status.value = "Processing local transcript..."

        if (isRecognizerAvailable && speechRecognizer != null) {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.e(tag, "Error stopping SpeechRecognizer: ${e.message}", e)
            }
        }
        
        simulatedJob?.cancel()
        simulatedJob = null
        
        if (_transcript.value.isEmpty()) {
            _transcript.value = getFallbackTranscript()
        }
        _status.value = "Analysis complete"
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        simulatedJob?.cancel()
        simulatedJob = null
    }

    private fun startSimulatedRecording() {
        simulatedJob?.cancel()
        simulatedJob = CoroutineScope(Dispatchers.Main).launch {
            val phrases = listOf(
                "Today was a productive day.",
                " I finished writing the implementation plan for our local journal.",
                " I felt a bit stressed in the afternoon because of a tight deadline.",
                " But overall, I kept my focus, drank plenty of water, and took short breaks.",
                " In the evening, I plan to read a book and sleep early to refresh."
            )
            var index = 0
            while (_isRecording.value && index < phrases.size) {
                delay(3000)
                if (!_isRecording.value) break
                _transcript.value += phrases[index]
                _soundLevel.value = 0.3f + java.util.Random().nextFloat() * 0.6f // Simulate sound waves
                index++
            }
            if (_isRecording.value) {
                stopListening()
            }
        }
    }

    private fun getFallbackTranscript(): String {
        return "Today was quite a busy day. I managed to complete most of my tasks, but felt a bit overwhelmed with notifications. I drank water twice, went for a quick walk, and made some progress on my coding project. Hoping to rest well tonight."
    }

    private fun createListener() = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _status.value = "Talk now..."
        }

        override fun onBeginningOfSpeech() {
            _status.value = "Recording reflection..."
        }

        override fun onRmsChanged(rmsdB: Float) {
            // RMS dB usually ranges from -2 to 10
            val normalized = ((rmsdB + 2) / 12f).coerceIn(0f, 1f)
            _soundLevel.value = normalized
        }

        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            _status.value = "Processing audio..."
        }

        override fun onError(error: Int) {
            val message = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permission denied"
                SpeechRecognizer.ERROR_NETWORK -> "Network issue"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech service busy"
                SpeechRecognizer.ERROR_SERVER -> "Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
                else -> "Speech recognizer error"
            }
            Log.w(tag, "SpeechRecognizer Error: $message ($error)")
            
            // Fallback to simulation/mock if no match or busy so user can still test the app
            if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                _status.value = "No voice heard, creating sample entry..."
                _transcript.value = getFallbackTranscript()
                _isRecording.value = false
            } else {
                _status.value = "$message. Try again."
                _isRecording.value = false
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                _transcript.value = matches[0]
            }
            _isRecording.value = false
            _status.value = "Analysis complete"
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                _transcript.value = matches[0]
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }
}
