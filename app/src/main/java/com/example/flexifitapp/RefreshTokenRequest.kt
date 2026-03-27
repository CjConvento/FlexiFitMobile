package com.example.flexifitapp

import com.google.gson.annotations.SerializedName

data class RefreshTokenRequest(
    @SerializedName("firebaseIdToken")
    val firebaseIdToken: String
)