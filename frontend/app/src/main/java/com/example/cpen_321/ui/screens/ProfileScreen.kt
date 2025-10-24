package com.example.cpen_321.ui.screens.profile

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.cpen_321.ui.viewmodels.UserViewModel
import com.example.cpen_321.utils.Base64ImageHelper
import com.example.cpen_321.utils.rememberBase64ImagePainter
import kotlinx.coroutines.launch

private const val TAG = "ProfileScreen"

@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit,
    viewModel: UserViewModel = hiltViewModel()
) {
    val userSettings by viewModel.userSettings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Local state for form fields
    var name by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var profilePictureUrl by remember { mutableStateOf("") }
    var selectedImageBase64 by remember { mutableStateOf("") } // Temporary storage for selected image
    var isUploadingImage by remember { mutableStateOf(false) }
    var hasUnsavedImage by remember { mutableStateOf(false) }
    var contactNumberError by remember { mutableStateOf("") }
    
    // Original values to track changes
    var originalName by remember { mutableStateOf("") }
    var originalBio by remember { mutableStateOf("") }
    var originalContactNumber by remember { mutableStateOf("") }
    var originalProfilePicture by remember { mutableStateOf("") }

    // Validate phone number (only digits)
    fun validatePhoneNumber(phone: String): String {
        return when {
            phone.isEmpty() -> "" // Empty is allowed
            phone.any { !it.isDigit() } -> "Phone number must contain only digits"
            phone.length < 10 -> "Phone number must be at least 10 digits"
            phone.length > 15 -> "Phone number must be no more than 15 digits"
            else -> ""
        }
    }

    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            Log.d(TAG, "Image selected, converting to Base64...")
            isUploadingImage = true
            hasUnsavedImage = true

            scope.launch {
                val result = Base64ImageHelper.encodeImageToBase64(it, context)
                result.onSuccess { base64String ->
                    Log.d(TAG, "Base64 encoding successful (${base64String.length} chars)")
                    selectedImageBase64 = base64String // Store temporarily, don't save yet
                    isUploadingImage = false
                    snackbarHostState.showSnackbar("Image selected - click Save to apply changes")
                }.onFailure { error ->
                    Log.e(TAG, "Base64 encoding failed", error)
                    isUploadingImage = false
                    hasUnsavedImage = false
                    snackbarHostState.showSnackbar("Failed to load image: ${error.message}")
                }
            }
        }
    }

    // Load user settings when screen opens
    LaunchedEffect(Unit) {
        Log.d(TAG, "Loading user settings...")
        viewModel.loadUserSettings()
        
        // Test image URL accessibility
        if (profilePictureUrl.isNotEmpty() && !profilePictureUrl.startsWith("data:image/")) {
            Log.d(TAG, "Testing image URL accessibility: $profilePictureUrl")
            // You can add a network test here if needed
        }
    }

    // Update form fields when user settings load
    LaunchedEffect(userSettings) {
        userSettings?.let { settings ->
            Log.d(TAG, "User settings received")
            name = settings.name ?: ""
            bio = settings.bio ?: ""
            contactNumber = settings.contactNumber ?: ""
            
            // Validate the loaded contact number
            contactNumberError = validatePhoneNumber(contactNumber)

            // Store original values for change detection
            originalName = settings.name ?: ""
            originalBio = settings.bio ?: ""
            originalContactNumber = settings.contactNumber ?: ""

            // CRITICAL FIX: Only load backend image if user hasn't selected a new one
            if (!hasUnsavedImage) {
                profilePictureUrl = settings.profilePicture ?: ""
                originalProfilePicture = settings.profilePicture ?: ""
                Log.d(TAG, "Loaded profile picture from backend (${profilePictureUrl.length} chars)")
                Log.d(TAG, "Profile picture URL: $profilePictureUrl")
                Log.d(TAG, "Profile picture type: ${if (profilePictureUrl.startsWith("data:image/")) "Base64" else "URL"}")
            } else {
                Log.d(TAG, "Keeping newly selected image (not overwriting with backend)")
            }
        }
    }

    // Clear selected image after successful save and update original values
    LaunchedEffect(isLoading) {
        if (!isLoading && selectedImageBase64.isNotEmpty()) {
            // Save was successful, clear the selected image and update original values
            selectedImageBase64 = ""
            hasUnsavedImage = false
            originalName = name
            originalBio = bio
            originalContactNumber = contactNumber
            originalProfilePicture = profilePictureUrl
            Log.d(TAG, "Cleared selected image after successful save and updated original values")
        }
    }

    // Check if there are any changes
    val hasChanges = remember(name, bio, contactNumber, selectedImageBase64, originalName, originalBio, originalContactNumber, originalProfilePicture) {
        val nameChanged = name != originalName
        val bioChanged = bio != originalBio
        val contactChanged = contactNumber != originalContactNumber
        val imageChanged = selectedImageBase64.isNotEmpty()
        
        Log.d(TAG, "Change detection - Name: $nameChanged, Bio: $bioChanged, Contact: $contactChanged, Image: $imageChanged")
        nameChanged || bioChanged || contactChanged || imageChanged
    }

    // Show success message and reload
    LaunchedEffect(successMessage) {
        successMessage?.let {
            Log.d(TAG, "Save successful, reloading settings...")
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()

            // Mark that we no longer have unsaved changes
            hasUnsavedImage = false
            
            // Update original values after successful save
            originalName = name
            originalBio = bio
            originalContactNumber = contactNumber
            originalProfilePicture = profilePictureUrl

            // Reload to get fresh data from backend
            viewModel.loadUserSettings()
        }
    }

    // Show error message
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Log.e(TAG, "Error: $it")
            snackbarHostState.showSnackbar("Error: $it")
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading && userSettings == null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading profile...")
                }
            } else {
                // Profile picture and change button row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile picture circle
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.Black, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        when {
                            isUploadingImage -> {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                            selectedImageBase64.isNotEmpty() -> {
                                // Show selected image (temporary)
                                Log.d(TAG, "Using selected image (temporary)")
                                rememberBase64ImagePainter(selectedImageBase64)
                            }
                            profilePictureUrl.isNotEmpty() -> {
                                // Check if it's a Base64 data URI or regular URL
                                val painter = if (profilePictureUrl.startsWith("data:image/")) {
                                    Log.d(TAG, "Using Base64 painter for profile picture")
                                    rememberBase64ImagePainter(profilePictureUrl)
                                } else {
                                    Log.d(TAG, "Using AsyncImagePainter for profile picture URL: $profilePictureUrl")
                                    // Add error handling and logging for AsyncImagePainter
                                    rememberAsyncImagePainter(
                                        model = ImageRequest.Builder(LocalContext.current)
                                            .data(profilePictureUrl)
                                            .crossfade(true)
                                            .placeholder(android.R.drawable.ic_menu_gallery) // Add placeholder
                                            .error(android.R.drawable.ic_menu_gallery) // Add error fallback
                                            .listener(
                                                onError = { _, result ->
                                                    Log.e(TAG, "AsyncImagePainter error: ${result.throwable?.message}")
                                                    Log.e(TAG, "Failed to load image from: $profilePictureUrl")
                                                },
                                                onSuccess = { _, _ ->
                                                    Log.d(TAG, "AsyncImagePainter success: Image loaded from $profilePictureUrl")
                                                }
                                            )
                                            .build()
                                    )
                                }
                                
                                Log.d(TAG, "Attempting to display profile picture")
                                Image(
                                    painter = painter,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            else -> {
                                Text(
                                    text = name.take(1).uppercase().ifEmpty { "?" },
                                    fontSize = 40.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }

                    // Change profile picture button
                    Button(
                        onClick = {
                            imagePickerLauncher.launch("image/*")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD54F)
                        ),
                        enabled = !isLoading && !isUploadingImage
                    ) {
                        Text(
                            text = if (isUploadingImage) "Processing..." else "Change Profile Picture",
                            color = Color.Black,
                            fontSize = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name:") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    enabled = !isLoading,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Bio field
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio:") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 5,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Contact Number field
                OutlinedTextField(
                    value = contactNumber,
                    onValueChange = { newValue ->
                        // Only allow digits
                        contactNumber = newValue.filter { it.isDigit() }
                        contactNumberError = validatePhoneNumber(contactNumber)
                    },
                    label = { Text("Phone Number:") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    enabled = !isLoading,
                    singleLine = true,
                    isError = contactNumberError.isNotEmpty(),
                    supportingText = if (contactNumberError.isNotEmpty()) {
                        { Text(contactNumberError, color = Color.Red) }
                    } else null
                )

                Spacer(modifier = Modifier.weight(1f))

                // Bottom buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD54F)
                        )
                    ) {
                        Text(
                            text = "Go Back",
                            color = Color.Black,
                            fontSize = 20.sp
                        )
                    }

                    Button(
                        onClick = {
                            val currentSettings = userSettings

                            viewModel.updateSettings(
                                name = name.ifEmpty { currentSettings?.name },
                                bio = bio.ifEmpty { null },
                                contactNumber = contactNumber.ifEmpty { null },
                                profilePicture = if (selectedImageBase64.isNotEmpty()) {
                                    selectedImageBase64
                                } else if (profilePictureUrl.isNotEmpty()) {
                                    profilePictureUrl
                                } else {
                                    null
                                },
                                preference = currentSettings?.preference,
                                budget = currentSettings?.budget,
                                radiusKm = currentSettings?.radiusKm
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(80.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD54F)
                        ),
                        enabled = !isLoading && !isUploadingImage && name.isNotEmpty() && hasChanges && contactNumberError.isEmpty()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(
                                text = "Save Profile",
                                color = Color.Black,
                                fontSize = 20.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}