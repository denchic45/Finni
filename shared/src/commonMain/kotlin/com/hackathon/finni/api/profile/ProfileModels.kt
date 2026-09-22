package com.hackathon.finni.api.profile.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val nickname: String = ""
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class TokensResponse(
    val accessToken: String,
    val refreshToken: String
)

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val name: String
)
