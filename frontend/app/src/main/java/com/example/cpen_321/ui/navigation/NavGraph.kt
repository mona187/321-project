package com.example.cpen_321.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.cpen_321.ui.screens.*
import com.example.cpen_321.ui.viewmodels.AuthViewModel
import com.example.cpen_321.ui.viewmodels.SettingsViewModel
import android.window.SplashScreen
@Composable
fun AppNavGraph(navController: NavHostController) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val uiState by authViewModel.uiState.collectAsState()

    // ✅ Only navigate after auth check is done
    LaunchedEffect(uiState.isCheckingAuth, uiState.isAuthenticated, uiState.requiresProfileSetup) {
        if (uiState.isCheckingAuth) return@LaunchedEffect

        when {
            // New user who just signed up via Google
            uiState.isAuthenticated && uiState.requiresProfileSetup -> {
                navController.navigate(NavRoutes.SETTINGS) {
                    popUpTo(NavRoutes.AUTH) { inclusive = true }
                }
            }

            // Returning authenticated user
            uiState.isAuthenticated && !uiState.requiresProfileSetup -> {
                navController.navigate(NavRoutes.HOME) {
                    popUpTo(NavRoutes.AUTH) { inclusive = true }
                }
            }

            // Not authenticated yet → stay on Auth screen
            else -> {
                // no nav — Auth is already the start destination
            }
        }
    }

    // 🔹 Build the navigation graph
    NavHost(
        navController = navController,
        startDestination = NavRoutes.AUTH
    ) {
        // ---------------- AUTH ----------------
        composable(NavRoutes.AUTH) {
            if (uiState.isCheckingAuth) {
                // optional small splash
                //SplashScreen()
            } else {
                AuthScreen(
                    authViewModel = authViewModel,
                    onNavigateToHome = {
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.AUTH) { inclusive = true }
                        }
                    }
                )
            }
        }

        // ---------------- SETTINGS ----------------
        composable(NavRoutes.SETTINGS) {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            SettingsScreen(
                navController = navController,
                viewModel = settingsViewModel,
                firstTimeSetup = true
            )
        }

        // ---------------- HOME ----------------
        composable(NavRoutes.HOME) {
            HomeScreen(navController, authViewModel = authViewModel)
        }

        // ---------------- WAITING ROOM ----------------
        composable(NavRoutes.WAITING_ROOM) {
            WaitingRoomScreen(navController)
        }

        // ---------------- GROUP ----------------
        composable(NavRoutes.GROUP) {
            GroupScreen(navController)
        }

        // ---------------- PROFILE ----------------
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
