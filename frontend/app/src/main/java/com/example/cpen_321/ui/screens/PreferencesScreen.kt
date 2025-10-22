package com.example.cpen_321.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cpen_321.ui.viewmodels.UserViewModel

@Composable
fun PreferencesScreen(
    onNavigateBack: () -> Unit,
    viewModel: UserViewModel = hiltViewModel()
) {
    val selectedCuisines by viewModel.selectedCuisines.collectAsState()
    val budget by viewModel.budget.collectAsState()
    val radius by viewModel.radius.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val cuisineOptions = listOf(
        "Sushi", "Italian",
        "Pizza", "Japanese",
        "European", "Korean",
        "Middle Eastern", "Chinese"
    )

    // Load user settings when screen opens
    LaunchedEffect(Unit) {
        viewModel.loadUserSettings()
    }

    // Show success message
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }

    // Show error message
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
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
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = "Preferences (Select)",
                fontSize = 20.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                textAlign = TextAlign.Center
            )

            if (isLoading) {
                // Show loading indicator
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading preferences...")
                }
            } else {
                // Cuisine options in a grid (2 columns, 4 rows)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    cuisineOptions.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { cuisine ->
                                val isSelected = selectedCuisines.contains(cuisine)
                                Button(
                                    onClick = {
                                        viewModel.toggleCuisine(cuisine)
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(60.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected)
                                            Color(0xFFFFB300) // Darker yellow when selected
                                        else
                                            Color(0xFFFFD54F) // Regular yellow
                                    )
                                ) {
                                    Text(
                                        text = cuisine,
                                        color = Color.Black,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Max budget section
                Text(
                    text = "Max amount of money to spend: $${budget.toInt()}",
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Budget slider
                Slider(
                    value = budget.toFloat(),
                    onValueChange = { viewModel.updateBudget(it.toDouble()) },
                    valueRange = 0f..200f,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Radius section
                Text(
                    text = "Search radius: ${radius.toInt()} km",
                    fontSize = 16.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Radius slider
                Slider(
                    value = radius.toFloat(),
                    onValueChange = { viewModel.updateRadius(it.toDouble()) },
                    valueRange = 1f..50f,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.weight(1f))

                // Save Preferences button
                Button(
                    onClick = {
                        viewModel.savePreferences()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD54F)
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.Black,
                            modifier = Modifier.height(24.dp)
                        )
                    } else {
                        Text(
                            text = "Save Preferences",
                            color = Color.Black,
                            fontSize = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Go Back button
                Button(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .fillMaxWidth()
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

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}