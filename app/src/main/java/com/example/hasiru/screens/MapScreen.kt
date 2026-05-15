package com.example.hasiru.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hasiru.ui.theme.*
import com.example.hasiru.viewmodel.HomeViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

// ── Marker positions ──
data class MarkerData(
    val id: Int,
    val xFraction: Float,
    val yFraction: Float,
    val color: Color,
    val label: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBack: () -> Unit = {},
    onMarkerClick: (MarkerData) -> Unit = {},
    onViewRegionDetails: () -> Unit = {},
    homeViewModel: HomeViewModel = viewModel()
) {
    // The stateful MapScreen now delegates to the stateless MapScreenContent
    // This allows the Preview to bypass ViewModel (and Firebase) initialization
    MapScreenContent(
        plants = homeViewModel.plants,
        onBack = onBack,
        onMarkerClick = onMarkerClick,
        onViewRegionDetails = onViewRegionDetails
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreenContent(
    plants: List<PlantEntry>,
    onBack: () -> Unit = {},
    onMarkerClick: (MarkerData) -> Unit = {},
    onViewRegionDetails: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("All") }
    
    // Zoom and Pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var lastMarkersCount by remember { mutableIntStateOf(0) }

    // Generate markers from real Firestore plants with dynamic bounding
    // ... (rest of the code remains same)
    val realMarkers = remember(plants) {
        if (plants.isEmpty()) return@remember emptyList<MarkerData>()

        val validPlants = plants.filter { it.latitude != 0.0 && it.longitude != 0.0 }
        
        // Dynamic viewport based on actual plants, fallback to Bangalore
        val minLat = if (validPlants.isNotEmpty()) validPlants.minOf { it.latitude } else 12.85
        val maxLat = if (validPlants.isNotEmpty()) validPlants.maxOf { it.latitude } else 13.1
        val minLng = if (validPlants.isNotEmpty()) validPlants.minOf { it.longitude } else 77.4
        val maxLng = if (validPlants.isNotEmpty()) validPlants.maxOf { it.longitude } else 77.7
        
        // Add padding (20%) to keep markers away from the literal screen edges
        val latSpan = (maxLat - minLat).coerceAtLeast(0.005)
        val lngSpan = (maxLng - minLng).coerceAtLeast(0.005)
        
        val paddedMinLat = minLat - latSpan * 0.2
        val paddedMaxLat = maxLat + latSpan * 0.2
        val paddedMinLng = minLng - lngSpan * 0.2
        val paddedMaxLng = maxLng + lngSpan * 0.2
        
        val finalLatSpan = paddedMaxLat - paddedMinLat
        val finalLngSpan = paddedMaxLng - paddedMinLng

        plants.map { plant ->
            val finalX: Float
            val finalY: Float
            
            if (plant.latitude == 0.0 || plant.longitude == 0.0) {
                // Use pseudo-random for plants without coordinates
                finalX = (Math.abs(plant.id % 1000) / 1000f).coerceIn(0.1f, 0.9f)
                finalY = (Math.abs((plant.id / 1000) % 1000) / 1000f).coerceIn(0.1f, 0.9f)
            } else {
                // Map coordinates to the 0..1 fraction space within our dynamic bounds
                finalX = ((plant.longitude - paddedMinLng) / finalLngSpan).toFloat().coerceIn(0f, 1f)
                finalY = (1.0 - (plant.latitude - paddedMinLat) / finalLatSpan).toFloat().coerceIn(0f, 1f)
            }

            MarkerData(
                id = plant.id,
                xFraction = finalX,
                yFraction = finalY,
                color = plant.status.color,
                label = plant.name
            )
        }
    }

    // Fallback to sample data if no plants exist
    val sampleMarkers = if (realMarkers.isEmpty()) listOf(
        MarkerData(1, 0.3f, 0.4f, Green40, "Alive tree"),
        MarkerData(2, 0.5f, 0.6f, Red40, "Dead tree"),
        MarkerData(3, 0.8f, 0.2f, Color.Gray, "Unknown tree"),
    ) else emptyList()

    val allMarkers = realMarkers + sampleMarkers

    val filteredMarkers = remember(selectedFilter, allMarkers) {
        when (selectedFilter) {
            "Alive" -> allMarkers.filter { it.color == Green40 }
            "Dead" -> allMarkers.filter { it.color == Red40 }
            "Unknown" -> allMarkers.filter { it.color == Color.Gray }
            else -> allMarkers
        }
    }
    
    val density = LocalDensity.current
    val primaryColor = MaterialTheme.colorScheme.primary
    val leafBackground = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)

    Scaffold { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .clip(RectangleShape) // Ensures map doesn't overlap UI elements when zoomed
        ) {
            val mapWidthPx = constraints.maxWidth.toFloat()
            val mapHeightPx = constraints.maxHeight.toFloat()

            // Auto-center when markers are ready or count changes
            LaunchedEffect(filteredMarkers.size, mapWidthPx, mapHeightPx) {
                if (filteredMarkers.isNotEmpty() && filteredMarkers.size != lastMarkersCount && mapWidthPx > 0) {
                    val minX = filteredMarkers.minOf { it.xFraction }
                    val maxX = filteredMarkers.maxOf { it.xFraction }
                    val minY = filteredMarkers.minOf { it.yFraction }
                    val maxY = filteredMarkers.maxOf { it.yFraction }
                    
                    val centerX = (minX + maxX) / 2f
                    val centerY = (minY + maxY) / 2f
                    
                    val spanX = maxX - minX
                    val spanY = maxY - minY
                    val maxSpan = maxOf(spanX, spanY).coerceAtLeast(0.01f)
                    
                    val targetScale = (0.6f / maxSpan).coerceIn(1.2f, 4f)
                    
                    scale = targetScale
                    offset = Offset(
                        mapWidthPx / 2f - centerX * mapWidthPx * targetScale,
                        mapHeightPx / 2f - centerY * mapHeightPx * targetScale
                    )
                    lastMarkersCount = filteredMarkers.size
                }
            }

            // ── 1. Map Content (Zoomable & Pannable) ──
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { centroid, pan, zoom, _ ->
                            val oldScale = scale
                            val newScale = (scale * zoom).coerceIn(1f, 10f)
                            
                            // Zoom around centroid logic
                            offset = (offset + pan) * (newScale / oldScale) + centroid * (1 - newScale / oldScale)
                            scale = newScale
                        }
                    }
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(leafBackground)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height

                        // Ultra-soft grid lines
                        for (i in 0..10) {
                            val x = i * (width / 10)
                            drawLine(
                                color = primaryColor.copy(alpha = 0.05f),
                                start = Offset(x, 0f),
                                end = Offset(x, height),
                                strokeWidth = 1f
                            )
                            val y = i * (height / 10)
                            drawLine(
                                color = primaryColor.copy(alpha = 0.05f),
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1f
                            )
                        }
                    }

                    // ── 2. Tree Markers ──
                    filteredMarkers.forEach { marker ->
                        val px = mapWidthPx * marker.xFraction
                        val py = mapHeightPx * marker.yFraction
                        val iconSizeDp = 44.dp
                        val iconSizePx = with(density) { iconSizeDp.toPx() }

                        Surface(
                            modifier = Modifier
                                .offset(
                                    x = with(density) { (px - iconSizePx / 2).toDp() },
                                    y = with(density) { (py - iconSizePx / 2).toDp() }
                                )
                                .size(iconSizeDp)
                                // Keep markers a constant size on screen
                                .graphicsLayer(scaleX = 1/scale, scaleY = 1/scale),
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 4.dp,
                            onClick = { onMarkerClick(marker) }
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(3.dp)
                                    .fillMaxSize()
                                    .background(marker.color, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Park,
                                    contentDescription = marker.label,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // ── 3. Floating Search Bar ──
            Surface(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(16.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                    Text(
                        "Search Bangalore...",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                    Icon(
                        Icons.Filled.Search,
                        contentDescription = "Search",
                        modifier = Modifier.padding(end = 16.dp),
                        tint = primaryColor
                    )
                }
            }

            // ── 4. Filter Chips ──
            Row(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(top = 80.dp)
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MapFilterChip("All", selectedFilter == "All") { selectedFilter = "All" }
                MapFilterChip("Alive", selectedFilter == "Alive") { selectedFilter = "Alive" }
                MapFilterChip("Dead", selectedFilter == "Dead") { selectedFilter = "Dead" }
                MapFilterChip("Unknown", selectedFilter == "Unknown") { selectedFilter = "Unknown" }
            }

            // ── 5. Zoom Controls ──
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = { scale = (scale * 1.5f).coerceIn(1f, 10f) },
                    containerColor = Color.White,
                    contentColor = primaryColor,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In")
                }
                SmallFloatingActionButton(
                    onClick = { scale = (scale / 1.5f).coerceIn(1f, 10f) },
                    containerColor = Color.White,
                    contentColor = primaryColor,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
                }
                SmallFloatingActionButton(
                    onClick = { 
                        if (filteredMarkers.isNotEmpty()) {
                            val minX = filteredMarkers.minOf { it.xFraction }
                            val maxX = filteredMarkers.maxOf { it.xFraction }
                            val minY = filteredMarkers.minOf { it.yFraction }
                            val maxY = filteredMarkers.maxOf { it.yFraction }
                            
                            val centerX = (minX + maxX) / 2f
                            val centerY = (minY + maxY) / 2f
                            
                            val spanX = maxX - minX
                            val spanY = maxY - minY
                            val maxSpan = maxOf(spanX, spanY).coerceAtLeast(0.01f)
                            val targetScale = (0.5f / maxSpan).coerceIn(1.5f, 5f)
                            
                            scale = targetScale
                            offset = Offset(
                                mapWidthPx / 2f - centerX * mapWidthPx * targetScale,
                                mapHeightPx / 2f - centerY * mapHeightPx * targetScale
                            )
                        } else {
                            scale = 1f
                            offset = Offset.Zero 
                        }
                    },
                    containerColor = Color.White,
                    contentColor = primaryColor,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Recenter View")
                }
            }

            // ── 6. Modern Bottom Card ──
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Handle bar
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray.copy(alpha = 0.5f))
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "PLANTATION WATCH",
                        style = MaterialTheme.typography.labelMedium,
                        color = primaryColor,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "High Vitality Area",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Plantations in this region show 94% survival rate. Keep up the good work!",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onViewRegionDetails,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text("View Region Details", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
fun MapFilterChip(label: String, isSelected: Boolean, onClick: () -> Unit = {}) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else Color.Black,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MapScreenPreview() {
    HasiruTheme {
        // We call MapScreenContent directly with dummy data to avoid Firebase initialization error
        MapScreenContent(
            plants = listOf(
                PlantEntry(1, "Banyan", "Ficus benghalensis", "2023-01-01", "Bangalore", PlantStatus.ALIVE, latitude = 12.9716, longitude = 77.5946),
                PlantEntry(2, "Neem", "Azadirachta indica", "2023-01-02", "Bangalore", PlantStatus.DEAD, latitude = 12.9816, longitude = 77.6046),
                PlantEntry(3, "Peepal", "Ficus religiosa", "2023-01-03", "Bangalore", PlantStatus.UNKNOWN, latitude = 12.9616, longitude = 77.5846)
            )
        )
    }
}
