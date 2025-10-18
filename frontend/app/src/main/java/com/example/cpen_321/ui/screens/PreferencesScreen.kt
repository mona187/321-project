package com.example.cpen_321.ui.screens

import NavRoutes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun PreferencesScreen(
    navController: NavController
) {
    var selectedPreferences by remember { mutableStateOf(setOf<String>()) }
    var maxBudget by remember { mutableStateOf("") }

    val cuisineOptions = listOf(
        "Sushi", "Italian",
        "Pizza", "Japanese",
        "European", "Korean",
        "Middle Eastern", "Chinese"
    )

    Scaffold { innerPadding ->
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
                    .padding(vertical = 16.dp)
            )

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
                            val isSelected = selectedPreferences.contains(cuisine)
                            Button(
                                onClick = {
                                    selectedPreferences = if (isSelected) {
                                        selectedPreferences - cuisine
                                    } else {
                                        selectedPreferences + cuisine
                                    }
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
                text = "Max amount of money to spend:",
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = maxBudget,
                onValueChange = { maxBudget = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Enter amount") }
            )

            Spacer(modifier = Modifier.weight(1f))

            // Save Preferences button
            Button(
                onClick = {
                    // Handle save preferences
                    // Save selectedPreferences and maxBudget to your data source
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD54F)
                )
            ) {
                Text(
                    text = "Save Preferences",
                    color = Color.Black,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Go Back button
            Button(
                onClick = {
                    navController.navigate(NavRoutes.PROFILE_CONFIG)
                },
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