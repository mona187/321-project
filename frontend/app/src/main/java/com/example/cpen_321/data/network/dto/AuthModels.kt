package com.example.cpen_321.data.network.dto
import com.example.cpen_321.data.model.User

//what us end to backend
data class GoogleSigninRequest(
    val idToken: String
)
//what we recieve from backend after signin, backends resposne payload
data class AuthData(
    val token: String,
    val user: User,
)
