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
import androidx.credentials.CredentialManager
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

                // Show error message if any
                errorMessage?.let { message ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = message,
                        color = Color.Red,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
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

            // FIXED: Configure to NOT filter by authorized accounts
            // This allows sign-in even without a Google account already on device
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // ← KEY FIX: Allow account picker
                .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
                .setAutoSelectEnabled(false) // Don't auto-select, show account picker
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            Log.d("AuthScreen", "📱 Requesting Google credentials...")

            // Get credential from Google
            val result = credentialManager.getCredential(
                request = request,
                context = context
            )

            // Extract ID token
            val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
            val idToken = credential.idToken

            Log.d("AuthScreen", "✅ Got Google ID token, ${if (isSignUp) "signing up" else "signing in"} with backend...")

            // Authenticate with backend
            if (isSignUp) {
                viewModel.signUpWithGoogle(idToken)
            } else {
                viewModel.signInWithGoogle(idToken)
            }

        } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
            Log.d("AuthScreen", "ℹ️ User cancelled sign-in")
            // User cancelled, don't show error

        } catch (e: androidx.credentials.exceptions.NoCredentialException) {
            Log.e("AuthScreen", "❌ No Google account available")
            snackbarHostState.showSnackbar(
                "No Google account found. Please add a Google account in Settings → Accounts.",
                duration = SnackbarDuration.Long
            )

        } catch (e: androidx.credentials.exceptions.GetCredentialException) {
            Log.e("AuthScreen", "❌ Credential error: ${e.message}", e)
            snackbarHostState.showSnackbar(
                "Sign-in error: ${e.message ?: "Please try again"}",
                duration = SnackbarDuration.Long
            )

        } catch (e: Exception) {
            Log.e("AuthScreen", "❌ Unexpected error: ${e.message}", e)
            snackbarHostState.showSnackbar(
                "Sign-in failed: ${e.message ?: "Unknown error"}",
                duration = SnackbarDuration.Long
            )
        }
    }
}