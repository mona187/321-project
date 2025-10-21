package com.example.cpen_321.ui.screens

import NavRoutes
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.cpen_321.ui.viewmodels.MatchViewModel
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import com.example.cpen_321.fake.FakeMatchViewModel
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import com.example.cpen_321.ui.components.MainBottomBar
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.collectAsState
import com.example.cpen_321.fake.FakeAuthViewModel
import com.example.cpen_321.ui.viewmodels.AuthViewModel
import androidx.compose.runtime.getValue
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: MatchViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
){
    val uiState by authViewModel.uiState.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                actions = {
                    Button(
                        onClick = { authViewModel.signOut() }
                    ) {
                        Text("Sign Out")
                    }
                }
            )
        },
        bottomBar = { MainBottomBar(navController) }
    ) { innerPadding ->
        Column(
            Modifier.fillMaxSize().padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Get authenticated userId
            val userId = uiState.user?.userId

            OutlinedButton(
                onClick = {
                    if (userId != null) {
                        viewModel.connectSocket(userId) // 🔗 actual user id
                        navController.navigate(NavRoutes.WAITING_ROOM)
                    } else {
                        // Handle missing user id safely
                        println("Error: userId is null")
                    }
                }
            ) {
                Text("Find Match")
            }
        }
    }
}




//@Preview
//@Composable






