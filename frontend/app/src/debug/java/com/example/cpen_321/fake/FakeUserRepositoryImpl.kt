package com.example.cpen_321.fake

import com.example.cpen_321.data.model.UserProfile
import com.example.cpen_321.data.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeUserRepositoryImpl @Inject constructor() : UserRepository {

    private val fakeUser = UserProfile(
        userId = 1,
        name = "Debug User",
        bio = "Hi I am the debug user",
        profilePicture = null
    )

    override suspend fun getCurrentUserProfile(): UserProfile = fakeUser

    override suspend fun getUserProfile(userId: Int): UserProfile = fakeUser
}
