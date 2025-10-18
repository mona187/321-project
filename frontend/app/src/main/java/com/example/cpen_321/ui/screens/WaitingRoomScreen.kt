// ui/screens/WaitingRoomScreen.kt
package com.example.cpen_321.ui.screens

import NavRoutes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import coil.compose.AsyncImage
import com.example.cpen_321.data.model.UserProfile
import com.example.cpen_321.fake.FakeMatchViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.cpen_321.ui.viewmodels.MatchViewModel
import kotlinx.coroutines.delay

@Composable
fun WaitingRoomScreen(
    navController: NavController,
    viewModel: FakeMatchViewModel = remember { FakeMatchViewModel() }
    //viewModel: MatchViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val maxNumberOfPeople = 10
    val minNumberOfPeople = 4

    var showFailureDialog by remember { mutableStateOf(false) }
    var timeRemaining by remember { mutableStateOf(60) } // Start at 60 seconds

    // Countdown timer
    LaunchedEffect(Unit) {
        while (timeRemaining > 0) {
            delay(1000L) // Wait 1 second
            timeRemaining--
        }
    }

    // Check if max capacity reached
    LaunchedEffect(state.members.size) {
        if (state.members.size >= maxNumberOfPeople) {
            navController.navigate(NavRoutes.VOTE_RESTAURANT)
        }
    }

    // Check timer expiration
    LaunchedEffect(timeRemaining) {
        if (timeRemaining == 0) {
            if (state.members.size >= minNumberOfPeople) {
                navController.navigate(NavRoutes.VOTE_RESTAURANT)
            } else {
                showFailureDialog = true
            }
        }
    }

    Scaffold { innerPadding ->
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
                if (!state.groupReady) {
                    Text(
                        "Waiting Room",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // User count
                    Text(
                        text = "Users in room: ${state.members.size}/$maxNumberOfPeople",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Display countdown timer
                    Text(
                        String.format(
                            "%d:%02d",
                            timeRemaining / 60,
                            timeRemaining % 60
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (timeRemaining <= 10) Color.Red else Color.Black
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    UserBubbleRow(state.members)
                } else {
                    Text(
                        "Group Ready!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    )
                    // leave socket and navigate to Group Screen
                    LaunchedEffect(Unit) {
                        // viewModel.leaveRoom("user123") //REPLACE WITH ACTUAL USER ID
                        navController.navigate(NavRoutes.VOTE_RESTAURANT)
                    }
                }
            }

            // Leave Room button at the bottom
            if (!state.groupReady) {
                Button(
                    onClick = {
                        val userId = "user123" // Replace with actual user ID
                        // viewModel.leaveRoom(userId)
                        navController.popBackStack()
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
                        // Delete/leave group
                        // viewModel.deleteGroup()
                        showFailureDialog = false
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.HOME) { inclusive = true }
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
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(users) { user ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    model = user.profilePicture,
                    contentDescription = user.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Gray, CircleShape)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(user.name)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WaitingRoomPreview() {
    WaitingRoomScreen(navController = rememberNavController())
}