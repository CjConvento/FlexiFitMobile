package com.example.flexifitapp.dashboard

import com.google.gson.annotations.SerializedName

/**
 * PHASE 1: THE BUCKET
 * Ito ang sasalo sa lahat ng dinidilig na data mula sa ASP.NET.
 * Pinagsama natin ang Profile Completion, BMI, at Dashboard Stats.
 */
data class ProfileStatusResponse(
    // Mula sa ProfileStatusController.cs
    @SerializedName("isProfileCompleted")
    val isProfileCompleted: Boolean,

    // Mula sa ProfileController.cs (Calculated fields)
    @SerializedName("bmi") val bmi: Double,
    @SerializedName("bmiCategory") val bmiCategory: String,
    @SerializedName("calorieTarget") val dailyCalorieTarget: Int,
    @SerializedName("proteinGrams") val proteinGrams: Int,
    @SerializedName("carbsGrams") val carbsGrams: Int,
    @SerializedName("fatGrams") val fatGrams: Int,
    @SerializedName("macroPercents") val macroPercents: MacroPercents?,

    // Mula sa MobileController.cs (Dashboard fields)
    @SerializedName("weight") val currentWeight: Double?,
    @SerializedName("targetWeight") val targetWeight: Double?,
    @SerializedName("fitnessLevel") val fitnessLevel: String?,
    @SerializedName("goal") val fitnessGoal: String?
)

data class MacroPercents(
    @SerializedName("protein") val protein: Double,
    @SerializedName("carbs") val carbs: Double,
    @SerializedName("fat") val fat: Double
)