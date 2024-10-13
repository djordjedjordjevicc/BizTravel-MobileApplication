package com.example.biztravel2.server

import android.graphics.Bitmap
import android.net.Uri
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

fun saveLocation(
    name: String,
    type: String,
    description: String,
    imageUri: Uri? = null,
    imageBitmap: Bitmap? = null,
    currentLocation: LatLng,
    firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    storage: FirebaseStorage = FirebaseStorage.getInstance(),
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    if (name.isBlank() || type.isBlank() || description.isBlank()) {
        onError("All fields are required.")
        return
    }

    addLocationData(name, type, description, currentLocation, imageUri, imageBitmap, firestore, storage) { error, status ->
        if (error != null) {
            onError(error)
        } else {
            status?.let { onSuccess() }
        }
    }
}

private fun addLocationData(
    name: String,
    type: String,
    description: String,
    currentLocation: LatLng,
    imageUri: Uri?,
    imageBitmap: Bitmap?,
    firestore: FirebaseFirestore,
    storage: FirebaseStorage,
    onComplete: (String?, String?) -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    if (userId == null) {
        onComplete("User not authenticated.", null)
        return
    }

    // Dohvatamo firstName korisnika
    val userRef = firestore.collection("users").document(userId)
    userRef.get().addOnSuccessListener { document ->
        if (document != null && document.exists()) {
            val firstName = document.getString("firstName") ?: "Unknown"

            val locationData = hashMapOf(
                "name" to name,
                "type" to type,
                "description" to description,
                "latitude" to currentLocation.latitude,
                "longitude" to currentLocation.longitude,
                "userId" to userId,
                "firstName" to firstName,  // Dodajemo firstName
                "createdAt" to FieldValue.serverTimestamp() // Dodavanje datuma kreiranja
            )

            val locationId = firestore.collection("locations").document().id

            firestore.collection("locations")
                .document(locationId)
                .set(locationData)
                .addOnSuccessListener {
                    // Dodavanje poena korisniku
                    updateUserPoints(userId, firestore) { pointsError ->
                        if (pointsError != null) {
                            onComplete(pointsError, null)
                        } else {
                            if (imageUri != null) {
                                uploadImage(locationId, imageUri, storage) { error, imageUrl ->
                                    if (error != null) {
                                        onComplete(error, null)
                                    } else {
                                        updateLocationWithImageUrl(locationId, imageUrl!!, firestore) { updateError, status ->
                                            if (updateError != null) {
                                                onComplete(updateError, null)
                                            } else {
                                                onComplete(null, status)
                                            }
                                        }
                                    }
                                }
                            } else if (imageBitmap != null) {
                                uploadImage(locationId, imageBitmap, storage) { error, imageUrl ->
                                    if (error != null) {
                                        onComplete(error, null)
                                    } else {
                                        updateLocationWithImageUrl(locationId, imageUrl!!, firestore) { updateError, status ->
                                            if (updateError != null) {
                                                onComplete(updateError, null)
                                            } else {
                                                onComplete(null, status)
                                            }
                                        }
                                    }
                                }
                            } else {
                                onComplete(null, "Location saved successfully.")
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    onComplete("Error saving location data: ${e.message}", null)
                }
        } else {
            onComplete("Error fetching user first name.", null)
        }
    }.addOnFailureListener { e ->
        onComplete("Error fetching user data: ${e.message}", null)
    }
}

private fun updateUserPoints(userId: String, firestore: FirebaseFirestore, onComplete: (String?) -> Unit) {
    val userRef = firestore.collection("users").document(userId)

    firestore.runTransaction { transaction ->
        val snapshot = transaction.get(userRef)
        val currentPoints = snapshot.getLong("points") ?: 0L
        val newPoints = currentPoints + 3
        transaction.update(userRef, "points", newPoints)
    }.addOnSuccessListener {
        onComplete(null) // Success
    }.addOnFailureListener { e ->
        onComplete("Error updating points: ${e.message}")
    }
}

private fun uploadImage(
    locationId: String,
    imageUri: Uri,
    storage: FirebaseStorage,
    onComplete: (String?, String?) -> Unit
) {
    val storageRef: StorageReference = storage.reference.child("images/$locationId")
    val uploadTask = storageRef.putFile(imageUri)

    uploadTask.addOnSuccessListener {
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            onComplete(null, uri.toString())
        }.addOnFailureListener { e ->
            onComplete("Error getting image URL: ${e.message}", null)
        }
    }.addOnFailureListener { e ->
        onComplete("Error uploading image: ${e.message}", null)
    }
}

private fun uploadImage(
    locationId: String,
    imageBitmap: Bitmap,
    storage: FirebaseStorage,
    onComplete: (String?, String?) -> Unit
) {
    val storageRef: StorageReference = storage.reference.child("images/$locationId")

    // Konverzija Bitmap u ByteArray
    val baos = ByteArrayOutputStream()
    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    val data = baos.toByteArray()

    val uploadTask = storageRef.putBytes(data)

    uploadTask.addOnSuccessListener {
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            onComplete(null, uri.toString())
        }.addOnFailureListener { e ->
            onComplete("Error getting image URL: ${e.message}", null)
        }
    }.addOnFailureListener { e ->
        onComplete("Error uploading image: ${e.message}", null)
    }
}

private fun updateLocationWithImageUrl(
    locationId: String,
    imageUrl: String,
    firestore: FirebaseFirestore,
    onComplete: (String?, String?) -> Unit
) {
    firestore.collection("locations").document(locationId)
        .update("imageUrl", imageUrl)
        .addOnSuccessListener {
            onComplete(null, "Location saved successfully with image.")
        }
        .addOnFailureListener { e ->
            onComplete("Error updating location with image URL: ${e.message}", null)
        }
}
