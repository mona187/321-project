package com.example.cpen_321.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.cpen_321.data.network.dto.ApiResult
import NavRoutes
import com.example.cpen_321.ui.viewmodels.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


/*
App Opens
    ↓
Splash Screen (shows loading spinner)
    ↓
Has token locally?
    ├─ No → Login Screen
    └─ Yes → Call /api/auth/verify
              ├─ Success (200) → Home Screen ✅ (STAYS LOGGED IN)
              └─ Error (401) → Clear token → Login Screen
 */


@Composable
fun SplashScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        // CRITICAL FIX: Wrap all navigation calls in withContext(Dispatchers.Main)
        if (viewModel.isLoggedIn()) {
            // Verify token with backend
            when (val result = viewModel.verifyToken()) {
                is ApiResult.Success -> {
                    // Token valid, go to home
                    withContext(Dispatchers.Main) {
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
                is ApiResult.Error -> {
                    // Token invalid, clear and go to login
                    viewModel.clearAuthData()
                    withContext(Dispatchers.Main) {
                        navController.navigate(NavRoutes.AUTH) {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
                is ApiResult.Loading -> {
                    // Should not happen, but handle it
                    // Stay on splash screen
                }
            }
        } else {
            // No token, go to login
            withContext(Dispatchers.Main) {
                navController.navigate(NavRoutes.AUTH) {
                    popUpTo("splash") { inclusive = true }
                }
            }
        }
    }

    // Show loading indicator
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}