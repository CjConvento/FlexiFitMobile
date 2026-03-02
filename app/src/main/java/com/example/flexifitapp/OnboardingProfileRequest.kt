package com.example.flexifitapp

import com.google.gson.annotations.SerializedName

data class OnboardingProfileRequest(
    @SerializedName("Age") val age: Int,
    @SerializedName("Gender") val gender: String,
    @SerializedName("HeightCm") val heightCm: Double,
    @SerializedName("WeightKg") val weightKg: Double,
    @SerializedName("ActivityLevel") val activityLevel: String,
    @SerializedName("BodyGoal") val bodyGoal: String,
    @SerializedName("DietType") val dietType: String
)