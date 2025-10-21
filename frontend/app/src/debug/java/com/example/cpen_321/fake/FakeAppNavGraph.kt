package com.example.cpen_321.fake

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cpen_321.ui.screens.*

@Composable
fun FakeAppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    // Fake viewmodels
    val authViewModel: FakeAuthViewModel = viewModel()
    val settingsViewModel: FakeSettingsViewModel = viewModel()
    val uiState by authViewModel.uiState.collectAsState()

    // ✅ Run navigation logic once when auth state changes
    LaunchedEffect(uiState) {
        when {
            uiState.requiresProfileSetup -> {
                // navigate to SETTINGS first
                navController.navigate(NavRoutes.SETTINGS) {
                    popUpTo(0) { inclusive = true }
                }
            }
            uiState.isAuthenticated -> {
                // then to HOME only after setup complete
                navController.navigate(NavRoutes.HOME) {
                    popUpTo(0) { inclusive = true }
                }
            }
            else -> {
                navController.navigate(NavRoutes.AUTH) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    // ✅ Define destinations
    NavHost(
        navController = navController,
        startDestination = NavRoutes.AUTH
    ) {
        // 🔐 Auth Screen
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

        // ⚙️ Settings Screen (first-time setup)
        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                navController = navController,
                viewModel = settingsViewModel,
                fakeAuthViewModel = authViewModel,   // ✅ important
                firstTimeSetup = true
            )
        }

        // 🏠 Home Screen
        composable(NavRoutes.HOME) {
            HomeScreen(navController, authViewModel = authViewModel)
        }

        // ⏳ Waiting Room
        composable(NavRoutes.WAITING_ROOM) {
            WaitingRoomScreen(navController)
        }

        // 👥 Group Screen
        composable(NavRoutes.GROUP) {
            GroupScreen(navController)
        }

        // 👤 Profile Screen
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
            val userIdString = backStackEntry.arguments?.getString("userId")
            val userId = userIdString?.toIntOrNull()
            ProfileScreen(userId = userId, navController = navController)
        }
    }
}
