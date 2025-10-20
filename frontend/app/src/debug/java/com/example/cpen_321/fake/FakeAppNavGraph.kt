package com.example.cpen_321.fake

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.cpen_321.ui.screens.AuthScreen
import com.example.cpen_321.ui.screens.HomeScreen
import com.example.cpen_321.ui.screens.ProfileScreen
import com.example.cpen_321.ui.screens.WaitingRoomScreen
import com.example.cpen_321.ui.screens.GroupScreen

@Composable
fun FakeAppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    // ✅ Get one lifecycle-aware instance of each fake viewmodel
    val authViewModel: FakeAuthViewModel = viewModel()
    val uiState by authViewModel.uiState.collectAsState()

    // Navigate automatically based on fake auth state
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
