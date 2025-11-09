package com.example.cpen_321.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.cpen_321.data.model.GroupMember
import com.example.cpen_321.ui.viewmodels.GroupViewModel

@Composable
fun GroupScreen(
    navController: NavController,
    groupId: String? = null,
    viewModel: GroupViewModel = hiltViewModel()
) {
    val currentGroup by viewModel.currentGroup.collectAsState()
    val groupMembers by viewModel.groupMembers.collectAsState()
    val selectedRestaurant by viewModel.selectedRestaurant.collectAsState()
    val currentVotes by viewModel.currentVotes.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    GroupScreenEffects(
        viewModel = viewModel,
        errorMessage = errorMessage,
        successMessage = successMessage,
        snackbarHostState = snackbarHostState
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        GroupScreenContent(
            modifier = Modifier.padding(innerPadding),
            currentGroup = currentGroup,
            groupMembers = groupMembers,
            selectedRestaurant = selectedRestaurant,
            currentVotes = currentVotes,
            isLoading = isLoading,
            viewModel = viewModel,
            navController = navController
        )
    }
}

@Composable
private fun GroupScreenEffects(
    viewModel: GroupViewModel,
    errorMessage: String?,
    successMessage: String?,
    snackbarHostState: SnackbarHostState
) {
    LaunchedEffect(Unit) {
        viewModel.loadGroupStatus()
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearSuccess()
        }
    }
}

@Composable
private fun GroupScreenContent(
    modifier: Modifier,
    currentGroup: com.example.cpen_321.data.model.Group?,
    groupMembers: List<GroupMember>,
    selectedRestaurant: com.example.cpen_321.data.model.Restaurant?,
    currentVotes: Map<String, Int>,
    isLoading: Boolean,
    viewModel: GroupViewModel,
    navController: NavController
) {
    when {
        isLoading && currentGroup == null -> LoadingState(modifier)
        currentGroup == null -> NoGroupState(modifier, navController)
        else -> GroupContent(
            modifier = modifier,
            currentGroup = currentGroup,
            groupMembers = groupMembers,
            selectedRestaurant = selectedRestaurant,
            currentVotes = currentVotes,
            isLoading = isLoading,
            viewModel = viewModel,
            navController = navController
        )
    }
}

@Composable
private fun LoadingState(modifier: Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun NoGroupState(modifier: Modifier, navController: NavController) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "No active group found",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "You are not currently in any group.",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    navController.navigate("home") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                Text("Go to Home")
            }
        }
    }
}

@Composable
private fun GroupContent(
    modifier: Modifier,
    currentGroup: com.example.cpen_321.data.model.Group,
    groupMembers: List<GroupMember>,
    selectedRestaurant: com.example.cpen_321.data.model.Restaurant?,
    currentVotes: Map<String, Int>,
    isLoading: Boolean,
    viewModel: GroupViewModel,
    navController: NavController
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            GroupHeader()
            RestaurantSection(
                selectedRestaurant = selectedRestaurant,
                currentGroup = currentGroup,
                currentVotes = currentVotes
            )
            MembersSection(
                currentGroup = currentGroup,
                groupMembers = groupMembers
            )
        }

        ActionButtons(
            currentGroup = currentGroup,
            isLoading = isLoading,
            viewModel = viewModel,
            navController = navController
        )
    }
}

@Composable
private fun GroupHeader() {
    Spacer(modifier = Modifier.height(16.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .background(Color(0xFFFFF9C4))
            .border(2.dp, Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Group",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            textAlign = TextAlign.Center
        )
    }
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
private fun RestaurantSection(
    selectedRestaurant: com.example.cpen_321.data.model.Restaurant?,
    currentGroup: com.example.cpen_321.data.model.Group,
    currentVotes: Map<String, Int>
) {
    val restaurant = selectedRestaurant ?: currentGroup.restaurant

    if (restaurant != null) {
        RestaurantCard(restaurant = restaurant, currentVotes = currentVotes)
    } else {
        NoRestaurantSelected()
    }
    Spacer(modifier = Modifier.height(24.dp))
}

@Composable
private fun RestaurantCard(
    restaurant: com.example.cpen_321.data.model.Restaurant,
    currentVotes: Map<String, Int>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
        shape = RoundedCornerShape(0.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color.Black)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            RestaurantPhoto(restaurant = restaurant)
            RestaurantDetails(restaurant = restaurant)
            RestaurantVoteCount(restaurant = restaurant, currentVotes = currentVotes)
        }
    }
}

@Composable
private fun RestaurantPhoto(restaurant: com.example.cpen_321.data.model.Restaurant) {
    restaurant.getMainPhotoUrl()?.let { photoUrl ->
        AsyncImage(
            model = photoUrl,
            contentDescription = restaurant.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray)
        )
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun RestaurantDetails(restaurant: com.example.cpen_321.data.model.Restaurant) {
    Text(
        text = restaurant.name,
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = "📍 ${restaurant.location}",
        fontSize = 16.sp,
        color = Color.Black
    )

    restaurant.rating?.let { rating ->
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Rating",
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = restaurant.getRatingString(),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }

    restaurant.priceLevel?.let {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Price: ${restaurant.getPriceLevelString()}",
            fontSize = 16.sp,
            color = Color.Black
        )
    }

    restaurant.phoneNumber?.let { phone ->
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "📞 $phone",
            fontSize = 16.sp,
            color = Color.Black
        )
    }
}

@Composable
private fun RestaurantVoteCount(
    restaurant: com.example.cpen_321.data.model.Restaurant,
    currentVotes: Map<String, Int>
) {
    restaurant.restaurantId?.let { restId ->
        val voteCount = currentVotes[restId] ?: 0
        if (voteCount > 0) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "🏆 Won with $voteCount vote${if (voteCount != 1) "s" else ""}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
private fun NoRestaurantSelected() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF9C4))
            .border(2.dp, Color.Black)
            .padding(16.dp)
    ) {
        Text(
            text = "Restaurant: Not selected yet\nWaiting for voting to complete...",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun MembersSection(
    currentGroup: com.example.cpen_321.data.model.Group,
    groupMembers: List<GroupMember>
) {
    Text(
        text = "Group Members (${currentGroup.getAllMembers()?.size ?: 0})",
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black
    )

    Spacer(modifier = Modifier.height(16.dp))

    val allMembers = currentGroup.getAllMembers() ?: emptyList()
    
    if (allMembers.isEmpty()) {
        MembersLoadingState()
    } else {
        MembersList(allMembers = allMembers, groupMembers = groupMembers)
    }
}

@Composable
private fun MembersLoadingState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MembersList(
    allMembers: List<String>,
    groupMembers: List<GroupMember>
) {
    allMembers.forEach { userId ->
        val memberDetails = groupMembers.find { it.userId == userId }
        MemberCard(userId = userId, memberDetails = memberDetails)
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun MemberCard(userId: String, memberDetails: GroupMember?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF9C4))
            .border(2.dp, Color.Black)
            .padding(16.dp)
    ) {
        if (memberDetails != null) {
            MemberDetailsContent(memberDetails)
        } else {
            MemberPlaceholderContent(userId)
        }
    }
}

@Composable
private fun MemberDetailsContent(member: GroupMember) {
    Column {
        Text(
            text = member.name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Credibility Score: ${member.credibilityScore.toInt()}",
            fontSize = 14.sp,
            color = Color.Black
        )
        member.phoneNumber?.let { phone ->
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Phone: $phone",
                fontSize = 14.sp,
                color = Color.Black
            )
        }
        if (member.hasVoted) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "✓ Voted",
                fontSize = 14.sp,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MemberPlaceholderContent(userId: String) {
    Column {
        Text(
            text = "User: $userId",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Loading details...",
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun ActionButtons(
    currentGroup: com.example.cpen_321.data.model.Group,
    isLoading: Boolean,
    viewModel: GroupViewModel,
    navController: NavController
) {
    Button(
        onClick = {
            navController.navigate("view_groups") {
                popUpTo("home") { inclusive = false }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F))
    ) {
        Text(
            text = "Back to View Groups",
            color = Color.Black,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }

    if (currentGroup.restaurantSelected == false) {
        Spacer(modifier = Modifier.height(8.dp))
        LeaveGroupButton(isLoading = isLoading, viewModel = viewModel, navController = navController)
    }

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
private fun LeaveGroupButton(
    isLoading: Boolean,
    viewModel: GroupViewModel,
    navController: NavController
) {
    OutlinedButton(
        onClick = {
            viewModel.leaveGroup {
                navController.navigate("home") {
                    popUpTo(0) { inclusive = true }
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.Red,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = "Leave Group",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}