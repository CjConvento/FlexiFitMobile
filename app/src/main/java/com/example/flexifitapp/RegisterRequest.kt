package com.example.flexifitapp

data class RegisterRequest(
    val firebaseIdToken: String,
    val name: String,
    val username: String,
    val fcmToken: String? = null
)