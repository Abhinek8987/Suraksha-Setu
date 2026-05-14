package com.example.surakshasetu.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.surakshasetu.R
import com.example.surakshasetu.services.BackgroundSOSService
import com.example.surakshasetu.utils.SosManager

class MainActivity : AppCompatActivity() {

    private lateinit var sosManager: SosManager
    private val PERMISSION_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sosManager = SosManager(this)

        setupButtons()
        checkAndRequestPermissions()
    }

    private fun setupButtons() {
        findViewById<Button>(R.id.btnSos).setOnClickListener {
            sosManager.triggerSOS()
            Toast.makeText(this, "SOS Triggered!", Toast.LENGTH_SHORT).show()
        }
        
        findViewById<Button>(R.id.btnSafeCircle).setOnClickListener {
            startActivity(Intent(this, SafeCircleActivity::class.java))
        }
        
        findViewById<Button>(R.id.btnVolunteerMode).setOnClickListener {
            startActivity(Intent(this, VolunteerActivity::class.java))
        }
        
        findViewById<Button>(R.id.btnAlertHistory).setOnClickListener {
            startActivity(Intent(this, AlertHistoryActivity::class.java))
        }
        
        findViewById<Button>(R.id.btnPoliceHelp).setOnClickListener {
            startActivity(Intent(this, PoliceHelpActivity::class.java))
        }
        
        findViewById<Button>(R.id.btnSettings).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsNeeded = mutableListOf<String>()

        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.SEND_SMS
        )

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(permission)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsNeeded.toTypedArray(), PERMISSION_REQUEST_CODE)
        } else {
            startBackgroundService()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startBackgroundService()
            } else {
                Toast.makeText(this, "Permissions are required for SOS features.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun startBackgroundService() {
        val serviceIntent = Intent(this, BackgroundSOSService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}
