package com.example.cpen_321.ui.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.cpen_321.ui.screens.HomeScreen
import com.example.cpen_321.ui.screens.WaitingRoomScreen

@Composable
fun AppNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME // change to LOGIN for authentication?
    ) {
        composable(NavRoutes.HOME) {
            HomeScreen(navController)
        }

        composable(NavRoutes.WAITING_ROOM) {
            WaitingRoomScreen(navController)
        }

    }
}
