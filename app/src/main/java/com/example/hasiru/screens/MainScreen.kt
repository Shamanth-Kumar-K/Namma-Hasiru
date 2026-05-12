package com.example.hasiru.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.hasiru.ui.theme.*


enum class BottomTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Rounded.Home),
    MAP("Map", Icons.Rounded.LocationOn),
    ADD("Add", Icons.Rounded.Add),
    PROFILE("Profile", Icons.Rounded.Person)
}

@Composable
fun MainScreen(
    navController: NavController,   // main navController for full-screen pushes
    onAddPlantClick: () -> Unit = {},
    onSignOut: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(BottomTab.HOME) }

    // Back press exits the app
    BackHandler {
        // do nothing, or show a toast; exits the app
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 20.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 12.dp,
                    tonalElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BottomTab.entries.forEach { tab ->
                            val isSelected = selectedTab == tab
                            val isAdd = tab == BottomTab.ADD
                            val contentColor by animateColorAsState(
                                targetValue = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                label = "tabColor"
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        if (tab == BottomTab.ADD) {
                                            navController.navigate("add_plant")
                                        } else {
                                            selectedTab = tab
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isAdd) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.label,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                } else {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .padding(vertical = 10.dp)
                                            .clip(CircleShape)
                                            .then(
                                                if (isSelected) Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                                else Modifier
                                            )
                                            .padding(horizontal = 16.dp)
                                    ) {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.label,
                                            tint = contentColor,
                                            modifier = Modifier.size(26.dp)
                                        )
                                        AnimatedVisibility(
                                            visible = isSelected,
                                            enter = fadeIn() + expandVertically(),
                                            exit = fadeOut() + shrinkVertically()
                                        ) {
                                            Text(
                                                text = tab.label,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = contentColor,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                BottomTab.HOME -> {
                    HomeScreen(
                        onViewAll = {
                            navController.navigate("plantationList")
                        },
                        onEntryClick = { plant ->
                            navController.navigate("plantDetail/${plant.id}")
                        },
                        onAddPlantClick = onAddPlantClick
                    )
                }
                BottomTab.MAP -> {
                    MapScreen(
                        onBack = { /* does nothing, tab switch is handled by bottom bar */ },
                        onMarkerClick = { marker ->
                            navController.navigate("plantDetail/${marker.id}")
                        },
                        onViewRegionDetails = {
                            navController.navigate("plantationList")
                        }
                    )
                }
                BottomTab.ADD -> {
                    // not used because we redirect to full screen
                }
                BottomTab.PROFILE -> {
                    ProfileScreen(
                        onBack = {},
                        onSettings = { navController.navigate("profileSettings") },
                        onSignOut = {
                            onSignOut()
                        },
                        onSpeciesGuide = { navController.navigate("speciesGuide") }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    HasiruTheme {
        MainScreen(
            navController = rememberNavController(),
            onSignOut = {}
        )
    }
}