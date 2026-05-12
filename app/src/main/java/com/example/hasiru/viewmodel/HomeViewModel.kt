package com.example.hasiru.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.hasiru.repository.AuthRepository
import com.example.hasiru.screens.PlantEntry

class HomeViewModel : ViewModel() {
    private val repository = AuthRepository()
    private var snapshotListener: com.google.firebase.firestore.ListenerRegistration? = null
    val plants = mutableStateListOf<PlantEntry>()

    init {
        fetchPlants()
    }

    private fun fetchPlants() {
        snapshotListener?.remove()
        snapshotListener = repository.getPlants { updatedPlants ->
            plants.clear()
            plants.addAll(updatedPlants)
        }
    }

    override fun onCleared() {
        super.onCleared()
        snapshotListener?.remove()
    }

    fun deletePlant(plant: PlantEntry) {
        repository.deletePlant(plant.documentId) { success ->
            // The snapshot listener in repository will automatically update the UI
            // but we could handle errors here if needed
        }
    }
}
