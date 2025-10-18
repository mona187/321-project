package com.example.cpen_321.ui.screens

import NavRoutes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay

@Composable
fun VoteRestaurantScreen(
    navController: NavController
) {
    // Replace with actual data from your ViewModel
    var currentRestaurantIndex by remember { mutableStateOf(0) }
    var usersJoined by remember { mutableStateOf(3) }
    var timeRemaining by remember { mutableStateOf(60) } // seconds
    val maxUsers = 10
    val maxTimeLimit = 60 // seconds

    // Track votes for each restaurant (null = no vote, true = thumbs up, false = thumbs down)
    val votes = remember { mutableStateMapOf<Int, Boolean?>() }

    var showVoteWarningDialog by remember { mutableStateOf(false) }
    var showTimeUpDialog by remember { mutableStateOf(false) }

    // Sample restaurant options - replace with actual data
    val restaurants = listOf(
        "Restaurant Option 1",
        "Restaurant Option 2",
        "Restaurant Option 3"
    )

    // Timer countdown
    LaunchedEffect(Unit) {
        while (timeRemaining > 0) {
            delay(1000L)
            timeRemaining--
        }
        // Time's up - show dialog
        showTimeUpDialog = true
    }

    // Check if conditions are met to auto-advance
    LaunchedEffect(usersJoined, timeRemaining) {
        if (usersJoined >= 4 || usersJoined >= maxUsers) {
            // Automatically move to group details screen
//            navController.navigate(NavRoutes.GROUP)
        }
    }

//    // Check if conditions are met to auto-advance
//    LaunchedEffect(usersJoined, timeRemaining) {
//        if (usersJoined >= 4 || usersJoined >= maxUsers || timeRemaining <= 0) {
//            // Automatically move to next screen
//            // navController.navigate(NavRoutes.NEXT_VOTING_SCREEN)
//        }
//    }


    /*Notes for future:

    Great idea! Here's the updated code that only navigates forward when **everyone has completed voting**:

```kotlin
// Check if all users have completed voting
LaunchedEffect(votes.size, usersJoined) {
    // Check if all users have voted on all restaurants
    // Each user needs to vote on all restaurants
    val totalVotesNeeded = restaurants.size * usersJoined
    val currentTotalVotes = votes.size // This counts votes for current user only

    // For proper implementation with multiple users, you'd track:
    // val allUsersCompletedVoting = viewModel.allUsersHaveVoted()

    // Placeholder: Navigate when all users report they've finished
    // if (allUsersCompletedVoting) {
    //     navController.navigate(NavRoutes.GROUP_DETAILS)
    // }
}
```

However, there's an important consideration: **The current `votes` map only tracks the current user's votes**, not all users in the group.

**Better approach using ViewModel:**

You'll need to track voting status across all users in your ViewModel. Here's how to structure it:

```kotlin
// In your ViewModel (or backend)
// Track which users have completed voting
val usersCompletedVoting = mutableStateOf(0)
val totalUsers = 4 // or however many are in the group

// In VoteRestaurantScreen
LaunchedEffect(usersJoined) {
    // Listen to backend/viewModel for all users' voting status
    // viewModel.usersCompletedVoting.collect { completedCount ->
    //     if (completedCount >= usersJoined) {
    //         // Everyone has finished voting
    //         navController.navigate(NavRoutes.GROUP_DETAILS)
    //     }
    // }
}
```

**Updated VoteRestaurantScreen with this logic:**

```kotlin
// Replace the auto-advance LaunchedEffect with this:
LaunchedEffect(usersJoined) {
    // This should listen to your ViewModel/backend
    // For now, just remove auto-advance and rely on manual submission
    // When you integrate with backend, add:
    // viewModel.observeVotingComplete().collect { allComplete ->
    //     if (allComplete) {
    //         navController.navigate(NavRoutes.GROUP_DETAILS)
    //     }
    // }
}
```

**For now, you can remove the auto-advance code entirely** and only navigate when:
1. User clicks "Submit Votes & Leave" (after completing all their votes)
2. Timer expires

This way, the backend/ViewModel will be responsible for determining when everyone has voted and triggering the transition to the group details screen.

Would you like me to show you how to set this up with a ViewModel that tracks all users' voting status?
     */

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Title box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(Color(0xFFFFF9C4))
                        .border(2.dp, Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Restaurant Voting Stage",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Restaurant option with navigation arrows
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left arrow
                    IconButton(
                        onClick = {
                            if (currentRestaurantIndex > 0) {
                                currentRestaurantIndex--
                            }
                        },
                        enabled = currentRestaurantIndex > 0
                    ) {
                        Text(
                            text = "◀",
                            fontSize = 40.sp,
                            color = if (currentRestaurantIndex > 0) Color.Black else Color.Gray
                        )
                    }

                    // Restaurant option box
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .background(Color(0xFFFFF9C4))
                            .border(2.dp, Color.Black),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = restaurants[currentRestaurantIndex],
                            fontSize = 18.sp,
                            color = Color.Black,
                            textAlign = TextAlign.Center
                        )
                    }

                    // Right arrow
                    IconButton(
                        onClick = {
                            if (currentRestaurantIndex < restaurants.size - 1) {
                                currentRestaurantIndex++
                            }
                        },
                        enabled = currentRestaurantIndex < restaurants.size - 1
                    ) {
                        Text(
                            text = "▶",
                            fontSize = 40.sp,
                            color = if (currentRestaurantIndex < restaurants.size - 1) Color.Black else Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Get current vote for this restaurant
                val currentVote = votes[currentRestaurantIndex]

                // Voting buttons (thumbs up and down)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Thumbs down button
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                if (currentVote == false) Color(0xFFFFB300) else Color.Transparent,
                                shape = MaterialTheme.shapes.medium
                            )
                            .border(
                                width = if (currentVote == false) 3.dp else 0.dp,
                                color = Color.Black,
                                shape = MaterialTheme.shapes.medium
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                // Toggle vote - if already voted down, remove vote, otherwise vote down
                                votes[currentRestaurantIndex] = if (currentVote == false) null else false
                                // viewModel.voteRestaurant(restaurants[currentRestaurantIndex], vote = false)
                            },
                            modifier = Modifier.size(80.dp)
                        ) {
                            Text(
                                text = "👎",
                                fontSize = 60.sp
                            )
                        }
                    }

                    // Thumbs up button
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                if (currentVote == true) Color(0xFFFFB300) else Color.Transparent,
                                shape = MaterialTheme.shapes.medium
                            )
                            .border(
                                width = if (currentVote == true) 3.dp else 0.dp,
                                color = Color.Black,
                                shape = MaterialTheme.shapes.medium
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = {
                                // Toggle vote - if already voted up, remove vote, otherwise vote up
                                votes[currentRestaurantIndex] = if (currentVote == true) null else true
                                // viewModel.voteRestaurant(restaurants[currentRestaurantIndex], vote = true)
                            },
                            modifier = Modifier.size(80.dp)
                        ) {
                            Text(
                                text = "👍",
                                fontSize = 60.sp
                            )
                        }
                    }
                }
            }

            // Bottom section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Status text
                Text(
                    text = "Users joined: $usersJoined/$maxUsers",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Time remaining: ${timeRemaining}s",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (timeRemaining <= 10) Color.Red else Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Votes & Leave button
                Button(
                    onClick = {
                        // Check if all restaurants have been voted on
                        val allVoted = restaurants.indices.all { votes.containsKey(it) }

                        if (allVoted) {
                            // Submit votes and navigate to group details
                            // viewModel.submitAllVotes(votes)
                            navController.navigate(NavRoutes.GROUP)
                        } else {
                            // Show warning dialog
                            showVoteWarningDialog = true
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD54F)
                    )
                ) {
                    Text(
                        text = "Submit Votes & Leave",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Warning dialog for incomplete voting
    if (showVoteWarningDialog) {
        AlertDialog(
            onDismissRequest = { showVoteWarningDialog = false },
            title = {
                Text(
                    "Incomplete Voting",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Please vote on all restaurants before leaving. You have ${restaurants.size - votes.size} restaurant(s) left to vote on.")
            },
            confirmButton = {
                TextButton(onClick = { showVoteWarningDialog = false }) {
                    Text("OK", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFFFFF9C4)
        )
    }

    // Time's up dialog
    if (showTimeUpDialog) {
        AlertDialog(
            onDismissRequest = {
                // Can't dismiss - must take action
            },
            title = {
                Text(
                    "Time's Up!",
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            },
            text = {
                Text("Voting time has expired. Unvoted restaurants will be marked as thumbs down.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Apply default votes (thumbs down) to unvoted restaurants
                        restaurants.forEachIndexed { index, _ ->
                            if (!votes.containsKey(index)) {
                                votes[index] = false
                            }
                        }

                        // Submit all votes
                        // viewModel.submitAllVotes(votes)

                        // Close dialog and navigate to group details
                        showTimeUpDialog = false
                        navController.navigate(NavRoutes.GROUP)
                    }
                ) {
                    Text("Submit Votes", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFFFFF9C4)
        )
    }
}