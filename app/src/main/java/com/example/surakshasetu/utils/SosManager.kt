package com.example.surakshasetu.utils

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.util.Log
import com.example.surakshasetu.activities.EmergencyActivity
import com.example.surakshasetu.audio.AudioRecorder
import com.example.surakshasetu.email.EmailSender
import com.example.surakshasetu.location.LocationTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.example.surakshasetu.database.DatabaseHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SosManager(private val context: Context) {

    private val locationTracker = LocationTracker(context)
    private val audioRecorder = AudioRecorder(context)
    private val emailSender = EmailSender()
    private val dbHelper = DatabaseHelper(context)
    private var mediaPlayer: MediaPlayer? = null
    
    // Flag to prevent multiple concurrent triggers
    private var isEmergencyActive = false

    fun triggerSOS() {
        if (isEmergencyActive) return
        isEmergencyActive = true
        Log.d("SosManager", "SOS Triggered!")
        
        // Play Alarm Sound
        playAlarmSound()
        
        // Show Emergency Screen
        showEmergencyScreen()

        // Log Alert to Database IMMEDIATELY so it's not missed
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val alertId = dbHelper.addAlert(0.0, 0.0, timestamp, "")

        // Capture GPS Location
        locationTracker.getLastKnownLocation { location ->
            val mapsLink = locationTracker.getGoogleMapsLink(location)
            
            // Update the alert with actual location if found
            if (location != null) {
                val db = dbHelper.writableDatabase
                val values = android.content.ContentValues().apply {
                    put(DatabaseHelper.COL_LAT, location.latitude)
                    put(DatabaseHelper.COL_LON, location.longitude)
                }
                db.update(DatabaseHelper.TABLE_ALERTS, values, "${DatabaseHelper.COL_ID}=?", arrayOf(alertId.toString()))
                db.close()
            }

            // Fetch actual Safe Circle contacts
            val safeCircleContacts = dbHelper.getSafeCircleEmails()
            val emailsToNotify = if (safeCircleContacts.isNotEmpty()) safeCircleContacts else listOf("kumar12345abhinek@gmail.com")

            val smsManager = try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    context.getSystemService(android.telephony.SmsManager::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    android.telephony.SmsManager.getDefault()
                }
            } catch (e: Exception) {
                null
            }

            // Send Immediate Emergency Email and SMS (Stage 1)
            CoroutineScope(Dispatchers.IO).launch {
                emailsToNotify.forEach { contact ->
                    // Try Email
                    if (contact.contains("@")) {
                        emailSender.sendEmergencyEmail(contact, "Suraksha User", mapsLink)
                    }
                    // Try SMS (assuming contact string contains a phone number if not an email, or just send to all if valid)
                    smsManager?.let { sms ->
                        try {
                            val msg = "EMERGENCY! I need help. Location: $mapsLink"
                            // If the contact is a phone number
                            if (contact.matches(Regex("^[0-9+]+$"))) {
                                sms.sendTextMessage(contact, null, msg, null, null)
                                Log.d("SosManager", "SMS sent to $contact")
                            }
                        } catch (e: Exception) {
                            Log.e("SosManager", "Failed to send SMS", e)
                        }
                    }
                }
            }
            
            // Start Audio Recording
            audioRecorder.startRecording { audioPath ->
                Log.d("SosManager", "Audio saved to: $audioPath")
                
                // Update Alert Log with Audio Path
                dbHelper.addAlert(location?.latitude ?: 0.0, location?.longitude ?: 0.0, timestamp, audioPath ?: "")
                
                // Send Follow-up Audio Email (Stage 2)
                if (audioPath != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        emailsToNotify.forEach { email ->
                            emailSender.sendEmergencyEmail(email, "Suraksha User", mapsLink, audioPath)
                        }
                    }
                }
                
                isEmergencyActive = false // reset for next emergency
            }
        }
        
        // Notify Volunteers
        notifyVolunteers()
    }

    private fun playAlarmSound() {
        try {
            // mediaPlayer = MediaPlayer.create(context, R.raw.alarm)
            // mediaPlayer?.start()
        } catch (e: Exception) {
            Log.e("SosManager", "Failed to play alarm", e)
        }
    }

    private fun notifyVolunteers() {
        // Broadcast or local notification logic
    }

    private fun showEmergencyScreen() {
        val intent = Intent(context, EmergencyActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        context.startActivity(intent)
    }
}
