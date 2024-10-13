package com.example.biztravel2.server

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class LocationDetailsViewModel : ViewModel() {

    private val firestore = Firebase.firestore

    suspend fun getLocationDetails(latitude: Double, longitude: Double): Map<String, Any?>? {
        return withContext(Dispatchers.IO) {
            try {
                val snapshot = firestore.collection("locations")
                    .whereEqualTo("latitude", latitude)
                    .whereEqualTo("longitude", longitude)
                    .get()
                    .await()

                if (!snapshot.isEmpty) {
                    val document = snapshot.documents[0]
                    document.data // ovo vraća Map<String, Any?>
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e("LocationDetailsViewModel", "Error fetching location details", e)
                null
            }
        }
    }
    suspend fun addComment(latitude: Double, longitude: Double, comment: String, userId: String, firstName: String, lastName: String) {
        withContext(Dispatchers.IO) {
            try {
                val locationRef = firestore.collection("locations")
                    .whereEqualTo("latitude", latitude)
                    .whereEqualTo("longitude", longitude)
                    .get()
                    .await()
                    .documents
                    .firstOrNull()

                locationRef?.let {
                    val comments = it.get("comments") as? List<Map<String, String>> ?: emptyList()

                    val newComment = mapOf(
                        "comment" to comment,
                        "userId" to userId,
                        "firstName" to firstName,
                        "lastName" to lastName
                    )

                    firestore.collection("locations")
                        .document(it.id)
                        .update("comments", comments + newComment)
                        .await()

                    // Update user's points
                    val userRef = firestore.collection("users").document(userId)
                    val user = userRef.get().await()
                    val currentPoints = user.getLong("points") ?: 0
                    userRef.update("points", currentPoints + 1).await()
                }
            } catch (e: Exception) {
                Log.e("LocationDetailsViewModel", "Error adding comment", e)
            }
        }
    }
}
