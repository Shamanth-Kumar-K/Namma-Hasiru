package com.example.hasiru.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
    var showImageSourceOptions by remember { mutableStateOf(false) }

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

            Spacer(modifier = Modifier.weight(1f))

            // ── Save Update button ──
            var isSaving by remember { mutableStateOf(false) }
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
                            "timestamp" to com.google.firebase.Timestamp.now()
                        )

                        val docId = if (documentId.isNotEmpty()) documentId else plantId.toString()

                        db.collection("plants").document(docId)
                            .collection("updates").add(updateData)
                            .addOnSuccessListener {
                                // Also update the main plant status
                                db.collection("plants").document(docId)
                                    .update("status", selectedStatus?.name ?: "ALIVE")
                                
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
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Green40,
                    disabledContainerColor = Green40.copy(alpha = 0.3f)
                ),
                enabled = selectedStatus != null && capturedImageUri != null && !isSaving,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Update", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
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
    val animatedElevation by animateDpAsState(if (selected) 8.dp else 2.dp)
    val animatedBackground by animateColorAsState(if (selected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)

    Surface(
        modifier = modifier
            .height(90.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = animatedBackground,
        border = if (selected) BorderStroke(2.dp, color) else null,
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
                tint = if (selected) color else color.copy(alpha = 0.5f),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                label,
                fontWeight = FontWeight.Bold,
                color = if (selected) color else TextGrey,
                style = MaterialTheme.typography.labelLarge
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
