package com.example.biztravel2.server

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class User(
    val firstName: String = "",
    val lastName: String = "",
    val location: GeoPoint = GeoPoint(0.0, 0.0),
    val phoneNumber: String = "",
    val points: Int = 0,
    val profileImageUrl: String = "",
    val timestamp: Long = 0
)

class UserRankingViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val _users = MutableStateFlow<List<Pair<String, User>>>(emptyList())
    val users: StateFlow<List<Pair<String, User>>> get() = _users

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> get() = _currentUser

    init {
        loadUsers()
        loadCurrentUserProfile()
    }

    private fun loadUsers() {
        firestore.collection("users")
            .orderBy("points", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val userList = result.documents.mapNotNull { document ->
                    val userProfile = document.toObject(User::class.java)
                    userProfile?.let { document.id to it }
                }
                _users.value = userList
            }
    }

    private fun loadCurrentUserProfile() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val userProfile = document.toObject(User::class.java)
                _currentUser.value = userProfile
            }
    }
}
