package com.example.cpen_321.ui.screens

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialException
import androidx.credentials.GetCredentialRequest
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import com.example.cpen_321.BuildConfig
import com.example.cpen_321.ui.viewmodels.AuthViewModel
import com.example.cpen_321.ui.viewmodels.AuthState
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

/**
 * Authentication Screen - Login with Google
 * Matches original FeastFriends design with yellow/beige color scheme
 */

// ========================= MAIN SCREEN =========================

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val authState by viewModel.authState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }

    // Navigate when authenticated
    LaunchedEffect(authState) {
        if (authState is AuthState.Authenticated) {
            Log.d("AuthScreen", "✅ User authenticated, navigating to home")
            onNavigateToHome()
        }
    }

    // Show error messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackBarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Long,
                actionLabel = "Dismiss"
            )
            viewModel.clearError()
        }
    }

    // Main Content with beige background
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFD2B48C)) // Beige/tan background from original design
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // App Title - Original Design
                Text(
                    text = "Welcome to\nFeastFriends",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                    lineHeight = 40.sp
                )

                Spacer(modifier = Modifier.height(64.dp))

                // Auth Buttons - Original Yellow Design
                AuthButtons(
                    isLoading = isLoading,
                    onSignInClick = {
                        handleGoogleSignIn(
                            context = context as? ComponentActivity,
                            viewModel = viewModel,
                            snackbarHostState = snackBarHostState,
                            isSignUp = false
                        )
                    },
                    onSignUpClick = {
                        handleGoogleSignIn(
                            context = context as? ComponentActivity,
                            viewModel = viewModel,
                            snackbarHostState = snackBarHostState,
                            isSignUp = true
                        )
                    }
                )

                if (isLoading) {
                    Spacer(modifier = Modifier.height(24.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = Color.Black,
                        strokeWidth = 3.dp
                    )
                }

                // Error dialog will be shown separately
            }
        }
    }

    // Error Alert Dialog
    errorMessage?.let { message ->
        Dialog(onDismissRequest = { viewModel.clearError() }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Authentication Error",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = message,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { viewModel.clearError() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5722)
                        )
                    ) {
                        Text(
                            text = "OK",
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}

// ========================= UI COMPONENTS =========================

@Composable
private fun AuthButtons(
    isLoading: Boolean,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Login Button - Yellow with black border (original design)
        Button(
            onClick = onSignInClick,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD54F), // Yellow from original
                disabledContainerColor = Color.Gray,
                contentColor = Color.Black
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = "Login with Google\nAuthentication",
                color = Color.Black,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }

        // Sign Up Button - Yellow with black border (original design)
        Button(
            onClick = onSignUpClick,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD54F), // Yellow from original
                disabledContainerColor = Color.Gray,
                contentColor = Color.Black
            ),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(
                text = "Sign up with Google\nAuthentication",
                color = Color.Black,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
        }
    }
}

// ========================= GOOGLE SIGN-IN LOGIC =========================

/**
 * Handle Google Sign-In/Sign-Up process
 * Fixed to work without Google account by setting filterByAuthorizedAccounts = false
 */
private fun handleGoogleSignIn(
    context: ComponentActivity?,
    viewModel: AuthViewModel,
    snackbarHostState: SnackbarHostState,
    isSignUp: Boolean
) {
    if (context == null) {
        Log.e("AuthScreen", "❌ Context is not a ComponentActivity")
        return
    }

    context.lifecycleScope.launch {
        try {
            Log.d("AuthScreen", "🔄 Starting Google Sign-In...")

            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            Log.d("AuthScreen", "📱 Requesting Google credentials...")

            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val idToken = credential.idToken

            Log.d("AuthScreen", "✅ Got Google ID token, ${if (isSignUp) "signing up" else "signing in"} with backend...")

            if (isSignUp) {
                viewModel.signUpWithGoogle(idToken)
            } else {
                viewModel.signInWithGoogle(idToken)
            }

        } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
            Log.d("AuthScreen", "ℹ️ User cancelled sign-in")

        } catch (e: androidx.credentials.exceptions.NoCredentialException) {
            Log.e("AuthScreen", "❌ No Google account available")
            // Try again WITHOUT filtering - this opens account picker
            context.lifecycleScope.launch {
                try {
                    val credentialManager = CredentialManager.create(context)
                    val googleIdOption = GetGoogleIdOption.Builder()
                        .setFilterByAuthorizedAccounts(false)
                        .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                        .build()

                    val request = GetCredentialRequest.Builder()
                        .addCredentialOption(googleIdOption)
                        .build()

                    val result = credentialManager.getCredential(
                        request = request,
                        context = context
                    )

                    val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
                    val idToken = credential.idToken

                    if (isSignUp) {
                        viewModel.signUpWithGoogle(idToken)
                    } else {
                        viewModel.signInWithGoogle(idToken)
                    }
                } catch (retryException: GetCredentialException) {
                    Log.e("AuthScreen", "❌ Retry failed: ${retryException.message}")
                    snackbarHostState.showSnackbar(
                        "Please add a Google account: Settings → Accounts → Add Account → Google",
                        duration = SnackbarDuration.Long
                    )
                }
            }

        } catch (e: GetCredentialException) {
            Log.e("AuthScreen", "❌ Sign-in error: ${e.message}", e)
            snackbarHostState.showSnackbar(
                "Sign-in failed: ${e.message ?: "Unknown error"}",
                duration = SnackbarDuration.Long
            )
        } catch (e: RuntimeException) {
            Log.e("AuthScreen", "❌ Sign-in error: ${e.message}", e)
            snackbarHostState.showSnackbar(
                "Sign-in failed: ${e.message ?: "Unknown error"}",
                duration = SnackbarDuration.Long
            )
        }
    }
}