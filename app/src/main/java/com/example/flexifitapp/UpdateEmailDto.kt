package com.example.flexifitapp

import com.google.gson.annotations.SerializedName

data class UpdateEmailDto(
    @SerializedName("newEmail")
    val newEmail: String
)

// ✅ Add this below
data class UpdateGoogleEmailDto(
    @SerializedName("newEmail")
    val newEmail: String,
    @SerializedName("newFirebaseUid")
    val newFirebaseUid: String
)