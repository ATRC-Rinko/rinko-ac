package com.rinko.auth.dto

data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class TokenPair(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long
)

data class AuthResponse(
    val tokenPair: TokenPair,
    val userId: Long,
    val username: String
)

data class MessageResponse(
    val message: String
)
