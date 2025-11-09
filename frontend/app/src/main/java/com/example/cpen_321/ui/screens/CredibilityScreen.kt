package com.example.cpen_321.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cpen_321.ui.viewmodels.UserViewModel

@Composable
fun CredibilityScreen(
    onNavigateBack: () -> Unit,
    viewModel: UserViewModel = hiltViewModel()
) {
    val userSettings by viewModel.userSettings.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUserSettings()
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            CredibilityContent(
                isLoading = isLoading,
                errorMessage = errorMessage,
                credibilityScore = userSettings?.credibilityScore?.toInt() ?: 0
            )
            
            BackButton(onNavigateBack = onNavigateBack)
        }
    }
}

@Composable
private fun CredibilityContent(
    isLoading: Boolean,
    errorMessage: String?,
    credibilityScore: Int
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(modifier = Modifier.height(32.dp))
        TitleBox()
        Spacer(modifier = Modifier.height(32.dp))
        CredibilityScoreDisplay(
            isLoading = isLoading,
            errorMessage = errorMessage,
            credibilityScore = credibilityScore
        )
    }
}

@Composable
private fun TitleBox() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color(0xFFFFF9C4))
            .border(2.dp, Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Credibility Score",
            fontSize = 24.sp,
            color = Color.Black
        )
    }
}

@Composable
private fun CredibilityScoreDisplay(
    isLoading: Boolean,
    errorMessage: String?,
    credibilityScore: Int
) {
    when {
        isLoading -> LoadingIndicator()
        errorMessage != null -> ErrorDisplay(errorMessage)
        else -> ProgressBar(credibilityScore)
    }
}

@Composable
private fun LoadingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorDisplay(errorMessage: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .border(2.dp, Color.Red)
            .background(Color(0xFFFFEBEE)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = errorMessage,
            fontSize = 16.sp,
            color = Color.Red,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ProgressBar(credibilityScore: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .border(2.dp, Color.Black)
    ) {
        if (credibilityScore > 0) {
            FilledProgressPortion(credibilityScore)
        }
        EmptyProgressPortion()
    }
}

@Composable
private fun FilledProgressPortion(credibilityScore: Int) {
    Box(
        modifier = Modifier
            .fillMaxWidth(credibilityScore / 100f)
            .height(80.dp)
            .background(Color(0xFF81C784))
            .border(1.dp, Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$credibilityScore%",
            fontSize = 20.sp,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyProgressPortion() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color.White)
    )
}

@Composable
private fun BackButton(onNavigateBack: () -> Unit) {
    Button(
        onClick = onNavigateBack,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(bottom = 16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFFD54F)
        )
    ) {
        Text(
            text = "Go Back",
            color = Color.Black,
            fontSize = 20.sp
        )
    }
}