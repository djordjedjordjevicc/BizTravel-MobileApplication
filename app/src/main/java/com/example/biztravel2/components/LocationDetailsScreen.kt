package com.example.biztravel2.components

import android.graphics.Bitmap
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavHostController
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.example.biztravel2.server.LocationDetailsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LocationDetailsScreen(
    navController: NavHostController? = null,
    latitude: Double,
    longitude: Double,
    viewModel: LocationDetailsViewModel
) {

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var userId by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }

    var locationDetails by remember { mutableStateOf<Map<String, Any?>?>(null) }
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }

    val context = LocalContext.current
    BackHandler {
        // Ostaviti prazno da onemogući povratak
    }

    LaunchedEffect(key1 = currentUser) {
        currentUser?.let { user ->
            val db = FirebaseFirestore.getInstance()
            val userDocRef = db.collection("users").document(user.uid)

            userDocRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    userId = user.uid
                    firstName = document.getString("firstName").orEmpty()
                    lastName = document.getString("lastName").orEmpty()
                    Log.d("LocationDetailsScreen", "User ID: $userId, First Name: $firstName, Last Name: $lastName")
                } else {
                    Log.e("LocationDetailsScreen", "User document does not exist")
                }
            }.addOnFailureListener { exception ->
                Log.e("LocationDetailsScreen", "Failed to retrieve user data: $exception")
            }
        }
    }

    LaunchedEffect(key1 = latitude, key2 = longitude) {
        locationDetails = viewModel.getLocationDetails(latitude, longitude)

        if (locationDetails != null) {
            val imageUrl = locationDetails?.get("imageUrl") as? String ?: ""
            Log.d("LocationDetailsScreen", "Location details loaded: $locationDetails")

            if (imageUrl.isNotEmpty()) {
                try {
                    val loader = ImageLoader(context)
                    val request = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .allowHardware(false)
                        .build()

                    val result = (loader.execute(request) as SuccessResult).drawable
                    bitmap = (result.toBitmap())
                } catch (e: Exception) {
                    Log.e("LocationDetailsScreen", "Failed to load image: $e")
                }
            } else {
                Log.d("LocationDetailsScreen", "Image URL is empty")
            }
        } else {
            Log.e("LocationDetailsScreen", "Failed to load location details")
        }
    }

    if (locationDetails != null) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                Text(
                    text = "Profile Page",
                    style = MaterialTheme.typography.h4,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))

                // Label for Name
                Text(text = "Name:", style = MaterialTheme.typography.h6)
                Text(text = locationDetails!!["name"] as? String ?: "Unknown", style = MaterialTheme.typography.body1)

                Spacer(modifier = Modifier.height(8.dp))

                // Label for Description
                Text(text = "Description:", style = MaterialTheme.typography.h6)
                Text(text = locationDetails!!["description"] as? String ?: "No description", style = MaterialTheme.typography.body1)

                Spacer(modifier = Modifier.height(8.dp))

                // Label for Object Type
                Text(text = "Object Type:", style = MaterialTheme.typography.h6)
                Text(text = locationDetails!!["type"] as? String ?: "Unknown", style = MaterialTheme.typography.body1)

                Spacer(modifier = Modifier.height(16.dp))

                // Label for Location Picture
                Text(text = "Location Picture:", style = MaterialTheme.typography.h6)

                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Location Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(onClick = {
                    navController?.navigate("locationcomments/${latitude}/${longitude}")
                }) {
                    Text("View comments")
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        navController?.navigate("mapscreen")
                    }
                ) {
                    Text(text = "Go to Map")
                }
            }
        }
    } else {
        Text(text = "Loading...")
        Log.d("LocationDetailsScreen", "Location details are null, displaying loading text")
    }
}
