package com.example.flexifitapp

import com.google.gson.annotations.SerializedName

data class BootstrapResponse(
    // ✅ Kung ang backend ay nagbabalik ng lowercase dito, guds na ito.
    @SerializedName("profileComplete")
    val profileComplete: Boolean,

    @SerializedName("userId")
    val userId: Int? = null,

    /**
     * 🔥 WARNING: Ang OnboardingProfileRequest ay gumagamit ng @SerializedName PascalCase.
     * Siguraduhin na ang "ExistingProfile" key ay match sa JSON na binubuga ng API mo.
     */
    @SerializedName("existingProfile")
    val existingProfile: OnboardingProfileRequest? = null
)