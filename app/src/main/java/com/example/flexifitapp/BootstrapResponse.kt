package com.example.flexifitapp

import com.example.flexifitapp.dashboard.ProfileStatusResponse
import com.google.gson.annotations.SerializedName

data class BootstrapResponse(
    // Ito yung hinahanap ng OnboardingActivity mo
    @SerializedName("profileComplete")
    val profileComplete: Boolean,

    @SerializedName("userId")
    val userId: Int? = null,

    // Ito naman yung para sa hydration
    @SerializedName("existingProfile")
    val existingProfile: OnboardingProfileRequest? = null,

    // Optional: Isama na rin natin ang status para flexible
    @SerializedName("status")
    val status: ProfileStatusResponse? = null
)