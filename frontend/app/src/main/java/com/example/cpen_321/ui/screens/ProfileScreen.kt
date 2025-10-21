package com.example.cpen_321.ui.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.cpen_321.ui.components.MainBottomBar
import com.example.cpen_321.data.model.UserProfile
import com.example.cpen_321.fake.FakeProfileViewModel
import com.example.cpen_321.ui.viewmodels.ProfileViewModel
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.cpen_321.R
@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel(),
    userId : Int?
){
    val userProfile by viewModel.userProfile.collectAsState()
    val scrollState = rememberScrollState()

    // Trigger loading when this composable appears NOTE: UN-COMMENT FOR REAL IMPLEMENTATION
    LaunchedEffect(userId) {
        viewModel.loadUserProfile(userId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") }
            )
        },
        bottomBar = { MainBottomBar(navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
                contentAlignment = Alignment.Center
        ) {
            when (val user = userProfile) {
                null -> CircularProgressIndicator()
                else -> ProfileContent(user)
            }
        }
    }
}

@Composable
private fun ProfileContent(user: UserProfile) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        // 🖼 Fixed banner at top
        ProfileHeader(user.profilePicture)

        // 🧾 User info below
        TitleSection(user)
        Divider()
        ProfileDetails(user)
    }
}

@Composable
private fun ProfileHeader(imageUrl: String?) {
    val bannerHeight = 220.dp

    if (imageUrl != null) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .height(bannerHeight),
            painter = rememberAsyncImagePainter(imageUrl),
            contentScale = ContentScale.Crop,
            contentDescription = "Profile Picture"
        )
    } else {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .height(bannerHeight),
            painter = painterResource(id = R.drawable.default_avatar),
            contentScale = ContentScale.Crop,
            contentDescription = "Default Avatar"
        )
    }
}

@Composable
private fun TitleSection(user: UserProfile) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = user.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ProfileDetails(user: UserProfile) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Bio",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = user.bio ?: "No bio available",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}


