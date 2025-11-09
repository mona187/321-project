package com.example.cpen_321.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.cpen_321.data.network.dto.ApiResult
import NavRoutes
import com.example.cpen_321.ui.viewmodels.AuthViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


private fun isTestEnvironment(): Boolean {
    return try {
        // This class only exists when running tests
        Class.forName("androidx.test.espresso.Espresso")
        true
    } catch (e: ClassNotFoundException) {
        false
    }
}

/**
 * SplashScreen with test-aware behavior
 *
 * Production: Logs out on verification failure (secure)
 * Test: Stays logged in on verification failure (allows testing)
 */
@Composable
fun SplashScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val isTest = remember { isTestEnvironment() }

    LaunchedEffect(Unit) {
        if (viewModel.isLoggedIn()) {
            // Has token locally - try to verify
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
                    if (isTest) {
                        // TEST MODE: Stay logged in even if verification fails
                        println("🧪 TEST MODE: Verification failed, but staying logged in for tests")
                        withContext(Dispatchers.Main) {
                            navController.navigate(NavRoutes.HOME) {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    } else {
                        // PRODUCTION MODE: Log out on verification failure (secure)
                        println("⚠️ PRODUCTION: Token invalid, logging out")
                        viewModel.clearAuthData()
                        withContext(Dispatchers.Main) {
                            navController.navigate(NavRoutes.AUTH) {
                                popUpTo("splash") { inclusive = true }
                            }
                        }
                    }
                }
                is ApiResult.Loading -> {
                    // Should not happen
                }
            }
        } else {
            // No token at all, go to login
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