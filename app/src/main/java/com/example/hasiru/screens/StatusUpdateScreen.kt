package com.example.hasiru.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.hasiru.ui.theme.*
import com.example.hasiru.utils.FileUtils
import com.example.hasiru.utils.StorageUtils
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusUpdateScreen(
    plantId: Int,
    documentId: String = "",
    plantName: String = "Tree",
    onBack: () -> Unit = {},
    onSaved: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedStatus by remember { mutableStateOf<PlantStatus?>(null) }
    
    // Camera & Gallery states
    var capturedImageUri by remember { mutableStateOf<Uri?>(null) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var notes by remember { mutableStateOf("") }
    var showImageSourceOptions by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            capturedImageUri = tempImageUri
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        capturedImageUri = uri
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val file = FileUtils.createImageFile(context)
            val uri = FileUtils.getUriForFile(context, file)
            tempImageUri = uri
            cameraLauncher.launch(uri)
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Update Status", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = SoftGrey.copy(alpha = 0.5f)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // ── Tree name header ──
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(Green80.copy(alpha = 0.3f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Park,
                                contentDescription = null,
                                tint = Green40,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                plantName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                "Tree ID: #$plantId",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextGrey
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ── Status selection ──
                Text(
                    "Current Status",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatusOption(
                        label = "Alive",
                        color = Green40,
                        icon = Icons.Filled.CheckCircle,
                        selected = selectedStatus == PlantStatus.ALIVE,
                        onClick = { selectedStatus = PlantStatus.ALIVE },
                        modifier = Modifier.weight(1f)
                    )
                    StatusOption(
                        label = "Dead",
                        color = Red40,
                        icon = Icons.Filled.Cancel,
                        selected = selectedStatus == PlantStatus.DEAD,
                        onClick = { selectedStatus = PlantStatus.DEAD },
                        modifier = Modifier.weight(1f)
                    )
                    StatusOption(
                        label = "Unknown",
                        color = Color.Gray,
                        icon = Icons.AutoMirrored.Filled.Help,
                        selected = selectedStatus == PlantStatus.UNKNOWN,
                        onClick = { selectedStatus = PlantStatus.UNKNOWN },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // ── Growth photo capture ──
                Text(
                    "Growth Photo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(16.dp))
                PhotoCaptureBox(
                    imageUri = capturedImageUri,
                    onClick = { showImageSourceOptions = true }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ── Notes field ──
                Text(
                    "Update Notes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp),
                    placeholder = { Text("How is the tree doing? Any visible growth or care needed?") },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green40,
                        unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ── Save Update button fixed at bottom ──
            Button(
                onClick = {
                    isSaving = true
                    scope.launch {
                        var imageUrl: String? = null
                        capturedImageUri?.let { uri ->
                            imageUrl = StorageUtils.uploadImage(uri, "update_images")
                        }

                        val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                        val updateData = hashMapOf(
                            "status" to (selectedStatus?.name ?: "ALIVE"),
                            "imageUrl" to (imageUrl ?: ""),
                            "notes" to notes,
                            "timestamp" to com.google.firebase.Timestamp.now(),
                            "date" to java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                        )

                        val docId = if (documentId.isNotEmpty()) documentId else plantId.toString()

                        db.collection("plants").document(docId)
                            .collection("updates").add(updateData)
                            .addOnSuccessListener {
                                // Also update the main plant status and latest photo
                                val mainUpdate = mutableMapOf<String, Any>(
                                    "status" to (selectedStatus?.name ?: "ALIVE")
                                )
                                if (imageUrl != null) {
                                    mainUpdate["imageUrl"] = imageUrl
                                }
                                
                                db.collection("plants").document(docId)
                                    .update(mainUpdate)
                                
                                isSaving = false
                                android.widget.Toast.makeText(context, "Status updated successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                onSaved()
                            }
                            .addOnFailureListener {
                                isSaving = false
                                android.widget.Toast.makeText(context, "Failed to update status.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green40,
                    disabledContainerColor = Green40.copy(alpha = 0.3f)
                ),
                enabled = selectedStatus != null && !isSaving,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Update", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = White)
                }
            }
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
                        "Select Photo Source",
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
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
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
    }
}


@Composable
fun StatusOption(
    label: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "scale"
    )
    val animatedElevation by animateDpAsState(
        targetValue = if (selected) 6.dp else 2.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "elevation"
    )
    val containerColor by animateColorAsState(
        if (selected) color.copy(alpha = 0.12f) else White,
        label = "background"
    )
    val contentColor by animateColorAsState(
        if (selected) color else TextGrey,
        label = "content"
    )
    val borderColor by animateColorAsState(
        if (selected) color else BorderGrey.copy(alpha = 0.5f),
        label = "border"
    )

    Surface(
        onClick = onClick,
        modifier = modifier
            .height(105.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(24.dp),
        color = containerColor,
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
        shadowElevation = animatedElevation
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(if (selected) 38.dp else 32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                label,
                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Bold,
                color = contentColor,
                style = MaterialTheme.typography.labelLarge,
                fontSize = if (selected) 15.sp else 14.sp
            )
        }
    }
}

@Composable
fun PhotoCaptureBox(
    imageUri: Uri?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surface)
            .drawBehind {
                if (imageUri == null) {
                    drawRoundRect(
                        color = Green40.copy(alpha = 0.4f),
                        style = Stroke(
                            width = 2.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        ),
                        cornerRadius = CornerRadius(20.dp.toPx())
                    )
                }
            }
            .border(
                width = if (imageUri != null) 2.dp else 0.dp,
                color = if (imageUri != null) Green40 else Color.Transparent,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (imageUri == null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Green80.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AddAPhoto,
                        contentDescription = null,
                        tint = Green40,
                        modifier = Modifier.size(32.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Tap to capture growth photo",
                    color = Green40,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            AsyncImage(
                model = imageUri,
                contentDescription = "Growth Photo",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Overlay to change photo
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.1f)),
                contentAlignment = Alignment.BottomEnd
            ) {
                IconButton(
                    onClick = onClick,
                    modifier = Modifier
                        .padding(8.dp)
                        .background(Color.White, CircleShape)
                        .size(36.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Photo", tint = Green40, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StatusUpdateScreenPreview() {
    HasiruTheme {
        StatusUpdateScreen(
            plantId = 1,
            plantName = "Neem Tree"
        )
    }
}
