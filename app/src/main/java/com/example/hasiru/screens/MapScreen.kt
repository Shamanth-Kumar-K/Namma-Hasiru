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
    val plants = homeViewModel.plants
    var selectedFilter by remember { mutableStateOf("All") }
    
    // Zoom and Pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var hasCentered by remember { mutableStateOf(false) }

    // Generate markers from real Firestore plants
    val realMarkers = remember(plants) {
        plants.map { plant ->
            // Convert Lat/Lng to fractions for Bangalore region (approx 12.8-13.1N, 77.4-77.7E)
            val x = ((plant.longitude - 77.4) / 0.35).toFloat().coerceIn(0.05f, 0.95f)
            val y = (1.0 - (plant.latitude - 12.85) / 0.25).toFloat().coerceIn(0.05f, 0.95f)
            
            // If location is missing, use stable pseudo-random based on ID
            val finalX = if (plant.longitude == 0.0) (Math.abs(plant.id) % 100) / 100f else x
            val finalY = if (plant.latitude == 0.0) (Math.abs(plant.id / 100) % 100) / 100f else y

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

            // Auto-center on first load when markers are ready
            LaunchedEffect(filteredMarkers, mapWidthPx, mapHeightPx) {
                if (filteredMarkers.isNotEmpty() && !hasCentered && mapWidthPx > 0) {
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
                    hasCentered = true
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
        MapScreen()
    }
}
