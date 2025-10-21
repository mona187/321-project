package com.example.cpen_321.fake

import com.example.cpen_321.data.model.UserProfile
import com.example.cpen_321.data.repository.UserRepository
import javax.inject.Inject
import javax.inject.Singleton
import com.example.cpen_321.data.model.User
import kotlinx.coroutines.delay
@Singleton
class FakeUserRepositoryImpl @Inject constructor() : UserRepository {

    private val fakeUser = UserProfile(
        userId = 1,
        name = "Debug User",
        bio = "Hi I am the debug user",
        profilePicture = null
    )

    private var fakeUserSettings = User(
        userId = 1,
        name = "Debug User",
        bio = "Hi I am the debug user.",
        profilePicture = null,
        radiusKm = null,
        contactNumber = null,
        preference = null,
        budget = null,
        credibilityScore = null,
        status = null,
        roomId = null,
        groupId = null,
    )
    override suspend fun getCurrentUserProfile(): UserProfile {
        delay(300) // simulate network latency
        return fakeUser
    }

    override suspend fun getUserProfile(userId: Int): UserProfile {
        delay(300)
        return fakeUser.copy(userId = userId)
    }


    override suspend fun getUserSettings(): User {
        delay(500) // simulate network delay
        return fakeUserSettings
    }

    override suspend fun updateUserSettings(user: User): User {
        delay(500)
        fakeUserSettings = user
        return fakeUserSettings
    }

    override suspend fun createUserSettings(user: User): User {
        delay(500)
        fakeUserSettings = user
        return fakeUserSettings
    }
}
