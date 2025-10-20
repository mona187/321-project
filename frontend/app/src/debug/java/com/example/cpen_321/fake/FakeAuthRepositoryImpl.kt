package com.example.cpen_321.fake

import android.content.Context
import com.example.cpen_321.data.model.User
import com.example.cpen_321.data.network.dto.AuthData
import com.example.cpen_321.data.repository.AuthRepository
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeAuthRepositoryImpl @Inject constructor() : AuthRepository {

    // Simulated cached user data for debug mode
    private val fakeUser = User(
        userId = 1,
        name = "Debug User",
        bio = "Hi I am the debug user",
        preference = TODO(),
        profilePicture = TODO(),
        credibilityScore = TODO(),
        contactNumber = TODO(),
        budget = TODO(),
        radiusKm = TODO(),
        status = TODO(),
        roomId = TODO(),
        groupId = TODO()
    )

    private val fakeToken = "fake-debug-token"

    // -------------------------
    // GOOGLE SIGN-IN SIMULATION
    // -------------------------

    override suspend fun signInWithGoogle(context: Context): Result<GoogleIdTokenCredential> =
        Result.failure<GoogleIdTokenCredential>(
            Exception("Google sign-in is not available in mock mode.")
        )

    override suspend fun googleSignIn(tokenId: String): Result<AuthData> {
        val fakeAuthData = AuthData(
            token = fakeToken,
            user = fakeUser
        )
        return Result.success(fakeAuthData)
    }

    override suspend fun googleSignUp(tokenId: String): Result<AuthData> {
        // Same behavior as sign-in in mock mode
        val fakeAuthData = AuthData(
            token = fakeToken,
            user = fakeUser
        )
        return Result.success(fakeAuthData)
    }

    // -------------------------
    // TOKEN MANAGEMENT
    // -------------------------

    override suspend fun clearToken(): Result<Unit> = Result.success(Unit)

    override suspend fun doesTokenExist(): Boolean = true

    override suspend fun getStoredToken(): String? = fakeToken

    // -------------------------
    // USER INFO
    // -------------------------

    override suspend fun getCurrentUser(): User? = fakeUser

    override suspend fun isUserAuthenticated(): Boolean = true
}
