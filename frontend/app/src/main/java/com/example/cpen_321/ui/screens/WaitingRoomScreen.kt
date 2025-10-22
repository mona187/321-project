package com.example.cpen_321.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
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

    var showFailureDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Convert milliseconds to seconds for display
    val timeRemainingSeconds = (timeRemaining / 1000).toInt()

    // Navigate to group when ready
    LaunchedEffect(groupReady, groupId) {
        if (groupReady && groupId != null) {
            navController.navigate("vote_restaurant/$groupId") {
                popUpTo("waiting_room") { inclusive = true }
            }
        }
    }

    // Handle room expired
    LaunchedEffect(roomExpired) {
        if (roomExpired) {
            if (roomMembers.size >= minNumberOfPeople) {
                // Still enough people, should create group
                // This case is handled by backend automatically
            } else {
                showFailureDialog = true
            }
        }
    }

    // Show error messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    // Update countdown timer
    LaunchedEffect(timeRemaining) {
        if (timeRemaining > 0) {
            delay(1000L)
            // ViewModel updates timeRemaining automatically via socket
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section with title and timer
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (!groupReady) {
                    Text(
                        "Waiting Room",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // User count
                    Text(
                        text = "Users in room: ${roomMembers.size}/$maxNumberOfPeople",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Display countdown timer
                    Text(
                        String.format(
                            "%d:%02d",
                            timeRemainingSeconds / 60,
                            timeRemainingSeconds % 60
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (timeRemainingSeconds <= 10) Color.Red else Color.Black
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Room info
                    currentRoom?.let { room ->
                        room.cuisine?.let { cuisine ->
                            Text(
                                text = "Cuisine: $cuisine",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    UserBubbleRow(roomMembers)
                } else {
                    Text(
                        "Group Ready!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Preparing your group...",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }

            // Leave Room button at the bottom
            if (!groupReady) {
                Button(
                    onClick = {
                        currentRoom?.let { room ->
                            viewModel.leaveRoom()
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD54F)
                    )
                ) {
                    Text(
                        text = "Leave Room",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
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
                    color = Color.Red
                )
            },
            text = {
                Text("Not enough people joined the waiting room. Minimum $minNumberOfPeople people required.")
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
                    Text("OK", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFFFFF9C4)
        )
    }
}

@Composable
fun UserBubbleRow(users: List<UserProfile>) {
    if (users.isEmpty()) {
        // Show placeholder or loading state
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Loading members...",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(users) { user ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(100.dp)
                ) {
                    AsyncImage(
                        model = user.profilePicture?.ifEmpty { null },
                        contentDescription = user.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.Gray, CircleShape),
                        placeholder = null, // You can add a placeholder image here
                        error = null // You can add an error image here
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = user.name,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}