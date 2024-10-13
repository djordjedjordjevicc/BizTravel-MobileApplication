package com.example.biztravel2.server

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class UserProfile(
    val firstName: String = "",
    val lastName: String = "",
    val phoneNumber: String = "",
    val profileImageUrl: String = "",
    val service: String=""
)

class ProfileViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> get() = _userProfile

    fun loadUserProfile() {
        val userId = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            firestore.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val user = document.toObject(UserProfile::class.java)
                        _userProfile.value = user
                    }
                }
                .addOnFailureListener {
                    // Handle error
                }
        }
    }
    fun updateServiceStatus(newStatus: String) {
        val uid = auth.currentUser?.uid ?: return
        firestore.collection("users").document(uid).update("service", newStatus)
            .addOnSuccessListener {
                // Successfully updated the service status
                loadUserProfile() // Reload profile to reflect changes
            }
            .addOnFailureListener { exception ->
                Log.e("ProfileViewModel", "Error updating service status", exception)
            }
    }
    fun logout() {
        auth.signOut()
    }
}
