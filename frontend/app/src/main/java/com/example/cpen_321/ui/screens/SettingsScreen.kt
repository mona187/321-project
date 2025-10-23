package com.example.cpen_321.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.cpen_321.data.model.User
import com.example.cpen_321.fake.FakeSettingsViewModel
import com.example.cpen_321.ui.viewmodels.SettingsViewModel
import kotlinx.coroutines.launch
import com.example.cpen_321.fake.FakeAuthViewModel
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.filled.StarOutline
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    viewModel: SettingsViewModel = hiltViewModel(),
    firstTimeSetup: Boolean = false
) {
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Load user info when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadUserSettings()
    }

    // Navigate after saving successfully
    if (isSaved) {
        LaunchedEffect(Unit) {
            viewModel.clearSavedFlag()
            navController.navigate("home") {
                popUpTo("settings") { inclusive = true }
            }
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Profile Settings") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (firstTimeSetup) "Complete your profile" else "Edit your profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(20.dp))

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                // ----- Profile Picture -----
                var imageUri by remember { mutableStateOf<Uri?>(null) }

                // launch gallery picker
                val imagePickerLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.GetContent()
                ) { uri: Uri? -> imageUri = uri }

                val painter = rememberAsyncImagePainter(
                    model = imageUri ?: user?.profilePicture
                    ?: "https://cdn-icons-png.flaticon.com/512/1077/1077114.png"
                )

                Image(
                    painter = painter,
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Gray, CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") }
                )

                Spacer(Modifier.height(24.dp))

                // ----- Name -----
                var name by remember { mutableStateOf(user?.name ?: "") }
                // Update name when user data changes
                LaunchedEffect(user) {
                    user?.name?.let { name = it }
                }
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                // ----- Credibility Score -----
                val credibilityScore = user?.credibilityScore ?: 4.2  // fallback if null
                Text(
                    text = "Credibility Score",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val filledStars = credibilityScore.toInt()
                    val halfStar = credibilityScore - filledStars >= 0.5

                    repeat(filledStars) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    if (halfStar) {
                        Icon(
                            imageVector = Icons.Default.StarHalf,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    repeat(5 - filledStars - if (halfStar) 1 else 0) {
                        Icon(
                            imageVector = Icons.Default.StarOutline,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))


                Spacer(Modifier.height(16.dp))

                // ----- Bio -----
                var bio by remember { mutableStateOf(user?.bio ?: "") }
                // Update bio when user data changes
                LaunchedEffect(user) {
                    user?.bio?.let { bio = it }
                }
                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text("Bio") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(16.dp))

                // ----- Contact Number -----
                var contactNumber by remember { mutableStateOf(user?.contactNumber ?: "") }
                // Update contact number when user data changes
                LaunchedEffect(user) {
                    user?.contactNumber?.let { contactNumber = it }
                }
                OutlinedTextField(
                    value = contactNumber,
                    onValueChange = { contactNumber = it },
                    label = { Text("Contact Number") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                // ----- Budget -----
                var budgetText by remember { mutableStateOf(user?.budget?.toString() ?: "") }
                // Update budget when user data changes
                LaunchedEffect(user) {
                    user?.budget?.let { budgetText = it.toString() }
                }
                OutlinedTextField(
                    value = budgetText,
                    onValueChange = { newValue ->
                        // only allow numbers and optional decimal
                        if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*\$"))) {
                            budgetText = newValue
                        }
                    },
                    label = { Text("Budget (\$)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                // ----- Cuisine Preferences -----
                val cuisineOptions = listOf(
                    "Italian", "Indian", "Chinese", "Japanese",
                    "Mexican", "Greek", "Thai", "American"
                )
                val selectedPrefs = remember { mutableStateListOf<String>() }
                // Update preferences when user data changes
                LaunchedEffect(user) {
                    user?.preference?.let { preferences ->
                        selectedPrefs.clear()
                        selectedPrefs.addAll(preferences)
                    }
                }

                Text(
                    text = "Cuisine Preferences",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                // Debug: Show current preferences
                Text(
                    text = "Selected: ${selectedPrefs.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cuisineOptions.forEach { cuisine ->
                        FilterChip(
                            selected = selectedPrefs.contains(cuisine),
                            onClick = {
                                if (selectedPrefs.contains(cuisine)) {
                                    selectedPrefs.remove(cuisine)
                                } else {
                                    selectedPrefs.add(cuisine)
                                }
                                // Debug: Print current preferences
                                println("DEBUG: Selected preferences: $selectedPrefs")
                            },
                            label = { Text(cuisine) }
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ----- Location Radius -----
                var radius by remember { mutableStateOf(user?.radiusKm?.toFloat() ?: 10f) }
                // Update radius when user data changes
                LaunchedEffect(user) {
                    user?.radiusKm?.let { radius = it.toFloat() }
                }
                Text("Maximum Search Radius: ${radius.toInt()} km")
                Slider(
                    value = radius,
                    onValueChange = { radius = it },
                    valueRange = 1f..50f
                )

                Spacer(Modifier.height(32.dp))

                // Debug: Show what will be saved
                Text(
                    text = "Will save preferences: ${selectedPrefs.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                Spacer(Modifier.height(8.dp))

                // ----- Save Button -----
                Button(
                    onClick = {
                        // Debug: Print what we're about to save
                        println("DEBUG: Saving preferences: $selectedPrefs")
                        println("DEBUG: Preferences as list: ${selectedPrefs.toList()}")
                        
                        val updatedUser = (user ?: User(
                            userId = 0,
                            name = name,
                            bio = bio,
                            contactNumber = contactNumber,
                            preference = selectedPrefs.toList(),
                            profilePicture = imageUri?.toString(),
                            credibilityScore = null,
                            budget = budgetText.toDoubleOrNull(),
                            radiusKm = radius.toDouble(),
                            status = null,
                            roomId = null,
                            groupId = null
                        )).copy(
                            name = name,
                            bio = bio,
                            contactNumber = contactNumber,
                            preference = selectedPrefs.toList(),
                            profilePicture = imageUri?.toString() ?: user?.profilePicture,
                            budget = budgetText.toDoubleOrNull(),
                            radiusKm = radius.toDouble()
                        )

                        coroutineScope.launch {
                            viewModel.saveUserSettings(updatedUser, firstTimeSetup)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    Text(if (firstTimeSetup) "Save and Continue" else "Save Changes")
                }

                if (errorMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(Modifier.height(40.dp))
            }
        }
    }
}
