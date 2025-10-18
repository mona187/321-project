package com.example.cpen_321.ui.screens

import NavRoutes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.cpen_321.ui.viewmodels.MatchViewModel
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import com.example.cpen_321.fake.FakeMatchViewModel
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import com.example.cpen_321.ui.components.MainBottomBar
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp


//Add fonts
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.cpen_321.R

//Add font
val PlaywriteFontFamily = FontFamily(
    Font(R.font.playwrite_usmodern_variablefont_wght)
)

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: FakeMatchViewModel = remember { FakeMatchViewModel() }
){
    Scaffold(
        bottomBar = { MainBottomBar(navController = navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Profile icon in top right
            IconButton(
                onClick = {
                    navController.navigate(NavRoutes.PROFILE_CONFIG)
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(
                    Icons.Filled.AccountCircle,
                    contentDescription = "Profile",
                    modifier = Modifier.width(48.dp).height(48.dp)
                )
            }

            // Center content with buttons
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Welcome text
                Text(
                    text = "Welcome to FeastFriends!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontFamily = PlaywriteFontFamily,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = {
                        val userId = "user123" // this needs to be actual userId
                        navController.navigate(NavRoutes.WAITING_ROOM)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD54F)
                    )
                ) {
                    Text(
                        text ="Start Matchmaking",
                        color = Color.Black,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        navController.navigate(NavRoutes.VIEW_GROUPS)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD54F) // Yellow color
                    )
                ) {
                    Text(
                        text = "Current Groups",
                        color = Color.Black,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}