package com.example.cpen_321.fake

import com.example.cpen_321.data.model.UserProfile
import com.example.cpen_321.data.network.api.JoinMatchingRequest
import com.example.cpen_321.data.network.api.JoinMatchingResponse
import com.example.cpen_321.data.network.api.RoomDetails
import retrofit2.Response
import com.example.cpen_321.data.repository.MatchRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeMatchRepositoryImpl @Inject constructor() : MatchRepository {
    override suspend fun getUserProfilesForRoom(memberIds: List<Int>): Response<List<UserProfile>> {
        val fakeUsers = listOf(
            UserProfile(1, "Alice", "Food lover", "https://picsum.photos/100"),
            UserProfile(2, "Bob", "Chef in progress", "https://picsum.photos/101"),
            UserProfile(3, "Charlie", "Coffee addict", "https://picsum.photos/102"),
            UserProfile(4, "Sandi", "Hi there", profilePicture = "https://picsum.photos/103")
        )
        return Response.success(fakeUsers)
    }

    override suspend fun joinMatching(request: JoinMatchingRequest): Response<JoinMatchingResponse> {
        // Fake successful response
        val fakeResponse = JoinMatchingResponse(
            roomId = "fake_room_123",
            room = RoomDetails(
                roomId = "fake_room_123",
                completionTime = "2025-10-23T09:00:00.000Z",
                maxMembers = 10,
                members = listOf("user1", "user2"),
                status = "waiting",
                cuisine = request.cuisine.joinToString(","),
                averageBudget = request.budget,
                averageRadius = request.radiusKm,
                createdAt = "2025-10-23T08:00:00.000Z",
                updatedAt = "2025-10-23T08:00:00.000Z",
                id = "fake_room_123"
            )
        )
        return Response.success(fakeResponse)
    }
}