package com.example.hasiru.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase

class AuthRepository {

    private val auth: FirebaseAuth = Firebase.auth
    private val db = FirebaseFirestore.getInstance()

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    fun getCurrentUser() = auth.currentUser

    fun signInWithGoogle(idToken: String, onResult: (Boolean, String?) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    // CREATE/UPDATE USER IN FIRESTORE
                    user?.let {
                        val userProfile = hashMapOf(
                            "uid" to it.uid,
                            "name" to (it.displayName ?: "Eco Guardian"),
                            "email" to (it.email ?: ""),
                            "photoUrl" to (it.photoUrl?.toString() ?: "")
                        )

                        // SetOptions.merge() prevents overwriting treesPlanted count on login
                        db.collection("users").document(it.uid)
                            .set(userProfile, SetOptions.merge())
                    }
                    onResult(true, null)
                } else {
                    onResult(false, task.exception?.message)
                }
            }
    }

    fun loginUser(email: String, pass: String, onResult: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task -> onResult(task.isSuccessful) }
    }

    fun signUpUser(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) onResult(true, null)
                else onResult(false, task.exception?.message)
            }
    }

    // Inside your AuthRepository class
    fun saveNewPlant(
        name: String,
        scientificName: String = "",
        notes: String = "",
        status: String = "ALIVE",
        lat: Double,
        lng: Double,
        imageUrl: String? = null,
        onResult: (Boolean, String?) -> Unit
    ) {
        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val userId = auth.currentUser?.uid ?: "anonymous"

        val plantData = hashMapOf(
            "uid" to userId,
            "name" to name,
            "scientificName" to scientificName,
            "notes" to notes,
            "latitude" to lat,
            "longitude" to lng,
            "status" to status,
            "imageUrl" to (imageUrl ?: ""),
            "timestamp" to com.google.firebase.Timestamp.now(),
            "date" to java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())
        )

        db.collection("plants")
            .add(plantData)
            .addOnSuccessListener { docRef ->
                // INCREMENT USER STATS
                if (userId != "anonymous") {
                    db.collection("users").document(userId)
                        .update(
                            "treesPlanted", com.google.firebase.firestore.FieldValue.increment(1),
                            // Optional: you could also increment areasCovered here if you want
                            "areasCovered", com.google.firebase.firestore.FieldValue.increment(1) 
                        )
                }
                onResult(true, docRef.id)
            }
            .addOnFailureListener { onResult(false, null) }
    }

    fun logout() {
        auth.signOut()
    }

    fun updateProfile(name: String, photoUrl: String? = null, onResult: (Boolean) -> Unit) {
        val user = auth.currentUser
        if (user == null) {
            onResult(false)
            return
        }

        // 1. Update Firebase Auth Profile
        val profileUpdates = com.google.firebase.auth.userProfileChangeRequest {
            this.displayName = name
            if (photoUrl != null) {
                this.photoUri = android.net.Uri.parse(photoUrl)
            }
        }

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { authTask ->
                if (authTask.isSuccessful) {
                    // 2. Update Firestore User Document
                    val data = mutableMapOf<String, Any>("name" to name)
                    if (photoUrl != null) {
                        data["photoUrl"] = photoUrl
                    }

                    db.collection("users").document(user.uid)
                        .set(data, com.google.firebase.firestore.SetOptions.merge())
                        .addOnCompleteListener { dbTask ->
                            onResult(dbTask.isSuccessful)
                        }
                } else {
                    onResult(false)
                }
            }
    }

    fun getPlants(onPlantsUpdated: (List<com.example.hasiru.screens.PlantEntry>) -> Unit): com.google.firebase.firestore.ListenerRegistration? {
        val userId = auth.currentUser?.uid ?: "anonymous"
        return db.collection("plants")
            .whereEqualTo("uid", userId)
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("HASIRU_DEBUG", "Firestore Error: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot == null) return@addSnapshotListener

                val plants = snapshot.documents.map { doc ->
                    val name = doc.getString("name") ?: "Unknown"
                    val scientificName = doc.getString("scientificName") ?: ""
                    val date = doc.getString("date") ?: ""
                    val statusStr = doc.getString("status") ?: "ALIVE"
                    val lat = doc.getDouble("latitude") ?: 0.0
                    val lng = doc.getDouble("longitude") ?: 0.0
                    
                    val status = try {
                        com.example.hasiru.screens.PlantStatus.valueOf(statusStr)
                    } catch (e: Exception) {
                        com.example.hasiru.screens.PlantStatus.ALIVE
                    }

                    com.example.hasiru.screens.PlantEntry(
                        id = doc.id.hashCode(),
                        documentId = doc.id,
                        name = name,
                        scientificName = scientificName,
                        date = date,
                        location = "${String.format(java.util.Locale.US, "%.4f", lat)}° N, ${String.format(java.util.Locale.US, "%.4f", lng)}° E",
                        status = status,
                        latitude = lat,
                        longitude = lng,
                        imageUrl = doc.getString("imageUrl")
                    )
                }
                onPlantsUpdated(plants)
            }
    }

    fun deletePlant(documentId: String, onResult: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: return
        
        db.collection("plants").document(documentId).delete()
            .addOnSuccessListener {
                // DECREMENT USER STATS
                db.collection("users").document(userId)
                    .update(
                        "treesPlanted", com.google.firebase.firestore.FieldValue.increment(-1),
                        "areasCovered", com.google.firebase.firestore.FieldValue.increment(-1)
                    )
                onResult(true)
            }
            .addOnFailureListener {
                onResult(false)
            }
    }
}
