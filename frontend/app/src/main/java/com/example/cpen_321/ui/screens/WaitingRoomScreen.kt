package com.example.cpen_321.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cpen_321.data.model.UserProfile
import com.example.cpen_321.ui.viewmodels.MatchViewModel
import kotlinx.coroutines.delay

@Composable
fun WaitingRoomScreen(
    navController: NavController,
    viewModel: MatchViewModel = hiltViewModel()
) {
    // Collect states from ViewModel
    val currentRoom by viewModel.currentRoom.collectAsState()
    val roomMembers by viewModel.roomMembers.collectAsState()
    val timeRemaining by viewModel.timeRemaining.collectAsState()
    val groupReady by viewModel.groupReady.collectAsState()
    val groupId by viewModel.groupId.collectAsState()
    val roomExpired by viewModel.roomExpired.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    val maxNumberOfPeople = currentRoom?.maxMembers ?: 10
    val minNumberOfPeople = 4

    var showLeaveDialog by remember { mutableStateOf(false) }
    var showFailureDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Convert milliseconds to seconds for display
    val timeRemainingSeconds = (timeRemaining / 1000).toInt()

    // Animate progress for visual feedback
    val progress by animateFloatAsState(
        targetValue = roomMembers.size.toFloat() / maxNumberOfPeople.toFloat(),
        animationSpec = tween(durationMillis = 500),
        label = "progress"
    )

    // Navigate to group when ready
    LaunchedEffect(groupReady, groupId) {
        if (groupReady && groupId != null) {
            // Small delay to show "Group Ready" message
            delay(1000L)
            navController.navigate("vote_restaurant/$groupId") {
                popUpTo("waiting_room") { inclusive = true }
            }
        }
    }

    // Handle room expired
    LaunchedEffect(roomExpired) {
        if (roomExpired) {
            if (roomMembers.size >= minNumberOfPeople) {
                // Still enough people, backend will create group
                // Wait for groupReady signal
            } else {
                showFailureDialog = true
            }
        }
    }

    // Show error messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFFFF6B6B),
                    contentColor = Color.White
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!groupReady) {
                // Normal waiting room UI
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top section
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Spacer(modifier = Modifier.height(24.dp))

                        // Title
                        Text(
                            "Waiting Room",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Finding your perfect dining group...",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Timer Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFF9C4)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = "Timer",
                                        tint = if (timeRemainingSeconds <= 10) Color.Red else Color.Black,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        String.format(
                                            "%d:%02d",
                                            timeRemainingSeconds / 60,
                                            timeRemainingSeconds % 60
                                        ),
                                        style = MaterialTheme.typography.displaySmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (timeRemainingSeconds <= 10) Color.Red else Color.Black
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = if (timeRemainingSeconds <= 10) "Finishing soon!" else "Time remaining",
                                    fontSize = 14.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Room Info Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                // Member count with progress
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = Color(0xFFFFD54F),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Members",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Text(
                                        text = "${roomMembers.size}/$maxNumberOfPeople",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (roomMembers.size >= minNumberOfPeople)
                                            Color(0xFF4CAF50) else Color.Gray
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Progress bar
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = if (roomMembers.size >= minNumberOfPeople)
                                        Color(0xFF4CAF50) else Color(0xFFFFD54F),
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = if (roomMembers.size >= minNumberOfPeople)
                                        "Minimum members reached! ✓"
                                    else
                                        "Waiting for at least $minNumberOfPeople members...",
                                    fontSize = 12.sp,
                                    color = if (roomMembers.size >= minNumberOfPeople)
                                        Color(0xFF4CAF50) else Color.Gray,
                                    fontWeight = if (roomMembers.size >= minNumberOfPeople)
                                        FontWeight.SemiBold else FontWeight.Normal
                                )

                                // Cuisine info if available
                                currentRoom?.cuisine?.let { cuisine ->
                                    Spacer(modifier = Modifier.height(16.dp))
                                    HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Restaurant,
                                            contentDescription = null,
                                            tint = Color(0xFFFFD54F),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Cuisine: $cuisine",
                                            fontSize = 14.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                // Budget and radius info if available
                                if (currentRoom?.averageBudget != null || currentRoom?.averageRadius != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        currentRoom!!.averageBudget?.let { budget ->
                                            Text(
                                                text = "Budget: $$budget",
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                        }
                                        currentRoom!!.averageRadius?.let { radius ->
                                            Text(
                                                text = "Radius: ${radius}km",
                                                fontSize = 12.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Members section
                        Text(
                            text = "Current Members",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        UserBubbleRow(roomMembers)
                    }

                    // Bottom section - Leave button
                    Column(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = { showLeaveDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFF6B6B)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.ExitToApp,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Leave Room",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            } else {
                // Group Ready UI
                GroupReadyContent()
            }
        }
    }

    // Leave confirmation dialog
    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            title = {
                Text(
                    "Leave Waiting Room?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to leave? You'll lose your spot in this room.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLeaveDialog = false
                        viewModel.leaveRoom()
                        navController.popBackStack()
                    }
                ) {
                    Text("Leave", color = Color(0xFFFF6B6B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text("Stay", color = Color.Black)
                }
            },
            containerColor = Color.White
        )
    }

    // Failure dialog - not enough people
    if (showFailureDialog) {
        AlertDialog(
            onDismissRequest = {
                // Can't dismiss - must acknowledge
            },
            title = {
                Text(
                    "Unable to Create Group",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6B6B)
                )
            },
            text = {
                Column {
                    Text("The waiting room timer expired, but not enough people joined.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Minimum $minNumberOfPeople members required to form a group.",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearRoomExpired()
                        showFailureDialog = false
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    }
                ) {
                    Text("Try Again", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFFFFF9C4)
        )
    }
}

@Composable
private fun GroupReadyContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Success animation
            Card(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF4CAF50)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "✓",
                        fontSize = 64.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Group Ready!",
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                color = Color(0xFFFFD54F),
                strokeWidth = 4.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Preparing your group...",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "You'll be redirected shortly",
                fontSize = 14.sp,
                color = Color.LightGray
            )
        }
    }
}

@Composable
fun UserBubbleRow(users: List<UserProfile>) {
    if (users.isEmpty()) {
        // Show placeholder
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = Color(0xFFFFD54F)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Loading members...",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(users, key = { it.userId }) { user ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        UserBubble(user = user)
                    }
                }
            }
        }
    }
}

@Composable
private fun UserBubble(user: UserProfile) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
    ) {
        // Profile picture
        if (user.profilePicture?.isNotEmpty() == true) {
            AsyncImage(
                model = user.profilePicture,
                contentDescription = user.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .border(3.dp, Color(0xFFFFD54F), CircleShape)
            )
        } else {
            // Default avatar
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFD54F))
                    .border(3.dp, Color(0xFFFFD54F), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Default avatar",
                    modifier = Modifier.size(40.dp),
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // User name
        Text(
            text = user.name,
            fontSize = 12.sp,
            maxLines = 2,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}