package com.example.flexifitapp

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("firebaseIdToken")
    val firebaseIdToken: String,

    @SerializedName("name")
    val name: String? = null,

    @SerializedName("username")
    val username: String? = null,

    @SerializedName("fcmToken")
    val fcmToken: String? = null
)