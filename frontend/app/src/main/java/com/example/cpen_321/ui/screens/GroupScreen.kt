package com.example.cpen_321.ui.screens

import NavRoutes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

data class GroupMember(
    val name: String,
    val credibilityScore: Int,
    val phoneNumber: String
)

@Composable
fun GroupScreen(
    navController: NavController
) {
    // Replace with actual data from your ViewModel
    val restaurantName = "Restaurant Name"
    val restaurantLocation = "Location"

    // Sample group members - replace with actual data
    val groupMembers = listOf(
        GroupMember("Member 1", 85, "123-456-7890"),
        GroupMember("Member 2", 92, "234-567-8901"),
        GroupMember("Member 3", 78, "345-678-9012"),
        GroupMember("Member 4", 88, "456-789-0123")
    )

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Scrollable content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Group title box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .background(Color(0xFFFFF9C4))
                        .border(2.dp, Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Group",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Restaurant info box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF9C4))
                        .border(2.dp, Color.Black)
                        .padding(16.dp)
                ) {
                    Column {
                        Text(
                            text = "Restaurant Name: $restaurantName",
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Location: $restaurantLocation",
                            fontSize = 16.sp,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Members list
                groupMembers.forEach { member ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFFF9C4))
                            .border(2.dp, Color.Black)
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = member.name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Name: ${member.name}",
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Credibility Score: ${member.credibilityScore}",
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Phone Number: ${member.phoneNumber}",
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // Close Group button at bottom
            Button(
                onClick = {
                    // Handle closing/leaving the group
                    // viewModel.leaveGroup()
                    navController.navigate(NavRoutes.HOME)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD54F)
                )
            ) {
                Text(
                    text = "Close Group",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}