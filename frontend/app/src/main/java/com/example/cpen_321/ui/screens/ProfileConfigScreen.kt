package com.example.cpen_321.ui.screens

import NavRoutes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.cpen_321.ui.components.MainBottomBar

@Composable
fun ProfileConfigScreen(
    navController: NavController
) {
    Scaffold(
        bottomBar = { MainBottomBar(navController = navController) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = {
                    navController.navigate(NavRoutes.PROFILE)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD54F) // Yellow color
                )
            ) {
                Text(
                    "Profile",
                    color = Color.Black,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    navController.navigate(NavRoutes.PREFERENCES)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD54F) // Yellow color
                )
            ) {
                Text(
                    "Preferences",
                    color = Color.Black,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    navController.navigate(NavRoutes.CREDIBILITY_SCORE)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD54F) // Yellow color
                )
            ) {
                Text(
                    text = "Credibility Score",
                    color = Color.Black,
                    fontSize = 20.sp
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Go Back button
            Button(
                onClick = {
                    navController.popBackStack()
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
        }
    }
}