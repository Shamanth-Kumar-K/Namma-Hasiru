package com.example.hasiru.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hasiru.ui.theme.*

// Data Model inside the file for easy access
data class GuideSpecies(
    val name: String,
    val scientific: String,
    val description: String,
    val successRate: String
)

private val speciesList = listOf(
    GuideSpecies("Neem", "Azadirachta indica", "Drought‑resistant, thrives in arid soil", "92%"),
    GuideSpecies("Peepal", "Ficus religiosa", "Broad leaves, high oxygen output", "85%"),
    GuideSpecies("Banyan", "Ficus benghalensis", "Massive canopy, needs space", "78%"),
    GuideSpecies("Mango", "Mangifera indica", "Fruit‑bearing, well‑drained soil", "88%"),
    GuideSpecies("Teak", "Tectona grandis", "Valuable timber, deep soil", "80%"),
    GuideSpecies("Jamun", "Syzygium cumini", "Evergreen, tolerates shade", "82%"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeciesGuideScreen(
    onBack: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = remember(searchQuery) {
        speciesList.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.scientific.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Species Guide", fontWeight = FontWeight.ExtraBold, color = Green20)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Green20
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SoftGrey
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(SoftGrey) // Matching Home Screen Background
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp, top = 8.dp)
        ) {
            item {
                Column {
                    Text(
                        "Best trees for your local soil",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Green20
                    )
                    Text(
                        "Based on real-world survival data from your area",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextGrey,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search species...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Green40) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = Green40,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                }
            }

            items(filteredList) { species ->
                SpeciesCard(species)
            }

            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No species found", color = TextGrey)
                    }
                }
            }
        }
    }
}

@Composable
fun SpeciesCard(species: GuideSpecies) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ){
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Background
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Green80.copy(alpha = 0.3f)), // Using your theme light green
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Park,
                    contentDescription = null,
                    tint = Green40,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    species.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = Green20
                )
                Text(
                    species.scientific,
                    style = MaterialTheme.typography.bodySmall,
                    color = Green40,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    species.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextGrey,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Success Rate Badge
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Amber80.copy(alpha = 0.2f),
                    contentColor = Amber20
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            species.successRate,
                            fontWeight = FontWeight.ExtraBold,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
                Text(
                    "Success",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextGrey,
                    fontSize = 9.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SpeciesGuideScreenPreview() {
    HasiruTheme {
        SpeciesGuideScreen()
    }
}