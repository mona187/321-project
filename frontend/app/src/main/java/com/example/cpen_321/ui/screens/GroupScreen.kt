package com.example.cpen_321.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cpen_321.data.model.RestaurantRecommendation
import com.example.cpen_321.ui.viewmodels.GroupViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupScreen(
    navController: NavController,
    viewModel: GroupViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    
    // Debug logging
    LaunchedEffect(state) {
        Log.d("GroupScreen", "🔄 State updated - isLoading: ${state.isLoading}, recommendations: ${state.recommendations.size}, error: ${state.error}")
        if (state.recommendations.isNotEmpty()) {
            Log.d("GroupScreen", "🍽️ First recommendation: ${state.recommendations.first().restaurant.name}")
        }
    }
    
    // Auto-load restaurants when screen opens
    LaunchedEffect(Unit) {
        // For now, use test data. Later we'll get real groupId from match state
        val testPreferences = listOf(
            com.example.cpen_321.data.network.api.UserPreference(
                userId = "test_user",
                cuisine = listOf("Italian", "Chinese"),
                budget = 25.0,
                radiusKm = 5.0
            )
        )
        viewModel.loadGroupRecommendations("test_group_123", testPreferences)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Restaurant Recommendations") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                state.error != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Error loading recommendations",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = state.error ?: "Unknown error",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                state.recommendations.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No recommendations available",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.recommendations) { recommendation ->
                            RestaurantCard(recommendation = recommendation)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RestaurantCard(recommendation: RestaurantRecommendation) {
    val restaurant = recommendation.restaurant
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Restaurant Image
            if (restaurant.photos.isNotEmpty()) {
                AsyncImage(
                    model = restaurant.photos.first(),
                    contentDescription = restaurant.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Restaurant Name
            Text(
                text = restaurant.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Address
            Text(
                text = restaurant.address,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Rating and Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = String.format("%.1f", restaurant.rating),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Text(
                    text = "$${restaurant.priceLevel}".repeat(restaurant.priceLevel),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
                   // Location
                   Text(
                       text = restaurant.location,
                       style = MaterialTheme.typography.bodySmall,
                       color = MaterialTheme.colorScheme.onSurfaceVariant
                   )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Recommendation Score
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Match Score: ${String.format("%.0f", recommendation.score * 100)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    
                    if (recommendation.reasons.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Why: ${recommendation.reasons.joinToString(", ")}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GroupScreenPreview() {
    GroupScreen(navController = androidx.navigation.compose.rememberNavController())
}
