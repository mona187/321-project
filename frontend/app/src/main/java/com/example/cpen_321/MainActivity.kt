package com.example.cpen_321

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.cpen_321.ui.navigation.AppNavigation
import com.example.cpen_321.ui.theme.Cpen321Theme
import com.example.cpen_321.ui.viewmodels.AuthViewModel
import com.example.cpen_321.ui.viewmodels.AuthUiState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Cpen321Theme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent() {
    // Initialize dependencies
    val authDataStore = remember { AppModule.provideAuthDataStore(androidx.compose.ui.platform.LocalContext.current) }
    val authInterceptor = remember { AppModule.provideAuthInterceptor() }
    val authRepository = remember { AppModule.provideAuthRepository(authDataStore, authInterceptor) }
    val authViewModel: AuthViewModel = viewModel { AppModule.provideAuthViewModel(authRepository) }
    
    val uiState by authViewModel.uiState.collectAsState()
    val navController = rememberNavController()
    
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        AppNavigation(
            navController = navController,
            startDestination = if (uiState.isLoggedIn) Screen.Home.route else Screen.Auth.route
        )
    }
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    Cpen321Theme {
        Text("Hello Android!")
    }
}