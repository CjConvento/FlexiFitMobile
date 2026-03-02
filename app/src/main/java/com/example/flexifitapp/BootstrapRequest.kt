package com.example.flexifitapp

data class BootstrapRequest(
    val username: String? = null,
    val fullName: String? = null,
    val address: String? = null,
    val fcmToken: String? = null
)