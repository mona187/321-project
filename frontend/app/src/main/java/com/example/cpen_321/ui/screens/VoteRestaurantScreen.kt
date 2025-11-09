package com.example.cpen_321.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedRestaurantForVote by remember { mutableStateOf<Restaurant?>(null) }
    var userLocation by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var locationPermissionGranted by remember { mutableStateOf(false) }

    val locationPermissionLauncher = createLocationPermissionLauncher(
        context = context,
        scope = scope,
        onPermissionGranted = { locationPermissionGranted = true },
        onLocationReceived = { userLocation = it }
    )

    VoteScreenEffects(
        groupViewModel = groupViewModel,
        restaurantViewModel = restaurantViewModel,
        navController = navController,
        snackbarHostState = snackbarHostState,
        userLocation = userLocation,
        locationPermissionLauncher = locationPermissionLauncher
    )

    VoteScreenContent(
        groupViewModel = groupViewModel,
        restaurantViewModel = restaurantViewModel,
        snackbarHostState = snackbarHostState,
        selectedRestaurantForVote = selectedRestaurantForVote,
        userLocation = userLocation,
        locationPermissionGranted = locationPermissionGranted,
        locationPermissionLauncher = locationPermissionLauncher,
        onRestaurantSelected = { selectedRestaurantForVote = it },
        onVoteSubmitted = { selectedRestaurantForVote = null }
    )
}

@Composable
private fun createLocationPermissionLauncher(
    context: android.content.Context,
    scope: kotlinx.coroutines.CoroutineScope,
    onPermissionGranted: () -> Unit,
    onLocationReceived: (Pair<Double, Double>) -> Unit
) = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestMultiplePermissions()
) { permissions ->
    val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

    if (granted) {
        onPermissionGranted()
        scope.launch {
            try {
                // Add explicit permission check
                if (androidx.core.content.ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                    androidx.core.content.ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    val locationManager = context.getSystemService(android.content.Context.LOCATION_SERVICE) as android.location.LocationManager
                    val location = withContext(Dispatchers.IO) {
                        locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                            ?: locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                    }
                    location?.let {
                        onLocationReceived(Pair(it.latitude, it.longitude))
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("VoteRestaurantScreen", "Location error", e)
            }
        }
    }
}

@Composable
private fun VoteScreenEffects(
    groupViewModel: GroupViewModel,
    restaurantViewModel: RestaurantViewModel,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    userLocation: Pair<Double, Double>?,
    locationPermissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>
) {
    val currentGroup by groupViewModel.currentGroup.collectAsState()
    val selectedRestaurant by groupViewModel.selectedRestaurant.collectAsState()
    val restaurantError by restaurantViewModel.errorMessage.collectAsState()

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
        groupViewModel.loadGroupStatus()
    }

    LaunchedEffect(currentGroup) {
        currentGroup?.groupId?.let { gId ->
            groupViewModel.subscribeToGroup(gId)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            currentGroup?.groupId?.let { gId ->
                groupViewModel.unsubscribeFromGroup(gId)
            }
        }
    }

    LaunchedEffect(userLocation, currentGroup) {
        if (userLocation != null && currentGroup != null) {
            val (latitude, longitude) = userLocation
            restaurantViewModel.searchRestaurants(
                latitude = latitude,
                longitude = longitude,
                radius = 5000,
                cuisineTypes = listOf(),
                priceLevel = null
            )
        }
    }

    LaunchedEffect(selectedRestaurant) {
        selectedRestaurant?.let { restaurant ->
            try {
                snackbarHostState.showSnackbar("${restaurant.name} has been selected!")
                kotlinx.coroutines.delay(1500)
                navController.navigate("group") {
                    popUpTo(0)
                }
            } catch (e: Exception) {
                android.util.Log.e("VoteRestaurantScreen", "Navigation error", e)
            }
        }
    }

    LaunchedEffect(restaurantError) {
        restaurantError?.let { message ->
            snackbarHostState.showSnackbar(message)
            restaurantViewModel.clearError()
        }
    }
}

@Composable
private fun VoteScreenContent(
    groupViewModel: GroupViewModel,
    restaurantViewModel: RestaurantViewModel,
    snackbarHostState: SnackbarHostState,
    selectedRestaurantForVote: Restaurant?,
    userLocation: Pair<Double, Double>?,
    locationPermissionGranted: Boolean,
    locationPermissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>,
    onRestaurantSelected: (Restaurant?) -> Unit,
    onVoteSubmitted: () -> Unit
) {
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            VoteScreenHeader()
            VoteStatusCard(groupViewModel)
            VoteScreenBody(
                groupViewModel = groupViewModel,
                restaurantViewModel = restaurantViewModel,
                selectedRestaurantForVote = selectedRestaurantForVote,
                userLocation = userLocation,
                locationPermissionGranted = locationPermissionGranted,
                locationPermissionLauncher = locationPermissionLauncher,
                onRestaurantSelected = onRestaurantSelected,
                onVoteSubmitted = onVoteSubmitted
            )
        }
    }
}

@Composable
private fun VoteScreenHeader() {
    Text(
        text = "Vote for Restaurant",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun VoteStatusCard(groupViewModel: GroupViewModel) {
    val currentGroup by groupViewModel.currentGroup.collectAsState()
    val currentVotes by groupViewModel.currentVotes.collectAsState()
    val userVote by groupViewModel.userVote.collectAsState()

    currentGroup?.let { group ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Group: ${group.groupId?.take(8) ?: ""}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${currentVotes.values.sum()}/${group.numMembers} voted",
                        fontSize = 14.sp,
                        color = if (currentVotes.values.sum() == group.numMembers) 
                            Color(0xFF4CAF50) else Color.Gray
                    )
                }

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
}

@Composable
private fun VoteScreenBody(
    groupViewModel: GroupViewModel,
    restaurantViewModel: RestaurantViewModel,
    selectedRestaurantForVote: Restaurant?,
    userLocation: Pair<Double, Double>?,
    locationPermissionGranted: Boolean,
    locationPermissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>,
    onRestaurantSelected: (Restaurant?) -> Unit,
    onVoteSubmitted: () -> Unit
) {
    val restaurants by restaurantViewModel.restaurants.collectAsState()
    val isLoadingRestaurants by restaurantViewModel.isLoading.collectAsState()

    when {
        isLoadingRestaurants -> LoadingState()
        userLocation == null -> LocationLoadingState(locationPermissionGranted, locationPermissionLauncher)
        restaurants.isEmpty() -> EmptyRestaurantsState(restaurantViewModel, userLocation)
        else -> RestaurantListWithVoteButton(
            restaurants = restaurants,
            selectedRestaurantForVote = selectedRestaurantForVote,
            groupViewModel = groupViewModel,
            onRestaurantSelected = onRestaurantSelected,
            onVoteSubmitted = onVoteSubmitted
        )
    }
}

@Composable
private fun LoadingState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun LocationLoadingState(
    locationPermissionGranted: Boolean,
    locationPermissionLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Getting your location...",
                fontSize = 16.sp,
                color = Color.Gray
            )
            if (!locationPermissionGranted) {
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
                    Text("Grant Location Permission")
                }
            }
        }
    }
}

@Composable
private fun EmptyRestaurantsState(
    restaurantViewModel: RestaurantViewModel,
    userLocation: Pair<Double, Double>?
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
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
}

@Composable
private fun RestaurantListWithVoteButton(
    restaurants: List<Restaurant>,
    selectedRestaurantForVote: Restaurant?,
    groupViewModel: GroupViewModel,
    onRestaurantSelected: (Restaurant?) -> Unit,
    onVoteSubmitted: () -> Unit,
    modifier: Modifier = Modifier  // Add modifier parameter
) {
    val currentVotes by groupViewModel.currentVotes.collectAsState()
    val userVote by groupViewModel.userVote.collectAsState()

    Column(modifier = modifier) {  // Wrap in Column and apply modifier
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(restaurants) { restaurant ->
                val effectiveId = restaurant.restaurantId ?: generateDeterministicId(restaurant)
                RestaurantCard(
                    restaurant = restaurant.copy(restaurantId = effectiveId),
                    isSelected = selectedRestaurantForVote?.let {
                        it.restaurantId == effectiveId ||
                                (it.name == restaurant.name && it.location == restaurant.location)
                    } ?: false,
                    hasVoted = userVote != null,
                    voteCount = currentVotes[effectiveId] ?: 0,
                    onClick = {
                        if (userVote == null) {
                            onRestaurantSelected(restaurant.copy(restaurantId = effectiveId))
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        VoteButton(
            selectedRestaurantForVote = selectedRestaurantForVote,
            userVote = userVote,
            groupViewModel = groupViewModel,
            onVoteSubmitted = onVoteSubmitted
        )
    }
}

@Composable
private fun VoteButton(
    selectedRestaurantForVote: Restaurant?,
    userVote: String?,
    groupViewModel: GroupViewModel,
    onVoteSubmitted: () -> Unit
) {
    Button(
        onClick = {
            selectedRestaurantForVote?.let { restaurant ->
                val restId = restaurant.restaurantId ?: generateDeterministicId(restaurant)
                groupViewModel.voteForRestaurant(
                    restId,
                    restaurant.copy(restaurantId = restId)
                )
                onVoteSubmitted()
            }
        },
        modifier = Modifier.fillMaxWidth().height(56.dp),
        enabled = selectedRestaurantForVote != null && userVote == null,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFFFD54F),
            disabledContainerColor = Color.Gray
        )
    ) {
        if (groupViewModel.isLoading.collectAsState().value) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.Black
            )
        } else {
            Text(
                text = if (userVote != null) "Already Voted" else "Submit Vote",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

fun generateDeterministicId(restaurant: Restaurant): String {
    val combined = "${restaurant.name}_${restaurant.location}".replace(" ", "").toLowerCase()
    return combined.hashCode().toString()
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
            .clickable(enabled = !hasVoted, onClick = onClick),
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
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RestaurantImage(restaurant)
            RestaurantInfo(restaurant, voteCount)
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

@Composable
private fun RestaurantImage(restaurant: Restaurant) {
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
}

@Composable
private fun RowScope.RestaurantInfo(restaurant: Restaurant, voteCount: Int) {
    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = restaurant.name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        RestaurantRatingAndPrice(restaurant)
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
}

@Composable
private fun RestaurantRatingAndPrice(restaurant: Restaurant) {
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
}