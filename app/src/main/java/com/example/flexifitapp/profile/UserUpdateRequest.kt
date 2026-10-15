package com.example.flexifitapp.profile

import com.google.gson.annotations.SerializedName

data class UserUpdateRequest(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("name") val name: String? = null,       // Nullable for partial update
    @SerializedName("username") val username: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("role") val role: String? = null
)