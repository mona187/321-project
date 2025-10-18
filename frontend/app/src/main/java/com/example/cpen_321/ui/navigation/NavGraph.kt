package com.example.cpen_321.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.cpen_321.ui.screens.AuthScreen
import com.example.cpen_321.ui.screens.HomeScreen
import com.example.cpen_321.ui.screens.WaitingRoomScreen
import com.example.cpen_321.ui.screens.GroupScreen
import com.example.cpen_321.ui.viewmodels.AuthViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val uiState by authViewModel.uiState.collectAsState()

    // Simple navigation based on authentication state
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            navController.navigate(NavRoutes.HOME) {
                popUpTo(NavRoutes.AUTH) { inclusive = true }
            }
        } else {
            navController.navigate(NavRoutes.AUTH) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = NavRoutes.AUTH
    ) {
        // 🔐 Authentication screen
        composable(NavRoutes.AUTH) {
            AuthScreen(
                authViewModel = authViewModel,
                onNavigateToHome = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        // 🏠 Home screen
        composable(NavRoutes.HOME) {
            HomeScreen(navController, authViewModel = authViewModel)
        }

        // ⏳ Waiting room screen
        composable(NavRoutes.WAITING_ROOM) {
            WaitingRoomScreen(navController)
        }

        // 👥 Group screen
        composable(NavRoutes.GROUP) {
            GroupScreen(navController)
        }
    }
}