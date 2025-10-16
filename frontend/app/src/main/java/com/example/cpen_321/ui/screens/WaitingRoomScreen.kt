// ui/screens/WaitingRoomScreen.kt
package com.example.cpen_321.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.cpen_321.data.model.UserProfile
import com.example.cpen_321.fake.FakeMatchViewModel
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.cpen_321.ui.viewmodels.MatchViewModel

@Composable
fun WaitingRoomScreen(
    navController: NavController,
    viewModel: FakeMatchViewModel = remember { FakeMatchViewModel() }
     //viewModel: MatchViewModel = hiltViewModel()

) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (!state.groupReady) {
            Text("Waiting Room", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(20.dp))
            Text(String.format("%d:%02d", state.timeRemainingSeconds / 60, state.timeRemainingSeconds % 60), style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(20.dp))
            UserBubbleRow(state.members)
        } else {
            Text("Group Ready!", fontWeight = FontWeight.Bold)
            // leave socket and navigate to Group Screen
            // viewModel.leaveRoom("user123") //REPLACE WITH ACTUAL USER ID
           navController.navigate(NavRoutes.GROUP)
        }
    }
}

@Composable
fun UserBubbleRow(users: List<UserProfile>) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(users) { user ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    model = user.profilePicture,
                    contentDescription = user.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .border(2.dp, Color.Gray, CircleShape)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(user.name)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WaitingRoomPreview() {
    WaitingRoomScreen(navController = rememberNavController())
}
