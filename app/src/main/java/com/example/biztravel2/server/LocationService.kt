package com.example.biztravel2.server

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.biztravel2.MainActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng

class LocationService : Service() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { location ->
                sendLocationToFirestore(location)
                checkNearbyLocations(location)
            }
        }
    }

    private var businessLocations: List<Pair<LatLng, String>> = emptyList()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundService()
        loadBusinessLocations()  // Učitaj lokacije iz Firestore-a
        startLocationUpdates()
        updateServiceStatus(true)
        return START_STICKY
    }

    @SuppressLint("ForegroundServiceType")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForegroundService() {
        val channelId = "location_channel"
        val channelName = "Location Service"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel =
            NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(channel)

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Location Service")
            .setContentText("Tracking your location in the background")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setMinUpdateIntervalMillis(5000)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    private fun loadBusinessLocations() {
        firestore.collection("locations")
            .get()
            .addOnSuccessListener { documents ->
                val fetchedLocations = mutableListOf<Pair<LatLng, String>>()
                for (document in documents) {
                    val latitude = document.getDouble("latitude") ?: 0.0
                    val longitude = document.getDouble("longitude") ?: 0.0
                    val name = document.getString("name") ?: "Unknown Location"
                    val latLng = LatLng(latitude, longitude)
                    fetchedLocations.add(Pair(latLng, name))
                }
                businessLocations = fetchedLocations
            }
    }

    private fun checkNearbyLocations(currentLocation: Location) {
        for (location in businessLocations) {
            val results = FloatArray(1)
            Location.distanceBetween(
                currentLocation.latitude, currentLocation.longitude,
                location.first.latitude, location.first.longitude,
                results
            )
            Log.d("LocationService", "Distance to ${location.second}: ${results[0]} meters")
            if (results[0] < 1000) { // Proveri da li je udaljenost manja od 100 metara
                sendNotification(location.second)
                break // Prikazujemo notifikaciju samo za najbližu lokaciju
            }
        }
    }

    private fun sendNotification(locationName: String) {
        val channelId = "nearby_location_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Nearby Location",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("Nearby Business Location")
            .setContentText("You are near $locationName")
            .setSmallIcon(android.R.drawable.ic_dialog_map)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(2, notification)
    }


    private fun sendLocationToFirestore(location: Location) {
        val uid = auth.currentUser?.uid ?: return
        val userLocation = mapOf(
            "location" to GeoPoint(location.latitude, location.longitude),
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("users")
            .document(uid)
            .update(userLocation)
            .addOnSuccessListener {
                // Successfully updated location
            }
            .addOnFailureListener { e ->
                // Handle failure
            }
    }
    private fun updateServiceStatus(isRunning: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        val status = if (isRunning) "on" else "off"
        firestore.collection("users").document(uid)
            .update("service", status)
            .addOnSuccessListener {
                // Successfully updated service status
            }
            .addOnFailureListener { e ->
                // Handle failure
            }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        updateServiceStatus(false)
    }
}
