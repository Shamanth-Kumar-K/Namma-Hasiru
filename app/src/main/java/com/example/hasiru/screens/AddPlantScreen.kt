package com.example.hasiru.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import coil.compose.AsyncImage
import com.example.hasiru.ui.theme.*
import com.example.hasiru.utils.FileUtils
import com.example.hasiru.utils.LocationManager
import com.example.hasiru.utils.StorageUtils
import com.example.hasiru.worker.PlantReminderWorker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlantScreen(onPlantSaved: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val locationManager = remember { LocationManager(context) }
    val repository = remember { com.example.hasiru.repository.AuthRepository() }

    // State Variables
    var commonName by remember { mutableStateOf("") }
    var scientificName by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var plantationDate by remember { mutableStateOf(SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())) }
    var selectedStatus by remember { mutableStateOf(PlantStatus.ALIVE) }
    
    // Image State
    var plantImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageSourceOptions by remember { mutableStateOf(false) }

    var isSaving by remember { mutableStateOf(false) }
    var isFetchingLocation by remember { mutableStateOf(false) }
    var lat by remember { mutableDoubleStateOf(0.0) }
    var lng by remember { mutableDoubleStateOf(0.0) }
    var locationData by remember { mutableStateOf("GPS not acquired") }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Launchers
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            plantImageUri = tempImageUri
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        plantImageUri = uri
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = FileUtils.createImageFile(context)
            val uri = FileUtils.getUriForFile(context, file)
            tempImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    // Location Fetching Logic
    val fetchLocation = {
        isFetchingLocation = true
        locationData = "Connecting to GPS..."
        locationManager.getCurrentLocation { latitude, longitude, error ->
            if (latitude != null && longitude != null) {
                lat = latitude
                lng = longitude
                locationData = "Lat: ${String.format(Locale.US, "%.4f", lat)}, Lng: ${String.format(Locale.US, "%.4f", lng)}"
            } else {
                locationData = error ?: "Failed to get location."
            }
            isFetchingLocation = false
        }
    }

    // Location Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            fetchLocation()
        } else {
            locationData = "Location permission denied."
        }
    }

    // Notification Permission Launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            // Optionally handle permission denial
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text("Register Plantation", fontWeight = FontWeight.Bold, color = Green20)
                },
                navigationIcon = {
                    IconButton(onClick = onPlantSaved) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Green20)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SoftGrey)
            )
        },
        containerColor = SoftGrey
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Photo Section
            PhotoCaptureSection(
                imageUri = plantImageUri,
                onTakePhoto = { showImageSourceOptions = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Identification Section
            SectionHeader(title = "Identification")
            PlantDetailsInput(
                commonName = commonName,
                onCommonNameChange = { commonName = it },
                scientificName = scientificName,
                onScientificNameChange = { scientificName = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Location & Status Section
            SectionHeader(title = "Location & Status")
            LogisticsInfo(
                location = if (isFetchingLocation) locationData else locationData,
                onLocationClick = {
                    val fineLocationGranted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                    val coarseLocationGranted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

                    if (fineLocationGranted && coarseLocationGranted) {
                        fetchLocation()
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                        )
                    }
                },
                date = plantationDate,
                onDateClick = { showDatePicker = true },
                selectedStatus = selectedStatus,
                onStatusSelected = { selectedStatus = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 4. Notes Section
            SectionHeader(title = "Additional Notes")
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Soil condition, surrounding plants...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Green40,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 5. Save Button with WorkManager Reminder
            Button(
                onClick = {
                    isSaving = true
                    scope.launch {
                        var imageUrl: String? = null
                        plantImageUri?.let { uri ->
                            imageUrl = StorageUtils.uploadImage(uri, "plant_images")
                        }

                        repository.saveNewPlant(
                            name = commonName,
                            scientificName = scientificName,
                            notes = notes,
                            status = selectedStatus.name,
                            lat = lat,
                            lng = lng,
                            imageUrl = imageUrl
                        ) { success, docId ->
                            isSaving = false
                            if (success && docId != null) {
                                android.widget.Toast.makeText(context, "Plant registered successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                
                                // --- WORKMANAGER REMINDER LOGIC ---
                                val plantId = docId.hashCode()
                                val reminderRequest = OneTimeWorkRequestBuilder<PlantReminderWorker>()
                                    .setInitialDelay(90, TimeUnit.DAYS)
                                    .setInputData(
                                        workDataOf(
                                            "plant_name" to commonName,
                                            "plant_id" to plantId
                                        )
                                    )
                                    .build()

                                WorkManager.getInstance(context).enqueue(reminderRequest)
                                // ----------------------------------

                                onPlantSaved()
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Failed to register plant. Try again.")
                                }
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = commonName.isNotBlank() && !isFetchingLocation && !isSaving && plantImageUri != null,
                colors = ButtonDefaults.buttonColors(containerColor = Green40)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Register Plantation", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showImageSourceOptions) {
            ModalBottomSheet(
                onDismissRequest = { showImageSourceOptions = false },
                containerColor = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp, start = 16.dp, end = 16.dp, top = 8.dp)
                ) {
                    Text(
                        "Select Plant Photo",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showImageSourceOptions = false
                                val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                                if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                                    val file = FileUtils.createImageFile(context)
                                    val uri = FileUtils.getUriForFile(context, file)
                                    tempImageUri = uri
                                    cameraLauncher.launch(uri)
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Green40)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Take Photo", fontSize = 16.sp)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showImageSourceOptions = false
                                galleryLauncher.launch("image/*")
                            }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Green40)
                        Spacer(modifier = Modifier.width(16.dp))
                        Text("Choose from Gallery", fontSize = 16.sp)
                    }
                }
            }
        }

        // Date Picker Logic
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            plantationDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))
                        }
                        showDatePicker = false
                    }) { Text("OK", color = Green40) }
                }
            ) { DatePicker(state = datePickerState) }
        }
    }
}

// --- HELPER UI COMPONENTS ---

@Composable
private fun PhotoCaptureSection(imageUri: Uri?, onTakePhoto: () -> Unit) {
    OutlinedCard(
        onClick = onTakePhoto,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth().height(180.dp),
        border = BorderStroke(1.dp, if (imageUri != null) Green40 else Color.LightGray),
        colors = CardDefaults.outlinedCardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Plant Photo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Edit badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                        .size(36.dp)
                        .background(Green40, CircleShape)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = null,
                        tint = Green40,
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Add Plant Photo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Green40,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun PlantDetailsInput(
    commonName: String, onCommonNameChange: (String) -> Unit,
    scientificName: String, onScientificNameChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = commonName, onValueChange = onCommonNameChange,
            label = { Text("Common Name") }, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
        )
        OutlinedTextField(
            value = scientificName, onValueChange = onScientificNameChange,
            label = { Text("Scientific Name (Optional)") }, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White)
        )
    }
}

@Composable
private fun LogisticsInfo(
    location: String, onLocationClick: () -> Unit,
    date: String, onDateClick: () -> Unit,
    selectedStatus: PlantStatus, onStatusSelected: (PlantStatus) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Location Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, null, tint = Green40, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(location, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onLocationClick) { Text("Get GPS", color = Green40, fontWeight = FontWeight.Bold) }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Date Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onDateClick() }
                    .padding(vertical = 8.dp)
            ) {
                Icon(Icons.Default.Park, null, tint = Green40, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("PLANTATION DATE", style = MaterialTheme.typography.labelSmall, color = TextGrey, fontWeight = FontWeight.Bold)
                    Text(date, style = MaterialTheme.typography.bodyMedium)
                }
                Text("Change", color = Green40, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = SoftGrey.copy(alpha = 0.5f))

            // Status Chips
            Text("Initial Health", style = MaterialTheme.typography.labelSmall, color = TextGrey, modifier = Modifier.padding(top = 8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                PlantStatus.entries.forEach { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { onStatusSelected(status) },
                        label = { Text(status.label) },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = status.color.copy(alpha = 0.2f), selectedLabelColor = status.color)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelLarge,
        color = Green40,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
    )
}
