// defining navigation flow between screens

package com.example.cpen_321.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.cpen_321.ui.screens.AuthScreen
import com.example.cpen_321.ui.screens.HomeScreen
import com.example.cpen_321.ui.screens.WaitingRoomScreen
import com.example.cpen_321.ui.screens.GroupScreen
import com.example.cpen_321.ui.screens.ProfileConfigScreen
import com.example.cpen_321.ui.screens.PreferencesScreen
import com.example.cpen_321.ui.screens.ProfileScreen
import com.example.cpen_321.ui.screens.CredibilityScreen
import com.example.cpen_321.ui.screens.ViewGroupsScreen
import com.example.cpen_321.ui.screens.VoteRestaurantScreen
import com.example.cpen_321.ui.viewmodels.AuthViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController
) {
    val authViewModel: AuthViewModel = hiltViewModel()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.AUTH
    ) {
        composable(NavRoutes.AUTH) {
            val authViewModel: AuthViewModel = hiltViewModel()
            AuthScreen(
                authViewModel = authViewModel,
                onNavigateToHome = {
                    navController.navigate(NavRoutes.HOME) {
                        popUpTo(NavRoutes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.HOME) {
            HomeScreen(navController)
        }

        composable(NavRoutes.WAITING_ROOM) {
            WaitingRoomScreen(navController)
        }

        composable(route = NavRoutes.GROUP){
            GroupScreen(navController = navController)
        }

        composable(NavRoutes.PROFILE_CONFIG) {
            ProfileConfigScreen(navController = navController)
        }

        composable(NavRoutes.PROFILE) {
            ProfileScreen(navController = navController)
        }

        composable(NavRoutes.PREFERENCES) {
            PreferencesScreen(navController = navController)
        }

        composable(NavRoutes.CREDIBILITY_SCORE) {
            CredibilityScreen(navController = navController)
        }

        composable(NavRoutes.VIEW_GROUPS) {
            ViewGroupsScreen(navController = navController)
        }

        composable(NavRoutes.VOTE_RESTAURANT) {
            VoteRestaurantScreen(navController = navController)
        }
    }
}