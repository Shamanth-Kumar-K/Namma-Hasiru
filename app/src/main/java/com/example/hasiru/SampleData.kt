package com.example.hasiru

import com.example.hasiru.screens.PlantEntry
import com.example.hasiru.screens.PlantStatus

val allPlants = listOf(
    PlantEntry(1, "Neem Tree", "Azadirachta indica", "15 Mar 2026", "12.97° N, 77.5° E", PlantStatus.ALIVE),
    PlantEntry(2, "Peepal Tree", "Ficus religiosa", "20 Feb 2026", "12.95° N, 77.5° E", PlantStatus.DEAD),
    PlantEntry(3, "Banyan Tree", "Ficus benghalensis", "10 Jan 2026", "12.98° N, 77.5° E", PlantStatus.UNKNOWN),
    PlantEntry(4, "Ashoka Tree", "Polyalthia longifolia", "05 Dec 2025", "12.97° N, 77.5° E", PlantStatus.DEAD),
    PlantEntry(5, "Mango Tree", "Mangifera indica", "01 Jan 2026", "Lalbagh, Bangalore", PlantStatus.ALIVE),
    PlantEntry(6, "Teak", "Tectona grandis", "18 Apr 2026", "Cubbon Park", PlantStatus.ALIVE),
    PlantEntry(7, "Jamun", "Syzygium cumini", "22 Mar 2026", "Jayanagar", PlantStatus.UNKNOWN)
)

fun getPlantById(id: Int): PlantEntry? = allPlants.find { it.id == id }