package com.example.hasiru.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hasiru.R
import com.example.hasiru.ui.theme.*

fun getGrowthUpdatesForPlant(plant: PlantEntry): List<GrowthUpdate> {
    // Return a list of milestones. For now, we generate a basic one based on the actual planting date.
    val initialMilestone = GrowthUpdate(plant.date, PlantStatus.ALIVE, "Planted sapling")
    
    return when (plant.status) {
        PlantStatus.ALIVE -> listOf(
            initialMilestone,
            GrowthUpdate(plant.date, PlantStatus.ALIVE, "Initial health check: Excellent")
        )
        PlantStatus.DEAD -> listOf(
            initialMilestone,
            GrowthUpdate(plant.date, PlantStatus.DEAD, "Status updated: Tree is no longer active")
        )
        PlantStatus.UNKNOWN -> listOf(
            initialMilestone,
            GrowthUpdate(plant.date, PlantStatus.UNKNOWN, "Initial status: Needs verification")
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlantDetailScreen(
    plant: PlantEntry,
    onBack: () -> Unit = {},
    onUpdateStatus: (plantId: Int) -> Unit = {}
) {
    val growthUpdates = getGrowthUpdatesForPlant(plant)
    Scaffold(
        containerColor = Color.White,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(plant.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(LeafGreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.tree),
                                contentDescription = null,
                                modifier = Modifier.size(50.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            plant.name,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            plant.scientificName.uppercase(),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            letterSpacing = 1.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = plant.status.color.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    plant.status.label,
                                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                    color = plant.status.color,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                            OutlinedButton(
                                onClick = { onUpdateStatus(plant.id) },
                                shape = CircleShape,
                                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                            ) {
                                Text("Update Tree Status", color = SlateBlue, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // Conservation Data
            item {
                Column {
                    Text(
                        "CONSERVATION DATA",
                        style = MaterialTheme.typography.labelMedium,
                        color = Green40,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        DataChip(Icons.Filled.CalendarMonth, "PLANTED ON", plant.date)
                        DataChip(Icons.Filled.LocationOn, "COORDINATES", plant.location)
                    }
                }
            }

            // Growth Milestones
            item {
                Text(
                    "GROWTH MILESTONES",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(growthUpdates) { update ->
                GrowthMilestoneCard(update)
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun DataChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = Green40, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GrowthMilestoneCard(update: GrowthUpdate) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(update.status.color)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(update.date, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                Text(update.description, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(update.status.label, style = MaterialTheme.typography.labelSmall, color = update.status.color)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PlantDetailScreenPreview() {
    HasiruTheme {
        PlantDetailScreen(
            plant = PlantEntry(1, "Neem Tree", "Azadirachta indica", "06 May 2026", "12.97° N, 77.5° E", PlantStatus.ALIVE)
        )
    }
}
