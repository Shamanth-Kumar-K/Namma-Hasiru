package com.example.hasiru

import com.example.hasiru.screens.PlantEntry
import com.example.hasiru.screens.PlantStatus
import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testPlantStatusLabels() {
        assertEquals("Alive", PlantStatus.ALIVE.label)
        assertEquals("Dead", PlantStatus.DEAD.label)
        assertEquals("Unknown", PlantStatus.UNKNOWN.label)
    }

    @Test
    fun testPlantEntryCreation() {
        val plant = PlantEntry(
            id = 101,
            name = "Test Oak",
            scientificName = "Quercus",
            date = "2024-05-20",
            location = "Test Park",
            status = PlantStatus.ALIVE
        )
        
        assertEquals(101, plant.id)
        assertEquals("Test Oak", plant.name)
        assertEquals(PlantStatus.ALIVE, plant.status)
    }

    @Test
    fun testGetPlantById_validId() {
        val plant = getPlantById(1)
        assertNotNull(plant)
        assertEquals("Neem Tree", plant?.name)
    }

    @Test
    fun testGetPlantById_invalidId() {
        val plant = getPlantById(-1)
        assertNull(plant)
    }
}
