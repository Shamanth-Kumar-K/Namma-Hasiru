package com.example.hasiru.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.hasiru.R
import com.example.hasiru.ui.theme.*
import com.example.hasiru.viewmodel.AuthViewModel
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel = viewModel(),
    onBack: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onSettings: () -> Unit = {},
    onSpeciesGuide: () -> Unit = {}
) {
    val user = authViewModel.currentUser
    val db = FirebaseFirestore.getInstance()

    // LIVE STATE FROM FIRESTORE
    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var personalPlantCount by remember { mutableIntStateOf(0) }
    var uniqueAreasCount by remember { mutableIntStateOf(0) }

    // Fetch data whenever this screen opens
    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            // 1. Listen to user profile (for name, level, etc.)
            db.collection("users").document(uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null && snapshot.exists()) {
                        userProfile = snapshot.toObject(UserProfile::class.java)
                    }
                }

            // 2. Listen to user's personal plants to get real-time stats
            db.collection("plants")
                .whereEqualTo("uid", uid)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        personalPlantCount = snapshot.size()
                        
                        // Calculate unique areas (roughly by unique lat/lng strings)
                        val areas = snapshot.documents.mapNotNull { 
                            val lat = it.getDouble("latitude") ?: 0.0
                            val lng = it.getDouble("longitude") ?: 0.0
                            // Round to 3 decimal places to group nearby plants into one "area"
                            "${String.format(Locale.US, "%.3f", lat)},${String.format(Locale.US, "%.3f", lng)}"
                        }.toSet()
                        uniqueAreasCount = areas.size
                    }
                }
        }
    }

    val userName = userProfile?.name ?: user?.displayName ?: "Eco Guardian"
    // Prefer personalPlantCount if it's non-zero, otherwise use what's in profile
    val treesPlanted = if (personalPlantCount > 0) personalPlantCount else (userProfile?.treesPlanted ?: 0)
    val areasCovered = if (uniqueAreasCount > 0) uniqueAreasCount else (userProfile?.areasCovered ?: 0)
    val userLevel = when {
        treesPlanted >= 20 -> "Forest Maker"
        treesPlanted >= 10 -> "Tree Hugger"
        treesPlanted >= 5 -> "Sapling"
        else -> "Seedling"
    }

    ProfileScreenContent(
        userName = userName,
        userLevel = userLevel,
        treesPlanted = treesPlanted,
        areasCovered = areasCovered,
        userPhotoUrl = userProfile?.photoUrl ?: user?.photoUrl,
        onBack = onBack,
        onSignOut = onSignOut,
        onSettings = onSettings,
        onSpeciesGuide = onSpeciesGuide,
        onEditName = { /* TODO: Implement name edit logic */ },
        onEditPhoto = { /* TODO: Implement photo edit logic */ }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenContent(
    userName: String,
    userLevel: String,
    treesPlanted: Int,
    areasCovered: Int,
    userPhotoUrl: Any?,
    onBack: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onSettings: () -> Unit = {},
    onSpeciesGuide: () -> Unit = {},
    onEditName: () -> Unit = {},
    onEditPhoto: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_back),
                            contentDescription = "Back",
                            tint = Color.Black
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = SoftGrey
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Header Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    colors = CardDefaults.cardColors(containerColor = Green40)
                ) {
                    Column(
                        modifier = Modifier
                            .background(Brush.verticalGradient(listOf(Green40, Green20)))
                            .padding(24.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            AsyncImage(
                                model = userPhotoUrl,
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(4.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                                    .clickable(onClick = onEditPhoto),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(id = R.drawable.app_logo),
                                error = painterResource(id = R.drawable.app_logo)
                            )
                            Surface(
                                color = Amber40,
                                shape = CircleShape,
                                modifier = Modifier
                                    .size(32.dp)
                                    .offset(x = 4.dp, y = 4.dp)
                                    .clickable(onClick = onEditPhoto),
                                border = BorderStroke(2.dp, Color.White)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_camera),
                                        contentDescription = "Change Photo",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable(onClick = onEditName)
                        ) {
                            Text(
                                userName,
                                color = Color.White,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                painter = painterResource(id = R.drawable.ic_edit),
                                contentDescription = "Edit Name",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(
                                userLevel,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = R.drawable.ic_tree,
                    label = "TREES PLANTED",
                    count = "$treesPlanted",
                    color = Green40
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = R.drawable.ic_location,
                    label = "AREAS COVERED",
                    count = "$areasCovered",
                    color = Brown40
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Options Section
            Text(
                "ACCOUNT SETTINGS",
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                style = MaterialTheme.typography.labelMedium,
                color = TextGrey,
                fontWeight = FontWeight.Bold
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.White)
            ) {
                ProfileOptionItem(
                    icon = R.drawable.ic_settings,
                    title = "Settings",
                    onClick = onSettings
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = SoftGrey)
                ProfileOptionItem(
                    icon = R.drawable.ic_guide,
                    title = "Species Guide",
                    onClick = onSpeciesGuide
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = SoftGrey)
                ProfileOptionItem(
                    icon = R.drawable.ic_logout,
                    title = "Sign Out",
                    onClick = onSignOut,
                    isDestructive = true
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: Int,
    label: String,
    count: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = CircleShape,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                count,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.Black
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = TextGrey,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ProfileOptionItem(
    icon: Int,
    title: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDestructive) Red40.copy(alpha = 0.1f) else Green40.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = if (isDestructive) Red40 else Green40,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = if (isDestructive) Red40 else Color.Black,
            modifier = Modifier.weight(1f)
        )
        Icon(
            painter = painterResource(id = R.drawable.ic_chevron_right),
            contentDescription = null,
            tint = BorderGrey,
            modifier = Modifier.size(16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    HasiruTheme {
        ProfileScreenContent(
            userName = "Eco Guardian",
            userLevel = "Seedling",
            treesPlanted = 5,
            areasCovered = 2,
            userPhotoUrl = null
        )
    }
}
