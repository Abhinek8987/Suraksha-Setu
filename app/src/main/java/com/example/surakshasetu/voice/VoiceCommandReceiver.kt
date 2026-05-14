package com.example.surakshasetu.voice

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log

class VoiceCommandReceiver(private val context: Context, private val onEmergencyDetected: () -> Unit) {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val emergencyKeywords = listOf("help", "save me", "emergency")

    fun startListening() {
        if (isListening) return

        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            muteBeepSound()

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            }

            speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    unmuteBeepSound()
                }
                override fun onBeginningOfSpeech() {}
                override fun onRmsChanged(rmsdB: Float) {}
                override fun onBufferReceived(buffer: ByteArray?) {}
                override fun onEndOfSpeech() {
                    restartListening()
                }

                override fun onError(error: Int) {
                    Log.e("VoiceCommandReceiver", "Error: $error")
                    restartListening()
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    matches?.let {
                        for (match in it) {
                            checkKeyword(match)
                        }
                    }
                    restartListening()
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    matches?.let {
                        for (match in it) {
                            checkKeyword(match)
                        }
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            speechRecognizer?.startListening(intent)
            isListening = true
        } else {
            Log.e("VoiceCommandReceiver", "Speech recognition not available")
        }
    }

    private fun checkKeyword(spokenText: String) {
        val lowerText = spokenText.lowercase()
        for (keyword in emergencyKeywords) {
            if (lowerText.contains(keyword)) {
                Log.d("VoiceCommandReceiver", "Emergency keyword detected: $keyword")
                onEmergencyDetected()
                break
            }
        }
    }

    private fun restartListening() {
        isListening = false
        speechRecognizer?.destroy()
        startListening()
    }

    fun stopListening() {
        isListening = false
        speechRecognizer?.stopListening()
        speechRecognizer?.destroy()
        unmuteBeepSound()
    }

    private fun muteBeepSound() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0)
        } else {
            @Suppress("DEPRECATION")
            audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true)
        }
    }

    private fun unmuteBeepSound() {
        // Small delay to ensure the beep has passed before unmuting
        Handler(Looper.getMainLooper()).postDelayed({
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val isMuted = audioManager.isStreamMute(AudioManager.STREAM_MUSIC)
                if (isMuted) {
                    audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0)
                }
            } else {
                @Suppress("DEPRECATION")
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, false)
            }
        }, 500)
    }
}
