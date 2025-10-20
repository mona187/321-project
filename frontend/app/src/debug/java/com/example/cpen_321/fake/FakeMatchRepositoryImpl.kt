package com.example.cpen_321.fake

import com.example.cpen_321.data.model.UserProfile
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
}