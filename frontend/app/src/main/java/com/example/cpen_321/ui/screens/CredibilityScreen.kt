package com.example.cpen_321.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun CredibilityScreen(
    navController: NavController
) {
    // Replace this with actual credibility score from your data source
    var credibilityScore by remember { mutableStateOf(75) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                // Title box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Color(0xFFFFF9C4))
                        .border(2.dp, Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Credibility Score",
                        fontSize = 24.sp,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Progress bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .border(2.dp, Color.Black)
                ) {
                    // Filled portion (green)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(credibilityScore / 100f)
                            .height(80.dp)
                            .background(Color(0xFF81C784))
                            .border(1.dp, Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$credibilityScore%",
                            fontSize = 20.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Empty portion (white)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(Color.White)
                    )
                }
            }

            // Go Back button at bottom
            Button(
                onClick = {
                    navController.navigate(NavRoutes.PROFILE_CONFIG)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(bottom = 16.dp),
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
        }
    }
}