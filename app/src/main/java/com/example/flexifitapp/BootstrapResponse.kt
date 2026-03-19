package com.example.flexifitapp

import com.example.flexifitapp.dashboard.ProfileStatusResponse
import com.google.gson.annotations.SerializedName

data class BootstrapResponse(
    @SerializedName("profileComplete")
    val profileComplete: Boolean,

    @SerializedName("userId")
    val userId: Int? = null,

    // 🔥 DAGDAGAN NATIN ITO PARA MAKUHA YUNG NASA SCREENSHOT MO
    @SerializedName("name")
    val name: String? = null,

    @SerializedName("username")
    val username: String? = null,

    @SerializedName("existingProfile")
    val existingProfile: OnboardingProfileRequest? = null,

    @SerializedName("status")
    val status: ProfileStatusResponse? = null
)