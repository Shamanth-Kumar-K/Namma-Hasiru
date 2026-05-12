package com.example.hasiru.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.hasiru.screens.PlantEntry
import com.example.hasiru.screens.PlantStatus
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class PlantViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    // This is the "Live List" that the UI will watch
    val publicPlants = mutableStateListOf<PlantEntry>()

    init {
        fetchRealPlants()
    }

    private fun fetchRealPlants() {
        db.collection("plants")
            .orderBy("timestamp", Query.Direction.DESCENDING) // Newest plants first
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener

                if (snapshot != null) {
                    publicPlants.clear()
                    for (doc in snapshot.documents) {
                        val name = doc.getString("name") ?: "Unknown"
                        val date = doc.getString("date") ?: ""
                        val statusStr = doc.getString("status") ?: "ALIVE"

                        // Map Firestore string to our PlantStatus enum
                        val status = when(statusStr) {
                            "ALIVE" -> PlantStatus.ALIVE
                            "DEAD" -> PlantStatus.DEAD
                            else -> PlantStatus.UNKNOWN
                        }

                        val lat = doc.getDouble("latitude") ?: 0.0
                        val lng = doc.getDouble("longitude") ?: 0.0

                        publicPlants.add(
                            PlantEntry(
                                id = doc.id.hashCode(), // temporary ID
                                name = name,
                                scientificName = "Species Info",
                                date = date,
                                location = "Community Project",
                                status = status,
                                latitude = lat,
                                longitude = lng
                            )
                        )
                    }
                }
            }
    }
}