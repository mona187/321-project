package com.example.cpen_321.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
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
import com.example.cpen_321.ui.viewmodels.GroupViewModel

@Composable
fun GroupScreen(
    navController: NavController,
    groupId: String? = null,
    viewModel: GroupViewModel = hiltViewModel()
) {
    // Collect states from ViewModel
    val currentGroup by viewModel.currentGroup.collectAsState()
    val groupMembers by viewModel.groupMembers.collectAsState()
    val selectedRestaurant by viewModel.selectedRestaurant.collectAsState()
    val currentVotes by viewModel.currentVotes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    // Load group status when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadGroupStatus()
    }

    // Show error messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    // Show success messages
    LaunchedEffect(successMessage) {
        successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSuccess()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        if (isLoading && currentGroup == null) {
            // Loading state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (currentGroup == null) {
            // No group found
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "No active group found",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Go Back")
                    }
                }
            }
        } else {
            // Group content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Group title box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(Color(0xFFFFF9C4))
                            .border(2.dp, Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Group",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Restaurant info box with photo
                    val restaurant = selectedRestaurant ?: currentGroup?.restaurant
                    if (restaurant != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFF9C4)
                            ),
                            shape = RoundedCornerShape(0.dp),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color.Black)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                // Restaurant photo
                                restaurant.getMainPhotoUrl()?.let { photoUrl ->
                                    AsyncImage(
                                        model = photoUrl,
                                        contentDescription = restaurant.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(200.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.LightGray)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                }

                                // Restaurant name
                                Text(
                                    text = restaurant.name,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Location
                                Text(
                                    text = "📍 ${restaurant.location}",
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )

                                // Rating
                                restaurant.rating?.let { rating ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Rating",
                                            tint = Color(0xFFFFC107),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = restaurant.getRatingString(),
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.Black
                                        )
                                    }
                                }

                                // Price level
                                restaurant.priceLevel?.let { _ ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Price: ${restaurant.getPriceLevelString()}",
                                        fontSize = 16.sp,
                                        color = Color.Black
                                    )
                                }

                                // Phone number
                                restaurant.phoneNumber?.let { phone ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "📞 $phone",
                                        fontSize = 16.sp,
                                        color = Color.Black
                                    )
                                }

                                // Vote count if available
                                restaurant.restaurantId?.let { restId ->
                                    val voteCount = currentVotes[restId] ?: 0
                                    if (voteCount > 0) {
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "🏆 Won with $voteCount vote${if (voteCount != 1) "s" else ""}",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4CAF50)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    } else {
                        // Restaurant not selected yet
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFF9C4))
                                .border(2.dp, Color.Black)
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Restaurant: Not selected yet\nWaiting for voting to complete...",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Group members title with count
                    Text(
                        text = "Group Members (${currentGroup?.getAllMembers()?.size ?: 0})",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Members list - show all members from the group
                    val allMembers = currentGroup?.getAllMembers() ?: emptyList()
                    if (allMembers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        allMembers.forEach { userId ->
                            // Find member details from groupMembers list
                            val memberDetails = groupMembers.find { it.userId == userId }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFFF9C4))
                                    .border(2.dp, Color.Black)
                                    .padding(16.dp)
                            ) {
                                if (memberDetails != null) {
                                    Column {
                                        Text(
                                            text = memberDetails.name,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Credibility Score: ${memberDetails.credibilityScore.toInt()}",
                                            fontSize = 14.sp,
                                            color = Color.Black
                                        )
                                        memberDetails.phoneNumber?.let { phone ->
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Phone: $phone",
                                                fontSize = 14.sp,
                                                color = Color.Black
                                            )
                                        }
                                        if (memberDetails.hasVoted) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "✓ Voted",
                                                fontSize = 14.sp,
                                                color = Color(0xFF4CAF50),
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                } else {
                                    // Show user ID if details not loaded yet
                                    Column {
                                        Text(
                                            text = "User: $userId",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Black
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Loading details...",
                                            fontSize = 14.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                // Close Group button at bottom
                Button(
                    onClick = {
                        // Just navigate back to home
                        // DON'T call leaveGroup() - the group should persist
                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
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
                        text = "Back to Home",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

// OPTIONAL: Add a separate "Leave Group" button if you want users to be able to leave
// Only show this BEFORE restaurant is selected
                if (currentGroup?.restaurantSelected == false) {
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            viewModel.leaveGroup {
                                navController.navigate("home") {
                                    popUpTo("home") { inclusive = true }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Red
                        ),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.Red,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Leave Group",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}