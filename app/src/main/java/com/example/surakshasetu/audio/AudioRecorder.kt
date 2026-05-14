package com.example.surakshasetu.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AudioRecorder(private val context: Context) {

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false

    // Record for 30 seconds
    private val RECORDING_DURATION = 30000L

    fun startRecording(onRecordingComplete: (String?) -> Unit) {
        if (isRecording) {
            Log.d("AudioRecorder", "Already recording")
            return
        }

        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val audioFileName = "SOS_Audio_$timeStamp.3gp"
            
            // Store locally in app-specific cache to avoid requiring massive storage permissions immediately
            val storageDir = context.cacheDir
            val audioFile = File(storageDir, audioFileName)

            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                MediaRecorder()
            }

            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFile.absolutePath)
                prepare()
                start()
            }

            isRecording = true
            Log.d("AudioRecorder", "Recording started: ${audioFile.absolutePath}")

            // Auto-stop after 30 seconds
            Handler(Looper.getMainLooper()).postDelayed({
                stopRecording()
                onRecordingComplete(audioFile.absolutePath)
            }, RECORDING_DURATION)

        } catch (e: Exception) {
            Log.e("AudioRecorder", "Failed to start recording", e)
            isRecording = false
            onRecordingComplete(null)
        }
    }

    private fun stopRecording() {
        if (isRecording) {
            try {
                mediaRecorder?.stop()
                mediaRecorder?.release()
                mediaRecorder = null
                isRecording = false
                Log.d("AudioRecorder", "Recording stopped")
            } catch (e: Exception) {
                Log.e("AudioRecorder", "Failed to stop recording", e)
            }
        }
    }
}
