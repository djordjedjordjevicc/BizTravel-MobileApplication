package com.example.biztravel2.server

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.biztravel2.components.MapScreen
import com.example.biztravel2.server.LocationDetailsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.LaunchedEffect as LaunchedEffect1

@Composable
fun LocationCommentsScreen(
    navController: NavHostController? = null,
    latitude: Double,
    longitude: Double,
    viewModel: LocationDetailsViewModel = viewModel()
) {
    var comment by rememberSaveable { mutableStateOf("") }
    var comments by remember { mutableStateOf(listOf<Map<String, String>>()) }
    var userId by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    BackHandler {
        //
    }

    LaunchedEffect1(key1 = currentUser) {
        currentUser?.let { user ->
            val db = FirebaseFirestore.getInstance()
            val userDocRef = db.collection("users").document(user.uid)

            userDocRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    userId = user.uid
                    firstName = document.getString("firstName").orEmpty()
                    lastName = document.getString("lastName").orEmpty()
                    Log.d("LocationCommentsScreen", "User data loaded: $firstName $lastName")
                } else {
                    Log.e("LocationCommentsScreen", "User document does not exist")
                }
            }.addOnFailureListener { exception ->
                Log.e("LocationCommentsScreen", "Failed to retrieve user data: $exception")
            }
        }
    }

    LaunchedEffect1(key1 = latitude, key2 = longitude) {
        val locationDetails = viewModel.getLocationDetails(latitude, longitude)
        if (locationDetails != null) {
            comments = locationDetails["comments"] as? List<Map<String, String>> ?: emptyList()
            Log.d("LocationCommentsScreen", "Comments loaded: $comments")
        } else {
            Log.e("LocationCommentsScreen", "Failed to load location details")
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Location Comments",
                style = MaterialTheme.typography.h4,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
        }

        items(comments) { commentItem ->
            Text(
                text = "${commentItem["firstName"]} ${commentItem["lastName"]}: ${commentItem["comment"]}",
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = comment,
                onValueChange = { comment = it },
                label = { Text("Add a comment") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    coroutineScope.launch {
                        viewModel.addComment(
                            latitude = latitude,
                            longitude = longitude,
                            comment = comment,
                            userId = userId,
                            firstName = firstName,
                            lastName = lastName
                        )
                        Log.d("LocationCommentsScreen", "Comment submitted: $comment")

                        // Ažuriraj lokalnu listu komentara
                        val updatedComments = comments.toMutableList()
                        updatedComments.add(
                            mapOf(
                                "firstName" to firstName,
                                "lastName" to lastName,
                                "comment" to comment
                            )
                        )
                        comments = updatedComments

                        // Resetuj input polje za komentar
                        comment = ""

                        delay(2000)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Submit")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    navController?.navigate("locationdetails/${latitude}/${longitude}")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Go back to Location Details")
            }
        }

    }
}
