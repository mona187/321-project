// Navigation Helper for Vote Restaurant Feature

package com.example.cpen_321.ui.navigation

import androidx.navigation.NavController

/**
 * Helper functions for navigating to voting screens
 */
object VotingNavigationHelper {

    /**
     * Navigate to vote restaurant screen from ViewGroups
     */
    fun navigateToVoteRestaurant(navController: NavController, groupId: String) {
        // Use the parameterized route
        navController.navigate("vote_restaurant/$groupId")
    }

    /**
     * Navigate to vote restaurant screen (uses current group)
     */
    fun navigateToVoteRestaurant(navController: NavController) {
        navController.navigate(NavRoutes.VOTE_RESTAURANT)
    }

    /**
     * Navigate back to group screen after voting
     */
    fun navigateBackToGroup(navController: NavController) {
        navController.navigate(NavRoutes.GROUP) {
            popUpTo(NavRoutes.GROUP) { inclusive = true }
        }
    }

    /**
     * Navigate to group screen with specific ID
     */
    fun navigateToGroup(navController: NavController, groupId: String) {
        navController.navigate("group/$groupId")
    }
}

// Usage in ViewGroupsScreen:
/*
Button(
    onClick = {
        currentGroup.groupId?.let { groupId ->
            VotingNavigationHelper.navigateToVoteRestaurant(navController, groupId)
        }
    }
) {
    Text("Vote Now")
}
*/

// Usage in VoteRestaurantScreen after restaurant selection:
/*
LaunchedEffect(selectedRestaurant) {
    selectedRestaurant?.let {
        delay(1500)
        VotingNavigationHelper.navigateBackToGroup(navController)
    }
}
*/