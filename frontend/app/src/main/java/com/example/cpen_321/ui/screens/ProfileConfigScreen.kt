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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.cpen_321.ui.components.MainBottomBar
import com.example.cpen_321.ui.viewmodels.AuthViewModel
import com.example.cpen_321.ui.viewmodels.AuthState

@Composable
fun ProfileConfigScreen(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState = authViewModel.authState.collectAsState()
    
    LaunchedEffect(authState.value) {
        if (authState.value is AuthState.Unauthenticated) {
            navController.navigate(NavRoutes.AUTH) {
                popUpTo(0) { inclusive = true }
            }
        }
    }
    
    Scaffold(
        bottomBar = { MainBottomBar(navController = navController) }
    ) { innerPadding ->
        ProfileConfigContent(
            navController = navController,
            authViewModel = authViewModel,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 32.dp)
        )
    }
}

@Composable
private fun ProfileConfigContent(
    navController: NavController,
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ConfigButton(
            text = "Profile",
            onClick = { navController.navigate(NavRoutes.PROFILE) }
        )
        ConfigButton(
            text = "Preferences",
            onClick = { navController.navigate(NavRoutes.PREFERENCES) }
        )
        ConfigButton(
            text = "Credibility Score",
            onClick = { navController.navigate(NavRoutes.CREDIBILITY_SCORE) }
        )
        ConfigButton(
            text = "Go Back",
            onClick = { navController.popBackStack() }
        )
        ConfigButton(
            text = "Logout",
            onClick = { authViewModel.logout() },
            containerColor = Color(0xFFFF5722),
            textColor = Color.White
        )
        ConfigButton(
            text = "Delete Account",
            onClick = {
                authViewModel.deleteAccount() {
                    // Auth data already cleared in repository
                    // Navigate to splash screen
                    navController.navigate(NavRoutes.SPLASH_SCREEN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            },
            containerColor = Color(0xFFFF5722),
            textColor = Color.White
        )
    }
}

@Composable
private fun ConfigButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color = Color(0xFFFFD54F),
    textColor: Color = Color.Black
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = ButtonDefaults.buttonColors(containerColor = containerColor)
    ) {
        Text(text = text, color = textColor, fontSize = 20.sp)
    }
    Spacer(modifier = Modifier.height(32.dp))
}