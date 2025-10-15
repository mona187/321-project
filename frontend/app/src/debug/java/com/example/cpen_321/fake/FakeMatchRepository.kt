package com.example.cpen_321.fake

import com.example.cpen_321.data.model.UserProfile
import retrofit2.Response
import com.example.cpen_321.data.repository.MatchRepository

class FakeMatchRepository : MatchRepository {
    override suspend fun getUserProfilesForRoom(memberIds: List<String>): Response<List<UserProfile>> {
        val fakeUsers = listOf(
            UserProfile("u1", "Alice", "Food lover", "https://picsum.photos/100"),
            UserProfile("u2", "Bob", "Chef in progress", "https://picsum.photos/101"),
            UserProfile("u3", "Charlie", "Coffee addict", "https://picsum.photos/102")
        )
        return Response.success(fakeUsers)
    }
}