package com.example.flexifitapp

data class AuthResponse(
    val token: String,
    val userId: Int,
    val role: String?,
    val status: String?,
    val isVerified: Boolean
)