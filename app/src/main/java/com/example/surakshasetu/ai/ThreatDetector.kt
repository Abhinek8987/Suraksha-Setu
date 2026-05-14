package com.example.surakshasetu.ai

import android.content.Context
import android.media.MediaRecorder
import android.os.Handler
import android.os.Looper
import android.util.Log

class ThreatDetector(private val context: Context, private val onThreatDetected: () -> Unit) {

    private var threatRecorder: MediaRecorder? = null
    private var isListeningForThreats = false
    private val handler = Handler(Looper.getMainLooper())

    private val THREAT_AMPLITUDE_THRESHOLD = 25000 // High value denotes loud scream/shout

    private val monitoringRunnable = object : Runnable {
        override fun run() {
            if (isListeningForThreats) {
                threatRecorder?.let {
                    val maxAmplitude = it.maxAmplitude
                    Log.d("ThreatDetector", "Current Amplitude: $maxAmplitude")
                    if (maxAmplitude > THREAT_AMPLITUDE_THRESHOLD) {
                        Log.d("ThreatDetector", "Threat Detected! Amplitude: $maxAmplitude")
                        onThreatDetected()
                        stopMonitoring()
                    }
                }
                handler.postDelayed(this, 1000) // Check every 1 second
            }
        }
    }

    fun startMonitoring() {
        if (isListeningForThreats) return

        try {
            // Need a temp file to read amplitude, we don't actually save this
            val tempFile = kotlin.io.path.createTempFile("threat_monitor", ".3gp").toFile()
            
            threatRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(tempFile.absolutePath)
                prepare()
                start()
            }

            isListeningForThreats = true
            handler.post(monitoringRunnable)
            Log.d("ThreatDetector", "Started listening for threat anomalies")

        } catch (e: Exception) {
            Log.e("ThreatDetector", "Failed to start threat monitoring", e)
            isListeningForThreats = false
        }
    }

    fun stopMonitoring() {
        isListeningForThreats = false
        handler.removeCallbacks(monitoringRunnable)
        try {
            threatRecorder?.stop()
            threatRecorder?.release()
            threatRecorder = null
            Log.d("ThreatDetector", "Stopped threat monitoring")
        } catch (e: Exception) {
            Log.e("ThreatDetector", "Error stopping threat monitor", e)
        }
    }
}
