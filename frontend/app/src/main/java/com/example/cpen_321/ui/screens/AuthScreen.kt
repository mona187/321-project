package com.example.cpen_321.ui.screens

import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.cpen_321.R
import com.example.cpen_321.ui.theme.LocalSpacing
import com.example.cpen_321.ui.viewmodels.AuthUiState
import com.example.cpen_321.ui.viewmodels.AuthViewModel
import kotlinx.coroutines.launch
import com.example.cpen_321.fake.FakeAuthViewModel

// ------------------------- Data Holders -------------------------

private data class AuthScreenActions(
    val isSigningIn: Boolean,
    val isSigningUp: Boolean,
    val onSignInClick: () -> Unit,
    val onSignUpClick: () -> Unit
)

// ------------------------- Main Screen -------------------------

@Composable
fun AuthScreen(
    // authViewModel: AuthViewModel,
    authViewModel: FakeAuthViewModel,
    onNavigateToHome: () -> Unit = {} // 👈 ADD THIS PARAMETER
) {
    val context = LocalContext.current
    val uiState by authViewModel.uiState.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }

    // 👇 ADD THIS EFFECT TO HANDLE NAVIGATION
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            onNavigateToHome()
        }
    }

    // 👇 ADD SUCCESS/ERROR MESSAGE HANDLING
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { message ->
            snackBarHostState.showSnackbar(message)
            authViewModel.clearSuccessMessage()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackBarHostState.showSnackbar(message)
            authViewModel.clearError()
        }
    }

    AuthContent(
        uiState = uiState,
        snackBarHostState = snackBarHostState,
        onSignInClick = {
            Log.d("AuthScreen", "Sign-In button clicked ✅")
            (context as? ComponentActivity)?.lifecycleScope?.launch {
                val result = authViewModel.signInWithGoogle(context)
                result.onSuccess { credential ->
                    Log.d("AuthScreen", "Got Google credential ✅: ${credential.idToken.take(15)}...")
                    authViewModel.handleGoogleSignInResult(credential)
                }.onFailure { e ->
                    Log.e("AuthScreen", "Google Sign-In failed ❌ ${e.message}", e)
                    // Optionally show an error
                    snackBarHostState.showSnackbar("Sign-in failed: ${e.message}")
                }
            }


},
        onSignUpClick = {
            (context as? ComponentActivity)?.lifecycleScope?.launch {
                val result = authViewModel.signInWithGoogle(context)
                result.onSuccess { credential ->
                    authViewModel.handleGoogleSignUpResult(credential)
                }
            }
        },
        onSuccessMessageShown = authViewModel::clearSuccessMessage,
        onErrorMessageShown = authViewModel::clearError
    )
}

// ------------------------- UI Content -------------------------

@Composable
private fun AuthContent(
    uiState: AuthUiState,
    snackBarHostState: SnackbarHostState,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit,
    onSuccessMessageShown: () -> Unit,
    onErrorMessageShown: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { paddingValues ->
        AuthBody(
            paddingValues = paddingValues,
            actions = AuthScreenActions(
                isSigningIn = uiState.isSigningIn,
                isSigningUp = uiState.isSigningUp,
                onSignInClick = onSignInClick,
                onSignUpClick = onSignUpClick
            )
        )
    }
}

@Composable
private fun AuthBody(
    paddingValues: PaddingValues,
    actions: AuthScreenActions,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AppTitle()

        Spacer(modifier = Modifier.height(spacing.large))

        AuthButtons(
            isSigningIn = actions.isSigningIn,
            isSigningUp = actions.isSigningUp,
            onSignInClick = actions.onSignInClick,
            onSignUpClick = actions.onSignUpClick
        )
    }
}

// ------------------------- UI Components -------------------------

@Composable
private fun AppTitle(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(R.string.app_name),
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}

@Composable
private fun AuthButtons(
    isSigningIn: Boolean,
    isSigningUp: Boolean,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing.medium)
    ) {
        GoogleSignInButton(
            isLoading = isSigningIn,
            onClick = onSignInClick,
            enabled = !isSigningIn && !isSigningUp
        )

        GoogleSignUpButton(
            isLoading = isSigningUp,
            onClick = onSignUpClick,
            enabled = !isSigningIn && !isSigningUp
        )
    }
}

@Composable
private fun GoogleSignInButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Button(onClick = onClick, enabled = enabled) {
        GoogleButtonContent(isLoading = isLoading, text = stringResource(R.string.sign_in_with_google))
    }
}

@Composable
private fun GoogleSignUpButton(
    isLoading: Boolean,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        GoogleButtonContent(isLoading = isLoading, text = stringResource(R.string.sign_up_with_google))
    }
}

@Composable
private fun GoogleButtonContent(
    isLoading: Boolean,
    text: String,
    modifier: Modifier = Modifier
) {
    val spacing = LocalSpacing.current

    if (isLoading) {
        CircularProgressIndicator(
            modifier = modifier.size(spacing.large),
            strokeWidth = 2.dp
        )
    } else {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = "Google logo",
                modifier = Modifier.size(spacing.large)
            )
            Spacer(modifier = Modifier.width(spacing.small))
            Text(text)
        }
    }
}