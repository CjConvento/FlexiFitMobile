package com.example.flexifitapp.profile

import com.google.gson.annotations.SerializedName

data class UserDto(
    @SerializedName("user_id") val userId: Int,
    @SerializedName("firebase_uid") val firebaseUid: String = "",
    @SerializedName("name") val name: String? = null,
    @SerializedName("username") val username: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("role") val role: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName("is_verified") val isVerified: Boolean = false,
    @SerializedName("auth_provider") val authProvider: String = "",
    @SerializedName("created_at") val createdAt: String = "",   // String is safer for JSON
    @SerializedName("updated_at") val updatedAt: String = ""
)