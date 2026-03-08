package com.example.flexifitapp

data class LoginRequest(
    val firebaseIdToken: String,
    val fcmToken: String? = null
)