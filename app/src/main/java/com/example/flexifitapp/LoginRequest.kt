package com.example.flexifitapp

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("firebaseIdToken")
    val firebaseIdToken: String,

    @SerializedName("fcmToken")
    val fcmToken: String? = null
)