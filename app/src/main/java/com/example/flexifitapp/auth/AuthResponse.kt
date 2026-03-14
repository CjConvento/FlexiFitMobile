package com.example.flexifitapp.auth

data class AuthResponse(
    val token: String,
    val userId: Int,
    val role: String?,
    val status: String?,
    val isVerified: Boolean,

    // DAGDAG MO ITO PARA SA DISPLAY:
    val name: String? = null,
    val photoUrl: String? = null
)