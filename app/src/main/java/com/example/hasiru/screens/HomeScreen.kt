package com.example.hasiru.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Park
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hasiru.ui.theme.*
import com.example.hasiru.viewmodel.HomeViewModel
import kotlin.math.roundToInt

@Composable
fun HomeScreen(
    onViewAll: () -> Unit = {},
    onEntryClick: (PlantEntry) -> Unit = {},
    onAddPlantClick: () -> Unit = {},
    homeViewModel: HomeViewModel = viewModel() // Connects to your Firestore logic
) {
    // Observe the live list from your ViewModel
    val plants = homeViewModel.plants

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPlantClick,
                containerColor = Green40,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add New Plant")
            }
        }
    ) { innerPadding ->
        // Box handles the inner padding from the Scaffold
        Box(modifier = Modifier.padding(innerPadding)) {
            HomeScreenContent(
                plants = plants,
                onViewAll = onViewAll,
                onEntryClick = onEntryClick
            )
        }
    }
}

@Composable
fun HomeScreenContent(
    plants: List<PlantEntry> = emptyList(),
    onViewAll: () -> Unit = {},
    onEntryClick: (PlantEntry) -> Unit = {}
) {
    // Dynamic calculations based on real Firestore data
    val totalPlants = plants.size
    val aliveCount = plants.count { it.status == PlantStatus.ALIVE }
    val deadCount = plants.count { it.status == PlantStatus.DEAD }
    val unknownCount = plants.count { it.status == PlantStatus.UNKNOWN }
    val survivalPercent = if (totalPlants > 0) ((aliveCount.toFloat() / totalPlants) * 100).roundToInt() else 0

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = SoftGrey
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- TOP BAR ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Namma Hasiru",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = Green20,
                        letterSpacing = (-0.5).sp
                    )
                }
            }

            // --- SURVIVAL STATS CARD ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Green40),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Park,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Namma-Hasiru",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "GREEN CITY INITIATIVE",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 2.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "TOTAL PLANTATIONS",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "$totalPlants",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = "$survivalPercent% Safe",
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.titleSmall,
                                color = Amber80,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // --- VITALITY PROGRESS CARD ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                "VITALITY",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextGrey,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                "LIVE RATE",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Amber40
                            )
                        }
                        Box(contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(
                                progress = { survivalPercent / 100f },
                                modifier = Modifier.size(70.dp),
                                strokeWidth = 8.dp,
                                color = Amber40,
                                trackColor = Green80.copy(alpha = 0.3f),
                                strokeCap = StrokeCap.Round
                            )
                            Text(
                                "$survivalPercent%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Amber40
                            )
                        }
                    }
                }
            }

            // --- STATUS BREAKDOWN ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            "PLANTATION STATUS",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextGrey,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem("$aliveCount", "ALIVE", Green40)
                            StatItem("$deadCount", "DEAD", Red40)
                            StatItem("$unknownCount", "UNKNOWN", Color.Gray)
                        }
                    }
                }
            }

            // --- LIST HEADER ---
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "LATEST ENTRIES",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Green20
                    )
                    Surface(
                        onClick = onViewAll,
                        shape = RoundedCornerShape(20.dp),
                        color = Green80.copy(alpha = 0.3f)
                    ) {
                        Text(
                            "View All →",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = Green40,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // --- EMPTY STATE OR PLANT LIST ---
            if (plants.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Park,
                                contentDescription = null,
                                tint = Green80,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "No plantations yet",
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextGrey
                            )
                            Text(
                                "Tap + to register your first tree!",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGrey.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            } else {
                items(plants.take(3)) { entry ->
                    PlantationCard(entry, onClick = { onEntryClick(entry) })
                }
            }
        }
    }
}

@Composable
fun StatItem(count: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextGrey, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun PlantationCard(entry: PlantEntry, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = Green80.copy(alpha = 0.4f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Filled.Park,
                        contentDescription = null,
                        tint = Green40,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = Green20
                )
                Text(
                    entry.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGrey
                )
                Text(
                    entry.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGrey
                )
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = entry.status.color.copy(alpha = 0.15f)
            ) {
                Text(
                    entry.status.label,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    color = entry.status.color,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    HasiruTheme {
        HomeScreenContent(
            plants = listOf(
                PlantEntry(1, "Sample Tree", "Scientific Name", "06 May 2026", "Bangalore", PlantStatus.ALIVE),
                PlantEntry(2, "Another Tree", "Scientific Name", "05 May 2026", "Bangalore", PlantStatus.DEAD)
            )
        )
    }
}