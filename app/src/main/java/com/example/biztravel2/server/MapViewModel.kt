package com.example.biztravel2.server

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

class MapViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _locations = MutableStateFlow<List<LocationData>>(emptyList())
    val locations: StateFlow<List<LocationData>> = _locations

    data class LocationData(
        val latLng: LatLng,
        val name: String,
        val type: String,
        val firstName: String,
        val createdAt: Date
    )

    fun loadLocations() {
        viewModelScope.launch {
            try {
                val documents = firestore.collection("locations").get().await()
                val fetchedLocations = mutableListOf<LocationData>()
                for (document in documents) {
                    val latitude = document.getDouble("latitude") ?: 0.0
                    val longitude = document.getDouble("longitude") ?: 0.0
                    val name = document.getString("name") ?: "Unknown Location"
                    val type = document.getString("type") ?: "Unknown Type"
                    val firstName = document.getString("firstName") ?: "Unknown User"
                    val createdAt = document.getDate("createdAt") ?: Date()

                    val latLng = LatLng(latitude, longitude)
                    val locationData = LocationData(latLng, name, type, firstName, createdAt)
                    fetchedLocations.add(locationData)
                }
                _locations.value = fetchedLocations
            } catch (e: Exception) {
                // Handle errors
            }
        }
    }


}
