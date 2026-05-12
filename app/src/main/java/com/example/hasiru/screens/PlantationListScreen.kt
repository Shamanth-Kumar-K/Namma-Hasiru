package com.example.hasiru.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.hasiru.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantationListScreen(
    plants: List<PlantEntry> = emptyList(),
    onBack: () -> Unit = {},
    onPlantClick: (PlantEntry) -> Unit = {},
    onDelete: (PlantEntry) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }
    var plantToDelete by remember { mutableStateOf<PlantEntry?>(null) }

    if (plantToDelete != null) {
        AlertDialog(
            onDismissRequest = { plantToDelete = null },
            title = { Text("Delete Plantation") },
            text = { Text("Are you sure you want to permanently remove \"${plantToDelete?.name}\"? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        plantToDelete?.let { onDelete(it) }
                        plantToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Red40)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { plantToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    val filteredPlants = plants.filter { plant ->
        val matchesStatus = when (selectedFilter) {
            "All" -> true
            "Alive" -> plant.status == PlantStatus.ALIVE
            "Dead" -> plant.status == PlantStatus.DEAD
            "Unknown" -> plant.status == PlantStatus.UNKNOWN
            else -> true
        }
        val matchesSearch = plant.name.contains(searchQuery, ignoreCase = true) ||
                plant.scientificName.contains(searchQuery, ignoreCase = true)
        matchesStatus && matchesSearch
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Plantation List", 
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack, 
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by name...", color = Color.Gray) },
                leadingIcon = { 
                    Icon(
                        Icons.Default.Search, 
                        null, 
                        tint = MaterialTheme.colorScheme.primary
                    ) 
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Filter Chips
            val filters = listOf("All", "Alive", "Dead", "Unknown")
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp), 
                modifier = Modifier.fillMaxWidth()
            ) {
                filters.forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter) },
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedFilter == filter,
                            borderColor = if (selectedFilter == filter) Color.Transparent else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            selectedBorderColor = Color.Transparent,
                            borderWidth = 1.dp
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Results Count
            Text(
                text = "${filteredPlants.size} plantations found",
                style = MaterialTheme.typography.labelLarge,
                color = Color.DarkGray,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Plant List
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(filteredPlants, key = { it.documentId }) { plant ->
                    PlantCard(
                        plant, 
                        onClick = { onPlantClick(plant) },
                        onDelete = { plantToDelete = plant }
                    )
                }
                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
fun PlantCard(entry: PlantEntry, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Park, 
                    null, 
                    tint = MaterialTheme.colorScheme.primary, 
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.name, 
                    fontWeight = FontWeight.Bold, 
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black
                )
                Text(
                    entry.scientificName, 
                    style = MaterialTheme.typography.bodySmall, 
                    color = Color.DarkGray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Park,
                        null, 
                        modifier = Modifier.size(12.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        entry.date, 
                        style = MaterialTheme.typography.labelSmall, 
                        color = Color.Gray
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Surface(
                    shape = RoundedCornerShape(8.dp), 
                    color = entry.status.color.copy(alpha = 0.1f)
                ) {
                    Text(
                        entry.status.label,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = entry.status.color,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Red40.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlantationListScreenPreview() {
    HasiruTheme {
        PlantationListScreen() 
    }
}
