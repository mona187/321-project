package com.example.cpen_321.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.cpen_321.ui.viewmodels.UserViewModel
import androidx.compose.foundation.layout.RowScope

@Composable
fun PreferencesScreen(
    onNavigateBack: () -> Unit,
    viewModel: UserViewModel = hiltViewModel()
) {
    val selectedCuisines by viewModel.selectedCuisines.collectAsState()
    val budget by viewModel.budget.collectAsState()
    val radius by viewModel.radius.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    PreferencesEffects(
        viewModel = viewModel,
        successMessage = successMessage,
        errorMessage = errorMessage,
        snackbarHostState = snackbarHostState
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        PreferencesContent(
            modifier = Modifier.padding(innerPadding),
            selectedCuisines = selectedCuisines.toList(),  // Convert Set to List
            budget = budget,
            radius = radius,
            isLoading = isLoading,
            viewModel = viewModel,
            onNavigateBack = onNavigateBack
        )
    }
}

@Composable
private fun PreferencesEffects(
    viewModel: UserViewModel,
    successMessage: String?,
    errorMessage: String?,
    snackbarHostState: SnackbarHostState
) {
    LaunchedEffect(Unit) {
        viewModel.loadUserSettings()
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar("Error: $it")
            viewModel.clearError()
        }
    }
}

@Composable
private fun PreferencesContent(
    modifier: Modifier,
    selectedCuisines: List<String>,
    budget: Double,
    radius: Double,
    isLoading: Boolean,
    viewModel: UserViewModel,
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Preferences (Select)",
            fontSize = 20.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            textAlign = TextAlign.Center
        )

        if (isLoading) {
            LoadingState()
        } else {
            PreferencesForm(
                selectedCuisines = selectedCuisines,
                budget = budget,
                radius = radius,
                isLoading = isLoading,
                viewModel = viewModel,
                onNavigateBack = onNavigateBack
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text("Loading preferences...")
    }
}

@Composable
private fun PreferencesForm(
    selectedCuisines: List<String>,
    budget: Double,
    radius: Double,
    isLoading: Boolean,
    viewModel: UserViewModel,
    onNavigateBack: () -> Unit
) {
    Column {  // Add Column wrapper
        CuisineGrid(
            selectedCuisines = selectedCuisines,
            onToggleCuisine = { viewModel.toggleCuisine(it) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        BudgetSection(
            budget = budget,
            onBudgetChange = { viewModel.updateBudget(it.toDouble()) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        RadiusSection(
            radius = radius,
            onRadiusChange = { viewModel.updateRadius(it.toDouble()) }
        )

        Spacer(modifier = Modifier.weight(1f))

        ActionButtons(
            isLoading = isLoading,
            onSave = { viewModel.savePreferences() },
            onNavigateBack = onNavigateBack
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CuisineGrid(
    selectedCuisines: List<String>,
    onToggleCuisine: (String) -> Unit
) {
    val cuisineOptions = listOf(
        "Sushi", "Italian",
        "Pizza", "Japanese",
        "European", "Korean",
        "Middle Eastern", "Chinese"
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        cuisineOptions.chunked(2).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { cuisine ->
                    CuisineButton(
                        cuisine = cuisine,
                        isSelected = selectedCuisines.contains(cuisine),
                        onClick = { onToggleCuisine(cuisine) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.CuisineButton(
    cuisine: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .weight(1f)
            .height(60.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected)
                Color(0xFFFFB300) // Darker yellow when selected
            else
                Color(0xFFFFD54F) // Regular yellow
        )
    ) {
        Text(
            text = cuisine,
            color = Color.Black,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun BudgetSection(
    budget: Double,
    onBudgetChange: (Float) -> Unit
) {
    Text(
        text = "Max amount of money to spend: $${budget.toInt()}",
        fontSize = 16.sp,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    Slider(
        value = budget.toFloat(),
        onValueChange = onBudgetChange,
        valueRange = 0f..200f,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun RadiusSection(
    radius: Double,
    onRadiusChange: (Float) -> Unit
) {
    Text(
        text = "Search radius: ${radius.toInt()} km",
        fontSize = 16.sp,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(modifier = Modifier.height(8.dp))

    Slider(
        value = radius.toFloat(),
        onValueChange = onRadiusChange,
        valueRange = 1f..50f,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun ActionButtons(
    isLoading: Boolean,
    onSave: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Button(
        onClick = onSave,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.Black,
                modifier = Modifier.height(24.dp)
            )
        } else {
            Text(
                text = "Save Preferences",
                color = Color.Black,
                fontSize = 20.sp
            )
        }
    }

    Spacer(modifier = Modifier.height(8.dp))

    Button(
        onClick = onNavigateBack,
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F))
    ) {
        Text(
            text = "Go Back",
            color = Color.Black,
            fontSize = 20.sp
        )
    }
}