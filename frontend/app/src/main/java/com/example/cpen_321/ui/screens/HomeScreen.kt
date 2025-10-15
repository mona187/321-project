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
@Composable
fun HomeScreen(
    navController: NavController,
    // viewModel: MatchViewModel = hiltViewModel()
    viewModel: FakeMatchViewModel = remember { FakeMatchViewModel() }

){
    Column (
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ){
      OutlinedButton(
            onClick = {
                val userId = "user123" // this needs to be actual userId
                // viewModel.connectSocket(userId)
                navController.navigate(NavRoutes.WAITING_ROOM)
          }
      ){
          Text("Find Match")
      }
    }
}

//@Preview
//@Composable
