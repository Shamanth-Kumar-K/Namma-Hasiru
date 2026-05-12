package com.example.hasiru.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.hasiru.ui.theme.*

// --- NEW DATA MODEL FOR FIRESTORE ---
data class UserProfile(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val treesPlanted: Int = 0,
    val areasCovered: Int = 0,
    val level: String = "Seedling",
    val photoUrl: String = ""
)

data class PlantEntry(
    val id: Int,
    val name: String,
    val scientificName: String,
    val date: String,
    val location: String,
    val status: PlantStatus,
    val documentId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val imageUrl: String? = null
)

enum class PlantStatus(val label: String, val color: Color) {
    ALIVE("Alive", Green40),
    DEAD("Dead", Red40),
    UNKNOWN("Unknown", Color.Gray)
}

data class GrowthUpdate(
    val date: String,
    val status: PlantStatus,
    val description: String
)

data class Badge(
    val name: String,
    val icon: ImageVector,
    val achieved: Boolean
)

val userBadges = listOf(
    Badge("Pioneer", Icons.Filled.EmojiNature, true),
    Badge("Sprout", Icons.Filled.Park, true),
    Badge("Sapling", Icons.Filled.Forest, false),
    Badge("Oak", Icons.Filled.Nature, false),
)

data class Species(
    val id: Int,
    val name: String,
    val scientificName: String,
    val description: String,
    val soilType: String,
    val waterNeed: String, // Low, Moderate, High
    val benefits: String,
    val imageRes: Int // We can use a placeholder icon for now
)