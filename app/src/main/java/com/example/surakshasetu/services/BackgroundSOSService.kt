package com.example.surakshasetu.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.surakshasetu.R
import com.example.surakshasetu.sensors.ShakeDetector
import com.example.surakshasetu.utils.SosManager
import com.example.surakshasetu.voice.VoiceCommandReceiver

import com.example.surakshasetu.ai.ThreatDetector

class BackgroundSOSService : Service() {

    private lateinit var shakeDetector: ShakeDetector
    private lateinit var voiceCommandReceiver: VoiceCommandReceiver
    private lateinit var threatDetector: ThreatDetector
    private lateinit var sosManager: SosManager

    override fun onCreate() {
        super.onCreate()
        sosManager = SosManager(this)

        shakeDetector = ShakeDetector(this) {
            sosManager.triggerSOS()
        }

        voiceCommandReceiver = VoiceCommandReceiver(this) {
            sosManager.triggerSOS()
        }
        
        threatDetector = ThreatDetector(this) {
            sosManager.triggerSOS()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createNotification())
        
        shakeDetector.start()
        voiceCommandReceiver.startListening()
        threatDetector.startMonitoring()

        return START_STICKY
    }

    override fun onDestroy() {
        shakeDetector.stop()
        voiceCommandReceiver.stopListening()
        threatDetector.stopMonitoring()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotification(): Notification {
        val channelId = "SosServiceChannel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "SOS Background Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Suraksha-Setu Active")
            .setContentText("Monitoring for emergency gestures and voice commands")
            .setSmallIcon(R.drawable.ic_launcher)
            .build()
    }
}
