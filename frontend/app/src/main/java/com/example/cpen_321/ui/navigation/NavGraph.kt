package com.example.cpen_321.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.cpen_321.ui.screens.*
import com.example.cpen_321.ui.screens.profile.CredibilityScreen
import com.example.cpen_321.ui.screens.profile.PreferencesScreen
import com.example.cpen_321.ui.screens.profile.ProfileScreen
import com.example.cpen_321.ui.viewmodels.AuthViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController
) {
    // Get shared AuthViewModel at top level
    val authViewModel: AuthViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.SPLASH_SCREEN
    ) {
        composable("splash") {
            SplashScreen(navController = navController)
        }

        // Auth Screen
        composable(NavRoutes.AUTH) {
            AuthScreen(
                viewModel = authViewModel,
                onNavigateToHome = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        // Home Screen
        composable(NavRoutes.HOME) {
            HomeScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // Waiting Room Screen
        composable(NavRoutes.WAITING_ROOM) {
            WaitingRoomScreen(
                navController = navController
            )
        }

        // Group Screen (generic - shows current user's active group)
        composable(NavRoutes.GROUP) {
            GroupScreen(
                navController = navController
            )
        }

        // Group Screen with ID parameter
        composable(
            route = "group/{groupId}",
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            if (groupId != null) {
                GroupScreen(
                    navController = navController,
                    groupId = groupId
                )
            }
        }

        // Vote Restaurant Screen (generic - uses current group)
        composable(NavRoutes.VOTE_RESTAURANT) {
            VoteRestaurantScreen(
                navController = navController
            )
        }

        // Vote Restaurant Screen with group ID parameter
        composable(
            route = "vote_restaurant/{groupId}",
            arguments = listOf(
                navArgument("groupId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId")
            if (groupId != null) {
                VoteRestaurantScreen(
                    navController = navController,
                    groupId = groupId
                )
            }
        }

        // Profile Config Screen
        composable(NavRoutes.PROFILE_CONFIG) {
            ProfileConfigScreen(
                navController = navController,
                authViewModel = authViewModel
            )
        }

        // Profile Screen - UPDATED
        composable(NavRoutes.PROFILE) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Preferences Screen - UPDATED
        composable(NavRoutes.PREFERENCES) {
            PreferencesScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Credibility Score Screen - UPDATED
        composable(NavRoutes.CREDIBILITY_SCORE) {
            CredibilityScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // View Groups Screen
        composable(NavRoutes.VIEW_GROUPS) {
            ViewGroupsScreen(
                navController = navController
            )
        }
    }
}