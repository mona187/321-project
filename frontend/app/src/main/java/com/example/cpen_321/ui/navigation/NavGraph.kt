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
import com.example.cpen_321.ui.screens.ProfileScreen
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import com.example.cpen_321.ui.viewmodels.SettingsViewModel
import com.example.cpen_321.ui.screens.SettingsScreen

@Composable
fun AppNavGraph(
    navController: NavHostController
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val uiState by authViewModel.uiState.collectAsState()

    // Simple navigation based on authentication state and if profile setup required
    LaunchedEffect(uiState.isAuthenticated, uiState.requiresProfileSetup) {
        when {
            uiState.requiresProfileSetup -> {
                navController.navigate(NavRoutes.SETTINGS) {
                    popUpTo(NavRoutes.AUTH) { inclusive = true }
                }
            }
            uiState.isAuthenticated -> {
                navController.navigate(NavRoutes.HOME) {
                    popUpTo(NavRoutes.AUTH) { inclusive = true }
                }
            }
            else -> {
                navController.navigate(NavRoutes.AUTH) {
                    popUpTo(0) { inclusive = true }
                }
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

        // Settings screen
        composable(NavRoutes.SETTINGS) {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                navController = navController,
                viewModel = settingsViewModel,
                firstTimeSetup = true
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

        // Profile Screen
    composable(route = NavRoutes.PROFILE) {
        ProfileScreen(
            navController = navController,
            userId = null
        )
    }
        composable(
            route = "profile/{userId?}",
            arguments = listOf(
                navArgument("userId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            // Get the argument as a string
            val userIdString = backStackEntry.arguments?.getString("userId")

            // Safely convert to Int if possible
            val userId = userIdString?.toIntOrNull()

            ProfileScreen(userId = userId, navController = navController)
        }
    }
}