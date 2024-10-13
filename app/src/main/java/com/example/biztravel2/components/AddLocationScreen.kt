package com.example.biztravel2.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.biztravel2.server.saveLocation
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

@Composable
fun AddLocationScreen(
    navController: NavHostController? = null,
    latitude: Double,
    longitude: Double
) {
    val context = LocalContext.current
    var locationName by rememberSaveable { mutableStateOf("") }
    var locationType by rememberSaveable { mutableStateOf("") }
    var locationDescription by rememberSaveable { mutableStateOf("") }
    var selectedImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    var imageBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showSuccessMessage by remember { mutableStateOf(false) }

    val currentLocation = LatLng(latitude, longitude)
    Log.d("AddLocationScreen", "Received Latitude: $latitude, Longitude: $longitude")
    // Firebase instances
    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    BackHandler {
        //
    }
    // Image pickers
    val getImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
        if (uri != null) {
            val inputStream = context.contentResolver.openInputStream(uri)
            imageBitmap = BitmapFactory.decodeStream(inputStream)
        }
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        imageBitmap = bitmap
        selectedImageUri = null // Clear the URI when taking a photo
    }

    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        item {
            Text(text = "Add New Business Location", style = MaterialTheme.typography.h6)

            TextField(
                value = locationName,
                onValueChange = { locationName = it },
                label = { Text("Location Name") }
            )

            TextField(
                value = locationType,
                onValueChange = { locationType = it },
                label = { Text("Location Type") }
            )

            TextField(
                value = locationDescription,
                onValueChange = { locationDescription = it },
                label = { Text("Description") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Row for image selection buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = { getImageLauncher.launch("image/*") }) {
                    Text("Select from Gallery")
                }

                Button(onClick = { takePictureLauncher.launch(null) }) {
                    Text("Take Photo")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Display selected image
            imageBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Selected Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(onClick = {
                saveLocation(
                    name = locationName,
                    type = locationType,
                    description = locationDescription,
                    imageUri = selectedImageUri,
                    imageBitmap = imageBitmap, // Dodaj bitmapu ovde
                    currentLocation = currentLocation,
                    firestore = firestore,
                    storage = storage,
                    onSuccess = {
                        Log.d("AddLocationScreen", "Location saved successfully")
                        showSuccessMessage = true
                        navController?.popBackStack()
                    },
                    onError = { errorMessage ->
                        Log.e("AddLocationScreen", "Error saving location: $errorMessage")
                    }
                )
            }) {
                Text("Submit")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    navController?.navigate("mapscreen")
                }
            ) {
                Text(text = "Go to Map")
            }

            if (showSuccessMessage) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Location added successfully!", color = MaterialTheme.colors.primary)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun AddLocationScreenPreview() {
    AddLocationScreen(navController = null, latitude = 40.7128, longitude = -74.0060)
}
