package com.example.data.service

import android.util.Log
import com.example.data.repo.UserRepository
import com.example.model.ProfileData
import com.example.model.UserData
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserService(private val firestore: FirebaseFirestore): UserRepository {
    override suspend fun addUserData(userData: UserData): String? {
        var result: String? = null
        firestore.collection("users").
            document(userData.id).set(userData)
                .addOnSuccessListener {
                    Log.d("FIRESTORE", "Created user successfully: $userData")
                    result = userData.toString()
                }
                .addOnFailureListener {exception ->
                    Log.e("FIRESTORE ERROR", "Error adding user data to Firestore: $exception")
                }.await()
        updateUserProfile(userData.id, ProfileData(userData.username, id = userData.id))
        return result
    }

    override suspend fun getUserDataById(userId: String): UserData? {
        var user: UserData? = null
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener {documentSnapshot ->
                val userName = documentSnapshot.data?.get("username").toString()
                val password = documentSnapshot.data?.get("password").toString()
                val profile = documentSnapshot.data?.get("profile").toString()
                val id = documentSnapshot.data?.get("id").toString()
                val email = documentSnapshot.data?.get("email").toString()
                val friends = documentSnapshot.data?.get("friends") as MutableList<String>?
                val joinedServers = documentSnapshot.data?.get("joinedServers") as MutableList<String>?
                user = UserData(userName, password, email, joinedServers, friends, id, profile)
                Log.d("FIRESTORE", "Get user data successfully: ${user!!.email}")
            }
            .addOnFailureListener { exception ->
                Log.e("FIRESTORE ERROR", "Error getting user's profile data to Firestore: $exception")
            }.await()
        return user
    }

    override suspend fun getUserDataByUsername(userName: String): String? {
        var user: String? = null
        firestore.collection("users")
            .whereEqualTo("username", userName).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot != null) {
                    user = querySnapshot.documents[0].data.toString()
                    Log.d("FIRESTORE", "Get user with username: $userName successfully")
                    Log.d("FIRESTORE", "User: $user")
                } else {
                    Log.d("FIRESTORE ERROR", "User not found with username: $userName")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FIRESTORE ERROR", "Error getting user: ", exception)
            }.await()
        return user
    }

    override suspend fun getUserIdByUsername(userName: String): String? {
        var id: String? = null
        firestore.collection("users")
            .whereEqualTo("username", userName).get()
            .addOnSuccessListener { querySnapshot ->
                if (querySnapshot != null) {
                    id = querySnapshot.documents[0].data?.get("id")?.toString()
                    Log.d("FIRESTORE", "Get user with username: $userName successfully")
                    Log.d("FIRESTORE", "User ID: $id")
                } else {
                    Log.d("FIRESTORE ERROR", "User not found with username: $userName")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FIRESTORE ERROR", "Error getting user: ", exception)
            }.await()
        return id
    }

    override suspend fun updateUserData(userId: String, userData: UserData) {
        val user = getUserDataById(userId)
        if (user != null) {
            firestore.collection("users").document(userId).set(userData)
                .addOnSuccessListener {
                    Log.d("FIRESTORE", "Updated user successfully: $userData")
                }
                .addOnFailureListener { exception ->
                    Log.e("FIRESTORE ERROR", "Error update user data to Firestore: $exception")
                }.await()
        }
        else {
            Log.d("FIRESTORE ERROR", "User not found with ID: $userId")
        }
    }

    override suspend fun deleteUserDataById(userId: String) {
        val user = getUserDataById(userId)
        if (user != null) {
            firestore.collection("users").document(userId).delete()
                .addOnSuccessListener {
                    Log.d("FIRESTORE", "Deleted user with ID: $userId successfully")
                }
                .addOnFailureListener { exception ->
                    Log.e("FIRESTORE ERROR", "Error deleting user data: $exception")
                }.await()
        }
        else {
            Log.d("FIRESTORE ERROR", "User not found with ID: $userId")
        }
    }

    override suspend fun checkUsernameAvailability(userName: String): Boolean {
        val usernamesRef = firestore.collection("users")
            .whereEqualTo("username", userName)
        val querySnapshot = usernamesRef.get().await()
        if (!querySnapshot.isEmpty) {
            Log.e("FIRESTORE ERROR", "Username '$userName' already exists. Please choose a different one.")
            return true
        }
        return false
    }

    override suspend fun checkEmailAvailability(email: String): Boolean {
        val usernamesRef = firestore.collection("users")
            .whereEqualTo("email", email)
        val querySnapshot = usernamesRef.get().await()
        if (!querySnapshot.isEmpty) {
            Log.e("FIRESTORE ERROR", "Email '$email' already exists. Please choose a different one.")
            return true
        }
        return false
    }

    override suspend fun verifyLoginInfo(userName: String, password: String): Boolean {
        val usernamesRef = firestore.collection("users")
            .whereEqualTo("username", userName).whereEqualTo("password", password)
        val querySnapshot = usernamesRef.get().await()
        if (!querySnapshot.isEmpty) {
            Log.d("FIRESTORE", "Login successfully")
            return true
        }
        Log.e("FIRESTORE", "Login info incorrect")
        return false
    }

    override suspend fun updateUserProfile(userId: String, profileData: ProfileData) {
        firestore.collection("profiles").document(userId).set(profileData)
            .addOnSuccessListener {
                Log.d("FIRESTORE", "Updated user's profile successfully: $profileData")
            }
            .addOnFailureListener { exception ->
                Log.e("FIRESTORE ERROR", "Error update user's profile data to Firestore: $exception")
            }.await()
    }

    override suspend fun getUserProfile(userId: String): ProfileData? {
        var profile: ProfileData? = null
        firestore.collection("profiles").document(userId).get()
            .addOnSuccessListener {documentSnapshot ->
                val displayName = documentSnapshot.data?.get("displayName").toString()
                val dob = documentSnapshot.data?.get("dob").toString()
                val avatar = documentSnapshot.data?.get("avatar").toString()
                val bio = documentSnapshot.data?.get("bio").toString()
                profile = ProfileData(displayName, dob, avatar, bio, userId)
                Log.d("FIRESTORE", "Get user's profile successfully: ${profile.toString()}")
            }
            .addOnFailureListener { exception ->
                Log.e("FIRESTORE ERROR", "Error getting user's profile data to Firestore: $exception")
            }.await()
        return profile
    }
}