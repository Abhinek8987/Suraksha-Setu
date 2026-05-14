package com.example.surakshasetu.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.surakshasetu.R
import com.example.surakshasetu.database.DatabaseHelper
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.util.Log
import com.example.surakshasetu.location.LocationTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import android.widget.TextView
import android.widget.Switch
import android.content.Context
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
class RegistrationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // setContentView(R.layout.activity_registration)
    }
}

class EmergencyActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_emergency)
        
        findViewById<Button>(R.id.btnCancelSos).setOnClickListener {
            finish()
        }
    }
}

class VolunteerActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_volunteer)
        
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        
        // Add a dummy marker for a volunteer and move the camera
        val dummyVolunteer = LatLng(28.7041, 77.1025) // Example: Delhi coords
        mMap.addMarker(MarkerOptions().position(dummyVolunteer).title("Volunteer: Ravi"))
        
        val dummyVolunteer2 = LatLng(28.7050, 77.1000)
        mMap.addMarker(MarkerOptions().position(dummyVolunteer2).title("Volunteer: Priya"))
        
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(dummyVolunteer, 14f))
    }
}

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        val switchShake = findViewById<Switch>(R.id.switchShake)
        val switchVoice = findViewById<Switch>(R.id.switchVoice)
        
        val prefs = getSharedPreferences("SurakshaSetuPrefs", Context.MODE_PRIVATE)
        switchShake.isChecked = prefs.getBoolean("shake_enabled", true)
        switchVoice.isChecked = prefs.getBoolean("voice_enabled", true)
        
        switchShake.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("shake_enabled", isChecked).apply()
        }
        switchVoice.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("voice_enabled", isChecked).apply()
        }
    }
}

class SafeCircleActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_safe_circle)
        
        dbHelper = DatabaseHelper(this)
        
        val etName = findViewById<EditText>(R.id.etContactName)
        val etEmail = findViewById<EditText>(R.id.etContactEmail)
        val btnAdd = findViewById<Button>(R.id.btnAddContact)
        val listView = findViewById<ListView>(R.id.listViewContacts)
        
        fun loadContacts() {
            val contacts = dbHelper.getAllSafeCircleContacts()
            val adapter = object : ArrayAdapter<com.example.surakshasetu.database.SafeContact>(this, R.layout.list_item_contact, contacts) {
                override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                    val view = convertView ?: android.view.LayoutInflater.from(context).inflate(R.layout.list_item_contact, parent, false)
                    val contact = contacts[position]
                    view.findViewById<TextView>(R.id.tvContactName).text = "${contact.name} - ${contact.email}"
                    view.findViewById<Button>(R.id.btnDeleteContact).setOnClickListener {
                        android.app.AlertDialog.Builder(this@SafeCircleActivity)
                            .setTitle("Delete Contact")
                            .setMessage("Are you sure you want to remove ${contact.name} from your Safe Circle?")
                            .setPositiveButton("Yes") { _, _ ->
                                dbHelper.deleteSafeCircleContact(contact.id)
                                Toast.makeText(this@SafeCircleActivity, "Contact Removed", Toast.LENGTH_SHORT).show()
                                loadContacts()
                            }
                            .setNegativeButton("No", null)
                            .show()
                    }
                    return view
                }
            }
            listView.adapter = adapter
        }
        
        loadContacts()
        
        btnAdd.setOnClickListener {
            val name = etName.text.toString()
            val email = etEmail.text.toString()
            if (name.isNotEmpty() && email.isNotEmpty()) {
                dbHelper.addSafeCircleContact(name, email, "Friend")
                Toast.makeText(this, "Contact Added", Toast.LENGTH_SHORT).show()
                etName.text.clear()
                etEmail.text.clear()
                loadContacts()
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
        
        // Delete handles itself in the adapter now
    }
}

class AlertItem(val time: String, val lat: Double, val lon: Double) {
    override fun toString(): String {
        val uniqueTimestamp = time.replace(":", "").replace(" ", "").replace("-", "")
        return "Alert on $time\nTap to view Location in Google Maps\nAudio Evidence: https://drive.google.com/drive/u/0/folders/1VRFk318eY3zOx_b-4k8uESeNCyz_JYzT/audio_$uniqueTimestamp.mp3"
    }
}

class AlertHistoryActivity : AppCompatActivity() {
    private lateinit var dbHelper: DatabaseHelper
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_alert_history)
        
        dbHelper = DatabaseHelper(this)
        val listView = findViewById<ListView>(R.id.listViewAlerts)
        
        val alerts = mutableListOf<Any>()
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${DatabaseHelper.TABLE_ALERTS} ORDER BY ${DatabaseHelper.COL_ID} DESC", null)
        if (cursor.moveToFirst()) {
            do {
                val time = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_TIMESTAMP))
                val lat = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LAT))
                val lon = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_LON))
                alerts.add(AlertItem(time, lat, lon))
            } while (cursor.moveToNext())
        } else {
            alerts.add("No alerts yet. Stay safe!")
        }
        cursor.close()
        db.close()
        
        val adapter = ArrayAdapter(this, R.layout.list_item_white_text, alerts)
        listView.adapter = adapter
        
        listView.setOnItemClickListener { _, _, position, _ ->
            val item = listView.getItemAtPosition(position)
            if (item is AlertItem) {
                val uri = android.net.Uri.parse("geo:${item.lat},${item.lon}?q=${item.lat},${item.lon}(SOS Alert Location)")
                val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                mapIntent.setPackage("com.google.android.apps.maps")
                if (mapIntent.resolveActivity(packageManager) != null) {
                    startActivity(mapIntent)
                } else {
                    startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, uri))
                }
            }
        }
    }
}

class PoliceStationItem(val name: String, val lat: Double, val lon: Double) {
    override fun toString(): String {
        return name
    }
}

class PoliceHelpActivity : AppCompatActivity() {
    private lateinit var locationTracker: LocationTracker
    private lateinit var adapter: ArrayAdapter<PoliceStationItem>
    private val stationList = mutableListOf<PoliceStationItem>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_police_help)
        
        val listView = findViewById<ListView>(R.id.listViewPolice)
        val btnDial = findViewById<Button>(R.id.btnEmergencyDial)
        
        locationTracker = LocationTracker(this)
        
        stationList.add(PoliceStationItem("Fetching precise location...", 0.0, 0.0))
        
        adapter = object : ArrayAdapter<PoliceStationItem>(this, R.layout.list_item_police, stationList) {
            override fun getView(position: Int, convertView: android.view.View?, parent: android.view.ViewGroup): android.view.View {
                val view = convertView ?: android.view.LayoutInflater.from(context).inflate(R.layout.list_item_police, parent, false)
                val station = stationList[position]
                view.findViewById<TextView>(R.id.tvPoliceStationName).text = station.name
                val subText = view.findViewById<TextView>(R.id.tvPoliceStationSubText)
                if (station.lat == 0.0 && station.lon == 0.0) {
                    subText.visibility = android.view.View.GONE
                } else {
                    subText.visibility = android.view.View.VISIBLE
                }
                return view
            }
        }
        listView.adapter = adapter
        
        btnDial.setOnClickListener {
            Toast.makeText(this, "Dialing 112...", Toast.LENGTH_SHORT).show()
            val intent = android.content.Intent(android.content.Intent.ACTION_DIAL)
            intent.data = android.net.Uri.parse("tel:112")
            startActivity(intent)
        }
        
        listView.setOnItemClickListener { _, _, position, _ ->
            val item = listView.getItemAtPosition(position)
            if (item is PoliceStationItem) {
                val uri = android.net.Uri.parse("geo:${item.lat},${item.lon}?q=${item.lat},${item.lon}(${item.name})")
                val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                mapIntent.setPackage("com.google.android.apps.maps")
                if (mapIntent.resolveActivity(packageManager) != null) {
                    startActivity(mapIntent)
                } else {
                    startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, uri))
                }
            }
        }
        
        fetchNearbyPoliceStations()
    }

    private fun fetchNearbyPoliceStations() {
        locationTracker.getLastKnownLocation { location ->
            if (location != null) {
                stationList.clear()
                stationList.add(PoliceStationItem("Searching for police stations within 5km...", 0.0, 0.0))
                adapter.notifyDataSetChanged()
                
                CoroutineScope(Dispatchers.IO).launch {
                    val lat = location.latitude
                    val lon = location.longitude
                    
                    // Overpass API Query: Find nodes with amenity=police within 5000 meters
                    val query = "[out:json];node(around:5000,$lat,$lon)[amenity=police];out;"
                    val encodedQuery = java.net.URLEncoder.encode(query, "UTF-8")
                    val urlString = "https://overpass-api.de/api/interpreter?data=$encodedQuery"
                    
                    try {
                        val url = URL(urlString)
                        val connection = url.openConnection() as HttpURLConnection
                        connection.requestMethod = "GET"
                        connection.connectTimeout = 10000
                        connection.readTimeout = 10000
                        
                        val reader = BufferedReader(InputStreamReader(connection.inputStream))
                        val response = reader.readText()
                        reader.close()
                        
                        val jsonObject = JSONObject(response)
                        val elements = jsonObject.getJSONArray("elements")
                        
                        val foundStations = mutableListOf<PoliceStationItem>()
                        for (i in 0 until elements.length()) {
                            val element = elements.getJSONObject(i)
                            if (element.has("tags")) {
                                val tags = element.getJSONObject("tags")
                                val name = if (tags.has("name")) tags.getString("name") else "Local Police Station"
                                foundStations.add(PoliceStationItem(name, element.getDouble("lat"), element.getDouble("lon")))
                            }
                        }
                        
                        withContext(Dispatchers.Main) {
                            stationList.clear()
                            if (foundStations.isNotEmpty()) {
                                stationList.addAll(foundStations)
                            } else {
                                stationList.add(PoliceStationItem("No police stations found within 5km.", 0.0, 0.0))
                            }
                            adapter.notifyDataSetChanged()
                        }
                        
                    } catch (e: Exception) {
                        Log.e("PoliceHelpActivity", "Error fetching from Overpass API", e)
                        withContext(Dispatchers.Main) {
                            stationList.clear()
                            stationList.add(PoliceStationItem("Failed to load live data. Check connection.", 0.0, 0.0))
                            adapter.notifyDataSetChanged()
                        }
                    }
                }
            } else {
                stationList.clear()
                stationList.add(PoliceStationItem("Unable to get current GPS location.", 0.0, 0.0))
                adapter.notifyDataSetChanged()
            }
        }
    }
}
