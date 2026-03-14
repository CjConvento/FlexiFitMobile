package com.example.flexifitapp.auth

import com.google.gson.annotations.SerializedName

data class UserAccountDto(
    @SerializedName("userId") val userId: Int,
    @SerializedName("name") val name: String?,
    @SerializedName("username") val username: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("status") val status: String?
)