package com.example.biztravel2.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.biztravel2.server.MapViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import java.util.Calendar

@SuppressLint("UnrememberedMutableState")
@Composable
fun MapScreen(navController: NavHostController? = null, mapViewModel: MapViewModel = viewModel()) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var locationPermissionGranted by rememberSaveable { mutableStateOf(false) }
    var currentLocation by rememberSaveable { mutableStateOf(LatLng(0.0, 0.0)) }
    val locations by mapViewModel.locations.collectAsState()

    var showDialog by rememberSaveable { mutableStateOf(false) }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var selectedLocation by rememberSaveable { mutableStateOf<Pair<LatLng, String>?>(null) }
    var addLocationLatLng by rememberSaveable { mutableStateOf<LatLng?>(null) }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var typeQuery by rememberSaveable { mutableStateOf("") }
    var authorQuery by rememberSaveable { mutableStateOf("") }

    var distanceRange by rememberSaveable { mutableStateOf(10f) }  // Default to 10 km
    var filterByDistance by rememberSaveable { mutableStateOf(false) }

    val filteredLocations by derivedStateOf {
        locations.filter { location ->
            val matchesSearchQuery = searchQuery.isEmpty() || location.name.contains(searchQuery, ignoreCase = true)
            val matchesTypeQuery = typeQuery.isEmpty() || location.type.contains(typeQuery, ignoreCase = true)
            val matchesAuthorQuery = authorQuery.isEmpty() || location.firstName.contains(authorQuery, ignoreCase = true)
            val withinRange = if (filterByDistance) isWithinDistance(currentLocation, location.latLng, distanceRange) else true

            matchesSearchQuery && matchesTypeQuery && matchesAuthorQuery && withinRange
        }
    }
    BackHandler {
        // Ostaviti prazno da onemogući povratak
    }
    val fusedLocationClient = remember {
        LocationServices.getFusedLocationProviderClient(context)
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        locationPermissionGranted = isGranted
    }

    DisposableEffect(key1 = lifecycleOwner) {
        val observer = object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                super.onResume(owner)
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    locationPermissionGranted = true
                    mapViewModel.loadLocations()
                } else {
                    launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(key1 = locationPermissionGranted) {
        if (locationPermissionGranted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    currentLocation = LatLng(location.latitude, location.longitude)
                }
            }
        }
    }
    val cameraPositionState = rememberCameraPositionState {
        currentLocation?.let {
            position = CameraPosition.fromLatLngZoom(it, 15f)
        } ?: run {
            position = CameraPosition.fromLatLngZoom(LatLng(0.0, 0.0), 15f)
        }
    }
    LaunchedEffect(currentLocation) {
        currentLocation?.let {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(it, 15f)
        }
    }
    Column(modifier = Modifier.fillMaxSize()) {
        // Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(Color.Gray.copy(alpha = 0.1f))
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { query -> searchQuery = query },
                label = { Text("Search by name") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextField(
                value = typeQuery,
                onValueChange = { query -> typeQuery = query },
                label = { Text("Type") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextField(
                value = authorQuery,
                onValueChange = { query -> authorQuery = query },
                label = { Text("Author") },
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text(
                text = "Range: ${distanceRange.toInt()} km",
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Slider(
                value = distanceRange,
                onValueChange = { distanceRange = it },
                valueRange = 1f..50f,
                modifier = Modifier
                    .weight(1f)
                    .align(Alignment.CenterVertically)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Checkbox(
                checked = filterByDistance,
                onCheckedChange = { checked ->
                    filterByDistance = checked
                }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Filter by distance")
        }

        if (searchQuery.isNotEmpty() || typeQuery.isNotEmpty() || authorQuery.isNotEmpty() || filterByDistance) {
            LazyColumn {
                items(filteredLocations) { location ->
                    Text(
                        text = location.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedLocation = location.latLng to location.name
                                showDialog = true
                            }
                            .padding(8.dp)
                    )
                }
            }
        } else {

            Text(
                text = "Start typing or enable distance filter to see locations.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.body1,
                color = Color.Gray
            )
        }

        if (locationPermissionGranted) {
            GoogleMap(
                modifier = Modifier.weight(1f),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    myLocationButtonEnabled = true,
                    zoomControlsEnabled = false
                ),
                properties = MapProperties(isMyLocationEnabled = true),
                onMapClick = { latLng ->
                    if (isSameLocation(latLng, currentLocation, tolerance = 100f)) {
                        addLocationLatLng = latLng
                        showAddDialog = true
                    }
                }
            ) {
                Marker(
                    state = MarkerState(position = currentLocation),
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                    title = "Current Location"
                )

                filteredLocations.forEach { location ->
                    Marker(
                        state = MarkerState(position = location.latLng),
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN),
                        title = location.name,
                        onClick = {
                            selectedLocation = location.latLng to location.name
                            showDialog = true
                            true
                        }
                    )
                }
            }

            selectedLocation?.let { (latLng, name) ->
                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text(name) },
                        confirmButton = {
                            Button(onClick = {
                                navController?.navigate("locationdetails/${latLng.latitude}/${latLng.longitude}")
                                showDialog = false
                            }) {
                                Text("See more details")
                            }
                        },
                        dismissButton = {
                            Button(onClick = { showDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }

            addLocationLatLng?.let { latLng ->
                if (showAddDialog) {
                    AlertDialog(
                        onDismissRequest = { showAddDialog = false },
                        title = { Text("Add Business Location?") },
                        confirmButton = {
                            Button(onClick = {
                                navController?.navigate("addlocation/${latLng.latitude}/${latLng.longitude}")
                                showAddDialog = false
                            }) {
                                Text("OK")
                            }
                        },
                        dismissButton = {
                            Button(onClick = { showAddDialog = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            }
            Button(
                onClick = { navController?.navigate("profile") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = "Go to Profile")
            }
        } else {
            Text(text = "Location permission is required to display the map.")
        }
    }
}

fun isWithinDistance(start: LatLng, end: LatLng, maxDistanceKm: Float): Boolean {
    val results = FloatArray(1)
    android.location.Location.distanceBetween(
        start.latitude, start.longitude,
        end.latitude, end.longitude,
        results
    )
    val distanceKm = results[0] / 1000 // convert to kilometers
    return distanceKm <= maxDistanceKm
}

fun isSameLocation(latLng1: LatLng, latLng2: LatLng, tolerance: Float = 50f): Boolean {
    val results = FloatArray(1)
    android.location.Location.distanceBetween(
        latLng1.latitude, latLng1.longitude,
        latLng2.latitude, latLng2.longitude,
        results
    )
    return results[0] < tolerance
}

@Preview(showBackground = true)
@Composable
fun MapScreenPreview() {
    MapScreen()
}
