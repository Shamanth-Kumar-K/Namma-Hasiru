package com.example.hasiru.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.hasiru.ui.theme.Green40
import com.example.hasiru.utils.FileUtils
import com.example.hasiru.utils.StorageUtils
import com.example.hasiru.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    authViewModel: AuthViewModel = viewModel(),
    onBack: () -> Unit,
    onSave: () -> Unit
) {
    val context = LocalContext.current
    val user = authViewModel.currentUser
    val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()

    // Initialize state
    var name by remember { mutableStateOf(user?.displayName ?: "") }
    var email by remember { mutableStateOf(user?.email ?: "") }
    var phone by remember { mutableStateOf(user?.phoneNumber ?: "") }
    
    val scope = rememberCoroutineScope()

    // Profile Picture states
    var profileImageUri by remember { mutableStateOf<Uri?>(user?.photoUrl) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    var showImageSourceOptions by remember { mutableStateOf(false) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            profileImageUri = tempImageUri
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        profileImageUri = uri
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

    // Fetch latest name from Firestore if available
    LaunchedEffect(user?.uid) {
        user?.uid?.let { uid ->
            db.collection("users").document(uid).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val firestoreName = snapshot.getString("name")
                    if (!firestoreName.isNullOrBlank()) {
                        name = firestoreName
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Picture Section
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.3f))
                    .border(2.dp, Green40, CircleShape)
                    .clickable { showImageSourceOptions = true },
                contentAlignment = Alignment.Center
            ) {
                if (profileImageUri != null) {
                    AsyncImage(
                        model = profileImageUri,
                        contentDescription = "Profile Picture",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Green40,
                        modifier = Modifier.size(64.dp)
                    )
                }
                
                // Camera badge
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .size(32.dp)
                        .background(Green40, CircleShape)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                "Tap to change profile photo",
                style = MaterialTheme.typography.labelMedium,
                color = Green40,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = false
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            if (authViewModel.isLoading.value) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }

            authViewModel.errorMessage.value?.let { error ->
                Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Button(
                onClick = {
                    scope.launch {
                        authViewModel.isLoading.value = true
                        var imageUrl: String? = null
                        // Check if URI is local (needs upload) or already remote
                        if (profileImageUri != null && profileImageUri.toString().startsWith("content://")) {
                            imageUrl = StorageUtils.uploadImage(profileImageUri!!, "profile_images")
                        } else if (profileImageUri != null) {
                            imageUrl = profileImageUri.toString()
                        }

                        authViewModel.updateProfile(name, imageUrl) { success ->
                            authViewModel.isLoading.value = false
                            if (success) {
                                android.widget.Toast.makeText(context, "Profile updated successfully!", android.widget.Toast.LENGTH_SHORT).show()
                                onSave()
                            } else {
                                android.widget.Toast.makeText(context, "Failed to update profile.", android.widget.Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = name.isNotBlank() && !authViewModel.isLoading.value,
                colors = ButtonDefaults.buttonColors(containerColor = Green40)
            ) {
                Text("Save Changes", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                        "Change Profile Photo",
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
