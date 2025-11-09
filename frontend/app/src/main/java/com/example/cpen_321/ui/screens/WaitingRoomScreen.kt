package com.example.cpen_321.ui.screens

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Timer
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
import com.example.cpen_321.data.model.UserProfile
import com.example.cpen_321.ui.viewmodels.MatchViewModel
import kotlinx.coroutines.delay

@Composable
fun WaitingRoomScreen(
    navController: NavController,
    viewModel: MatchViewModel = hiltViewModel()
) {
    val currentRoom by viewModel.currentRoom.collectAsState()
    val roomMembers by viewModel.roomMembers.collectAsState()
    val timeRemaining by viewModel.timeRemaining.collectAsState()
    val groupReady by viewModel.groupReady.collectAsState()
    val groupId by viewModel.groupId.collectAsState()
    val roomExpired by viewModel.roomExpired.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showLeaveDialog by remember { mutableStateOf(false) }
    var showFailureDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val minNumberOfPeople = 2

    WaitingRoomEffects(
        currentRoom = currentRoom,
        timeRemaining = timeRemaining,
        groupReady = groupReady,
        groupId = groupId,
        roomExpired = roomExpired,
        roomMembers = roomMembers,
        errorMessage = errorMessage,
        minNumberOfPeople = minNumberOfPeople,
        viewModel = viewModel,
        navController = navController,
        snackbarHostState = snackbarHostState,
        onShowFailureDialog = { showFailureDialog = true }
    )

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = Color(0xFFFF6B6B),
                    contentColor = Color.White
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!groupReady) {
                WaitingRoomContent(
                    currentRoom = currentRoom,
                    roomMembers = roomMembers,
                    timeRemaining = timeRemaining,
                    minNumberOfPeople = minNumberOfPeople,
                    onLeaveClick = { showLeaveDialog = true }
                )
            } else {
                GroupReadyContent()
            }
        }
    }

    LeaveRoomDialog(
        showDialog = showLeaveDialog,
        onDismiss = { showLeaveDialog = false },
        onConfirm = {
            showLeaveDialog = false
            viewModel.leaveRoom()
            navController.popBackStack()
        }
    )

    FailureDialog(
        showDialog = showFailureDialog,
        minNumberOfPeople = minNumberOfPeople,
        onConfirm = {
            viewModel.clearRoomExpired()
            showFailureDialog = false
            navController.navigate("home") {
                popUpTo("home") { inclusive = true }
            }
        }
    )
}

@Composable
private fun WaitingRoomEffects(
    currentRoom: Any?,
    timeRemaining: Long,
    groupReady: Boolean,
    groupId: String?,
    roomExpired: Boolean,
    roomMembers: List<UserProfile>,
    errorMessage: String?,
    minNumberOfPeople: Int,
    viewModel: MatchViewModel,
    navController: NavController,
    snackbarHostState: SnackbarHostState,
    onShowFailureDialog: () -> Unit
) {
    val timeRemainingSeconds = (timeRemaining / 1000).toInt()

    LaunchedEffect(timeRemaining) {
        if (timeRemaining > 0) {
            Log.d("WaitingRoom", "Timer: ${timeRemainingSeconds / 60}:${String.format("%02d", timeRemainingSeconds % 60)}")
        }
    }

    LaunchedEffect(currentRoom) {
        (currentRoom as? com.example.cpen_321.data.model.Room)?.let { room ->
            Log.d("WaitingRoom", "Loading room status for: ${room.roomId}")
            viewModel.getRoomStatus(room.roomId)
        }
    }

    LaunchedEffect(groupReady, groupId) {
        if (groupReady && groupId != null) {
            delay(1000L)
            navController.navigate("vote_restaurant/$groupId") {
                popUpTo("waiting_room") { inclusive = true }
            }
        }
    }

    LaunchedEffect(roomExpired) {
        if (roomExpired) {
            if (roomMembers.size < minNumberOfPeople) {
                onShowFailureDialog()
            }
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let { message ->
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }
}

@Composable
private fun WaitingRoomContent(
    currentRoom: Any?,
    roomMembers: List<UserProfile>,
    timeRemaining: Long,
    minNumberOfPeople: Int,
    onLeaveClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        WaitingRoomTopSection(
            currentRoom = currentRoom,
            roomMembers = roomMembers,
            timeRemaining = timeRemaining,
            minNumberOfPeople = minNumberOfPeople
        )

        WaitingRoomBottomSection(onLeaveClick = onLeaveClick)
    }
}

@Composable
private fun WaitingRoomTopSection(
    currentRoom: Any?,
    roomMembers: List<UserProfile>,
    timeRemaining: Long,
    minNumberOfPeople: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        WaitingRoomHeader()

        Spacer(modifier = Modifier.height(32.dp))

        TimerCard(timeRemaining = timeRemaining)

        Spacer(modifier = Modifier.height(24.dp))

        RoomInfoCard(
            currentRoom = currentRoom,
            roomMembers = roomMembers,
            minNumberOfPeople = minNumberOfPeople
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Current Members",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        UserBubbleRow(roomMembers)
    }
}

@Composable
private fun WaitingRoomHeader() {
    Text(
        "Waiting Room",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = Color.Black
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Finding your perfect dining group...",
        fontSize = 14.sp,
        color = Color.Gray,
        textAlign = TextAlign.Center
    )
}

@Composable
private fun TimerCard(timeRemaining: Long) {
    val timeRemainingSeconds = (timeRemaining / 1000).toInt()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Timer",
                    tint = if (timeRemainingSeconds <= 10) Color.Red else Color.Black,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = String.format(
                        "%d:%02d",
                        timeRemainingSeconds / 60,
                        timeRemainingSeconds % 60
                    ),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = if (timeRemainingSeconds <= 10) Color.Red else Color.Black
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (timeRemainingSeconds <= 10) "Finishing soon!" else "Time remaining",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun RoomInfoCard(
    currentRoom: Any?,
    roomMembers: List<UserProfile>,
    minNumberOfPeople: Int
) {
    val room = currentRoom as? com.example.cpen_321.data.model.Room
    val maxNumberOfPeople = room?.maxMembers ?: 10
    val progress by animateFloatAsState(
        targetValue = roomMembers.size.toFloat() / maxNumberOfPeople.toFloat(),
        animationSpec = tween(durationMillis = 500),
        label = "progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            MemberCountRow(
                roomMembers = roomMembers,
                maxNumberOfPeople = maxNumberOfPeople,
                minNumberOfPeople = minNumberOfPeople
            )

            Spacer(modifier = Modifier.height(12.dp))

            RoomProgressIndicator(
                progress = progress,
                roomMembers = roomMembers,
                minNumberOfPeople = minNumberOfPeople
            )

            room?.let { RoomDetails(room = it) }
        }
    }
}

@Composable
private fun MemberCountRow(
    roomMembers: List<UserProfile>,
    maxNumberOfPeople: Int,
    minNumberOfPeople: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color(0xFFFFD54F),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Members",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        Text(
            text = "${roomMembers.size}/$maxNumberOfPeople",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (roomMembers.size >= minNumberOfPeople)
                Color(0xFF4CAF50) else Color.Gray
        )
    }
}

@Composable
private fun RoomProgressIndicator(
    progress: Float,
    roomMembers: List<UserProfile>,
    minNumberOfPeople: Int
) {
    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
        color = if (roomMembers.size >= minNumberOfPeople)
            Color(0xFF4CAF50) else Color(0xFFFFD54F),
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = if (roomMembers.size >= minNumberOfPeople)
            "Minimum members reached! ✓"
        else
            "Waiting for at least $minNumberOfPeople members...",
        fontSize = 12.sp,
        color = if (roomMembers.size >= minNumberOfPeople)
            Color(0xFF4CAF50) else Color.Gray,
        fontWeight = if (roomMembers.size >= minNumberOfPeople)
            FontWeight.SemiBold else FontWeight.Normal
    )
}

@Composable
private fun RoomDetails(room: com.example.cpen_321.data.model.Room) {
    room.cuisine?.let { cuisine ->
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Restaurant,
                contentDescription = null,
                tint = Color(0xFFFFD54F),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Cuisine: $cuisine",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }

    if (room.averageBudget != null || room.averageRadius != null) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            room.averageBudget?.let { budget ->
                Text(
                    text = "Budget: $$budget",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            room.averageRadius?.let { radius ->
                Text(
                    text = "Radius: ${radius}km",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun WaitingRoomBottomSection(onLeaveClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onLeaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B))
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = null,
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Leave Room",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun GroupReadyContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                modifier = Modifier.size(120.dp),
                shape = CircleShape,
                colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "✓",
                        fontSize = 64.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                "Group Ready!",
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(16.dp))

            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                color = Color(0xFFFFD54F),
                strokeWidth = 4.dp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Preparing your group...",
                fontSize = 16.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "You'll be redirected shortly",
                fontSize = 14.sp,
                color = Color.LightGray
            )
        }
    }
}

@Composable
fun UserBubbleRow(users: List<UserProfile>) {
    if (users.isEmpty()) {
        EmptyMembersPlaceholder()
    } else {
        MembersList(users = users)
    }
}

@Composable
private fun EmptyMembersPlaceholder() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = Color(0xFFFFD54F)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Loading members...",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
private fun MembersList(users: List<UserProfile>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(users, key = { it.userId }) { user ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + scaleIn(),
                    exit = fadeOut() + scaleOut()
                ) {
                    UserBubble(user = user)
                }
            }
        }
    }
}

@Composable
private fun UserBubble(user: UserProfile) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(90.dp)
    ) {
        if (user.profilePicture?.isNotEmpty() == true) {
            AsyncImage(
                model = user.profilePicture,
                contentDescription = user.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .border(3.dp, Color(0xFFFFD54F), CircleShape)
            )
        } else {
            DefaultUserAvatar()
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = user.name,
            fontSize = 12.sp,
            maxLines = 2,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Medium,
            color = Color.Black
        )
    }
}

@Composable
private fun DefaultUserAvatar() {
    Box(
        modifier = Modifier
            .size(70.dp)
            .clip(CircleShape)
            .background(Color(0xFFFFD54F))
            .border(3.dp, Color(0xFFFFD54F), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Person,
            contentDescription = "Default avatar",
            modifier = Modifier.size(40.dp),
            tint = Color.White
        )
    }
}

@Composable
private fun LeaveRoomDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    "Leave Waiting Room?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Are you sure you want to leave? You'll lose your spot in this room.")
            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("Leave", color = Color(0xFFFF6B6B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Stay", color = Color.Black)
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
private fun FailureDialog(
    showDialog: Boolean,
    minNumberOfPeople: Int,
    onConfirm: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { },
            title = {
                Text(
                    "Unable to Create Group",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF6B6B)
                )
            },
            text = {
                Column {
                    Text("The waiting room timer expired, but not enough people joined.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Minimum $minNumberOfPeople members required to form a group.",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onConfirm) {
                    Text("Try Again", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = Color(0xFFFFF9C4)
        )
    }
}