package com.example.biztravel2.server

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class SignUpViewModel : ViewModel() {

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    private val _signUpStatus = MutableStateFlow("")
    val signUpStatus: StateFlow<String> = _signUpStatus

    private val auth = FirebaseAuth.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val storageReference = storage.reference
    private val firestore = FirebaseFirestore.getInstance()

    fun signUp(
        username: String,
        password: String,
        confirmPassword: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        profileImage: Bitmap?,
        navController: NavHostController?
    ) {
        viewModelScope.launch {
            _errorMessage.value = ""
            _signUpStatus.value = ""

            if (password != confirmPassword) {
                _errorMessage.value = "Passwords do not match."
            } else if (username.isBlank() || password.isBlank() || firstName.isBlank() || lastName.isBlank() || phoneNumber.isBlank()) {
                _errorMessage.value = "All fields are required."
            } else {
                createAccount(username, password, firstName, lastName, phoneNumber, profileImage) { error, status ->
                    if (error != null) {
                        _errorMessage.value = error
                    } else {
                        _signUpStatus.value = status ?: "Registration and data saving successful."
                        navController?.navigate("login")
                    }
                }
            }
        }
    }

    private fun createAccount(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        profileImage: Bitmap?,
        onComplete: (String?, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        val userId = it.uid

                        // Upload profile image to Firebase Storage
                        profileImage?.let { image ->
                            val storageRef: StorageReference = storageReference.child("profile_images/$userId.jpg")
                            val baos = ByteArrayOutputStream()
                            image.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                            val data = baos.toByteArray()

                            val uploadTask = storageRef.putBytes(data)
                            uploadTask.addOnCompleteListener { uploadTask ->
                                if (uploadTask.isSuccessful) {
                                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                                        val imageUrl = uri.toString()
                                        saveUserData(userId, firstName, lastName, phoneNumber, imageUrl) { error, status ->
                                            if (error != null) {
                                                onComplete(error, null)
                                            } else {
                                                onComplete(null, status)
                                            }
                                        }
                                    }
                                } else {
                                    onComplete("Failed to upload profile image.", null)
                                }
                            }
                        }

                        // Save additional user information if no profile image
                        if (profileImage == null) {
                            saveUserData(userId, firstName, lastName, phoneNumber, null) { error, status ->
                                if (error != null) {
                                    onComplete(error, null)
                                } else {
                                    onComplete(null, status)
                                }
                            }
                        }
                    }
                } else {
                    onComplete(task.exception?.message ?: "Registration failed.", null)
                }
            }
    }

    private fun saveUserData(
        userId: String,
        firstName: String,
        lastName: String,
        phoneNumber: String,
        profileImageUrl: String?,
        onComplete: (String?, String?) -> Unit
    ) {
        val user = hashMapOf(
            "firstName" to firstName,
            "lastName" to lastName,
            "phoneNumber" to phoneNumber,
            "profileImageUrl" to profileImageUrl
        )

        firestore.collection("users")
            .document(userId)
            .set(user)
            .addOnSuccessListener {
                onComplete(null, "User data saved successfully")
            }
            .addOnFailureListener { e ->
                onComplete("Error saving user data: ${e.message}", null)
            }
    }
}
