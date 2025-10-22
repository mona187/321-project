package com.example.cpen_321.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
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

                    // Restaurant info box
                    val restaurant = selectedRestaurant ?: currentGroup?.restaurant
                    if (restaurant != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFFF9C4))
                                .border(2.dp, Color.Black)
                                .padding(16.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Restaurant Name: ${restaurant.name}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Location: ${restaurant.location}",
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                                restaurant.phoneNumber?.let { phone ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Phone: $phone",
                                        fontSize = 16.sp,
                                        color = Color.Black
                                    )
                                }
                                restaurant.rating?.let { rating ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Rating: ${restaurant.getRatingString()} ⭐",
                                        fontSize = 16.sp,
                                        color = Color.Black
                                    )
                                }
                                restaurant.priceLevel?.let { _ ->
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Price: ${restaurant.getPriceLevelString()}",
                                        fontSize = 16.sp,
                                        color = Color.Black
                                    )
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
                                text = "Restaurant: Not selected yet",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Group members title
                    Text(
                        text = "Group Members (${groupMembers.size})",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Members list
                    if (groupMembers.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        groupMembers.forEach { member ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFFFF9C4))
                                    .border(2.dp, Color.Black)
                                    .padding(16.dp)
                            ) {
                                Column {
                                    Text(
                                        text = member.name,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Credibility Score: ${member.credibilityScore.toInt()}",
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                    member.phoneNumber?.let { phone ->
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Phone: $phone",
                                            fontSize = 14.sp,
                                            color = Color.Black
                                        )
                                    }
                                    if (member.hasVoted) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "✓ Voted",
                                            fontSize = 14.sp,
                                            color = Color(0xFF4CAF50),
                                            fontWeight = FontWeight.Bold
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
                        viewModel.leaveGroup {
                            navController.navigate("home") {
                                popUpTo("home") { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD54F)
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Close Group",
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}