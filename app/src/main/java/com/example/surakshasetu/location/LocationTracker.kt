package com.example.surakshasetu.location

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class LocationTracker(private val context: Context) {

    private var fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    fun getLastKnownLocation(onLocationReceived: (Location?) -> Unit) {
        if (ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onLocationReceived(null)
            return
        }
        
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            onLocationReceived(location)
        }.addOnFailureListener {
            onLocationReceived(null)
        }
    }
    
    fun getGoogleMapsLink(location: Location?): String {
        if (location == null) return "Location unavailable"
        return "https://maps.google.com/?q=${location.latitude},${location.longitude}"
    }
}
