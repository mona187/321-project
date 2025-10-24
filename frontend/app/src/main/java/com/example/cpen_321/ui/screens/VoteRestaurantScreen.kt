package com.example.cpen_321.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cpen_321.data.model.Restaurant
import com.example.cpen_321.ui.viewmodels.GroupViewModel
import com.example.cpen_321.ui.viewmodels.RestaurantViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("MissingPermission")
@Composable
fun VoteRestaurantScreen(
    navController: NavController,
    groupId: String? = null,
    groupViewModel: GroupViewModel = hiltViewModel(),
    restaurantViewModel: RestaurantViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Collect states
    val currentGroup by groupViewModel.currentGroup.collectAsState()
    val groupMembers by groupViewModel.groupMembers.collectAsState()
    val currentVotes by groupViewModel.currentVotes.collectAsState()
    val selectedRestaurant by groupViewModel.selectedRestaurant.collectAsState()
    val userVote by groupViewModel.userVote.collectAsState()

    val restaurants by restaurantViewModel.restaurants.collectAsState()
    val isLoadingRestaurants by restaurantViewModel.isLoading.collectAsState()
    val restaurantError by restaurantViewModel.errorMessage.collectAsState()

    var selectedRestaurantForVote by remember { mutableStateOf<Restaurant?>(null) }
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var locationPermissionGranted by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Location permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (locationPermissionGranted) {
            // Get location after permission granted
            scope.launch {
                try {
                    val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
                    val location = withContext(Dispatchers.IO) {
                        locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                            ?: locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                    }
                    location?.let {
                        userLocation = Pair(it.latitude, it.longitude)
                    }
                } catch (e: SecurityException) {
                    // Handle exception
                }
            }
        }
    }

    // Request location permission on launch
    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Load group status
    LaunchedEffect(Unit) {
        groupViewModel.loadGroupStatus()
    }

    // Subscribe to group socket for restaurant_selected event
    LaunchedEffect(currentGroup) {
        currentGroup?.groupId?.let { gId ->
            android.util.Log.d("VoteRestaurantScreen", "Subscribing to group: $gId")
            groupViewModel.subscribeToGroup(gId)
        }
    }

    // Cleanup: Unsubscribe when leaving screen
    DisposableEffect(Unit) {
        onDispose {
            currentGroup?.groupId?.let { gId ->
                android.util.Log.d("VoteRestaurantScreen", "Unsubscribing from group: $gId")
                groupViewModel.unsubscribeFromGroup(gId)
            }
        }
    }

    // Load restaurants when user location is available
    LaunchedEffect(userLocation, currentGroup) {
        if (userLocation != null && currentGroup != null) {
            val (latitude, longitude) = userLocation!!
            val radius = 5000 // 5km in meters
            val cuisines = listOf<String>() // You can derive this from group preferences

            restaurantViewModel.searchRestaurants(
                latitude = latitude,
                longitude = longitude,
                radius = radius,
                cuisineTypes = cuisines,
                priceLevel = null
            )
        }
    }

    // Navigate when restaurant is selected
    LaunchedEffect(selectedRestaurant) {
        selectedRestaurant?.let { restaurant ->
            snackbarHostState.showSnackbar("${restaurant.name} has been selected!")
            kotlinx.coroutines.delay(1500)
            navController.navigate("group") {
                popUpTo(0)
            }
        }
    }

    // Show error messages
    LaunchedEffect(restaurantError) {
        restaurantError?.let { message ->
            snackbarHostState.showSnackbar(message)
            restaurantViewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Header
            Text(
                text = "Vote for Restaurant",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Voting status
            currentGroup?.let { group ->
                val totalMembers = group.getAllMembers().size
                val votedMembers = currentVotes.size

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF9C4)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Voting Progress: $votedMembers / $totalMembers",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { votedMembers.toFloat() / totalMembers.toFloat() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp),
                            color = Color(0xFF4CAF50),
                        )

                        if (userVote != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "✓ You have voted",
                                fontSize = 14.sp,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Location status
            if (!locationPermissionGranted) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFEBEE)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Location permission required",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        ) {
                            Text("Grant Permission")
                        }
                    }
                }
            } else if (userLocation == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Getting your location...",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Restaurant list
            if (userLocation != null) {
                if (isLoadingRestaurants) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (restaurants.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No restaurants found nearby",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    userLocation?.let { (lat, lon) ->
                                        restaurantViewModel.searchRestaurants(
                                            latitude = lat,
                                            longitude = lon,
                                            radius = 5000
                                        )
                                    }
                                }
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(restaurants) { restaurant ->
                            RestaurantCard(
                                restaurant = restaurant,
                                isSelected = selectedRestaurantForVote?.restaurantId == restaurant.restaurantId,
                                hasVoted = userVote != null,
                                voteCount = currentVotes[restaurant.restaurantId] ?: 0,
                                onClick = {
                                    if (userVote == null) {
                                        selectedRestaurantForVote = restaurant
                                    }
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Vote button
                    Button(
                        onClick = {
                            selectedRestaurantForVote?.let { restaurant ->
                                val restId = restaurant.restaurantId
                                if (restId != null) {
                                    groupViewModel.voteForRestaurant(
                                        restId,
                                        restaurant
                                    )
                                    selectedRestaurantForVote = null
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = selectedRestaurantForVote != null && userVote == null && selectedRestaurantForVote?.restaurantId != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD54F),
                            disabledContainerColor = Color.Gray
                        )
                    ) {
                        Text(
                            text = if (userVote != null) "Already Voted" else "Submit Vote",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    isSelected: Boolean,
    hasVoted: Boolean,
    voteCount: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !hasVoted) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF2196F3))
        } else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Restaurant image
            restaurant.getMainPhotoUrl()?.let { photoUrl ->
                AsyncImage(
                    model = photoUrl,
                    contentDescription = restaurant.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.LightGray)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            // Restaurant info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = restaurant.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    restaurant.rating?.let { rating ->
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = restaurant.getRatingString(),
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    restaurant.priceLevel?.let {
                        Text(
                            text = restaurant.getPriceLevelString(),
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = restaurant.location,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (voteCount > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$voteCount vote${if (voteCount != 1) "s" else ""}",
                        fontSize = 12.sp,
                        color = Color(0xFF4CAF50),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Selection indicator
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}