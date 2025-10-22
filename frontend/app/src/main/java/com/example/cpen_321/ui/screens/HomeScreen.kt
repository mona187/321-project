package com.example.cpen_321.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.cpen_321.R
import com.example.cpen_321.ui.components.MainBottomBar
import com.example.cpen_321.ui.viewmodels.AuthViewModel
import com.example.cpen_321.ui.viewmodels.MatchViewModel
import com.example.cpen_321.ui.viewmodels.GroupViewModel
import com.example.cpen_321.ui.viewmodels.UserViewModel

// Add font
val PlaywriteFontFamily = FontFamily(
    Font(R.font.playwrite_usmodern_variablefont_wght)
)

@Composable
fun HomeScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
    matchViewModel: MatchViewModel = hiltViewModel(),
    groupViewModel: GroupViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    // Collect states
    val currentUser by authViewModel.currentUser.collectAsState()
    val userSettings by userViewModel.userSettings.collectAsState()
    val currentGroup by groupViewModel.currentGroup.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Load user settings when screen opens
    LaunchedEffect(Unit) {
        userViewModel.loadUserSettings()
    }

    // Check if user has an active group
    LaunchedEffect(Unit) {
        try {
            groupViewModel.loadGroupStatus()
        } catch (e: Exception) {
            // No active group, that's fine
        }
    }

    Scaffold(
        bottomBar = { MainBottomBar(navController = navController) },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Profile icon in top right
            IconButton(
                onClick = {
                    navController.navigate("profile_config")
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Filled.AccountCircle,
                    contentDescription = "Profile",
                    modifier = Modifier
                        .width(48.dp)
                        .height(48.dp)
                )
            }

            // Center content with buttons
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Welcome text with user's name
                Text(
                    text = "Welcome${currentUser?.name?.let { ", $it" } ?: ""}!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontFamily = PlaywriteFontFamily,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Show credibility score if available
                currentUser?.credibilityScore?.let { score ->
                    Text(
                        text = "Credibility Score: ${score.toInt()}",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Start Matchmaking Button
                Button(
                    onClick = {
                        // Check if user has preferences set
                        val cuisines = userSettings?.preference ?: emptyList()
                        val budget = userSettings?.budget ?: 50.0
                        val radius = userSettings?.radiusKm ?: 5.0

                        if (cuisines.isEmpty()) {
                            // Navigate to preferences screen first
                            navController.navigate("preferences")
                        } else {
                            // Start matching with user preferences
                            matchViewModel.joinMatching(
                                cuisine = cuisines,
                                budget = budget,
                                radiusKm = radius
                            )
                            navController.navigate("waiting_room")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD54F)
                    )
                ) {
                    Text(
                        text = "Start Matchmaking",
                        color = Color.Black,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Current Groups Button
                Button(
                    onClick = {
                        // Check if user has an active group
                        if (currentGroup != null) {
                            // Navigate to active group
                            navController.navigate("group/${currentGroup!!.groupId}")
                        } else {
                            // Navigate to view groups screen or show message
                            navController.navigate("view_groups")
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentGroup != null) Color(0xFF4CAF50) else Color(0xFFFFD54F)
                    )
                ) {
                    Text(
                        text = if (currentGroup != null) "View Active Group" else "Current Groups",
                        color = Color.Black,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}